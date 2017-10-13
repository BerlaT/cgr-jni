/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

package core;

/**
 * The Message class has been extended to PriorityMessage, which just includes
 * the priority integer field, which can assume the values [0,2] corresponding
 * to bulk, normal and expedited cardinal priorities. Moreover, the methods
 * replicate() e copyFrom(), have also been modified to include the new priority
 * field in the replicated messages.
 * 
 * @author Federico Fiorini
 *
 */
public class PriorityMessage extends Message {

	/** priority of the message */
	private int priority;
	private boolean reforwarded;
	private DTNHost reforwardedFrom;
	private int reforwardIndex;

	/**
	 * Creates a new PriorityMessage.
	 * 
	 * @param from
	 *            Who the message is (originally) from
	 * @param to
	 *            Who the message is (originally) to
	 * @param id
	 *            Message identifier (must be unique for message but will be the
	 *            same for all replicates of the message)
	 * @param size
	 *            Size of the message (in bytes)
	 * @param priority
	 *            Priority of the message (range 0..2, from "bulk" to
	 *            "expedited")
	 */
	public PriorityMessage(DTNHost from, DTNHost to, String id, int size, int priority) {
		super(from, to, id, size);
		this.priority = priority;
		this.reforwarded = false;
	}

	/**
	 * Returns the priority of the PriorityMessage
	 * 
	 * @return the priority of the PriorityMessage
	 */
	public int getPriority() {
		return priority;
	}

	@Override
	protected void copyFrom(Message m) {
		super.copyFrom(m);
		// this.priority = ((PriorityMessage)m).getPriority();
	}

	@Override
	public Message replicate() {
		PriorityMessage m = new PriorityMessage(this.getFrom(), this.getTo(), this.getId(), this.getSize(), priority);
		m.copyFrom(this);
		return m;
	}

	public boolean isReforwarded() {
		return reforwarded;
	}

	public void setReforwarded(boolean reforwarded) {
		this.reforwarded = reforwarded;
	}

	public DTNHost getReforwardedFrom() {
		return reforwardedFrom;
	}

	public void setReforwardedFrom(DTNHost reforwardedFrom) {
		this.reforwardedFrom = reforwardedFrom;
	}

	public int getReforwardIndex() {
		return reforwardIndex;
	}

	public void setReforwardIndex(int reforwardIndex) {
		this.reforwardIndex = reforwardIndex;
	}

}
