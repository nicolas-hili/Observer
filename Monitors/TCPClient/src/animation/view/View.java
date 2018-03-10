package animation.view;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JFrame;

import animation.model.Model;
import animation.model.Stage;

public class View extends JFrame {

	private static final long serialVersionUID = 1L;

	
	// For buffering 
	private Graphics2D bufferGraphics;
	private Image offscreen;
    
    // Dimension of the frame
	private Dimension dim = new Dimension(850, 550); 
    
    // Position of the graphics
	private int x, y;

	private Color[] colors = {
			Color.orange,
			Color.magenta,
			Color.red,
			Color.cyan
	};
	
	private Color green = new Color(0,128,0);

	// the model
	private Model model;
	
	public View(Model model) throws HeadlessException {
		
		this.model = model;
		
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		setSize(dim);
	    this.setVisible(true);
	    
	    init();
	    
	    
    	
        this.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent e) {
            	init();
            }

			@Override
			public void componentHidden(ComponentEvent arg0) {}

			@Override
			public void componentMoved(ComponentEvent arg0) {}

			@Override
			public void componentShown(ComponentEvent arg0) {}
        });
        
	}
	
	private void init () {
		
		dim = getSize();
    	x = dim.width / 2 - 40;
		y = 50; 
		
		// Create an offscreen image to draw on 
		offscreen = createImage(dim.width,dim.height); 
		
		// by doing this everything that is drawn by bufferGraphics 
        // will be written on the offscreen image.
        bufferGraphics = (Graphics2D)offscreen.getGraphics();
	}
	
	@Override
	public void paint(Graphics g) {
		
		// super.paint(g);
		if (bufferGraphics == null || dim == null)
			return;

		// Wipe off everything that has been drawn before 
        // Otherwise previous drawings would also be displayed.
		
		bufferGraphics.clearRect(0, 0, dim.width, dim.width); 
		
    	
    	this.createStage(bufferGraphics, x, y, 0, model.stages[0]);
    	this.createStage(bufferGraphics, x-50, 200+y, -1, model.stages[1]);
    	this.createStage(bufferGraphics, x+50, 200+y, 1, model.stages[2]);
    	this.createBin(bufferGraphics, x-300, 400+y, 1, model.bins[0]); // middle stage - 300
    	this.createBin(bufferGraphics, x-100, 400+y, 2, model.bins[1]); // bin 1 + 200
    	this.createBin(bufferGraphics, x+100, 400+y, 3, model.bins[2]);
    	this.createBin(bufferGraphics, x+300, 400+y, 4, model.bins[3]);
    	
    	// draw the offscreen image to the screen like a normal image.
        g.drawImage(offscreen,0,0,this);
        
        for (int i = 0; i < model.bins.length; i++)
        	model.bins[i] = 0;
	}
	
	public void update(Graphics g) {
		paint(g); 
	} 
	
	private void createStage(Graphics2D g2, int x, int y, int d, Stage s) {
    	
    	int door = s.door;
    	
    	this.createChute(g2, x, y, d, "#888", s.chutes[0]);
    	this.createChute(g2, d*50+x, 50+y, d, "#aaa", s.chutes[1]);
    	this.createSwitcher(g2,3*d*50+x, 100+y, d, door, "#ccc", s.switcher);
    }
	
	private void createChute(Graphics2D g2, int x, int y, int d, String color, int parcel) {
    	
    	g2.setStroke(new BasicStroke(10,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
    	g2.draw(new Line2D.Float(d*50+x, y, 2*d*50+x, 50+y));
    	g2.draw(new Line2D.Float(100+2*d*50+x, 50+y, 100+d*50+x, y));
    	
    	if (parcel > 0) {
    		g2.setStroke(new BasicStroke(2,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
    		g2.setColor(colors[parcel-1]);
    		g2.fill(new Rectangle2D.Double(35+50*1.5*d+x, 10+y, 30, 30));
    		g2.setFont(new Font("Arial", Font.PLAIN, 20));
    		long xt = Math.round(45+50*1.5*d+x);
    		g2.setColor(Color.black);
        	g2.drawString(String.valueOf(parcel), xt, 30+y);
    	}

    }
    
	private void createBin(Graphics2D g2, int x, int y, int number, int parcel) {
    	
    	g2.setStroke(new BasicStroke(10,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
    	g2.draw(new Line2D.Float(x, y, x, 70 + y));
    	g2.draw(new Line2D.Float(x, 70 + y, 100+x, 70 + y));
    	g2.draw(new Line2D.Float(100+x, y, 100+x, 70 + y));
    	
    	g2.setFont(new Font("Arial", Font.PLAIN, 20));
    	g2.drawString("Bin " + String.valueOf(number), -60+x, 70+y);
    	
    	if (parcel > 0) {
    		g2.setStroke(new BasicStroke(2,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
    		g2.setColor(colors[parcel-1]);
    		g2.fill(new Rectangle2D.Double(35+x, 20+y, 30, 30));
    		g2.setFont(new Font("Arial", Font.PLAIN, 20));
    		g2.setColor(Color.black);
        	g2.drawString(String.valueOf(parcel), 45+x, 40+y);
    	}
    }    
    
	private void createSwitcher(Graphics2D g2, int x, int y, int d, int door, String color, int parcel) {
    	
    	
    	// Shape
		g2.setStroke(new BasicStroke(10,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
    	g2.draw(new Line2D.Float(x, y, -100 + x, 100 + y));
    	g2.draw(new Line2D.Float(100+x,y,200+x, 100+y));
    	g2.draw(new Line2D.Float(50+x,50+y,x,100+y));
    	g2.draw(new Line2D.Float(50+x,50+y,100+x,100+y));
    	
    	// Switcher door
    	g2.setStroke(new BasicStroke(5,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
    	g2.setColor(green);
    	g2.draw(new Line2D.Float(50+x,50+y,50+door*40+x,y+10));
    	g2.setColor(Color.black);
    	
    	// Switcher origin
    	g2.setColor(green);
    	g2.fill(new Ellipse2D.Double(50+x-12, 50+y-12, 24, 24));
    	g2.setColor(Color.black);
    	
    	if (parcel > 0) {
    		g2.setStroke(new BasicStroke(2,BasicStroke.JOIN_ROUND,BasicStroke.CAP_ROUND));
    		g2.setColor(colors[parcel-1]);
    		g2.fill(new Rectangle2D.Double(35-door*50+x, 40+y, 30, 30));
    		g2.setFont(new Font("Arial", Font.PLAIN, 20));
    		g2.setColor(Color.black);
        	g2.drawString(String.valueOf(parcel), 35-door*50+x+10, 60+y);
    	}
    }

}
