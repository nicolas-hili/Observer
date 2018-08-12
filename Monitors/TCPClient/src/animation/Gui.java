package animation;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.concurrent.ArrayBlockingQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;

import animation.server.SocketClient;


public class Gui  extends JFrame implements Runnable {
	
   private SocketClient client;

   private volatile ArrayBlockingQueue<String> events = new ArrayBlockingQueue<String>(20);
	
   // Connect status constants
   final static int DISCONNECTED = 0;
   final static int BEGIN_CONNECT = 1;
   final static int CONNECTED = 2;

   // Various GUI components and info
//   public static JFrame mainFrame = null;
   public static JTextArea chatText = null;
   public static JTextField chatLine = null;
   public static JLabel statusBar = null;
   public static JTextField ipField = null;
   public static JTextField portField = null;
//   public static JRadioButton hostOption = null;
//   public static JRadioButton guestOption = null;
   public static JButton connectButton = null;
   public static JButton disconnectButton = null;

   // Connection info
   public static String hostIP = "localhost";
   public static int port = 8080;
   public static int connectionStatus = DISCONNECTED;
   public static boolean isHost = true;
   
   public static Gui instance;
   
   public Gui(String title) {
	   super(title);
	   instance = this;
   }

   private JPanel initOptionsPane() {
		
		
      JPanel pane = null;
      ActionAdapter buttonListener = null;

      // Create an options pane
      JPanel optionsPane = new JPanel(new GridLayout(4, 1));

      // IP address input
      pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pane.add(new JLabel("Host IP:"));
      ipField = new JTextField(10); ipField.setText(hostIP);
      ipField.setEditable(true);
      pane.add(ipField);
      optionsPane.add(pane);

      // Port input
      pane = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      pane.add(new JLabel("Port:"));
      portField = new JTextField(10); portField.setEditable(true);
      portField.setText((new Integer(port)).toString());
      pane.add(portField);
      optionsPane.add(pane);

      // Connect/disconnect buttons
      JPanel buttonPane = new JPanel(new GridLayout(1, 2));
      buttonListener = new ActionAdapter() {
            public void actionPerformed(ActionEvent e) {
               // Request a connection initiation
               if (e.getActionCommand().equals("connect")) {
                  connectButton.setEnabled(false);
                  disconnectButton.setEnabled(true);
                  connectionStatus = BEGIN_CONNECT;
                  ipField.setEnabled(false);
                  portField.setEnabled(false);
                  chatLine.setEnabled(true);
                  statusBar.setText("Online");
                  repaint();
                  client = new SocketClient(instance);

                  client.start();
               }
               // Disconnect
               else {
                  connectButton.setEnabled(true);
                  disconnectButton.setEnabled(false);
                  connectionStatus = DISCONNECTED;
                  ipField.setEnabled(true);
                  portField.setEnabled(true);
                  chatLine.setText(""); chatLine.setEnabled(false);
                  statusBar.setText("Offline");
                  repaint();
                  client.interrupt();
               }
            }
         };
      connectButton = new JButton("Connect");
      connectButton.setMnemonic(KeyEvent.VK_C);
      connectButton.setActionCommand("connect");
      connectButton.addActionListener(buttonListener);
      connectButton.setEnabled(true);
      disconnectButton = new JButton("Disconnect");
      disconnectButton.setMnemonic(KeyEvent.VK_D);
      disconnectButton.setActionCommand("disconnect");
      disconnectButton.addActionListener(buttonListener);
      disconnectButton.setEnabled(false);
      buttonPane.add(connectButton);
      buttonPane.add(disconnectButton);
      optionsPane.add(buttonPane);

      return optionsPane;
   }

   private void initGUI() {
	   
	   instance = this;
		  
      // Set up the status bar
      statusBar = new JLabel();
      statusBar.setText("Offline");

      // Set up the options pane
      JPanel optionsPane = initOptionsPane();

      // Set up the chat pane
      JPanel chatPane = new JPanel(new BorderLayout());
      chatText = new JTextArea(10, 20);
      DefaultCaret caret = (DefaultCaret)chatText.getCaret();
      caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
      
      chatText.setLineWrap(true);
      chatText.setEditable(false);
      chatText.setForeground(Color.blue);
      JScrollPane chatTextPane = new JScrollPane(chatText,
         JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
         JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      chatLine = new JTextField();
      chatLine.setEnabled(false);
      chatLine.addActionListener(new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			client.write(e.getActionCommand());
			chatLine.setText("");
		}
	});
      chatPane.add(chatLine, BorderLayout.SOUTH);
      chatPane.add(chatTextPane, BorderLayout.CENTER);
      chatPane.setPreferredSize(new Dimension(200, 200));
      

      // Set up the main pane
      JPanel mainPane = new JPanel(new BorderLayout());
      mainPane.add(statusBar, BorderLayout.SOUTH);
      mainPane.add(optionsPane, BorderLayout.WEST);
      mainPane.add(chatPane, BorderLayout.CENTER);

      // Set up the main frame
      //mainFrame = new JFrame("TCP Client");
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.setContentPane(mainPane);
      this.setSize(this.getPreferredSize());
      this.setLocation(200, 200);
      this.setSize(new Dimension(1200, 400));
      this.setVisible(true);
   }
   
/*   public void pushEvent(String data) {
	   chatText.append("\n"+data);
   } */

   public static void main(String args[]) {
      Gui gui = new Gui("TCP CLIent");
      gui.initGUI();
   }

	@Override
	public void run() {
		synchronized (this.events) {
			String el = null;
			while ((el = this.events.poll()) != null) {
				chatText.append(el + "\n");
			}
		}
	}
	
	public void updateGUI(String event) {
	   setEvent(event);
	   SwingUtilities.invokeLater(this);
	}

	private synchronized void setEvent(String event) {
		this.events.add(event);
	}
}

// Action adapter for easy event-listener coding
class ActionAdapter implements ActionListener {
   public void actionPerformed(ActionEvent e) {}
}