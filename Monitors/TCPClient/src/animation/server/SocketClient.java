package animation.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import animation.Gui;

public class SocketClient extends Thread {
	
	int port = 8080;
	String address = "127.0.0.1";
	boolean connected = false;
	
	Socket clientSocket = null;
	private Gui gui;
	
	public SocketClient(Gui gui) {
		this.gui = gui;
	}
	
	private DataOutputStream outToServer;
	
	@Override
	public void run() {
		super.run();
		BufferedReader inFromServer = null;
		String event = null;
		
		while (!Thread.currentThread().isInterrupted()) {
			
			while (!connected && !Thread.currentThread().isInterrupted()) {
		    	try {
		    		clientSocket = new Socket("localhost", 8080);
		    		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		    		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		    		connected = true;
		    	} catch (UnknownHostException e) {
				} catch (IOException e) {
				}
	    	}
			
			while (connected && !Thread.currentThread().isInterrupted()) {
	    		
	    		try {
					event = inFromServer.readLine();
				} catch (IOException e) {
					connected = false;
				}
	    		
	    		if (event == null) {
	    			connected = false;
	    		}
	    		else {
	    			gui.pushEvent(event);
	    		}
	    		
	    		
	    	}
		}
		if (clientSocket != null) {
			try {
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void write(String data) {
		if (connected) {
			System.out.println("write data:" + data);
			try {
				outToServer.writeBytes(data + "\r\n");
			} catch (IOException e) {
				connected = false;
			}
		}
	}
}
