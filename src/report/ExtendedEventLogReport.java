/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package report;

import java.io.File;
import java.util.List;

import core.Connection;
import core.DTNHost;
import core.Message;
import input.CPConnectionEvent;
import input.CPEventsReader;
import input.ExternalEvent;

/**
 * Extends EventLogReport in order to use the transmitSpeed defined in the
 * external Contact Plan. No constructor needed.
 * 
 * @author Federico Fiorini
 * 
 */

public class ExtendedEventLogReport extends EventLogReport {
	/**
	 * External ContactPlan parameters:
	 * 
	 * -File: it specifies the absolute path of the input ".txt" containing the
	 * user-defined ContactPlan; if the path changes on local disk it must be
	 * changed in this class as well. -Reader: it associates the CPEventsReader
	 * defined in the input package and the file before created. -Events: list
	 * of external events read from the external file. IMPORTANT: must be used a
	 * cast
	 */

	@Override
	public void hostsConnected(DTNHost host1, DTNHost host2) {
		/**
		 * "CONN" and "up" used because not specified by the reader Remember:
		 * Contact Plan only specifies open/close of connections between nodes
		 */
		processEvent("CONN", host1, host2, null, "up" + " " + host1.getInterface(1).getTransmitSpeed(host2.getInterface(1)));
	}

	private void processEvent(final String action, final DTNHost host1, final DTNHost host2, final Message message,
			final String extra) {
		write(getSimTime() + " " + action + " " + (host1 != null ? host1 : "") + (host2 != null ? (" " + host2) : "")
				+ (message != null ? " " + message : "") + (extra != null ? " " + extra : ""));
	}
}
