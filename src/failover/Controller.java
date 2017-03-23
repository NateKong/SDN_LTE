package failover;

/**
 * A Software Defined Network controller.
 * A controller for a LTE network
 * over eNodeBs.
 * 
 * @author Nathan Kong
 * @since Jan 2017
 */

import java.util.ArrayList;
import java.util.HashMap;

public class Controller extends Entity implements Runnable {
	private ArrayList<ENodeB> eNodeBs;
	private ArrayList<Message> orphans;
	
	public Controller(int name, long maxTime, int load) {
		super(("Controller" + Integer.toString(name)), maxTime, load);
		eNodeBs = new ArrayList<ENodeB>();
		orphans = new ArrayList<Message>();//new HashMap<ENodeB, HashMap<ENodeB,ENodeB>>();
		//System.out.println(getName() + " is created");
	}

	/**
	 * Adds an eNodeB to the controllers database. Sets the controller to the
	 * eNodeB
	 * 
	 * @param e an eNodeB (LTE tower)
	 */
	public void addENodeB(ENodeB e1, Entity e2) {
		e1.setController(this);
		e1.setEntity(e2);
		eNodeBs.add(e1);
		//System.out.println(name + " adopts " + e1.getName());
	}

	/**
	 * Runs the thread ( thread.start() )
	 */
	@Override
	public void run() {
		System.out.println(getTime() + ": Running thread " + name);
		try {
			while (checkTime(System.currentTimeMillis())) {
				Thread.sleep(random());

				if (!orphans.isEmpty()) {
					adoptOrphans();
				}
			}
		} catch (InterruptedException e) {
			System.out.println(name + " interrupted.");
		}

		if (name.equals("Controller1")){
			for (Connection c: connections) {
				Entity e = c.getEndpoint(this);
				e.removeConnection(c);
			}
			removeController();
			System.out.println("\n" + getTime() + ": Closing thread " + name + "\n");
		} else {
			System.out.println(getTime() + ": Closing thread " + name);
		}
	}

	/**
	 * Sends adoption message back to eNodeBs
	 */
	private void adoptOrphans() {
		for (Message m: orphans) {
			ENodeB orphan = m.getOrphan();
			ENodeB e = m.removeBreadcrumb();
			m.setController(this);
			e.sendAdoptionMessage(m);
			//System.out.println(getTime() + ": " + name + " sends adoption message to " + e.getName() + " for orphan " + orphan.getName());
		
		}
		orphans.clear();
	}

	/**
	 * Removes the controller from the eNodeBs This acts as controller failure
	 */
	private void removeController() {
		for (ENodeB b : eNodeBs) {
			b.setController(null);
		}
		eNodeBs.clear();

	}
	
	/**
	 * Sends a message to the controller
	 * 
	 * @param eNodeB
	 */
	 public void messageController(Message orphanMessage) {
		orphans.add(orphanMessage);
	}
}
