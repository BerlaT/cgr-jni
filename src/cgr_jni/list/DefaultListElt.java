/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.list;

public class DefaultListElt {
	
	protected Object data;
	private DefaultList list;
	private DefaultListElt prev;
	private DefaultListElt next;
	
	public DefaultListElt(DefaultList list, DefaultListElt prev, DefaultListElt next, Object data)
	{
		this. list = list;
		this.prev = prev;
		this.next = next;
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public DefaultListElt getPrev() {
		return prev;
	}

	public void setPrev(DefaultListElt prev) {
		this.prev = prev;
	}

	public DefaultListElt getNext() {
		return next;
	}

	public void setNext(DefaultListElt next) {
		this.next = next;
	}

	public DefaultList getList() {
		return list;
	}

	public void destroy() {
		next = null;
		prev = null;
		list = null;
		data = null;		
	}
	

}
