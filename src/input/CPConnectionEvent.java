/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */
package input;

import core.DTNHost;
import core.SimError;
import core.World;
import interfaces.SimpleAsymmetricInterface;

/**
 * Class used to define a specific ConnectionEvent, made by the ContactPlan
 * reading class. It extends ConnectionEvent adding the transmit datarate
 * between hosts (param: speed)
 * The constructor calls the superclass' one and extends it by adding the speed
 * parameter.
 * 
 * @author Federico Fiorini
 * 
 */

public class CPConnectionEvent extends ConnectionEvent {

	private static final long serialVersionUID = 1L;


	/**
	 * Speed of the connection between two nodes. It's simply the datarate used
	 * by the network interface
	 */
	private int speed;
	private boolean sim;

	public CPConnectionEvent(int from, int to, String interf, boolean up, double time, int txSpeed, boolean sim) {
		super(from, to, interf, up, time);
		this.speed = txSpeed;
		this.sim = sim;
	}

	@Override
	public void processEvent(World world) {
		
		DTNHost from =  world.getNodeByAddress(this.fromAddr);
		DTNHost to =  world.getNodeByAddress(this.toAddr);
		if(!(sim || from.getInterface(1) instanceof SimpleAsymmetricInterface)) throw new SimError("First Interface must be AsymmtericInterface");
		from.getInterface(1).setTransmitSpeed(this.speed);
		from.forceConnection(to, null, this.isUp);
	}

	@Override
	public String toString() {
		return "CONN " + (isUp ? "up" : "down") + " @" + this.time + " " + this.fromAddr + "<->" + this.toAddr + " . c"
				+ this.speed;
	}
}
