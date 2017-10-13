/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package report;


import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.SimClock;

/**
 * No documentation exists about this class!
 *
 */
public class SimpleMessageStatsReport extends Report implements MessageListener {
	
	private int nrofDropped;
	private int nrofRemoved;
	private int nrofStarted;
	private int nrofAborted;
	private int nrofRelayed;
	private int nrofCreated;
	private int nrofResponseReqCreated;
	private int nrofResponseDelivered;
	private int nrofDelivered;

	/**
	 * Constructor.
	 */
	public SimpleMessageStatsReport() {
		init();
	}

	@Override
	protected void init() {
		super.init();
	
		this.nrofDropped = 0;
		this.nrofRemoved = 0;
		this.nrofStarted = 0;
		this.nrofAborted = 0;
		this.nrofRelayed = 0;
		this.nrofCreated = 0;
		this.nrofResponseReqCreated = 0;
		this.nrofResponseDelivered = 0;
		this.nrofDelivered = 0;
	}


	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (isWarmup() ||SimClock.getTime()>32400) {
			return;
		}

		if (dropped) {
			this.nrofDropped++;
		}
		else {
			this.nrofRemoved++;
		}

	}


	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if (isWarmup() ||SimClock.getTime()>32400) {
			return;
		}

		this.nrofAborted++;
	}


	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean finalTarget) {
		if (isWarmup() ||SimClock.getTime()>32400) {
			return;
		}

		this.nrofRelayed++;
		if (finalTarget) {		
			this.nrofDelivered++;			

			if (m.isResponse()) {
				this.nrofResponseDelivered++;
			}
		}
	}


	public void newMessage(Message m) {
		if (isWarmup() ||SimClock.getTime()>32400) {
			return;
		}
		
		this.nrofCreated++;
		if (m.getResponseSize() > 0) {
			this.nrofResponseReqCreated++;
		}
	}


	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if (isWarmup() ||SimClock.getTime()>32400) {
			return;
		}

		this.nrofStarted++;
	}

	public String getMoreInfo()
	{
		return "";
	}

	@Override
	public void done() {
		write("Message stats for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));
		double deliveryProb = 0; // delivery probability
		double responseProb = 0; // request-response success probability
		double overHead = Double.NaN;	// overhead ratio

		if (this.nrofCreated > 0) {
			deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
		}
		if (this.nrofDelivered > 0) {
			overHead = (1.0 * (this.nrofRelayed - this.nrofDelivered)) /
				this.nrofDelivered;
		}
		if (this.nrofResponseReqCreated > 0) {
			responseProb = (1.0* this.nrofResponseDelivered) /
				this.nrofResponseReqCreated;
		}

		String statsText = "created: " + this.nrofCreated +
			"\nstarted: " + this.nrofStarted +
			"\nrelayed: " + this.nrofRelayed +
			"\naborted: " + this.nrofAborted +
			"\ndropped: " + this.nrofDropped +
			"\nremoved: " + this.nrofRemoved +
			"\ndelivered: " + this.nrofDelivered +
			"\ndelivery_prob: " + format(deliveryProb) +
			"\nresponse_prob: " + format(responseProb) +
			"\noverhead_ratio: " + format(overHead)
			;

		write(statsText+"\n"+getMoreInfo());
		super.done();
	}

}
