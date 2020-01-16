
/* A Java program for a Client */
import java.net.*; 
import java.io.*; 
  
public class chat_client 
{ 
/* initialize socket and input output streams */
private Socket socket = null;
private BufferedReader input = null; 
private DataOutputStream out = null; 
private DataInputStream in = null;
private String inString = "";

/* constructor to put ip address and port */
public chat_client(String address, int port) 
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
	
	String outString = ""; 
	thread inThread = new thread();

	while (!outString.equals("Over")) { 
		try { 
			outString = input.readLine(); 
			out.writeUTF(outString);

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

public class thread extends Thread {
  public thread() {
	  super();
	  start();
  }

  public void run() {
      
    while (!inString.equals("Over")) {
      try {
        inString = in.readUTF();
        System.out.println(inString);
    } catch(IOException i) {
	    System.out.println(i);
      }
     }
    try {
    in.close();
    } catch (IOException i) {
      System.out.println(i);
    }
  }
}
public static void main(String args[]) 
{ 
	if (args.length < 2) {
		System.out.println("Client usage: java Client #IP_address #port_number");
	}
	else {
		chat_client client = new chat_client(args[0], Integer.parseInt(args[1])); 
	}
} 

}
