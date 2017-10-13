/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

package routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import cgr_jni.Utils;
import core.Connection;
import core.DTNHost;
import core.Message;
import core.PriorityMessage;
import core.Settings;
import core.SimClock;
import routing.PriorityContactGraphRouter.OverbookingStructure;
import util.Tuple;

/**
 * Similarly to PriorityContactGraphRouter, this class extends the
 * OpportunisticContactGraphRouter class to include the support of messages with
 * the priority attribute and the Overbooking Management support, both lacking
 * in the existing OpportunisticContactGraphRouter.
 * 
 * @author Simone Pozza
 *
 */
public class PriorityOpportunisticContactGraphRouter extends OpportunisticContactGraphRouter {

	/**
	 * Since Java does not support multiple inheritance, the new class contains
	 * the inner class PriorityOutduct, like PriorityContactGraphRouter does,
	 * which extends ContactGraphRouter.Outduct. The class
	 * PriorityContactGraphRouter.OverbookingStructure is static so the code was
	 * not replicated but the class was simply imported.
	 * 
	 * @author Simone Pozza
	 *
	 */
	public class PriorityOutduct extends ContactGraphRouter.Outduct {

		private LinkedList<Message> bulkQueue;
		private LinkedList<Message> expeditedQueue;
		private long bulkBacklog;
		private long normalBacklog;
		private long expeditedBacklog;

		public PriorityOutduct(DTNHost host) {
			super(host);
			bulkBacklog = 0;
			normalBacklog = 0;
			expeditedBacklog = 0;
			bulkQueue = new LinkedList<Message>();
			expeditedQueue = new LinkedList<Message>();

		}

		public LinkedList<Message> getBulkQueue() {
			return bulkQueue;
		}

		public LinkedList<Message> getExpeditedQueue() {
			return expeditedQueue;
		}

		public LinkedList<Message> getNormalQueue() {
			return super.getQueue();
		}

		public long getBulkBacklog() {
			return bulkBacklog;
		}

		public long getNormalBacklog() {
			return normalBacklog;
		}

		public long getExpeditedBacklog() {
			return expeditedBacklog;
		}

		@Override
		public int getEnqueuedMessageNum() {
			return getQueue().size() + bulkQueue.size() + expeditedQueue.size();
		}

		@Override
		public boolean containsMessage(Message m) {
			switch (((PriorityMessage) m).getPriority()) {
			case 0:
				for (Message mex : this.bulkQueue)
					if (mex.getId().equals(m.getId()))
						return true;

			case 1:
				for (Message mex : this.getNormalQueue())
					if (mex.getId().equals(m.getId()))
						return true;
			case 2:
				for (Message mex : this.expeditedQueue)
					if (mex.getId().equals(m.getId()))
						return true;
			}

			return false;
		}

		@Override
		public int insertMessageIntoOutduct(Message message, boolean removeFromLimbo) {

			if (((PriorityMessage) message).isReforwarded()
					&& ((PriorityMessage) message).getReforwardedFrom().equals(this.getHost())) {
				int index = ((PriorityMessage) message).getReforwardIndex();
				switch (((PriorityMessage) message).getPriority()) {
				case 0:
					this.bulkQueue.add(index, message);
					this.bulkBacklog += message.getSize();
					break;
				case 1:
					this.getNormalQueue().add(index, message);
					this.normalBacklog += message.getSize();
					break;
				case 2:
					this.expeditedQueue.add(index, message);
					this.expeditedBacklog += message.getSize();
					break;
				}

				((PriorityMessage) message).setReforwarded(false);
				MessageStatus status = getMessageStatus(message);
				status.addOutductReference(this);
				setTotalEnqueuedBytes(getTotalEnqueuedBytes() + message.getSize());
				return 0;
			}

			switch (((PriorityMessage) message).getPriority()) {
			case 0:
				this.bulkQueue.add(message);
				this.bulkBacklog += message.getSize();
				break;
			case 1:
				this.getNormalQueue().add(message);
				this.normalBacklog += message.getSize();
				break;
			case 2:
				this.expeditedQueue.add(message);
				this.expeditedBacklog += message.getSize();
				break;
			}

			boolean thisIsLimbo = (getHost() == null);
			MessageStatus status = getMessageStatus(message);
			if (thisIsLimbo) {
				status.addOutductReference(this);
				return 0;
			}
			if (removeFromLimbo && isMessageIntoLimbo(message)) {
				removeMessageFromLimbo(message);
			}
			status.addOutductReference(this);
			setTotalEnqueuedBytes(getTotalEnqueuedBytes() + message.getSize());
			return 0;
		}

		@Override
		public void removeMessageFromOutduct(Message m) {
			switch (((PriorityMessage) m).getPriority()) {
			case 0: {
				MessageStatus status = getMessageStatus(m);
				Iterator<Message> iter = getBulkQueue().iterator();
				Message m1;
				while (iter.hasNext()) {
					m1 = iter.next();
					if (m1.getId().equals(m.getId())) {
						iter.remove();
						status.removeOutductReference(this);
						bulkBacklog -= m.getSize();
						setTotalEnqueuedBytes(getTotalEnqueuedBytes() - m.getSize());
						return;
					}
				}
			}

			case 1: {
				MessageStatus status = getMessageStatus(m);
				Iterator<Message> iter = getNormalQueue().iterator();
				Message m1;
				while (iter.hasNext()) {
					m1 = iter.next();
					if (m1.equals(m)) {
						iter.remove();
						status.removeOutductReference(this);
						normalBacklog -= m.getSize();
						setTotalEnqueuedBytes(getTotalEnqueuedBytes() - m.getSize());
						return;
					}
				}
			}

			case 2: {
				MessageStatus status = getMessageStatus(m);
				Iterator<Message> iter = getExpeditedQueue().iterator();
				Message m1;
				while (iter.hasNext()) {
					m1 = iter.next();
					if (m1.equals(m)) {
						iter.remove();
						status.removeOutductReference(this);
						expeditedBacklog -= m.getSize();
						setTotalEnqueuedBytes(getTotalEnqueuedBytes() - m.getSize());
						return;
					}
				}
			}
			}
		}

	}

	public PriorityOpportunisticContactGraphRouter(ActiveRouter r) {
		super(r);
	}

	public PriorityOpportunisticContactGraphRouter(Settings s) {
		super(s);

	}

	@Override
	public void updateOutducts(Collection<DTNHost> hosts) {
		if (outducts.length != Utils.getAllNodes().size() + 1) {
			outducts = new Outduct[Utils.getAllNodes().size() + 1];
			outducts[0] = limbo;
			for (DTNHost h : Utils.getAllNodes()) {
				outducts[h.getAddress()] = new PriorityOutduct(h);
			}
		}
	}

	@Override
	public MessageRouter replicate() {
		return new PriorityOpportunisticContactGraphRouter(this);
	}

	@Override
	protected List<Tuple<Message, Connection>> getMessagesForConnected() {
		if (getNrofMessages() == 0 || getConnections().size() == 0) {
			return new ArrayList<Tuple<Message, Connection>>(0);
		}

		List<Tuple<Message, Connection>> forTuples = new ArrayList<Tuple<Message, Connection>>();
		Connection[] connections = getSortedConnectionsArray();
		Outduct o;
		for (Connection c : connections) {
			o = getOutducts()[c.getOtherNode(getHost()).getAddress()];
			if ((getConnectionTo(o.getHost())) != null && o.getEnqueuedMessageNum() > 0) {
				if (((PriorityOutduct) o).getExpeditedQueue().size() != 0)
					forTuples.add(
							new Tuple<Message, Connection>(((PriorityOutduct) o).getExpeditedQueue().getFirst(), c));
				else if (((PriorityOutduct) o).getNormalQueue().size() != 0)
					forTuples.add(new Tuple<Message, Connection>(((PriorityOutduct) o).getNormalQueue().getFirst(), c));
				else if (((PriorityOutduct) o).getBulkQueue().size() != 0)
					forTuples.add(new Tuple<Message, Connection>(((PriorityOutduct) o).getBulkQueue().getFirst(), c));
			}
		}
		return forTuples;

	}

	@Override
	protected void checkExpiredRoutes() {
		List<Message> expired = new ArrayList<>(getNrofMessages());
		for (Outduct o : getOutducts()) {
			if (o instanceof PriorityOutduct) {
				for (Message m : ((PriorityOutduct) o).getExpeditedQueue()) {
					MessageStatus status = getMessageStatus(m);
					long fwdTimelimit = status.getRouteTimelimit();
					if (fwdTimelimit == 0) // This Message hasn't been routed
											// yet
						return;
					if (SimClock.getIntTime() > fwdTimelimit) {
						expired.add(m);
					}
				}
				for (Message m : ((PriorityOutduct) o).getNormalQueue()) {
					MessageStatus status = getMessageStatus(m);
					long fwdTimelimit = status.getRouteTimelimit();
					if (fwdTimelimit == 0) // This Message hasn't been routed
											// yet
						return;
					if (SimClock.getIntTime() > fwdTimelimit) {
						expired.add(m);
					}
				}
				for (Message m : ((PriorityOutduct) o).getBulkQueue()) {
					MessageStatus status = getMessageStatus(m);
					long fwdTimelimit = status.getRouteTimelimit();
					if (fwdTimelimit == 0) // This Message hasn't been routed
											// yet
						return;
					if (SimClock.getIntTime() > fwdTimelimit) {
						expired.add(m);
					}
				}
			}

			for (Message m : expired) {
				o.removeMessageFromOutduct(m);
				putMessageIntoLimbo(m);
				cgrForward(m, m.getTo());
			}
			expired.clear();
		}
	}

	protected LinkedList<OverbookingStructure> listOverbooked = new LinkedList<OverbookingStructure>();

	public void manageOverbooking() {
		if (listOverbooked.isEmpty())
			return;

		OverbookingStructure current = listOverbooked.getLast();

		// don't reforward protected bundle
		while (current.getProtect() > 0.0) {
			if (current.getWhichQueue() == 0) {
				current.subtractFromProtected(((PriorityOutduct) getOutducts()[current.getToHost().getAddress()])
						.getBulkQueue().get(current.getQueueIndex()).getSize());
				current.setQueueIndex(current.getQueueIndex() - 1);
				if (current.getQueueIndex() < 0) {
					if (((PriorityOutduct) getOutducts()[current.getToHost().getAddress()]).getNormalBacklog() > 0) {
						current.setWhichQueue(1);
						current.setQueueIndex(((PriorityOutduct) getOutducts()[current.getToHost().getAddress()])
								.getNormalQueue().size() - 1);
					} else {
						current.setWhichQueue(2);
						current.setQueueIndex(((PriorityOutduct) getOutducts()[current.getToHost().getAddress()])
								.getExpeditedQueue().size() - 1);
					}
				}

				continue;

			} else if (current.getWhichQueue() == 1) {
				current.subtractFromProtected(((PriorityOutduct) getOutducts()[current.getToHost().getAddress()])
						.getNormalQueue().get(current.getQueueIndex()).getSize());
				current.setQueueIndex(current.getQueueIndex() - 1);
				if (current.getQueueIndex() < 0) {
					current.setWhichQueue(2);
					current.setQueueIndex(((PriorityOutduct) getOutducts()[current.getToHost().getAddress()])
							.getExpeditedQueue().size() - 1);
				}

				continue;
			} else {
				current.subtractFromProtected(((PriorityOutduct) getOutducts()[current.getToHost().getAddress()])
						.getExpeditedQueue().get(current.getQueueIndex()).getSize());
				current.setQueueIndex(current.getQueueIndex() - 1);
			}
		}

		Message m = null;

		if (current.getWhichQueue() == 0) {
			m = ((PriorityOutduct) getOutducts()[current.getToHost().getAddress()]).getBulkQueue()
					.get(current.getQueueIndex());
			current.subtractFromOverbooked(m.getSize());
			current.setQueueIndex(current.getQueueIndex() - 1);

			if (current.getQueueIndex() < 0 && current.isOverbooked()) {
				if (((PriorityOutduct) getOutducts()[current.getToHost().getAddress()]).getNormalBacklog() > 0) {
					current.setWhichQueue(1);
					current.setQueueIndex(
							((PriorityOutduct) getOutducts()[current.getToHost().getAddress()]).getNormalQueue().size()
									- 1);
				} else {
					current.setWhichQueue(2);
					current.setQueueIndex(((PriorityOutduct) getOutducts()[current.getToHost().getAddress()])
							.getExpeditedQueue().size() - 1);
				}
			}
		} else if (current.getWhichQueue() == 1) {
			m = ((PriorityOutduct) getOutducts()[current.getToHost().getAddress()]).getNormalQueue()
					.get(current.getQueueIndex());
			current.subtractFromOverbooked(m.getSize());
			current.setQueueIndex(current.getQueueIndex() - 1);

			if (current.getQueueIndex() < 0 && current.isOverbooked()) {
				current.setWhichQueue(2);
				current.setQueueIndex(
						((PriorityOutduct) getOutducts()[current.getToHost().getAddress()]).getExpeditedQueue().size()
								- 1);
			}

		} else {
			m = ((PriorityOutduct) getOutducts()[current.getToHost().getAddress()]).getExpeditedQueue()
					.get(current.getQueueIndex());
			current.subtractFromOverbooked(m.getSize());
			current.setQueueIndex(current.getQueueIndex() - 1);
		}

		((PriorityOutduct) getOutducts()[current.getToHost().getAddress()]).removeMessageFromOutduct(m);
		if (!current.isOverbooked())
			listOverbooked.remove(current);

		((PriorityMessage) m).setReforwarded(true);
		((PriorityMessage) m).setReforwardedFrom(current.getToHost());
		((PriorityMessage) m).setReforwardIndex(current.getQueueIndex() + 1);
		cgrForward(m, m.getTo());
	}

	public void setManageOverbooking(DTNHost to, double overbooked, double protect) {
		OverbookingStructure entry = new OverbookingStructure(to, overbooked, protect);
		if (((PriorityOutduct) getOutducts()[to.getAddress()]).getBulkBacklog() > 0) {
			entry.setQueueIndex(((PriorityOutduct) getOutducts()[to.getAddress()]).getBulkQueue().size() - 1);
			entry.setWhichQueue(0);
		} else if (((PriorityOutduct) getOutducts()[to.getAddress()]).getNormalBacklog() > 0) {
			entry.setQueueIndex(((PriorityOutduct) getOutducts()[to.getAddress()]).getNormalQueue().size() - 1);
			entry.setWhichQueue(1);
		} else if (((PriorityOutduct) getOutducts()[to.getAddress()]).getExpeditedBacklog() > 0) {
			entry.setQueueIndex(((PriorityOutduct) getOutducts()[to.getAddress()]).getExpeditedQueue().size() - 1);
			entry.setWhichQueue(2);
		}
		listOverbooked.add(entry);
		return;
	}
}
