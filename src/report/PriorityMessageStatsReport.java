/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.PriorityMessage;

/**
 * Extends report because all the attributes of MessageStatsReport are private.
 * It's a copy of MessageStatsReport for the most part.
 * 
 * @author Federico Fiorini
 *
 */
public class PriorityMessageStatsReport extends Report implements MessageListener {

	private Map<String, Double> creationTimes;
	private List<Double> latencies;
	private List<Integer> hopCounts;
	private List<Double> msgBufferTime;
	private List<Double> rtt; // round trip times

	private int nrofDropped;
	private int nrofBulkDropped;
	private int nrofNormDropped;
	private int nrofExpDropped;
	
	private int nrofRemoved;
	private int nrofBulkRemoved;
	private int nrofNormRemoved;
	private int nrofExpRemoved;
	
	private int nrofStarted;
	private int nrofAborted;
	private int nrofRelayed;
	
	private int nrofBulkRelayed;
	private int nrofNormRelayed;
	private int nrofExpRelayed;
	
	
	private int nrofCreated;
	private int nrofBulkCreated;
	private int nrofNormCreated;
	private int nrofExpCreated;
	
	private int nrofResponseReqCreated;
	private int nrofResponseDelivered;
	
	private int nrofDelivered;
	private int nrofBulkDelivered;
	private int nrofNormDelivered;
	private int nrofExpDelivered;



	public PriorityMessageStatsReport() {
		init();
	}

	@Override
	protected void init() {
		super.init();
		this.creationTimes = new HashMap<String, Double>();
		this.latencies = new ArrayList<Double>();
		this.msgBufferTime = new ArrayList<Double>();
		this.hopCounts = new ArrayList<Integer>();
		this.rtt = new ArrayList<Double>();

		this.nrofDropped = 0;
		this.nrofBulkDropped = 0;
		this.nrofNormDropped = 0;
		this.nrofExpDropped = 0;
		this.nrofRemoved = 0;
		this.nrofBulkRemoved = 0;
		this.nrofNormRemoved = 0;
		this.nrofExpRemoved = 0;
		this.nrofStarted = 0;
		this.nrofAborted = 0;
		this.nrofRelayed = 0;
		this.nrofBulkRelayed = 0;
		this.nrofNormRelayed = 0;
		this.nrofExpRelayed = 0;
		this.nrofCreated = 0;
		this.nrofBulkCreated = 0;
		this.nrofNormCreated = 0;
		this.nrofExpCreated = 0;
		this.nrofResponseReqCreated = 0;
		this.nrofResponseDelivered = 0;
		this.nrofDelivered = 0;
		this.nrofBulkDelivered = 0;
		this.nrofNormDelivered = 0;
		this.nrofExpDelivered = 0;
	}


	public void messageDeleted(Message m, DTNHost where, boolean dropped) {
		if (isWarmupID(m.getId())) {
			return;
		}

		if (dropped) {
			this.nrofDropped++;
			switch ( ((PriorityMessage)m).getPriority() )
			{
				case 0:
					this.nrofBulkDropped++; break;
				case 1:
					this.nrofNormDropped++; break;
				case 2:
					this.nrofExpDropped++; break;
			}
		}
		else {
			this.nrofRemoved++;
			switch ( ((PriorityMessage)m).getPriority() )
			{
				case 0:
					this.nrofBulkRemoved++; break;
				case 1:
					this.nrofNormRemoved++; break;
				case 2:
					this.nrofExpRemoved++; break;
			}
		}

		this.msgBufferTime.add(getSimTime() - m.getReceiveTime());
	}


	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofAborted++;
	}


	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean finalTarget) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofRelayed++;
		switch ( ((PriorityMessage)m).getPriority() )
		{
			case 0:
				this.nrofBulkRelayed++; break;
			case 1:
				this.nrofNormRelayed++; break;
			case 2:
				this.nrofExpRelayed++; break;
		}
		if (finalTarget) {
			this.latencies.add(getSimTime() -
				this.creationTimes.get(m.getId()) );
			this.nrofDelivered++;
			switch ( ((PriorityMessage)m).getPriority() )
			{
				case 0:
					this.nrofBulkDelivered++; break;
				case 1:
					this.nrofNormDelivered++; break;
				case 2:
					this.nrofExpDelivered++; break;
			}
			this.hopCounts.add(m.getHops().size() - 1);

			if (m.isResponse()) {
				this.rtt.add(getSimTime() -	m.getRequest().getCreationTime());
				this.nrofResponseDelivered++;
			}
		}
	}


	public void newMessage(Message m) {
		if (isWarmup()) {
			addWarmupID(m.getId());
			return;
		}

		this.creationTimes.put(m.getId(), getSimTime());
		this.nrofCreated++;
		switch ( ((PriorityMessage)m).getPriority() )
		{
			case 0:
				this.nrofBulkCreated++; break;
			case 1:
				this.nrofNormCreated++; break;
			case 2:
				this.nrofExpCreated++; break;
		}
		if (m.getResponseSize() > 0) {
			this.nrofResponseReqCreated++;
		}
	}


	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}

		this.nrofStarted++;
	}


	@Override
	public void done() {
		write("Message stats for scenario " + getScenarioName() +
				"\nsim_time: " + format(getSimTime()));
		double deliveryProb = 0; // delivery probability
		double deliveryBulkProb = 0;
		double deliveryNormProb = 0;
		double deliveryExpProb = 0;
		
		double responseProb = 0; // request-response success probability
		double overHead = Double.NaN;	// overhead ratio

		if (this.nrofCreated > 0) {
			deliveryProb = (1.0 * this.nrofDelivered) / this.nrofCreated;
		}
		if(this.nrofBulkCreated > 0){
			deliveryBulkProb = (1.0 * this.nrofBulkDelivered) / this.nrofBulkCreated;
		}
		if(this.nrofNormCreated > 0){
			deliveryNormProb = (1.0 * this.nrofNormDelivered) / this.nrofNormCreated;
		}
		if(this.nrofExpCreated > 0){
			deliveryExpProb = (1.0 * this.nrofExpDelivered) / this.nrofExpCreated;
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
				"\nbulk created " + this.nrofBulkCreated +
				"\nnormal created " + this.nrofNormCreated +
				"\nexpedited created " + this.nrofExpCreated + "\n" +
				
			"\nstarted: " + this.nrofStarted +
			"\nrelayed: " + this.nrofRelayed +
			"\nbulk relayed " + this.nrofBulkRelayed +
			"\nnormal relayed " + this.nrofNormRelayed +
			"\nexpedited relayed " + this.nrofExpRelayed + "\n" +
			
			"\naborted: " + this.nrofAborted +
			"\ndropped: " + this.nrofDropped +
			"\nbulk dropped " + this.nrofBulkDropped +
			"\nnormal dropped " + this.nrofNormDropped +
			"\nexpedited dropped " + this.nrofExpDropped + "\n" +
			
			"\nremoved: " + this.nrofRemoved +
			"\nbulk removed " + this.nrofBulkRemoved +
			"\nnormal removed " + this.nrofNormRemoved +
			"\nexpedited removed " + this.nrofExpRemoved + "\n" +
			
			"\ndelivered: " + this.nrofDelivered +
			"\nbulk delivered " + this.nrofBulkDelivered +
			"\nnormal delivered " + this.nrofNormDelivered +
			"\nexpedited delivered " + this.nrofExpDelivered + "\n" +
			
			"\ndelivery_prob: " + format(deliveryProb) +
			"\nbulk_delivery_prob: " + format(deliveryBulkProb) +
			"\nnormal_delivery_prob: " + format(deliveryNormProb) +
			"\nexp_delivery_prob: " + format(deliveryExpProb) +
			
			"\nresponse_prob: " + format(responseProb) +
			"\noverhead_ratio: " + format(overHead) +
			"\nlatency_avg: " + getAverage(this.latencies) +
			"\nlatency_med: " + getMedian(this.latencies) +
			"\nhopcount_avg: " + getIntAverage(this.hopCounts) +
			"\nhopcount_med: " + getIntMedian(this.hopCounts) +
			"\nbuffertime_avg: " + getAverage(this.msgBufferTime) +
			"\nbuffertime_med: " + getMedian(this.msgBufferTime) +
			"\nrtt_avg: " + getAverage(this.rtt) +
			"\nrtt_med: " + getMedian(this.rtt)
			;

		write(statsText);
		super.done();
	}

}
