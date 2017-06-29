package animation.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketServer  extends Thread {
	
	ServerSocket serverSocket;
    Socket clientSocket;
    
    int port = 8080;
	
	@Override
	public void run() {

		super.run();
		
		try {
			System.out.println("Opening server socket on port " + port);
			serverSocket =  new ServerSocket(port);
			clientSocket = serverSocket.accept();
			
			BufferedReader inFromClient =
	                 new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	         
	         while(true) {
	        	 String event = inFromClient.readLine();
	        	 if (event == null) {
	             	break;
	             }
	         }
	         
	         serverSocket.close();
	         System.out.println("Closing server socket");
	        	 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
