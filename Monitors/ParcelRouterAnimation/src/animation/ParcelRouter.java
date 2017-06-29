package animation;

import animation.server.SocketClient;

public class ParcelRouter {

	private Interpreter interpreter;
	private SocketClient client;
	
	public ParcelRouter() {
		interpreter = new Interpreter();
		client = new SocketClient();
		client.setInterpreter(interpreter);
	}
	
	public void play() {
		interpreter.start();
		client.start();
	}

	public static void main(String[] args) {
		new ParcelRouter().play();
	}

}
