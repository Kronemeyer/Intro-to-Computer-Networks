import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class dvrouter {

	private class router {
		private int numOfRouters;
		private int dists[][];
		private int nextHops[][];
		
		public router(int max) {
			numOfRouters = max;
			dists = new int[numOfRouters][numOfRouters];
			nextHops = new int[numOfRouters][numOfRouters];

			for (int i = 0; i < numOfRouters; ++i) {
				for (int j = 0; j < numOfRouters; ++j) {
					dists[i][j] = Integer.MAX_VALUE/2;
					nextHops[i][j] = -1;
				}
				dists[i][i] = 0;
				nextHops[i][i] = i;
			}
		}

		public int[] applyChange(int in[]) {
			int from = in[0] - 1, to = in[1] - 1, dist = in[2];
			nextHops[from][to] = to;
			nextHops[to][from] = from;
			dists[from][to] = dist;
			dists[to][from] = dist;
			return in;
		}

		public void print(PrintStream out) {
			for (int from = 0; from < numOfRouters; ++from) {
				for (int to = 0; to < numOfRouters; ++to) {
					if (nextHops[from][to] == -1)
					//	System.out.println(to + " " + (nextHops[from][to]) + " " + dists[from][to]);
						out.println(to+1 + " " + (nextHops[from][to]) + " " + dists[from][to]);
					else
					//	System.out.println(to + " " + (nextHops[from][to]) + " " + dists[from][to]);
						out.println(to+1 + " " + (nextHops[from][to]+1) + " " + dists[from][to]);
				}
				out.println();
			}
		}
	}

	private static router routers;

	public static void main(String[] args) throws IOException {
		new dvrouter(args);
	}

	public dvrouter(String args[]) throws IOException {
		
		Scanner ff;
		try {
			ArrayList<int[]> topoIn = new ArrayList<int[]>();
			int max = 0;
			ff = new Scanner(new File(args[0]));
			while (ff.hasNextLine()) {
				int processed[] = getChange(ff.nextLine());
				topoIn.add(processed);
				int temp = processed[0] > processed[1] ? processed[0] : processed[1];
				if (temp > max)
					max = temp;
			}
			ff.close();

			routers = new router(max);

			for (int[] connection : topoIn)
				routers.applyChange(connection);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ArrayList<int[]> changes = new ArrayList<int[]>();
		try {
			ff = new Scanner(new File(args[1]));
			while (ff.hasNextLine())
				changes.add(getChange(ff.nextLine()));
			ff.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		ArrayList<int[]> messageNums = new ArrayList<int[]>();
		ArrayList<String> messageString = new ArrayList<>();
		try {
			ff = new Scanner(new File(args[2]));
			while (ff.hasNextLine())
				messageNums.add(getMessage(ff.nextLine(), messageString));
			ff.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
		System.setOut(out);

		// Run Bellman-Ford on all routers.
		dv(routers);

		routers.print(out);
		
		printMessageRoute(messageNums, messageString, routers, out);

		while (changes.size() > 0) {
			routers.applyChange(changes.get(0));
			changes.remove(0);
			for (int i = 0; i < routers.numOfRouters; ++i) {
				dv(routers);
			}
			routers.print(out);
			printMessageRoute(messageNums, messageString, routers, out);
		}
	}
	
	private void dv(router route) {
		// for each router
		for (int i = 0; i < route.numOfRouters; ++i) {
			// for each seeable neighbor
			for (int j = 0; j < route.nextHops.length; ++j) {
				if (route.nextHops[i][j] != Integer.MAX_VALUE/2) {
					for (int p = 0; p < route.nextHops.length; ++p) {
						// give neighbor my routing table
						int temp = route.dists[i][p];
						route.dists[i][p] = minimum(route.dists[i][p],(route.dists[j][p]+route.dists[i][j]));
						if (temp != route.dists[i][p] ) {
							route.nextHops[i][j] = p;
							route.nextHops[j][i] = p;
						}
					}
				}
			}
		}
		
		// "I just did a Big-O of n^3... thats horrible." My Brain: "Do It Again"
		for (int i = route.numOfRouters-1; i >= 0; --i) {
			// for each seeable neighbor
			for (int j = route.nextHops.length-1; j >= 0; --j) {
				if (route.nextHops[i][j] != Integer.MAX_VALUE/2) {
					for (int p = route.nextHops.length-1; p >= 0; --p) {
						// give neighbor my routing table
						int temp = route.dists[i][p];
						route.dists[i][p] = minimum(route.dists[i][p],(route.dists[j][p]+route.dists[i][j]));
						if (temp != route.dists[i][p]) {
							route.nextHops[i][j] = p;
							route.nextHops[j][i] = p;
						}
					}
				}
			}
		}
	}
	
	private int minimum(int me, int meandyou) {
		if (me < meandyou)
			return me;
		else
			return meandyou;
	}
	
	private void printMessageRoute(ArrayList<int[]> messageNums, ArrayList<String> messageString, router router, PrintStream out) {
		for (int i = 0; i < messageNums.size(); ++i) {
			int temp;
			int from = temp = messageNums.get(i) [0];
			int to = messageNums.get(i) [1];
			String message = messageString.get(i);
			if (router.nextHops[from][to] == -1) {
				out.print("From " + (from+1) + " to " + (to+1) + ": hops unreachable; message: "+ message);
				break;
			}
			List<Integer> hops = new ArrayList<>();
			
			for(int j =0; j < router.numOfRouters; ++j) {
				hops.add(temp+1);
				temp = routers.nextHops[temp][to];
			}
			
			out.print("From " + (from+1) + " to " + (to+1) + ": hops ");
			for (int j = 0; j < hops.size();++j) {
				out.print(hops.get(j)+ " ");
			}
			out.println((to+1) + "; message: " + message);
		}
		out.println();
		out.println();
	}

	private static int[] getChange(String s) {
		String processed[] = s.split(" ");
		int toRet[] = new int[3];
		for (int i = 0; i < toRet.length; ++i)
			toRet[i] = Integer.parseInt(processed[i]);
		if (toRet[2]== -999) 
			toRet[2] = Integer.MAX_VALUE/2;
		return toRet;
	}

	private static int[] getMessage(String s, ArrayList<String> message) {
		int toRet[] = new int[2];
		int index = s.indexOf(" ");
		for (int i = 0; i < 2; ++i) {
			toRet[i] = Integer.parseInt(s.substring(0, index)) - 1;
			s = s.substring(index + 1);
			index = s.indexOf(" ");
		}
		message.add(s);
		return toRet;
	}
	
}
