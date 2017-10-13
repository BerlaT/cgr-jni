/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

@SuppressWarnings("rawtypes")
/**
 * The contact plan conversion consists of a stand - alone Java application
 * including two classes, ContactPlanConverter.java and ContactPlanLine.java. The
 * later is just a support class representing a line of the contact plan, and
 * the collections of all the lines is the contact plan itself.
 * 
 * @author Jako Jo Messina
 * 
 */
public class ContactPlanLine implements Comparable {
	private int from;
	private int to;

	private int start;
	private int stop;

	private int datarate;

	public ContactPlanLine(int start, int stop, int from, int to, int datarate) {
		this.from = from;
		this.to = to;
		this.start = start;
		this.stop = stop;
		this.datarate = datarate;
	}

	public double getDatarate() {
		return datarate;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public void setTo(int to) {
		this.to = to;
	}

	public double getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public double getStop() {
		return stop;
	}

	public void setStop(int stop) {
		this.stop = stop;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public void setDatarate(int datarate) {
		this.datarate = datarate;
	}

	@Override
	public String toString() {
		return "a contact " + "\t+" + start + "\t+" + stop + "\t" + from + "\t" + to + "\t" + datarate;
	}

	public String toStringTwoWays() {
		return "a contact " + "\t+" + start + "\t+" + stop + "\t" + to + "\t" + from + "\t" + datarate;
	}

	public String toStringRange() {
		String res = null;

		if (to > from)
			res = "a range " + "\t+" + start + "\t+" + stop + "\t" + from + "\t" + to + "\t" + "1";
		else
			res = "a range " + "\t+" + start + "\t+" + stop + "\t" + to + "\t" + from + "\t" + "1";

		return res;
	}

	@Override
	public int compareTo(Object o) {
		ContactPlanLine cpl = (ContactPlanLine) o;
		int risultato;
		if ((this.getStart() - cpl.getStart()) == 0)
			return this.getFrom() - cpl.getFrom();
		else {
			risultato = (int) (this.getStart() - cpl.getStart());
			return risultato;
		}
	}

	public boolean connectionUp(int node1, int node2) {
		if (((this.getFrom() == node1) && (this.getTo() == node2))
				|| ((this.getFrom() == node2) && (this.getTo() == node1)))
			if (this.getStop() == 0)
				return true;
		return false;
	}
}
