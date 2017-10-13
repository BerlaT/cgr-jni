/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import core.Settings;


public class DMExternalEventsQueue implements EventQueue{
	private File eventsFile;
	private int nrofPreload;
	private List<ExternalEvent> queue;
	private int nextEventIndex;
	private boolean allEventsRead = false;
	private DMEventsReader reader;
	
	public DMExternalEventsQueue(Settings s) {
		String msgPrefix, filePath;
		if (s.contains(CPExternalEventsQueue.PRELOAD_SETTING)) {
			setNrofPreload(s.getInt(CPExternalEventsQueue.PRELOAD_SETTING));
		}
		else {
			setNrofPreload(CPExternalEventsQueue.DEFAULT_NROF_PRELOAD);
		}
		msgPrefix = s.getSetting(MessageEventGenerator.MESSAGE_ID_PREFIX_S, "DM");
		filePath = s.valueFillString(s.getSetting(CPExternalEventsQueue.PATH_SETTING));

		this.eventsFile = new File(filePath);

		/** Used for external Contact Plan reading */
		this.reader = new DMEventsReader(eventsFile, msgPrefix);

		this.queue = readEvents(nrofPreload);
		this.nrofPreload = 0;
		this.nextEventIndex = 0;
	}
	
	public DMExternalEventsQueue(String filePath, int nrofPreload) {
		setNrofPreload(nrofPreload);

		this.eventsFile = new File(filePath);

		/** Used for external Contact Plan reading */
		this.reader = new DMEventsReader(eventsFile);

		this.queue = readEvents(nrofPreload);
		this.nrofPreload = 0;
		this.nextEventIndex = 0;
	}
	
	public void setNrofPreload(int nrof) {
		if (nrof < 1) {
			nrof = CPExternalEventsQueue.DEFAULT_NROF_PRELOAD;
		}
		this.nrofPreload = nrof;
	}
	
	
	private List<ExternalEvent> readEvents(int nrof) {
		if (allEventsRead) {
			return new ArrayList<ExternalEvent>(0);
		}

		List<ExternalEvent> events = reader.readEvents(nrof);

		if (nrof > 0 && events.size() == 0) {
			reader.close();
			allEventsRead = true;
		}

		return events;
	}
	
	@Override
	public ExternalEvent nextEvent() {
		if (queue.size() == 0) { // no more events
			return new ExternalEvent(Double.MAX_VALUE);
		}

		ExternalEvent ee = queue.get(nextEventIndex);
		nextEventIndex++;

		if (nextEventIndex >= queue.size()) { // ran out of events
			queue = readEvents(nrofPreload);
			nextEventIndex = 0;
		}

		return ee;
	}

	@Override
	public double nextEventsTime() {
		if (eventsLeftInBuffer() <= 0 ) {
			// in case user request time of an event that doesn't exist
			return Double.MAX_VALUE;
		}
		else {
			return queue.get(nextEventIndex).getTime();
		}
	}

	public int eventsLeftInBuffer() {
		if (queue == null || queue.size() == 0) {
			return 0;
		}
		else {
			return this.queue.size() - this.nextEventIndex;
		
		}
	}
}
