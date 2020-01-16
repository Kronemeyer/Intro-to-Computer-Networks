
/* A Java program for a Client */
import java.net.*; 
import java.io.*; 
  
public class Client 
{ 
/* initialize socket and input output streams */
private Socket socket = null;
private BufferedReader input = null; 
private DataOutputStream out = null; 
private DataInputStream in = null;

/* constructor to put ip address and port */
public Client(String address, int port) 
{ 
	/* establish a connection */
	try {
		socket = new Socket(address, port); 
	} catch(Exception i) {
		System.out.println("Error in IP or port");
		System.exit(0);
    	}
	System.out.println("Connected"); 
	try { 
		input = new BufferedReader(new InputStreamReader(System.in)); 
		out = new DataOutputStream(socket.getOutputStream()); 
		in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	} catch(IOException i) { 
		System.out.println(i); 
	} 
	
	String cstring = ""; 
	String sstring = "";

	while (!cstring.equals("Over")) { 
		try { 
			cstring = input.readLine(); 
			out.writeUTF(cstring);
			sstring = in.readUTF();	
			System.out.println("Got input from Server ...");
			System.out.println("Printing input: " + sstring);
			System.out.println();

		} catch(Exception i) { 
			System.out.println(i); 
	  } 
	}	
	try { 
		input.close(); 
		out.close(); 
		socket.close();
	} catch(Exception i) {
		System.out.println(i);  
	} 
}

public static void main(String args[]) 
{ 
	if (args.length < 2) {
		System.out.println("Client usage: java Client #IP_address #port_number");
	}
	else {
		Client client = new Client(args[0], Integer.parseInt(args[1])); 
	}
} 

}
