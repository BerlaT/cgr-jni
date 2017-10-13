/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package input;

import core.DTNHost;
import core.Message;
import core.PriorityMessage;
import core.World;

public class PriorityMessageCreateEvent extends MessageCreateEvent 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** Priority of the message (0..2) */
	protected int priority;
	protected int psize;
	protected int presponseSize;
	
	/**
	 * Creates a message creation event with a optional response request
	 * @param from The creator of the message
	 * @param to Where the message is destined to
	 * @param id ID of the message
	 * @param size Size of the message
	 * @param responseSize Size of the requested response message or 0 if
	 * no response is requested
	 * @param time Time, when the message is created
	 * @param priority The priority of the message
	 */
	public PriorityMessageCreateEvent(int from, int to, String id, int size, int responseSize, double time, int priority)
	{
		super(from, to, id, size, responseSize, time);
		this.psize = size;
		this.presponseSize = responseSize;
		this.priority = priority;
	}

	/**
	 * Creates the message this event represents.
	 */
	@Override
	public void processEvent(World world)
	{
		DTNHost to = world.getNodeByAddress(this.toAddr);
		DTNHost from = world.getNodeByAddress(this.fromAddr);
		
		Message m = new PriorityMessage(from, to, this.id, this.psize, this.priority);
		m.setResponseSize(this.presponseSize);
		from.createNewMessage(m);
	}

}
