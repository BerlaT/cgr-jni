/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import core.Settings;

/**
 * Queue of extended external events. It handles the specific case of external
 * ContactPlan events read by a "contact.txt" file, using the methods from
 * input.CPEventsReader.
 * 
 * The constructors are exactly the same as the superclass, ExternalEventsQueue,
 * but the "init" method has been modified, in order to support the reading with
 * input.CPEventsReader.
 * 
 * @author Federico Fiorini
 * 
 */
public class CPExternalEventsQueue implements EventQueue {
	/** default number of preloaded events */
	public static final int DEFAULT_NROF_PRELOAD = 500;
	/** number of event to preload -setting id ({@value})*/
	public static final String PRELOAD_SETTING = "nrofPreload";
	/** path of external events file -setting id ({@value})*/
	public static final String PATH_SETTING = "filePath";
	
	private File eventsFile;
	private CPEventsReader reader;
	private int nrofPreload;
	private int nextEventIndex;
	private boolean allEventsRead = false;
	private List<ExternalEvent> queue;
	
	public CPExternalEventsQueue(Settings s) {
		if (s.contains(PRELOAD_SETTING)) {
			setNrofPreload(s.getInt(PRELOAD_SETTING));
		}
		else {
			setNrofPreload(DEFAULT_NROF_PRELOAD);
		}
        String eeFilePath = s.valueFillString(s.getSetting(PATH_SETTING));
        init(eeFilePath);
	}

	public CPExternalEventsQueue(String filePath, int nrofPreload) {
		setNrofPreload(nrofPreload);
		init(filePath);
	}
	
	public void setNrofPreload(int nrof) {
		if (nrof < 1) {
			nrof = DEFAULT_NROF_PRELOAD;
		}
		this.nrofPreload = nrof;
	}
	
	/**
	 * This method creates an association between an instance of this class and
	 * one reading class; it has been modified to allow reading from
	 * CPEventsReader. This method calls the next one. For the sake of
	 * simplicity, it is assumed that the input file cannot be binary, thus
	 * omitting one if instruction.
	 * 
	 * @param eeFilePath
	 */
	protected void init(String eeFilePath) {
		this.eventsFile = new File(eeFilePath);

		/** Used for external Contact Plan reading */
		this.reader = new CPEventsReader(eventsFile);

		this.queue = readEvents(nrofPreload);
		this.nrofPreload = 0;
		this.nextEventIndex = 0;
	}

	/**
	 * This method is called inside init(), it reads the events by means of the
	 * associated reader and adds them to the queue of the external events,
	 * defined as a field of the class.
	 * 
	 * @param nrof
	 * @return
	 */
	protected List<ExternalEvent> readEvents(int nrof) {
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
	/**
	 * Returns the next event in the queue or ExternalEvent with time of
	 * double.MAX_VALUE if there are no events left.
	 * @return The next event
	 */
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

	/**
	 * Returns next event's time or Double.MAX_VALUE if there are no
	 * events left in the queue.
	 * @return Next event's time
	 */
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