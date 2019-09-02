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
	String address = "localhost";
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
		    		clientSocket = new Socket(address, port);
		    		inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		    		outToServer = new DataOutputStream(clientSocket.getOutputStream());
		    		connected = true;
		    	} catch (UnknownHostException e) {
				} catch (IOException e) {
				}
	    	}
			System.out.println("connected");
			
			while (connected && !Thread.currentThread().isInterrupted()) {
	    		try {
					event = inFromServer.readLine();
					System.out.println("receiving event: " + event);
				} catch (IOException e) {
					connected = false;
				}
	    		
	    		if (event == null) {
	    			connected = false;
	    		}
	    		else {
	    			gui.updateGUI(event);
	    		//	gui.pushEvent(event);
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
		
		byte[] message = data.getBytes();
				
		if (connected) {
			System.out.println("write data:" + data);
			try {
				outToServer.writeInt(message.length);
				outToServer.write(message);
			} catch (IOException e) {
				connected = false;
			}
		}
	}
}
