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
import java.util.List;
import java.util.StringTokenizer;

import core.SimError;

public class DMEventsReader implements ExternalEventsReader{
	public static final int DEFAULT_PAYLOAD=50*1024, DEFAULT_PRIORITY=0;
	/* BufferedReader needed for file's reading */
	private BufferedReader reader;
	/* Events queue */
	private List<ExternalEvent> events;
	private String msgPrefix;
	
	public DMEventsReader(File eventsFile, String msgPrefix) {
		this.msgPrefix = msgPrefix;
		try {
			this.reader = new BufferedReader(new FileReader(eventsFile));
		} catch (FileNotFoundException e) {
			throw new SimError(e.getMessage(), e);
		}
	}
	public DMEventsReader(File eventsFile) {
		this(eventsFile, "DM");
	}
	@Override
	public List<ExternalEvent> readEvents(int nrof) {
		this.events = new ArrayList<ExternalEvent>(nrof);
		String line, token, msgPrefix;
		int responseSize = 0; /* zero stands for one way messages */
		int dest, source, time, data, payload, priority, i=1;
		double rate, startTime;
		char rateType, dataType;
		StringTokenizer st;
		
		try {
			while((line = this.reader.readLine()) != null) {
				st = new StringTokenizer(line, "-\t ");
				dest=-1;
				source=-1;
				time=-1;
				data=-1;
				payload=DEFAULT_PAYLOAD;
				priority=DEFAULT_PRIORITY;
				rate=-1;
				startTime=0;
				rateType=0;
				msgPrefix = this.msgPrefix;
				while(st.hasMoreTokens()) {
					token = st.nextToken();
					if(token.equals("d") || token.equals("destination")) {
						dest = Integer.parseInt(st.nextToken())-1;
					}
					else if(token.equals("s") || token.equals("source")) {
						source = Integer.parseInt(st.nextToken())-1;
					}
					else if(token.equals("T") || token.equals("time")) {
						time = Integer.parseInt(st.nextToken());
					}
					else if(token.equals("D") || token.equals("data")) {
						token = st.nextToken();
						dataType=token.charAt(token.length()-1);
						data = Integer.parseInt(token.substring(0, token.length()-1));
						switch(dataType) {
							case 'k' : data = data * 1000;
								break;
							case 'M' : data = data * 1000 * 1000;
								break;
							case 'B' :
								break;
							default : throw new IOException("Wrong data type");
						}
					}
					else if(token.equals("P") || token.equals("payload")) {
						token = st.nextToken();
						dataType=token.charAt(token.length()-1);
						payload= Integer.parseInt(token.substring(0, token.length()-1));
						switch(dataType) {
							case 'k' : payload = payload * 1000;
								break;
							case 'M' : payload = payload * 1000 * 1000;
								break;
							case 'B' :
								break;
							default : throw new IOException("Wrong payload type");
						}
					}
					else if(token.equals("p") || token.equals("priority")) {
						priority = Integer.parseInt(st.nextToken());
						switch(priority) {
							case 0 : msgPrefix = msgPrefix + "_B";
								break;
							case 1 : msgPrefix = msgPrefix + "_N";
								break;
							case 2 : msgPrefix = msgPrefix + "_E";
								break;
							default :
								throw new IOException("Wrong data priority level");	
						}
					}
					else if(token.equals("R") || token.equals("rate")) {
						token = st.nextToken();
						rateType=token.charAt(token.length()-1);
						rate = Double.parseDouble(token.substring(0, token.length()-1));
					}
					else if(token.charAt(0)=='+') {
						startTime = Double.parseDouble(token.substring(1));
					}
					else {
						throw new IOException("'" + token + "' option doesn't exist");
					}
				}
				switch(rateType) {
					case 'k' : rate = payload/rate/1000;
						break;
					case 'M' : rate = payload/rate/1000000;
						break;
					case 'b' : rate = 1/rate;
						break;
					default : throw new IOException("Not implemented rate type");
				}
				if(dest<0 || source <0 || rate<0) {
					throw new IOException("'destination', 'source' and 'rate' are mandatory options");
				}
				if((data>=0 && time>=0) || (data<0 && time<0)) {
					throw new IOException("'data' either-or 'time' must be set");
				}
				else if(data>=0) {
					while(data>=payload) {
						add(new PriorityMessageCreateEvent(source, dest,  msgPrefix+(i++), payload, responseSize, startTime, priority));
						startTime = startTime + rate;
						data = data - payload;
					}
					if(data!=0) add(new PriorityMessageCreateEvent(source, dest,  msgPrefix+(i++), data, responseSize, startTime, priority));
				}
				else {
					while(startTime<=time) {
						add(new PriorityMessageCreateEvent(source, dest,  msgPrefix+(i++), payload, responseSize, startTime, priority));
						startTime = startTime + rate;
					}
				}
			}
		}
		catch (IOException e) {
			throw new SimError("Reading from external file failed!");
		}
		return this.events;
	}
	
	private void add(ExternalEvent e) {
		int i;
		for(i=0; i<events.size(); i++) {
			if(e.getTime() < events.get(i).getTime()) break;
		}
		events.add(i, e);
	}
	
	@Override
	public void close() {
		try {
			this.reader.close();
		} catch (IOException e) {
		}
	}

}
