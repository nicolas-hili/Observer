package animation.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import animation.Interpreter;

public class SocketClient extends Thread {
	Interpreter interpreter;
	
	int port = 8080;
	String address = "127.0.0.1";
	boolean connected = false;
	
	Socket clientSocket = null;
	
	public void setInterpreter(Interpreter interpreter) {
		this.interpreter = interpreter;
	}
	
	@Override
	public void run() {
		super.run();
		BufferedReader inFromServer = null;
		String event = null;
		
		while (true) {
		
			while (!connected) {
		    	try {
		    		clientSocket = new Socket("localhost", 8080);
		    		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		    	//	System.out.println("connected");
		    		connected = true;
		    	} catch (UnknownHostException e) {
				} catch (IOException e) {
				}
	    	}
			
			while (connected) {
	    		
	    		try {
					event = inFromServer.readLine();
				} catch (IOException e) {
					connected = false;
				//	System.out.println("IOException: server disconnected");
	    			break;
				}
	    		
	    		if (event == null) {
	    			connected = false;
	    		//	System.out.println("server disconnected");
	    			break;
	    		}
	    		
	    		interpreter.pushEvent(event);
	    		
	    		
	    	}
		}
	}
}
