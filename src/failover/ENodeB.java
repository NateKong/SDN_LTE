package failover;

/**
 * An eNodeB in a LTE
 * architecture. The eNodeB
 * is controlled using SDN.
 * 
 * @author Nathan Kong
 * @since Jan 2017
 */

import java.util.concurrent.ConcurrentLinkedQueue;

public class ENodeB extends Entity implements Runnable {
	private Controller controller;
	private Controller bkController;
	private Entity toController;
	private Entity toBkController;
	private ConcurrentLinkedQueue<Message> orphanMessages;
	private ConcurrentLinkedQueue<Message> replyMessages;
	private int domain;
	private int bw;
	private int hops;
	private int backupBw;
	private int backupHops;
	
	
	public ENodeB(int name, long maxTime, int load, int domain, int bw, int hops) {
		super(("eNodeB" + Integer.toString(name)), maxTime, load);
		orphanMessages = new ConcurrentLinkedQueue<Message>();
		replyMessages = new ConcurrentLinkedQueue<Message>();
		this.domain = domain;
		this.bw = bw;
		this.hops = hops;
		this.backupBw = 0;
		this.backupHops = 100;
		//System.out.println(getName() + " is created");
	}

	/**
	 * Sets the controller for the eNodeB
	 * 
	 * @param c is the new controller for the eNodeB
	 */
	public void setController(Controller c) {
		controller = c;
	}
	
	/**
	 * Emulates a table registry in the eNodeB
	 * to contact the controller the eNodeB knows to
	 * send all messages to Entity e, where e could be
	 * another eNodeB or the controller itself
	 * 
	 * @param e the Entity to the controller
	 */
	public void setEntity(Entity e) {
		toController = e;
	}
	
	public boolean hasController(){
		return controller !=null;
	}
	
	public boolean hasBkController() {
		return bkController != null;
	}
	
	/**
	 * Gets the domain of the eNodeB
	 */
	public int getDomain(){
		return domain;
	}
	
	/**
	 * Runs the thread ( thread.start() )
	 */
	@Override
	public void run() {
		System.out.println(getTime() + ": Running thread " + name);
		
		//pauses the system to start at the same time
		while ( time(System.currentTimeMillis() ) < 1.0 ) {	}

		try {
			while (checkTime(System.currentTimeMillis())) {
				Thread.sleep(random());
				
				//eNodeB becomes an orphan
				if (controller == null) {
					orphanNode();
				}
				
				// needs a backup controller
				if ( bkController == null ) {
					//System.out.println(getTime() + ": " + name + " is an orphan");
					getBackup();
				}
				
				// processes messages from orphan to controller
				while (!orphanMessages.isEmpty() && controller != null){
					Message m = orphanMessages.poll();
					ENodeB e = m.getOrphan();
					if (domain == e.getDomain() && toBkController != null){
						m = findConnectionBw(m, true);
						toBkController.messageController(m);
					}else if (domain != e.getDomain()){
						m = findConnectionBw(m, false);
						toController.messageController(m);	
					}
				}
				
				// pass message from controller to orphan
				while ( !replyMessages.isEmpty() ) {
					Message m = replyMessages.poll();
					//if(m.getOrphan().getName().equals("eNodeB5") && name.equals("eNodeB8")) {System.out.println(m.atOrphan());}
					if( m.atOrphan() ){
						ENodeB orphan = m.getOrphan();
						if (!orphan.hasController()){
							orphan.acceptAdoption(domain);
						} else {
							orphan.acceptBackup(m, this);
							//if (m.getOrphan().getName().equals("eNodeB5")) {System.out.println(getTime() + ": " + name + " sends adoption message from " + m.getController().getName() + " to " + orphan.getName());}
						}
					}else{
						ENodeB e = m.removeBreadcrumb();
						e.replyMessage(m);
						//if (m.getOrphan().getName().equals("eNodeB4")) {System.out.println(getTime() + ": " + name + " sends adoption message from " + m.getController().getName() + " to " + e.getName());}
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(getTime() + ": Closing thread " + name);
	}

	/**
	 * call out to backup controller
	 */
	private void orphanNode() {
		Message newController = new Message(this);
		toBkController.messageController(newController);
		//if (name.equals("eNodeB7")) {System.out.println(getTime() + ": " + name + " broadcasts message to " + b.getName());}	
	}
	
	/**
	 * calls out to other connected eNodeBs 
	 * and inform them this eNodeB needs
	 * a backup
	 */
	private void getBackup() {
		for (Connection c : connections) {
			Entity b = c.getEndpoint(this);
			if (!b.equals(controller)) {
				Message orphanBroadcast = new Message(this);
				orphanBroadcast.setBw(c.getBw());
				b.messageController(orphanBroadcast);
				//if (name.equals("eNodeB8")) {System.out.println(getTime() + ": " + name + " broadcasts message to " + b.getName());}
			}
		}
	}

	/**
	 * Sends a message to the controller
	 * 
	 * @param eNodeB
	 */
	public void messageController(Message orphanMessage) {
		orphanMessage.addBreadcrumb(this);
		orphanMessages.add(orphanMessage);
		//if(orphanMessage.getOrphan().getName().equals("eNodeB8") && name.equals("eNodeB10")){ System.out.println(name + " receives message from " + orphanMessage.getOrphan().getName()); }
	}
	
	/**
	 * Adds messages for adoption
	 * @param adoptMessage
	 */
	public void replyMessage(Message adoptMessage) {
		//System.out.println(name + " receives message");
		replyMessages.add(adoptMessage);
		//if(name.equals("eNodeB8") && adoptMessage.getOrphan().getName().equals("eNodeB5")){System.out.println(name + " sends reply to " + adoptMessage.getOrphan().getName());}
	}
	
	/**
	 * Finds the lowest bandwith for the path
	 * @param message the message from the orphan
	 * @param isBackup is a boolean to determine which direction to go to.
	 * @return the messages with the lowest bandwidth for the path
	 */
	private Message findConnectionBw(Message message, boolean isBackup) {
		Entity e = isBackup?toBkController:toController; 
		for (Connection c: connections) {
			if (c.getEndpoint(this).equals(e)){
				//update to lowest bandwidth
				int messageBw = message.getBw();
				int connectionBw = c.getBw();
				if (connectionBw < messageBw) {
					message.setBw(connectionBw);
				}
			}
		}
		return message;
	}
	
	/**
	 * Accepts the message from the Controller
	 * Assigns a backup controller
	 * @param c is the backup controller
	 * @param e is the eNodeB to get to the backup controller
	 */
	private void acceptBackup(Message m, ENodeB e) {
		Controller c = m.getController();
		int messageBw = m.getBw();
		int messageHops = m.getHops();
		
		//if (c.getName().equals("Controller2") && name.equals("eNodeB5")){System.out.println("message BW: " + messageBw + "\nbackup bw: " + backupBw + "\nmessage hops: " + messageHops + "\nBackup hops: " + backupHops);}
		
		if (!c.equals(controller)){
			if (bkController == null) {
				bkController = c;
				backupBw = messageBw;
				backupHops = messageHops;
				System.out.println(getTime() + ": " + c.getName() + " is the backup for " + name + "\tBW: " + backupBw + "\thops: " +  backupHops);
				toBkController = e;
			}else if( messageHops <= backupHops && messageBw > backupBw ){
				bkController = c;
				backupBw = messageBw;
				backupHops = messageHops;
				System.out.println(getTime() + ": " + c.getName() + " UPGRADES for the backup position on " + name + "\tBW: " + backupBw + "\thops: " +  backupHops);
				toBkController = e;
			}
		}
	}
	
	/**
	 * Accepts the adoption.
	 * Switches the backup controller
	 * to the controller
	 */
	private void acceptAdoption(int domainNum){
		controller = bkController;
		bkController = null;
		toController = toBkController;
		toBkController = null;
		domain = domainNum;
		bw = backupBw;
		hops = backupHops;
		backupBw = 0;
		backupHops = 100;
		System.out.println(getTime() + ": " + controller.getName() + " adopts " + name);
	}
}
