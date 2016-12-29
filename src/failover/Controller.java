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
	private int remainingCap; // Remaining capacity of the system (system capacity - load)
	private HashMap<ENodeB, String> orphans; // orphan eNodeBs
	private boolean isAlive;

	public Controller(int name, int rCap, long maxTime) {
		super(("Controller" + Integer.toString(name)), maxTime);
		this.remainingCap = rCap;
		eNodeBs = new ArrayList<ENodeB>();
		orphans = new HashMap<ENodeB, String>();
		isAlive = false;
		System.out.println(getName() + " is created");
	}

	/**
	 * Adds an eNodeB to the controllers database. Sets the controller to the
	 * eNodeB
	 * 
	 * @param e an eNodeB (LTE tower)
	 */
	public void addENodeB(ENodeB e) {
		e.setController(this);
		eNodeBs.add(e);
		System.out.println(name + " adopts " + e.getName());
	}

	/**
	 * Adds to a list of orphan nodes
	 */
	public void addOrphan(ENodeB b) {
		orphans.put(b, "");
	}
	
	/**
	 * Adds to a list of orphan nodes for backup
	 */
	public void addBackup(ENodeB b) {
		if ( !b.hasBackupController() ) {
			b.setBackupController(this);
		}
	}

	/**
	 * Runs the thread ( thread.start() )
	 */
	@Override
	public void run() {
		System.out.println(getTime(System.currentTimeMillis()) + ": Running thread " + name);
		isAlive = true;
		
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
			removeController();
			System.out.println();
		}
		
		isAlive = false;
		System.out.println(getTime(System.currentTimeMillis()) + ": Closing thread " + name);
	}

	/**
	 * Adopts orphan eNodeBs when controller fails
	 */
	private void adoptOrphans() {
		for (ENodeB b: orphans.keySet()) {
			System.out.print( getTime(System.currentTimeMillis()) + ": ");
			addENodeB(b);
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

	public boolean isAlive() {
		return isAlive;
	}
}
