/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package input;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import core.SimError;

/**
 * Class used for ContactPlan reading from an external file, in order to create
 * a list of external events used by the simulation
 * 
 * @author Federico Fiorini
 * 
 */
public class CPEventsReader  implements ExternalEventsReader{
	/** Identifier for ContactPlan Range between nodes */
	public static final String RANGE = "range";
	/** Identifier for ContactPlan Contact between nodes */
	public static final String CONTACT = "contact";
	/**
	 * Anticipated distance between nodes in the interval, in Light Seconds (for
	 * Earth transmissions it's always 1)
	 */
	public static final int distance = 1;

	/* BufferedReader needed for file's reading */
	private BufferedReader reader;
	/* Events queue */
	private List<ExternalEvent> events;
	/** Error messages */
	private static final String timeError = "Start time must be lower than end time of contact";
	private static final String rangeHostError = "Host1 must be lower than Host2 for bidirectional communication";

	/**
	 * Constructor
	 * 
	 * @param eventsFile:
	 *            the file read in order to obtain the Contact Plan. It's
	 *            possible to specify either a defined file (with its absolute
	 *            path) or a filePath in the settings.txt file. **it calls the
	 *            superclass constructor**
	 */
	public CPEventsReader(File eventsFile) {
		try {
			this.reader = new BufferedReader(new FileReader(eventsFile));
		} catch (FileNotFoundException e) {
			throw new SimError(e.getMessage(), e);
		}
	}

	/**
	 * For every contact read in the file it generates the corresponding contact
	 * event and puts it in a queue of ExternalEvent, given in output. The range
	 * instructions are formally checked, and then skipped, as propagation
	 * delays are negligible in terrestrial communications and not implemented
	 * in ONE.
	 */
	public List<ExternalEvent> readEvents(int nrof) {
		this.events = new ArrayList<ExternalEvent>(nrof);
		String startTimeStr_cont1, startTimeStr_cont2;
		String endTimeStr_cont1, endTimeStr_cont2;
		String host1Addr_cont1, host1Addr_cont2;
		String host2Addr_cont1, host2Addr_cont2;
		String transmitSpeedStr_cont1, transmitSpeedStr_cont2;
		long startTime_cont1 = 0, startTime_cont2 = 0, endTime_cont1 = 0, endTime_cont2 = 0;
		int host1_cont1 = 0, host1_cont2 = 0, host2_cont1 = 0, host2_cont2 = 0, transmitSpeed_cont1 = 0,
				transmitSpeed_cont2 = 0;
		String startTimeStrRANGE, endTimeStrRANGE, host1AddrRANGE, host2AddrRANGE, distStrRANGE;
		long startTimeRANGE = 0, endTimeRANGE = 0;
		int host1RANGE = 0, host2RANGE = 0, distRANGE;
		String line, letter, action;
		StringTokenizer stk = null;
		int range_is_present = 0;
		boolean sim;
		
		try {
			while ((line = this.reader.readLine()) != null) {
				/**
				 * RANGE line
				 */
				stk = new StringTokenizer(line, "\t ");

				letter = stk.nextToken();
				if (!letter.equals("a"))
					throw new IllegalArgumentException("Must be ADD (a)");

				action = stk.nextToken();

				if (action.equals(RANGE)) {
					range_is_present++;

					startTimeStrRANGE = stk.nextToken();
					endTimeStrRANGE = stk.nextToken();
					host1AddrRANGE = stk.nextToken();
					host2AddrRANGE = stk.nextToken();
					distStrRANGE = stk.nextToken();

					startTimeStrRANGE = startTimeStrRANGE.substring(1);
					endTimeStrRANGE = endTimeStrRANGE.substring(1);

					startTimeRANGE = Long.parseLong(startTimeStrRANGE);
					endTimeRANGE = Long.parseLong(endTimeStrRANGE);

					if (startTimeRANGE > endTimeRANGE)
						throw new IllegalArgumentException(timeError);

					host1RANGE = Integer.parseInt(host1AddrRANGE) - 1;
					host2RANGE = Integer.parseInt(host2AddrRANGE) - 1;
					if (host2RANGE < host1RANGE)
						throw new IllegalArgumentException(rangeHostError);
					if (host1RANGE < 0 && host2RANGE < 0)
						throw new SimError("Unknown Hosts");

					distRANGE = Integer.parseInt(distStrRANGE);
					if (distRANGE != distance)
						throw new IllegalArgumentException("Must be 1 for terrestrial transmissions");
				}

				if (action.equals(CONTACT)) {

					startTimeStr_cont1 = stk.nextToken();
					endTimeStr_cont1 = stk.nextToken();
					host1Addr_cont1 = stk.nextToken();
					host2Addr_cont1 = stk.nextToken();
					transmitSpeedStr_cont1 = stk.nextToken();

					startTimeStr_cont1 = startTimeStr_cont1.substring(1);
					endTimeStr_cont1 = endTimeStr_cont1.substring(1);

					startTime_cont1 = Long.parseLong(startTimeStr_cont1);
					endTime_cont1 = Long.parseLong(endTimeStr_cont1);
					if (startTime_cont1 > endTime_cont1)
						throw new IllegalArgumentException(timeError);

					host1_cont1 = Integer.parseInt(host1Addr_cont1) - 1;
					host2_cont1 = Integer.parseInt(host2Addr_cont1) - 1;
					if (host1_cont1 < 0 && host2_cont1 < 0)
						throw new SimError("Unknown Hosts");

					transmitSpeed_cont1 = Integer.parseInt(transmitSpeedStr_cont1);
					if (transmitSpeed_cont1 < 0)
						throw new IllegalArgumentException("TransmitSpeed must be higher than zero");

					line = this.reader.readLine();
					stk = new StringTokenizer(line, "\t ");

					letter = stk.nextToken();
					if (!letter.equals("a"))
						throw new IllegalArgumentException("Must be ADD (a)");

					action = stk.nextToken();

					if (action.equals(CONTACT)) {

						startTimeStr_cont2 = stk.nextToken();
						endTimeStr_cont2 = stk.nextToken();
						host1Addr_cont2 = stk.nextToken();
						host2Addr_cont2 = stk.nextToken();
						transmitSpeedStr_cont2 = stk.nextToken();

						startTimeStr_cont2 = startTimeStr_cont2.substring(1);
						endTimeStr_cont2 = endTimeStr_cont2.substring(1);

						startTime_cont2 = Long.parseLong(startTimeStr_cont2);
						endTime_cont2 = Long.parseLong(endTimeStr_cont2);
						if (startTime_cont2 > endTime_cont2)
							throw new IllegalArgumentException(timeError);

						host1_cont2 = Integer.parseInt(host1Addr_cont2) - 1;
						host2_cont2 = Integer.parseInt(host2Addr_cont2) - 1;
						if (host1_cont2 < 0 && host2_cont2 < 0)
							throw new SimError("Unknown Hosts");

						transmitSpeed_cont2 = Integer.parseInt(transmitSpeedStr_cont2);
						if (transmitSpeed_cont2 < 0)
							throw new IllegalArgumentException("TransmitSpeed must be higher than zero");
					}else {
						throw new IllegalArgumentException("Expected second contact");
					}

					/**
					 * principal control, if it isn't right, it will throws an
					 * exception
					 */
					if ((host1_cont1 != host2_cont2) || (host2_cont1 != host1_cont2))
						throw new IllegalArgumentException("No right order: contact plan error");
					sim = startTime_cont1 == startTime_cont2 && endTime_cont1 == endTime_cont2 &&
							 transmitSpeed_cont1 == transmitSpeed_cont2;
					events.add(new CPConnectionEvent(host1_cont1, host2_cont1, String.valueOf(transmitSpeed_cont1),
							true, startTime_cont1, transmitSpeed_cont1, sim));
					
					events.add(new CPConnectionEvent(host1_cont1, host2_cont1, String.valueOf(transmitSpeed_cont1),
							false, endTime_cont1, transmitSpeed_cont1, sim));
					
					events.add(new CPConnectionEvent(host2_cont1, host1_cont1, String.valueOf(transmitSpeed_cont1),
							true, startTime_cont2, transmitSpeed_cont2, sim));
					
					events.add(new CPConnectionEvent(host2_cont1, host1_cont1, String.valueOf(transmitSpeed_cont1),
							false, endTime_cont2, transmitSpeed_cont2, sim));					
				}
			}
		} catch (IOException e) {
			throw new SimError("Reading from external file failed!");
		}

		/**
		 * no range is found in all file
		 */
		
		if ( range_is_present == 0 && stk != null )
			throw new IllegalArgumentException("expected at least one \"a range\": contact plan error");

		events.sort(new Comparator<ExternalEvent>() {
			@Override
			public int compare(ExternalEvent e1, ExternalEvent e2) {
				if(e1.getTime() > e2.getTime()) return 1;
				return -1;
			}
		});
		
		return this.events;
	}
	
	public void close() {
		try {
			this.reader.close();
		} catch (IOException e) {
		}
	}
}
