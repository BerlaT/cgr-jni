/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package input;



import core.Settings;
import core.SettingsError;

public class PriorityMessageEventGenerator extends MessageEventGenerator 
{
	/** Message priority -setting id ({@value}). Can be a single integer
	 *  value between 0-2.	  
	 * Defines the message type and its related priority */
	public static final String MESSAGE_PRIORITY = "priority";
	
	protected int priority;
	
	public PriorityMessageEventGenerator(Settings s) 
	{
		super(s);
		//controllare eccezione
		//aggiungere define
		this.priority= s.getInt(MESSAGE_PRIORITY);
		System.out.println("............................................................."+this.priority);
		if(this.priority<0 || this.priority>2)
			throw new SettingsError("Message priority must be in range 0-2");
	}

	@Override
	public ExternalEvent nextEvent() {
		
		int responseSize = 0; /* zero stands for one way messages */
		int msgSize;
		int interval;
		int from;
		int to;		

		/* Get two *different* nodes randomly from the host ranges */
		from = drawHostAddress(this.hostRange);
		to = drawToAddress(hostRange, from);

		msgSize = drawMessageSize();
		interval = drawNextEventTimeDiff();
		
		/* Create event and advance to next event */
		PriorityMessageCreateEvent mce = new PriorityMessageCreateEvent(from, to, this.getID(),
				msgSize, responseSize, this.nextEventsTime,this.priority);
		this.nextEventsTime += interval;

		if (this.msgTime != null && this.nextEventsTime > this.msgTime[1]) {
			/* next event would be later than the end time */
			this.nextEventsTime = Double.MAX_VALUE;
		}

		return mce;
	}

}
