/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

package routing;

import java.util.LinkedList;
import java.util.Queue;

import cgr_jni.Libocgr;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;

/**
 * Implements Opportunistic CGR without priorities and overbooking management.
 * In order to simulate OCGR the ContactGraphRouter class was extended.
 * The new OpportunisticContactGraphRouter class provides methods to
 * inform the ION libraries of the acquisition or the loss of a discovered
 * contact. It also provide a mechanism to support a epidemic routing drop back,
 * if no routes can be found for a given bundle.
 * 
 * @author Michele Rodolfi
 *
 */
public class OpportunisticContactGraphRouter extends ContactGraphRouter {

	/**
	 * Setting namespace for the OpportunisictContactGraphRouter: ({@value})
	 */
	public static final String OCGR_NS = "OpportunisticContactGraphRouter";
	/**
	 * Epidemic dropback setting ({@value}): 
	 * if true OCGR tries to send the message epidemically if no routes
	 * can be found by the cgrForward() function.
	 */
	public static final String EPIDEMIC_DROPBACK_S = "epidemicDropBack";
	/**
	 * prevent cgrForward setting ({@value}): 
	 * for test and debug purpose: if true the function cgrForward() will
	 * never be invoked.
	 */
	public static final String PREVENT_CGRFORWARD_S = "preventCGRForward";
	/**
	 * message epidemic flag property ({@value}):
	 * if true this message is being epidemically forwarded.
	 */
	public static final String EPIDEMIC_FLAG_PROP = "epidemicFlag";
	/**
	 * list of pending discovered contact to be added to the contact plan.
	 * Used by the implementation of the discovered contact exchange algorithm.
	 */
	protected Queue<DiscoveryInfo> pendingDiscoveryInfos = new LinkedList<>();
	/**
	 * if true OCGR tries to send the message epidemically if no routes
	 * can be found by the cgrForward() function.
	 */
	protected boolean epidemicDropBack = false;
	/**
	 * for test and debug purpose: if true the function cgrForward() will
	 * never be invoked.
	 */
	protected boolean preventCGRForward = false;
	private int epidemicSearchIndex;
	
	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected OpportunisticContactGraphRouter(ActiveRouter r) {
		super(r);
		epidemicDropBack = ((OpportunisticContactGraphRouter)r)
				.epidemicDropBack;
		preventCGRForward = ((OpportunisticContactGraphRouter)r)
				.preventCGRForward;
	}
	
	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * @param s The settings object
	 */
	public OpportunisticContactGraphRouter(Settings s) {
		super(s);
		Settings ocgrSettings = new Settings(OCGR_NS);
		if (ocgrSettings.contains(EPIDEMIC_DROPBACK_S))
		{
			epidemicDropBack = ocgrSettings.getBoolean(EPIDEMIC_DROPBACK_S);
		}
		if (ocgrSettings.contains(PREVENT_CGRFORWARD_S))
		{
			preventCGRForward = ocgrSettings.getBoolean(PREVENT_CGRFORWARD_S);
		}
		if (ocgrSettings.contains(DEBUG_S))
		{
			debug = ocgrSettings.getBoolean(DEBUG_S);
		}
	}
	
	@Override
	public MessageRouter replicate() {
		return new OpportunisticContactGraphRouter(this);
	}
	
	/**
	 * check if this router has the epidemic dropback enabled
	 * @return true if this router has the epidemic dropback enabled
	 */
	public boolean isEpidemicDropBack() {
		return epidemicDropBack;
	}

	/**
	 * sets this router epidemic dropback behavior.
	 * @param epidemicDropBack true to enable the epidemic dropback, false
	 * to disable it.
	 */
	public void setEpidemicDropBack(boolean epidemicDropBack) {
		this.epidemicDropBack = epidemicDropBack;
	}

	/**
	 * invoked upon the detection of a new connection.
	 * it performs discovered contacts and contact history exchange, triggers
	 * the contact prediction and the new discovered contact insertion to 
	 * both ends of the connection.
	 * @param con the new connection detected
	 */
	protected void discoveredContactStart(Connection con)
	{
		if(!con.getOtherNode(getHost()).equals(this.getHost())) {
			exchangeCurrentDiscoveredContacts(con);
			excangeContactHistory(con);
			predictContacts();
			contactAquired(con);
			contactPlanChanged();
		}
	}

	/**
	 * invoked upon the loss of a connection.
	 * it informs the OCGR that the discovered contact has ended 
	 * @param con the connection lost.
	 */
	protected void discoveredContactEnd(Connection con)
	{
		contactLost(con);
		contactPlanChanged();
	}
		
	/**
	 * This function invoke the predictContacts algorithm on the local node
	 */
	private void predictContacts() {
		if (debug)
			System.out.println("NODE " + getHost().getAddress() 
					+ " CONTACT PREDICTION INITIATED");
		Libocgr.predictContacts(getHost().getAddress());
	}

	/**
	 * This function copies the contact history from node con.fromNode to
	 * node con.toNode AND VICE VERSA. The operation is made only at the 
	 * sender end of the connection.
	 * @param con new discovered Connection
	 */
	private void excangeContactHistory(Connection con) {
		if (con.isInitiator(getHost()))
		{
			if (debug)
				System.out.println("EXCHANGING CONTACT HISTORY between "
						+ getHost().getAddress() + " and " + con.getOtherNode(
								getHost()).getAddress());
			Libocgr.exchangeContactHistory(getHost().getAddress(), 
					con.getOtherNode(getHost()).getAddress());
		}
	}

	/**
	 * This function copies all current discovered contacts from the contact
	 * plan of node con.fromNode to the contact plan of node con.toNode
	 * AND VICE VERSA. The operation is made only at the sender end of the
	 * connection.
	 * @param con new discovered Connection
	 */
	private void exchangeCurrentDiscoveredContacts(Connection con) {
		if (con.isInitiator(getHost()))
		{
			if (debug)
				System.out.println("EXCHANGING CURRENT DISCOVERED CONTACTS between "
						+ getHost().getAddress() + " and " + con.getOtherNode(
								getHost()).getAddress());
			Libocgr.exchangeCurrentDiscoveredContatcs(getHost().getAddress(), 
					con.getOtherNode(getHost()).getAddress());
		}
	}

	/**
	 * This function informs the OCGR of a new discovered contact
	 * @param con the new discovered Connection
	 */
	private void contactAquired(Connection con)
	{
		if(!con.getOtherNode(getHost()).equals(this.getHost())) Libocgr.contactDiscoveryAquired(getHost().getAddress(), 
				con.getOtherNode(getHost()).getAddress(), (int)con.getSpeed());
	}

	/**
	 * This function informs the OCGR that a discovered connection went down
	 * @param con the Connection lost
	 */
	private void contactLost(Connection con) {
		if (debug)
			System.out.println("NODE " + getHost().getAddress() 
					+ " REMOVE DISCOVERED CONTACT");
		Libocgr.contactDiscoveryLost(getHost().getAddress(), 
				con.getOtherNode(getHost()).getAddress());
	}

	@Override
	protected int startTransfer(Message m, Connection con) {
		int result = super.startTransfer(m, con);
		if (result == DENIED_OLD)
		{
			removeMessageFromOutduct(con.getOtherNode(getHost()), m);
		}
		return result;
	}
	@Override
	public void update() {
		int result;
		Message first, cur;
		applyDiscoveryInfos();
		super.update();
		if (!isTransferring() && epidemicDropBack)
		{
			first = getFirstEpidemicMessage();
			if (first == null)
				return;
			for (Connection c : getConnections()){
				if(c.getOtherNode(getHost()).equals(getHost())) continue;
				cur = first;
				epidemicSearchIndex = 1;
				while (cur != null)
				{
					result = startTransfer(cur, c);
					if (result == RCV_OK)
					{
						if (debug)
						{
							System.out.println("" + SimClock.getTime() + 
									": Begin epidemic transmission " 
									+ cur + " " + c);
						}
						cur.updateProperty(EPIDEMIC_FLAG_PROP, false);
						cur = null;
						break;
					}
					cur = getNextEpidemicMessage();
				}
			}
		}
	}
	private Message getFirstEpidemicMessage() {
		for (Message m : limbo.getQueue())
		{
			if ((boolean) m.getProperty(EPIDEMIC_FLAG_PROP))
			{
				return m;
			}
		}
		return null;
	}
	private Message getNextEpidemicMessage()
	{
		int count = 0;
		for (Message m : limbo.getQueue())
		{
			if ((boolean) m.getProperty(EPIDEMIC_FLAG_PROP))
			{
				count++;
				if (count > epidemicSearchIndex)
				{
					epidemicSearchIndex = count;
					return m;
				}
			}
		}
		return null;
	}

	@Override
	public void changedConnection(Connection con) {
		super.changedConnection(con);
		if (con.isUp()) // this is a new connection
		{
			discoveredContactStart(con);
			applyDiscoveryInfos();
		}
		else // this connection went down
		{
			applyDiscoveryInfos();
			discoveredContactEnd(con);
		}
	}
	
	@Override 
	public boolean createNewMessage(Message m) {
		if (debug)
			System.out.println("" + SimClock.getIntTime() + 
					" Node " + getHost().getAddress() + ": create new message");
		m.addProperty(EPIDEMIC_FLAG_PROP, false);
		return super.createNewMessage(m);
	}
	
	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message transferred = super.messageTransferred(id, from);
		transferred.updateProperty(EPIDEMIC_FLAG_PROP, false);
		return transferred;
	}
	
	@Override
	protected String getRouterName() {
		return OCGR_NS;
	}
	
	/**
	 * prepare the insertion of a new discovered contact announced by a
	 * neighbor. This function is invoked by the IONInterface and implements
	 * a discovered contacts exchange protocol.
	 * @param fromNode 
	 * @param toNode
	 * @param fromTime
	 * @param toTime
	 * @param xmitSpeed
	 */
	public void addDiscoveryInfo(long fromNode, long toNode, long fromTime,
			long toTime, int xmitSpeed)
	{
		DiscoveryInfo info = new DiscoveryInfo(fromNode, toNode, fromTime,
				toTime, xmitSpeed);
		pendingDiscoveryInfos.add(info);
	}
	
	/**
	 * inserts in this node's contact plan the discovered contacts announced
	 * by neighboring nodes.
	 */
	protected void applyDiscoveryInfos()
	{
		DiscoveryInfo info;
		while ((info = pendingDiscoveryInfos.poll()) != null)
		{
			Libocgr.applyDiscoveryInfos(getHost().getAddress(),
					info.fromNode, info.toNode,
					info.fromTime, info.toTime, info.xmitSpeed);
		}
	}
	
	/**
	 * Describes a discovered contact to be added to this node's contact
	 * plan. 
	 * @author michele
	 *
	 */
	protected class DiscoveryInfo{
		private long fromNode;
		private long toNode;
		private long fromTime;
		private long toTime;
		private int xmitSpeed;
		
		public DiscoveryInfo(long fromNode, long toNode, 
				long fromTime, long toTime, int xmitSpeed) {
			super();
			this.fromNode = fromNode;
			this.toNode = toNode;
			this.fromTime = fromTime;
			this.toTime = toTime;
			this.xmitSpeed = xmitSpeed;
		}
		
	}
	
	@Override
	public int cgrForward(Message m, DTNHost terminusNode) {
		int result;
		if (!preventCGRForward )
			//&& getConnections().size() != 0)
		{
			result = super.cgrForward(m, terminusNode);
			if (debug)
			{
				if (result == 0)
					System.out.println("Node " + getHost().getAddress() + 
							": no routes found to " + terminusNode.getAddress());
				else if (result > 0)
					System.out.println("Node " + getHost().getAddress() + 
							": route found to " + terminusNode.getAddress() + 
							" through " + result);
				else
					System.out.println("Node " + getHost().getAddress() + 
							": ERROR finding routes to " + terminusNode.getAddress());
			}
		}
		else result = 0;
		if (result == 0 && epidemicDropBack)
		{
			m.updateProperty(EPIDEMIC_FLAG_PROP, true);
		}
		else
			m.updateProperty(EPIDEMIC_FLAG_PROP, false);
		return result;
	}
}
