/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package report;

import core.DTNHost;
import core.Message;
import routing.OpportunisticContactGraphRouter;

public class OCGRMessageStatsReport extends MessageStatsReport {

	private int nmrofRegularRelayed;
	private int nmrofRegularStarted;
	private int nmrofRegularAborted;
	private int nmrofEpidemicStarted;
	private int nmrofEpidemicRelayed;
	private int nmrofEpidemicAborted;
	
	public OCGRMessageStatsReport() {
		init();
	}
	
	@Override
	protected void init() {
		super.init();
		nmrofRegularRelayed = 0;
		nmrofRegularStarted = 0;
		nmrofRegularAborted = 0;
		nmrofEpidemicRelayed = 0;
		nmrofEpidemicStarted = 0;
		nmrofEpidemicAborted = 0;
	}
	
	@Override
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}
		super.messageTransferAborted(m, from, to);
		boolean epidemic = (boolean) m.getProperty(
				OpportunisticContactGraphRouter.EPIDEMIC_FLAG_PROP);
		if (epidemic)
			this.nmrofEpidemicAborted++;
		else
			this.nmrofRegularAborted++;
	}
	
	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean finalTarget) {
		if (isWarmupID(m.getId())) {
			return;
		}
		super.messageTransferred(m, from, to, finalTarget);
		boolean epidemic = (boolean) m.getProperty(
				OpportunisticContactGraphRouter.EPIDEMIC_FLAG_PROP);
		if (epidemic)
			this.nmrofEpidemicRelayed++;
		else
			this.nmrofRegularRelayed++;
	}

	public void messageTransferStarted(Message m, DTNHost from, DTNHost to) {
		if (isWarmupID(m.getId())) {
			return;
		}
		super.messageTransferStarted(m, from, to);
		boolean epidemic = (boolean) m.getProperty(
				OpportunisticContactGraphRouter.EPIDEMIC_FLAG_PROP);
		if (epidemic)
			this.nmrofEpidemicStarted++;
		else
			this.nmrofRegularStarted++;
	}

	@Override
	public String getMoreInfo() {
		String statsText = "Regular stats:" +
				"\nstarted: " + this.nmrofRegularStarted + 
				"\nrelayed: " + this.nmrofRegularRelayed +
				"\naborted: " + this.nmrofRegularAborted +
				"\nEpidemic Stats: " +
				"\nstarted: " + this.nmrofEpidemicStarted + 
				"\nrelayed: " + this.nmrofEpidemicRelayed +
				"\naborted: " + this.nmrofEpidemicAborted
				;
		return statsText;
	}
	
	@Override
	public void done() {
		super.done();
	}
}
