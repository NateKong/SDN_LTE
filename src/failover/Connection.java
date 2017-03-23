package failover;

/**
 * A General connection between two Entities.
 * 
 * @author Nathan Kong
 * @since Jan 2017
 */

public class Connection {
	private Entity[] endpoints;
	private String name;
	private int bw;

	public Connection(String name, Entity endpt0, Entity endpt1, int bw) {
		this.name = name;
		this.bw = bw;
		endpoints = new Entity[2];
		endpoints[0] = endpt0;
		endpoints[1] = endpt1;
		endpt0.addConnection(this);
		endpt1.addConnection(this);
		//System.out.println(endpt0.getName() + " has a X2 connected to " + endpt1.getName());
	}

	/**
	 * Gets the name of the connection
	 * @return the name of the connection
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Used to for an eNodeB to figure out the other eNodeB thats attached to
	 * it.
	 * 
	 * @param me is the endpoint that is asking
	 * @return the endpoint of the other eNodeB
	 */
	public Entity getEndpoint(Entity me) {
		if (endpoints[0].equals(me)) {
			return endpoints[1];
		} else if (endpoints[1].equals(me)) {
			return endpoints[0];
		}
		return null;
	}
	
	/**
	 * Get the bandwidth of the connection
	 * 
	 * @return the bandwidth of the connection
	 */
	public int getBw(){
		return bw;
	}
}
