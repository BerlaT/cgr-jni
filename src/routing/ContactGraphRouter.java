/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

package routing;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;

import cgr_jni.Libcgr;
import cgr_jni.Utils;
import core.Connection;
import core.DTNHost;
import core.DTNSim;
import core.Message;
import core.MessageListener;
import core.Settings;
import core.SimClock;
import util.Tuple;

/**
 * Implements Contact Graph Routing without priorities and overbooking management.
 * In every ContactGraphRouter all the outducts are collected inside an array.
 * Moreover, every router contains one more outduct, the limbo, hosting the
 * messages whose intended route has expired, or no known route to destination,
 * with respective methods for adding e removing messages from it. Note that
 * this policy differs from the current CGR implementation in ION, where bundles
 * with no known route to destination are purged to save memory space. This
 * drastic policy has not been implemented in ONE, as considered likely too
 * drastic for terrestrial environments, although justified in space
 * communications.
 * 
 * @author Jako Jo Messina
 *
 */
public class ContactGraphRouter extends ActiveRouter {

	/**
	 * In ION outducts are the queues towards proximate nodes where bundles are
	 * put after having been forwarded by CGR, waiting to be transmitted; to
	 * support CGR it was necessary to build an equivalent in ONE. To this end,
	 * outducts have been abstracted with a class containing the DTNHost
	 * indicating the local host, and a queue of messages representing the
	 * outduct itself.
	 * 
	 * @author Jako Jo Messina
	 *
	 */
	public class Outduct implements Comparable<Outduct> {
		public static final int LIMBO_ID = -1;
		public static final int NONE_ID = -2;
		private DTNHost host;
		private int address;
		private LinkedList<Message> queue;
		private long totalEnqueuedBytes;
		protected boolean debug = false;

		public Outduct(DTNHost host) {
			this.host = host;
			this.address = host != null ? host.getAddress() : -1;
			this.queue = new LinkedList<Message>();
			this.totalEnqueuedBytes = 0;
		}

		public DTNHost getHost() {
			return host;
		}

		public void setHost(DTNHost host) {
			this.host = host;
		}

		public LinkedList<Message> getQueue() {
			return queue;
		}

		public long getTotalEnqueuedBytes() {
			return totalEnqueuedBytes;
		}

		public void setTotalEnqueuedBytes(long totalEnqueuedBytes) {
			this.totalEnqueuedBytes = totalEnqueuedBytes;
		}

		public boolean containsMessage(Message m) {
			for (Message m1 : queue) {
				if (m.getId().equals(m1.getId())) {
					return true;
				}
			}
			return false;
		}

		public Message getMessage(String id) {
			for (Message m : queue) {
				if (m.getId().equals(id))
					return m;
			}
			return null;
		}

		/**
		 * Enqueues a message in this outduct
		 * 
		 * @param message
		 *            the message to be enqueued
		 * @param removeFromLimbo
		 *            if true it tries to remove this message from limbo
		 */
		protected int insertMessageIntoOutduct(Message message, boolean removeFromLimbo) {
			boolean thisIsLimbo = host == null;
			MessageStatus status = getMessageStatus(message);
			if (thisIsLimbo) {
				// message.updateProperty(OUTDUCT_REF_PROP, LIMBO_ID);
				status.addOutductReference(this);
				if (!queue.contains(message))
					queue.add(message);
				else
					return -1;
				return 0;
			}
			this.queue.add(message);
			if (removeFromLimbo && isMessageIntoLimbo(message)) {
				removeMessageFromLimbo(message);
			}
			// message.updateProperty(OUTDUCT_REF_PROP, host.getAddress());
			status.addOutductReference(this);
			totalEnqueuedBytes += message.getSize();
			return 0;
		}

		protected void removeMessageFromOutduct(Message message) {
			try {
				MessageStatus status = getMessageStatus(message);
				Iterator<Message> iter = queue.iterator();
				Message m1;
				while (iter.hasNext()) {
					m1 = iter.next();
					if (m1.getId().equals(message.getId())) {
						iter.remove();
						// message.updateProperty(OUTDUCT_REF_PROP, NONE_ID);
						status.removeOutductReference(this);
						totalEnqueuedBytes -= message.getSize();
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}

		public int getEnqueuedMessageNum() {
			return queue.size();
		}

		@Override
		public int compareTo(Outduct o) {
			return address - o.address;
		}

		@Override
		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(" to: ");
			if (host != null)
				b.append(host.toString());
			else
				b.append("Limbo");
			b.append(", size: ");
			b.append(queue.size());
			b.append(", msgs: [ ");
			for (Message m : queue) {
				b.append(m.toString() + ",");
			}
			b.insert(b.length() - 1, "]\n");
			return b.toString();
		}
	}

	public class RouteExpirationEvent implements Comparable<RouteExpirationEvent> {
		private long time;
		private Outduct outduct;
		private Message message;

		public RouteExpirationEvent(long time, Outduct outduct, Message message) {
			this.time = time;
			this.outduct = outduct;
			this.message = message;
		}

		@Override
		public int compareTo(RouteExpirationEvent o) {
			int result = (int) (time - o.time);
			if (result == 0)
				result = outduct.address - o.outduct.address;
			if (result == 0)
				result = message.getUniqueId() - o.message.getUniqueId();
			return result;

		}

		public int execute() {
			/*
			 * If a route has expired for a message, I put it into the limbo and
			 * invoke CGR, which possibly remove the message from limbo and
			 * enqueue it into an outduct if a route has been found.
			 */
			destroy();
			outduct.removeMessageFromOutduct(message);
			if (isMessageIntoLimbo(message)) {
				@SuppressWarnings("unused")
				Message temp = limbo.getMessage(message.getId());
			}
			putMessageIntoLimbo(message);
			return cgrForward(message, message.getTo());

		}

		public void destroy() {
			events.remove(this);
		}

		public String toString() {
			return "Time: " + time + ", Outduct: " + outduct.address + " Message: " + message;
		}
	}

	public class MessageStatus {
		@SuppressWarnings("unused")
		private Message message;
		private HashSet<Integer> xmitCopies = new HashSet<>();
		private double dlvConfidence;
		private boolean isInLimbo;
		@SuppressWarnings("unused")
		private boolean epidemicFlag;
		private long routeTimelimit; // temp
		private HashMap<Outduct, RouteExpirationEvent> outductsReference = new HashMap<>();

		/**
		 * Constructor for creating a new MessageStatus object. To use only on
		 * new message creation or reception.
		 * 
		 * @param m
		 *            message back reference.
		 */
		protected MessageStatus(Message m) {
			this.message = m;
			this.dlvConfidence = 0;
			this.epidemicFlag = false;
			this.isInLimbo = false;
			this.routeTimelimit = 0;
		}

		public void updateRouteTimelimit(long timelimit) {
			this.routeTimelimit = timelimit;
		}

		public long getRouteTimelimit() {
			return this.routeTimelimit;
		}

		public HashSet<Integer> getXmitCopies() {
			return xmitCopies;
		}

		public double getDlvConfidence() {
			return dlvConfidence;
		}

		public void setDlvConfidence(double conf) {
			this.dlvConfidence = conf;
		}

		public void addOutductReference(Outduct outduct) {
			if (outduct.host == null) {
				// outduct is limbo
				this.isInLimbo = true;
			} else {
				this.outductsReference.put(outduct, null);
			}

		}

		public void removeOutductReference(Outduct outduct) {
			if (outduct.host == null) {
				// outduct is limbo
				this.isInLimbo = false;
			} else {
				RouteExpirationEvent exp = outductsReference.get(outduct);
				if (exp != null)
					exp.destroy();
				this.outductsReference.remove(outduct);
			}
		}

		public void addRouteExpirationEvent(RouteExpirationEvent exp) {
			outductsReference.put(exp.outduct, exp);
		}

		public Set<Outduct> getOutducts() {
			return this.outductsReference.keySet();
		}

		/**
		 * returns the number of outduct this message is currently enqueued in
		 */
		public int getEnqueuedNum() {
			return this.outductsReference.size();
		}

		public String toString() {
			StringBuilder b = new StringBuilder();
			b.append(" isInLimbo=" + isInLimbo);
			b.append(" xmitCopies=" + xmitCopies.toString());
			b.append(" outducts=" + outductsReference.keySet().toString());
			return b.toString();
		}

	}

	public static MessageStatus getMessageStatus(Message message) {
		MessageStatus result;
		result = (MessageStatus) message.getProperty(ContactGraphRouter.MESSAGE_STATUS_PROP);
		return result;
	}

	public static final String CGR_NS = "ContactGraphRouter";
	public static final String CONTACT_PLAN_PATH_S = "ContactPlanPath";
	public static final String DEBUG_S = "debug";
	// public static final String ROUTE_FORWARD_TIMELIMIT_PROP =
	// "ForwardTimelimit";
	// public static final String OUTDUCT_REF_PROP = "OutductReference";
	// public static final String XMIT_COPIES_PROP = "XmitCopies";
	// public static final String XMIT_COPIES_COUNT_PROP = "XmitCopiesCount";
	// public static final String DLV_CONFIDENCE_PROP = "DlvConfidence";
	// public static final String ROUTE_EXP_EV_PROP = "RouteExpirationEvent";
	public static final String MESSAGE_STATUS_PROP = "MessageStatus";

	/**
	 * static reference to initialized nodes that need to be reset after every
	 * batch run of the simulator.
	 */
	private static ArrayList<ContactGraphRouter> initializedNodes = new ArrayList<>();
	static {
		DTNSim.registerForReset(ContactGraphRouter.class.getCanonicalName());
	}

	public static void reset() {
		System.out.println("RESET");
		for (ContactGraphRouter r : initializedNodes) {
			r.finalizeCGR();
		}
		initializedNodes.clear();
		Libcgr.finalizeGlobalMem();
	}

	/**
	 * counter incremented every time a message is delivered to the local node,
	 * i.e. the message has reached its final destination.
	 */
	protected int deliveredCount = 0;
	private boolean contactPlanChanged = false;
	protected boolean debug = false;
	/** Used as reference for round-robin outducts sorting */
	// private DTNHost firstOutductIndex;
	private int firstOutductIndex;
	protected String contactPlanPath;

	/**
	 * In every ContactGraphRouter all the outducts are collected inside an
	 * array.
	 */
	protected Outduct[] outducts = new Outduct[0];
	protected Outduct limbo = new Outduct(null);
	protected SortedSet<RouteExpirationEvent> events = new TreeSet<>();
	// protected int hostAddress = -2;

	/**
	 * Copy constructor.
	 * 
	 * @param r
	 *            The router prototype where setting values are copied from
	 */
	protected ContactGraphRouter(ActiveRouter r) {
		super(r);
		contactPlanPath = ((ContactGraphRouter) r).contactPlanPath;
		debug = ((ContactGraphRouter) r).debug;
		// hostAddress = getHost().getAddress();
	}

	/**
	 * Constructor. Creates a new message router based on the settings in the
	 * given Settings object.
	 * 
	 * @param s
	 *            The settings object
	 */
	public ContactGraphRouter(Settings s) {
		super(s);
		Settings cgrSettings = new Settings(CGR_NS);
		contactPlanPath = cgrSettings.getSetting(CONTACT_PLAN_PATH_S, "");
		if (cgrSettings.contains(DEBUG_S)) {
			debug = cgrSettings.getBoolean(DEBUG_S);
		}
		firstOutductIndex = -2;
		// hostAddress = getHost().getAddress();
	}

	/**
	 * Initalizes the router. it also initializes the CGR library and read the
	 * contact plan from the file that has been provided with the
	 * {@link ContactGraphRouter#CONTACT_PLAN_PATH_S} property. {@inheritDoc}
	 */
	@Override
	public void init(DTNHost host, List<MessageListener> mListeners) {
		super.init(host, mListeners);
		initCGR();
		if (contactPlanPath.equals(""))
			return;
		else
			readContactPlan(contactPlanPath);
	}

	/**
	 * Gets all the outducts currently used by this node
	 * 
	 * @return the outducts
	 * 
	 *         public TreeMap<DTNHost, Outduct> getOutducts() {
	 *         updateOutducts(Utils.getAllNodes()); return this.outducts; }
	 */
	public Outduct[] getOutducts() {
		updateOutducts(Utils.getAllNodes());
		return this.outducts;
	}

	/**
	 * Gets the number of Messages enqueued into a specific outduct
	 * 
	 * @param h
	 *            the host that the outduct is directed to
	 * @return the total number of message enqueued into the outduct
	 */
	public int getOutductSize(DTNHost h) {
		// Outduct o = outducts.get(h);
		Outduct o = getOutducts()[h.getAddress()];
		if (o == null)
			return -1;
		return o.getEnqueuedMessageNum();
	}

	public boolean isMessageIntoOutduct(DTNHost h, Message m) {
		// Outduct o = getOutducts().get(h);
		Outduct o = getOutducts()[h.getAddress()];
		if (o != null) {
			return o.containsMessage(m);
		}
		return false;
	}

	public int putMessageIntoOutduct(DTNHost neighbor, Message message, boolean removeFromLimbo) {
		MessageStatus status = getMessageStatus(message);
		int toAdd = neighbor.getAddress();
		if (outducts[toAdd] != null) {
			outducts[toAdd].insertMessageIntoOutduct(message, removeFromLimbo);
			RouteExpirationEvent exp = new RouteExpirationEvent(
					// (long) message.getProperty(ROUTE_FORWARD_TIMELIMIT_PROP),
					status.getRouteTimelimit(), outducts[toAdd], message);
			// message.updateProperty(ROUTE_EXP_EV_PROP, exp);
			status.addRouteExpirationEvent(exp);
			events.add(exp);
			return 0;
		}
		return -1;
	}

	public void removeMessageFromOutduct(DTNHost h, Message m) {
		// Outduct o = getOutducts().get(h);
		Outduct o = getOutducts()[h.getAddress()];
		if (o != null && o.containsMessage(m)) {
			o.removeMessageFromOutduct(m);
			// events.remove(m.getProperty(ROUTE_EXP_EV_PROP));
			// m.updateProperty(ROUTE_EXP_EV_PROP, null);
		}
	}

	/**
	 * Gets the number of Messages currently into the limbo. If a message is
	 * into the limbo it means that the CGR hasn't found a feasible route for
	 * it.
	 * 
	 * @return the total number of message into the limbo
	 */
	public int getLimboSize() {
		return limbo.getEnqueuedMessageNum();
	}

	/**
	 * Gets the total number of Messages that has been delivered to this node. A
	 * message is delivered when it reaches his final destination
	 * 
	 * @return the number of delivered messages
	 */
	public int getDeliveredCount() {
		return deliveredCount;
	}

	/**
	 * When status is changed the router needs to recalculate routes for
	 * messages into limbo. Status changes when
	 */
	protected void contactPlanChanged() {
		contactPlanChanged = true;
		if (debug)
			processLine("l contact");
	}

	protected boolean isContactPlanChanged() {
		return contactPlanChanged;
	}

	/**
	 * Puts a message into the limbo. The outduct reference property
	 * {@link ContactGraphRouter#OUTDUCT_REF_PROP} is updated.
	 * 
	 * @param message
	 *            to put into the limbo
	 */
	public void putMessageIntoLimbo(Message message) {
		// int outductNum = (int) message.getProperty(OUTDUCT_REF_PROP);
		// if (removeFromOutduct && outductNum >= 0)
		// getOutducts().get(Utils.getHostFromNumber(outductNum)).
		// removeMessageFromOutduct(
		// Utils.getHostFromNumber(outductNum), message);
		limbo.insertMessageIntoOutduct(message, false);
		// message.updateProperty(OUTDUCT_REF_PROP, Outduct.LIMBO_ID);
	}

	/**
	 * Remove a message from the limbo. The message won't be in any outduct and
	 * the outduct reference property
	 * {@link ContactGraphRouter#OUTDUCT_REF_PROP} is set to
	 * {@link Outduct#NONE_ID}.
	 * 
	 * @param message
	 *            the message to remove from limbo.
	 */
	public void removeMessageFromLimbo(Message message) {
		limbo.removeMessageFromOutduct(message);
		// message.updateProperty(OUTDUCT_REF_PROP, Outduct.NONE_ID);
	}

	/**
	 * Checks if a message is into the limbo.
	 * 
	 * @param message
	 *            the message to check.
	 * @return true if the message is currently into the limbo. False otherwise.
	 */
	public boolean isMessageIntoLimbo(Message message) {
		MessageStatus status = getMessageStatus(message);
		return status.isInLimbo;
		// return limbo.containsMessage(message);
	}

	/**
	 * Tries to find a feasible route for every message currently into the limbo
	 * using {@link ContactGraphRouter#cgrForward(Message, DTNHost)}. If a route
	 * is found for a message, it is automatically removed from the limbo and
	 * moved to the selected outduct.
	 */
	protected void tryRouteForMessageIntoLimbo() {
		/*
		 * I can't operate directly on the queue itself or a
		 * ConcurrentModificationException would be thrown.
		 * insertBundleIntoOutduct() will remove the Message from the limbo.
		 */
		Object[] temp = (Object[]) limbo.getQueue().toArray();
		for (int i = 0; i < temp.length; i++) {
			Message m = (Message) temp[i];
			cgrForward(m, m.getTo());
		}
	}

	/**
	 * Looks through all the outducts for messages whose route is expired and
	 * needs to be recalculated. These messages are moved into the limbo.
	 */
	protected void checkExpiredRoutes() {
		/*
		 * List<Message> expired = new ArrayList<>(getNrofMessages()); //for
		 * (Outduct o : getOutducts().values()) for (Outduct o : getOutducts())
		 * { for (Message m : o.getQueue()) { long fwdTimelimit = (long)
		 * m.getProperty(ROUTE_FORWARD_TIMELIMIT_PROP); if (fwdTimelimit == 0)
		 * // This Message hasn't been routed yet return; if
		 * (SimClock.getIntTime() > fwdTimelimit) { expired.add(m); } } for
		 * (Message m : expired) { /* If a route has expired for a message, I
		 * put it into the limbo and invoke CGR, which possibly remove the
		 * message from limbo and enqueue it into an outduct if a route has been
		 * found.
		 */
		/*
		 * o.removeMessageFromOutduct(m); putMessageIntoLimbo(m, false);
		 * cgrForward(m, m.getTo()); } expired.clear(); }
		 */
		RouteExpirationEvent exp;
		List<RouteExpirationEvent> toExecute = new ArrayList<>();
		Iterator<RouteExpirationEvent> iter = events.iterator();
		while (iter.hasNext()) {
			exp = iter.next();
			if (exp.time > SimClock.getIntTime())
				break;
			toExecute.add(exp);
		}
		for (RouteExpirationEvent e : toExecute) {
			e.execute();
		}
	}

	@Override
	public void update() {
		checkExpiredRoutes();
		if (isContactPlanChanged()) {
			tryRouteForMessageIntoLimbo();
			contactPlanChanged = false;
		}
		super.update();
		if (!canStartTransfer()) {
			return; // allows concurrent transmission
		}
		List<Tuple<Message, Connection>> outboundMessages = getMessagesForConnected();
		Tuple<Message, Connection> sent = tryMessagesForConnected(outboundMessages);
		if (sent != null) {
			// transmission started
			if (debug) {
				System.out.println(
						"" + SimClock.getTime() + ": Begin transmission " + sent.getKey() + " " + sent.getValue());
			}
			// I look for next messafe starting form next outduct
			// firstOutductIndex =
			// getOutducts().higherKey(sent.getValue().getOtherNode(getHost()));
			firstOutductIndex = sent.getValue().getOtherNode(getHost()).getAddress();
		}
		// if (firstOutductIndex == null)
		// firstOutductIndex = getOutducts().firstKey();
		if (firstOutductIndex < 0)
			firstOutductIndex = 1;
	}

	/**
	 * Checks if router "wants" to start receiving message (i.e. router doesn't
	 * have the message and has room for it). Allows concurrent transmission.
	 */
	@Override
	protected int checkReceiving(Message m, DTNHost from) {
		int result = super.checkReceiving(m, from);
		if (result == TRY_LATER_BUSY)
			result = RCV_OK;
		return result;
	}

	@Override
	protected void addToMessages(Message m, boolean newMessage) {
		if (newMessage) {
			try {
				if (m.getTo().compareTo(getHost()) == 0) {
					// message should not be forwarded
					if (!isDeliveredMessage(m))
						deliveredCount++;
					return;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				return;
			}
		}
		if (contactPlanChanged) {
			/*
			 * contact plan has changed but messages in limbo have not been
			 * reforwarded yet. I need to try routes for messages in limbo
			 * before invoke cgrForward for the new bundle. By doing this I can
			 * avoid a double CGR computation if the new bundle remains in limbo
			 */
			tryRouteForMessageIntoLimbo();
			contactPlanChanged = false;
		}
		putMessageIntoLimbo(m);
		super.addToMessages(m, newMessage);
		cgrForward(m, m.getTo());
	}

	@Override
	public boolean createNewMessage(Message m) {
		// m.addProperty(ROUTE_FORWARD_TIMELIMIT_PROP, (long)0);
		// m.addProperty(OUTDUCT_REF_PROP, Outduct.NONE_ID);
		// m.addProperty(XMIT_COPIES_PROP, new HashSet<Integer>());
		// m.addProperty(XMIT_COPIES_COUNT_PROP, 0);
		// m.addProperty(DLV_CONFIDENCE_PROP, 0.0);
		// m.addProperty(ROUTE_EXP_EV_PROP, null);
		m.addProperty(MESSAGE_STATUS_PROP, new MessageStatus(m));
		return super.createNewMessage(m);
	}

	protected Message removeFromOutducts(Message m) {
		MessageStatus status = getMessageStatus(m);
		Outduct[] outducts = new Outduct[status.getEnqueuedNum()];
		outducts = status.getOutducts().toArray(outducts);
		for (Outduct o : outducts) {
			o.removeMessageFromOutduct(m);
		}
		if (status.isInLimbo) {
			limbo.removeMessageFromOutduct(m);
			status.isInLimbo = false;
		}
		return m;
	}

	protected void removeFromOutducts(String id) {
		// for(Outduct o : getOutducts().values())
		Message m = getMessage(id);
		removeFromOutducts(m);
	}

	@Override
	protected Message removeFromMessages(String id) {
		removeFromOutducts(id);
		return super.removeFromMessages(id);
	}

	@Override
	protected void transferDone(Connection con) {
		super.transferDone(con);
		Message transferred = con.getMessage();
		// removeFromOutducts(transferred);
		removeMessageFromOutduct(con.getOtherNode(getHost()), transferred);
		if (debug)
			System.out.println("" + SimClock.getTime() + ": End transmission " + transferred + " " + con);
	}

	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message transferred = super.messageTransferred(id, from);
		// transferred.updateProperty(XMIT_COPIES_PROP, new HashSet<Integer>());
		// transferred.updateProperty(XMIT_COPIES_COUNT_PROP, 0);
		// transferred.updateProperty(DLV_CONFIDENCE_PROP, 0.0);
		transferred.updateProperty(MESSAGE_STATUS_PROP, new MessageStatus(transferred));
		if (transferred.getTo().equals(getHost())) {
			deliveredCount++;
		}
		return transferred;
	}

	@Override
	public MessageRouter replicate() {
		return new ContactGraphRouter(this);
	}

	public void updateOutducts(Collection<DTNHost> hosts) {
		/*
		 * if (getOutducts().size() != hosts.size()) { for (DTNHost h : hosts) {
		 * if (! getOutducts().keySet().contains(h)) getOutducts().put(h, new
		 * Outduct(h)); }
		 * 
		 * }
		 */
		if (outducts.length != Utils.getAllNodes().size() + 1) {
			outducts = new Outduct[Utils.getAllNodes().size() + 1];
			outducts[0] = limbo;
			for (DTNHost h : Utils.getAllNodes()) {
				outducts[h.getAddress()] = new Outduct(h);
			}
		}
	}

	protected Connection getConnectionTo(DTNHost h) {
		for (Connection c : getConnections()) {
			if (h == c.getOtherNode(this.getHost())) {
				return c;
			}
		}
		return null;
	}

	protected Connection[] getSortedConnectionsArray() {
		List<Connection> cons;
		int size = getConnections().size();
		if (size == 0)
			return null;
		else {
			cons = getConnections();
			cons.sort(new Comparator<Connection>() {
				@Override
				public int compare(Connection o1, Connection o2) {
					int a1 = o1.getOtherNode(getHost()).getAddress();
					int a2 = o2.getOtherNode(getHost()).getAddress();
					if (a1 >= firstOutductIndex) {
						if (a2 > a1 || a2 < firstOutductIndex)
							return -1;
						else
							return 1;
					} else {
						if (a2 >= firstOutductIndex || a2 < a1)
							return 1;
						else
							return -1;
					}
				}
			});
			return cons.toArray(new Connection[size]);
		}
	}

	@Override
	protected List<Tuple<Message, Connection>> getMessagesForConnected() {
		if (getNrofMessages() == 0 || getConnections().size() == 0) {
			/* no messages -> empty list */
			return new ArrayList<Tuple<Message, Connection>>(0);
		}

		List<Tuple<Message, Connection>> forTuples = new ArrayList<Tuple<Message, Connection>>();
		/*
		 * if (firstOutductIndex == null) firstOutductIndex =
		 * outducts.firstKey(); Outduct o = outducts.get(firstOutductIndex);
		 * Connection c; for (int j = 0; j < outducts.size(); j++) { if ((c =
		 * getConnectionTo(o.getHost())) != null && o.getEnqueuedMessageNum() >
		 * 0) { forTuples.add(new Tuple<Message,
		 * Connection>(o.getQueue().getFirst(), c)); } DTNHost next =
		 * outducts.higherKey(o.getHost()); if (next == null) next =
		 * outducts.firstKey(); o = outducts.get(next); }
		 */
		Connection[] connections = getSortedConnectionsArray();
		Outduct o;
		for (Connection c : connections) {
			o = getOutducts()[c.getOtherNode(getHost()).getAddress()];
			if (o.getQueue().size() != 0) {
				forTuples.add(new Tuple<Message, Connection>(o.getQueue().getFirst(), c));
			}
		}
		return forTuples;
	}

	protected Tuple<Message, Connection> tryAllMessages() {
		List<Connection> connections = super.getConnections();
		for (Connection c : connections) {
			// Outduct o = getOutducts().get(c.getOtherNode(getHost()));
			Outduct o = getOutducts()[c.getOtherNode(getHost()).getAddress()];
			for (Message m : o.getQueue()) {
				if (super.startTransfer(m, c) == RCV_OK) {
					System.out.println("Begin transmission " + m + " " + c);
					return new Tuple<Message, Connection>(m, c);
				} else
					break;
			}
		}
		return null;
	}

	@Override
	public boolean isDeliveredMessage(Message m) {
		return super.isDeliveredMessage(m);
	}

	protected String getRouterName() {
		return CGR_NS;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append(getRouterName());
		b.append(" node " + getHost().getAddress());
		b.append('\n');
		// b.append(limbo.toString());
		// b.append(getOutducts().toString());
		// for (int i = 1; i <= getOutducts().length; i++)
		// b.append(outducts[i].toString() + "\n");
		b.append(Arrays.toString(outducts));
		return b.toString();
	}

	protected void initCGR() {
		int localAddress = getHost().getAddress();
		if (localAddress == 0) {
			System.out.println("ERROR: ContactGraphRouter cannot be inizialized if " + "local node number is 0");
			System.exit(1);
		}
		Libcgr.initializeNode(localAddress);
		initializedNodes.add(this);
	}

	/**
	 * Finalizes the router. User needs to invoke this method at the end of the
	 * simulation to deallocate memory used by CGR lib
	 */
	public void finalizeCGR() {
		if (debug)
			System.out.println("Finalizing node " + getHost().getAddress());
		Libcgr.finalizeNode(getHost().getAddress());
	}

	public void readContactPlan(String filePath) {
		Libcgr.readContactPlan(this.getHost().getAddress(), filePath);
		contactPlanChanged();
	}

	public void processLine(String line) {
		System.out.println("Node " + getHost().getAddress() + ": process line '" + line + "'");
		Libcgr.processLine(this.getHost().getAddress(), line);
		if (!line.startsWith("l "))
			contactPlanChanged();
	}

	public int cgrForward(Message m, DTNHost terminusNode) {
		return Libcgr.cgrForward(this.getHost().getAddress(), m, terminusNode.getAddress());
	}

}



