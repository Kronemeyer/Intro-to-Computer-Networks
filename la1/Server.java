
/* A Java program for a Server */
import java.net.*; 
import java.io.*; 
  
public class Server 
{ 
/* initialize socket and input stream */
private Socket socket = null; 
private ServerSocket server = null;
private BufferedReader input = null;
private DataOutputStream out = null;
private DataInputStream in = null; 

/* constructor with port */
public Server(int port) 
{ 
	try { 
		server = new ServerSocket(port); 
	} catch(Exception i) {
		System.out.println("Error in port");
		System.exit(0);
	}
	System.out.println("Server started"); 

	System.out.println("Waiting for a client ..."); 
	try {
		socket = server.accept(); 
		System.out.println("Client accepted"); 

		input = new BufferedReader(new InputStreamReader(System.in));
		out = new DataOutputStream(socket.getOutputStream());
		in = new DataInputStream( 
		    new BufferedInputStream(socket.getInputStream())); 
		
		String cstring = ""; 
		String sstring = "";

		while (!cstring.equals("Over")) 
		{	
		        cstring = in.readUTF(); 
			System.out.println("Got input from Client ...");
		        System.out.println("Printing input: " + cstring);
			sstring = input.readLine();
			out.writeUTF(sstring);
			System.out.println();	
		} 

		System.out.println("Closing connection"); 
		socket.close(); 
		in.close(); 

	} catch(EOFException i) { 
	    System.out.println(i); 
	} 
	catch(Exception i) { 
	    System.out.println(i); 
	} 
}

public static void main(String args[]) 
{ 
	if (args.length < 1) {
		System.out.println("Server usage: java Server #port_number");
	}
	else {
		try {
			Server server = new Server(Integer.parseInt(args[0])); 
		} catch(Exception i) {
			System.out.println("Error in port");	
		}
	}
} 

} 
