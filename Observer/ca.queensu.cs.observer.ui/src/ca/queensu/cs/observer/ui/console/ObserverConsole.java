package ca.queensu.cs.observer.ui.console;

import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class ObserverConsole {
	
	private MessageConsole console;
	private MessageConsoleStream stream;
	private IOConsoleOutputStream iostream;

	public ObserverConsole() {
		// Create a console
		this.console = findConsole("Observer Console");
		this.stream = this.console.newMessageStream();	
		this.stream.setActivateOnWrite(true);
		this.iostream = this.console.newOutputStream();
	}

	private static ObserverConsole instance;

	public static ObserverConsole getInstance () {
		if (instance == null)
			instance = new ObserverConsole();
		
		return instance;
	}
	
	public void write(String msg) {
		try {
			this.stream.write(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	   }

	public IOConsoleOutputStream getStream() {
		return this.iostream;
	}
}
