package animation;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import animation.model.Model;
import animation.view.View;

public class Interpreter extends Thread {

	private BlockingQueue<String> queue  = new PriorityBlockingQueue<String>();
	
	private View view;
	
	boolean debug = true;
	
	Model model = new Model();
	
	public Interpreter() {
		super();
	}
	
	@Override
	public void run() {
		super.run();	
		
		view = new View(model);
			
		Timer timer = new Timer();
    	TimerTask myTask = new TimerTask() {
    	    @Override
    	    public void run() {
    	    	pollingEvents();	
    	    }
    	};
    	
    	timer.schedule(myTask, 200, 200);
	    		
	}
	
	public void pollingEvents() {
		String event;
    	
    	while ((event = queue.poll()) != null) {
    		
    		if (debug)
    			System.out.println(event);
			
			String[] fields = event.split("[|]");

    		if (fields.length < 9	) {
    			//System.err.println("malformed event");
    			continue;
    		}
    		
    		String sourceName = fields[1];
    		String state = fields[2];
    		String eventSource = fields[3];
    		String eventKind= fields[4];
    		
    		String[] p = fields[8].split(";");
    		Map<String, String> params = new HashMap<String, String> ();
    		
    		for (String param : p) {
    			String[] keyValue = param.split(":");
    			if (keyValue.length < 2)
    				continue;
    			params.put(keyValue[0], keyValue[1]);
    		}
    		//String capsuleName = params.get("capsuleName");
    		String capsuleName = params.get("SenderCapsule");
    		int chute = 0, 
    				parcel = 0,
    				stage = 0,
    				bin = 0,
    				door = 0;
    		
//    		System.err.println(eventKind);
    		
    		// Chute occupied
    		if (capsuleName.equals("Chute") && state.equals("OCCUPIED") && eventKind.equals("15") && params.get("p.level") != null) {
    			chute = Integer.valueOf(sourceName.substring(11,sourceName.length()-2));
    			parcel = Integer.valueOf(params.get("p.number")) + 1;
				stage = Integer.valueOf(params.get("p.stage"));
				model.stages[stage].chutes[chute-1] = parcel;
				
				if (chute == 2) {
					model.stages[stage].chutes[0] = 0;
				}
				else if (chute == 1) {
					if (stage != 0) {
						model.stages[0].switcher = 0;	
					}
				}
    		}
    		
    		// Switch occupied
    		if (capsuleName.equals("Switcher") && state.equals("OCCUPIED") && eventKind.equals("15") && params.get("p.level") != null) {
    			parcel = Integer.valueOf(params.get("p.number")) + 1;
				stage = Integer.valueOf(params.get("p.stage"));
				model.stages[stage].switcher = parcel;
				
				model.stages[stage].chutes[1] = 0;
    		}
    		
    		// Switch door
    		if (capsuleName.equals("Switcher") && state.equals("IDLE") && eventKind.equals("15") && params.get("c") != null && params.get("stage") != null) {
				door = Integer.valueOf(params.get("c"));
				door = (door == 0) ? 1 : -door;
				stage = Integer.valueOf(params.get("stage"));
				model.stages[stage].door = door;
    		}
    		
    		// Bin occupied
    		if (capsuleName.equals("Bin") && state.equals("IDLE") && eventKind.equals("15") && params.get("p.number") != null) {
    			parcel = Integer.valueOf(params.get("p.number")) + 1;
				bin = Integer.valueOf(sourceName.substring(17,sourceName.length()-2));
				model.bins[bin] = parcel;
				if (bin == 0 || bin == 1) {
					model.stages[1].switcher = 0;
				}
				else if (bin == 2 || bin == 3) {
					model.stages[2].switcher = 0;
				}
				
    		}
    		
		}
    	view.repaint();
    	
	}

	public void pushEvent(String event) {
		if (!queue.offer(event))
			System.out.println("Impossible to insert into queue");
	}
}
