package failover;

import java.util.ArrayList;
/**
 * This is a message that gets sent
 * between eNodeBs and Controllers
 * 
 * @author nathankong
 *
 */
public class Message {
	private Controller controller;
	private ENodeB orphan;
	private ArrayList<ENodeB> eNodeB;
	private int hops;
	
	public Message (ENodeB orphan){
		controller = null;
		this.orphan = orphan;
		this.eNodeB = new ArrayList<ENodeB>();
		this.hops = 1; // includes the last hop to the controller
	}
	
	/**
	 * Used for the controller's
	 * adoption message
	 * 
	 * @param c the controller
	 */
	public void setController(Controller c) {
		controller = c;
	}
	
	/**
	 * Returns the amount of hops
	 * to get to the controller.
	 * Gives an idea about the path
	 * to the controller
	 * @return hops
	 */
	public int getHops() {
		return hops;
	}
	
	/**
	 * Used for controller messages to
	 * orphan eNodeBs. Tells the orphan
	 * the response from the controller
	 * 
	 * @return the controller
	 */
	public Controller getController() {
		if (controller != null) {
			return controller;
		}
		return controller;
	}
	
	/**
	 * Adds the last eNodeB that
	 * sends the orphan message
	 * 
	 * @param e
	 */
	public void addBreadcrumb(ENodeB e){
		eNodeB.add(e);
		hops++;
	}
	
	/**
	 * Getting the breadcrumb when the
	 * controller responses to the orphan
	 * message
	 * 
	 * @return eNodeB that sends 
	 */
	public ENodeB removeBreadcrumb(){
		if(!eNodeB.isEmpty()) {
			return eNodeB.remove(eNodeB.size()-1);
		}
		else {
			return null;
		}
	}
	
	/**
	 * Gets the Orphan eNodeB when
	 * 1) sending messages to the controller
	 * 2) determining who the orphan is
	 * 
	 * @return the orphan eNodeB
	 */
	public ENodeB getOrphan() {
		return orphan;
	}
	
	/**
	 * Determines if it is at its location
	 */
	public boolean atOrphan(){
		if (eNodeB.isEmpty()){
			return true;
		}
		return false;
	}
}
