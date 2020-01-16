
/* A Java program for a Server */
import java.net.Socket;
import java.net.ServerSocket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;


/**
 * @author Ben Kronemeyer
 * 
 *         Creates a chat ServerSocket to accept incoming connections. Once an
 *         acceptable name has been entered chat_server creates a thread for
 *         each new connection. These threads are named based on the username
 *         chosen.
 *
 *         Unacceptable names: Already existing names, Empty names, "Y", "N"
 */
public class chat_server {
	/* initialize socket and input stream */
	private Socket socket = null;
	private ServerSocket server = null;
	private DataOutputStream out = null;
	private DataInputStream in = null;
	private static ConcurrentHashMap<String, client> userMap = null;
	private Semaphore sema = new Semaphore(0);
	private String name = null;

	/* constructor with port */
	public chat_server(int port) {
		userMap = new ConcurrentHashMap<String, client>();
		try {
			server = new ServerSocket(port);
			if (!server.isBound()) {
				System.exit(0);
			}
			System.out.println("Server started");
			System.out.println("Waiting for a clients ...");
			while (true) {
				socket = server.accept();
				out = new DataOutputStream(socket.getOutputStream());
				in = new DataInputStream(socket.getInputStream());
				out.writeUTF("Please enter a username.");
				name = in.readUTF().toLowerCase();
				while (userMap.containsKey(name) || name.isEmpty() || name.equalsIgnoreCase("y")
						|| name.equalsIgnoreCase("n")) {
					out.writeUTF("Error, please choose a different username.");
					name = in.readUTF().toLowerCase();
				}
				System.out.println("Client " + name + " Accepted.");
				client newClient = new client(socket, name, in, out);
				Thread thread = new Thread(newClient);
				userMap.put(name, newClient); 
				out.writeUTF("Log in successful\nType \"off\" to turn off auto user list update \n"
						+ "Type \"update\" to update user list");
				thread.setName(name);
				thread.start();
			}
		} catch (IOException i) {
			System.out.println(i);
		}
	}



	/**
	 * @author Ben Kronemeyer
	 * 
	 *         Client object that implements runnable.
	 *
	 */
	private class client implements Runnable {

		boolean busy = false;
		boolean autoupdate = true;
		boolean dnd = false;
		boolean accepted = false;
		String name;
		String chatter = "";
		Socket clientSocket;;
		final DataOutputStream out;
		final DataInputStream in;
		BlockingQueue<String> q = new ArrayBlockingQueue<>(5);
		boolean exit;

		/**
		 * Constructor Method for clients.
		 * 
		 * @param sSock - The client socket
		 * @param name  - The clients Username
		 * @param din   - The DataInputStream for the client
		 * @param dout  - The DataOutputStream for the client
		 */
		public client(Socket sSock, String name, DataInputStream din, DataOutputStream dout) {
			this.name = name;
			this.clientSocket = sSock;
			this.out = dout;
			this.in = din;
			this.busy = false;
		}

		/**
		 * The thread execution of client. Allows for requesting connections to chat
		 * with other users and denying chat requests. Also allows for switching chat
		 * settings for quality of life purposes.
		 */
		public void run() {
			String outgoingString = "";

			update();

			while (!exit) {
				try {
					if (in.available() > 0) {
						outgoingString = this.in.readUTF().toLowerCase();
					}
					
					// If the clients queue contains anything, stop and deal with it
					if (q.size() > 0) {
						chat:
						for (int i = 0; i < q.size(); i++) {
							chatter = q.poll();
							out.writeUTF("\n"+chatter + " is requesting to chat, press \"y\" to accept, \"n\" to decline");
							outgoingString = in.readUTF();
							// Deal with it, foo'
							while (!exit) {
								// Yeah lets chat
								if (outgoingString.equalsIgnoreCase("y")) {
									userMap.get(chatter).accepted = true;
									sema.release();
									out.writeUTF("Connected to " + chatter +"\n");
									while (!outgoingString.equalsIgnoreCase("over") && !this.chatter.isEmpty()) {
										outgoingString = in.readUTF();
										if (!this.chatter.isEmpty())
											userMap.get(chatter).out.writeUTF(this.name + ": "  + outgoingString);
									}
									chatEnd();
									out.writeUTF("You have disconnected from chat");
									update(this.name);
									exit = true;
								// Nah I dont like you
								} else if(outgoingString.equalsIgnoreCase("n")) {
									sema.release();
									out.writeUTF(chatter + " denied your chat request.");
									chatter = "";
									exit = true;
								// Wait what did you say?
								} else {
									out.writeUTF("Incorrect input, please enter \"y\" or \"n\"");
									outgoingString = in.readUTF();
								}
							}
							exit = false;
						}
					}
					
					// Check for system choices
					choices(outgoingString);

					// Send request to other user with timeout
					if (userMap.containsKey(outgoingString) && !userMap.get(outgoingString).busy
							&& !outgoingString.equals(this.name)) {
						chatter = outgoingString;
						setBusy();
						userMap.get(chatter).setBusy();
						userMap.get(chatter).q.add(this.name);
						
						//wait for chatter to deal with your request
						sema.acquire();
						
						// chat request accepted, sets up outgoing messages to other user
						if (accepted) {
							outgoingString = "";
							out.writeUTF("You are connected with " + chatter);
							while (!outgoingString.equalsIgnoreCase("Over") && !this.chatter.isEmpty()) {
								outgoingString = this.in.readUTF();
								if (!this.chatter.isEmpty())
									userMap.get(chatter).out.writeUTF(this.name + ": " + outgoingString);
							}
							
							chatEnd();
							out.writeUTF("You have disconnected from chat");
							update(this.name);
						}
						
						// chat request denied, sends a denial message and sets user back to not busy
						else {
							out.writeUTF(chatter + " denied your request.");
							userMap.get(chatter).setNotBusy();
							setNotBusy();
						}
					}

					// Other person is busy
					else if (userMap.containsKey(outgoingString.toLowerCase()) && userMap.get(outgoingString).busy) {
						out.writeUTF("User is busy, please select another user");
					}

					// reset the outgoing string and flush the outputstream
					synchronized (this) {
					accepted = false;
					outgoingString = "";
					chatter = "";
					out.flush();
					}

				} catch (IOException | InterruptedException i) {
					try {
						this.out.close();
						this.in.close();
						this.clientSocket.close();
						userMap.remove(this.name);
						exit = true;
					} catch (IOException io) {
						break;
					}
				}
			}
		}

		/**
		 * @param inString - the choice the user gives to change chat settings
		 * @throws IOException - DataOutputException
		 */
		private void choices(String inString) throws IOException {

			switch (inString) {
			case ("logout"):
				this.out.close();
				this.in.close();
				userMap.remove(this.name);
				this.clientSocket.close();
				break;
			case ("update"):
				this.out.writeUTF("-------------------------\nCurrently Logged In Users");
				for (ConcurrentHashMap.Entry<String, client> sockets : userMap.entrySet())
					this.out.writeUTF(sockets.getKey() + ": " + sockets.getValue().isBusy());
				this.out.writeUTF("-------------------------");
				break;
			case ("off"):
				this.out.writeUTF("Automatic user update: OFF");
				autoupdate = false;
				break;
			case ("dnd"):
				if (!dnd) {
					this.out.writeUTF("Entering DND mode, type DNDOFF to resume");
					dnd = true;
					setBusy();
					autoupdate = false;
				}
				break;
			case ("dndoff"):
				if (dnd) {
					this.out.writeUTF("Exiting DND mode, welcome back!");
					dnd = false;
					busy = false;
					autoupdate = true;
					update();
				}
				break;
			default:
				break;
			}

		}
		
		/**
		 * Updates all the current users as long as the following criteria met: 1. The
		 * autoupdate is turned on 2. DnD mode turned off 3. The user is not in a chat
		 */
		public void update() {
			try {
				for (ConcurrentHashMap.Entry<String, client> user : userMap.entrySet()) {
					if (user.getValue().autoupdate) {
						user.getValue().out.writeUTF("-------------------------\nCurrently Logged In Users");
						for (ConcurrentHashMap.Entry<String, client> sockets : userMap.entrySet())
							user.getValue().out.writeUTF(sockets.getKey() + ": " + sockets.getValue().isBusy());
						user.getValue().out.writeUTF("-------------------------");
					}
				}
			} catch (Exception i) {
				i.printStackTrace();
			}
		}
		
		/**
		 * Updates only the user given.
		 * @param name - the person whose screen to update
		 */
		private void update(String name) {
			try {
				userMap.get(name).out.writeUTF("-------------------------\nCurrently Logged In Users");
				for (ConcurrentHashMap.Entry<String, client> sockets : userMap.entrySet())
					userMap.get(name).out.writeUTF(sockets.getKey() + ": " + sockets.getValue().isBusy());
				userMap.get(name).out.writeUTF("-------------------------");
			} catch (Exception i) {
				i.printStackTrace();
			}
		}
		
		/**
		 * @return Sets busy boolean to true
		 */
		private boolean setBusy() {
			return this.busy = true;
		}

		/**
		 * @return sets busy boolean to false
		 */
		private boolean setNotBusy() {
			return this.busy = false;
		}

		/**
		 * @return - the string form showing whether a user is busy or available
		 */
		private String isBusy() {
			if (busy)
				return "Busy";
			return "Available";
		}

		/**
		 * Sets the other users chatter to empty
		 */
		private void chatEnd() {
			if (!this.chatter.isEmpty())
				userMap.get(chatter).chatter = "";
			setNotBusy();
		}

	}

	public static void main(String args[]) {
		 if (args.length < 1) {
		 System.out.println("Server usage: java Server #port_number");
		 } else {
		try {
			 chat_server server = new chat_server(Integer.parseInt(args[0]));
		} catch (Exception i) {
			System.out.println(i);
			 }
		}
	}

}

