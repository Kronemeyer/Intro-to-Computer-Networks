import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class lsrouter {

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
					dists[i][j] = -999;
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
		new lsrouter(args);
	}

	public lsrouter(String args[]) throws IOException {
		
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

		// RUN DIJKSTRAS ON ALL ROUTERS

		for (int i = 0; i < routers.numOfRouters; ++i) {
			dijkstras(routers.nextHops, routers.dists, i);
		}

		routers.print(out);
		
		printMessageRoute(messageNums, messageString, routers, out);

		while (changes.size() > 0) {
			routers.applyChange(changes.get(0));
			changes.remove(0);
			for (int i = 0; i < routers.numOfRouters; ++i) {
				dijkstras(routers.nextHops, routers.dists, i);
			}
			routers.print(out);
			printMessageRoute(messageNums, messageString, routers, out);
		}
		System.out.println();
	}
	
	private void printMessageRoute(ArrayList<int[]> messageNums, ArrayList<String> messageString, router router, PrintStream out) {
		for (int i = 0; i < messageNums.size(); ++i) {
			int temp;
			int from = temp = messageNums.get(i) [0];
			int to = messageNums.get(i) [1];
			String message = messageString.get(i);
			List<Integer> hops = new ArrayList<>();
			
			while (temp != to) {
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
	}

	private static int[] getChange(String s) {
		String processed[] = s.split(" ");
		int toRet[] = new int[3];
		for (int i = 0; i < toRet.length; ++i)
			toRet[i] = Integer.parseInt(processed[i]);
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

	private void dijkstras(int nextH[][], int routDist[][], int router) {
		int visited[] = new int[routers.numOfRouters];
		int dist[] = new int[routers.numOfRouters];
		Integer parent[] = new Integer[routers.numOfRouters];
		ArrayList<Integer> parenta = new ArrayList<>();
		
		for (int i = 0; i < routers.numOfRouters; ++i) {
			dist[i] = Integer.MAX_VALUE;
			visited[i] = 0;
		}
		dist[router] = 0;
		parent[router] = -1;
		
		for (int at = 0; at < routers.numOfRouters; ++at) {
			int pos = minimum(dist, visited);
			visited[pos] = 1;
			for (int to = 0; to < routers.numOfRouters; ++to) {
				if (visited[to] == 0 && routDist[pos][to] != -999 && dist[pos] != Integer.MAX_VALUE && dist[pos] + routDist[pos][to] < dist[to]) {
					routDist[router][to] = dist[to] = dist[pos] + routDist[pos][to];
					parent[to] = pos;
				}
			}
		}
		
		for (int i = 0; i < parent.length; ++i) {
			parenta.add(parent[i]);
		}
		
		int rooter, root=0;
		for (int i = 0; i < routers.numOfRouters;++i) {
			if (parenta.get(i) == -1) {
				root = i;
				nextH[router][router] = root;
			}
		}
			for (int i = 0; i < routers.numOfRouters; ++i) {
				rooter = i;
				ArrayList<Integer> walk = new ArrayList<>();
				while (rooter != root) {
					walk.add(rooter);	
					rooter = parenta.get(rooter);
				}
				if (walk.size() > 0) 
					nextH[router][i] = walk.get(walk.size()-1);
			}
	}

	private int minimum(int dist[], int[] visited) {
		int min = Integer.MAX_VALUE, index = 0;
		for (int i = 0; i < routers.numOfRouters; ++i) {
			if (dist[i] <= min && visited[i] == 0) {
				min = dist[i];
				index = i;
			}
		}
		return index;
	}
}
