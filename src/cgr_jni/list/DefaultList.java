/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.list;

import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class DefaultList {

	protected Object userData;
	private LinkedList<DefaultListElt> list;
	private Lock lock;
	
	public DefaultList()
	{
		this.list = new LinkedList<>();
		this.lock = new ReentrantLock();
	}
	
	public long getLength()
	{
		return list.size();
	}
	
	public DefaultListElt getFirst()
	{
		return list.getFirst();
	}
	
	public DefaultListElt getLast()
	{
		return list.getLast();
	}
	
	public Object getUserData()
	{
		return userData;
	}
	
	public void setUserData(Object userData)
	{
		lock.lock();
		this.userData = userData;
		lock.unlock();
	}
	
	public void destroy()
	{
		lock.lock();
		for(DefaultListElt elt : list)
		{
			elt.destroy();
		}
		list.clear();
		list = null;
		lock.unlock();
		lock = null;
	}
	
	public DefaultListElt insertFirst(Object data)
	{
		lock.lock();
		DefaultListElt first = list.getFirst();
		DefaultListElt elt = new DefaultListElt(this, null, first, data);
		first.setPrev(elt);
		list.addFirst(elt);
		lock.unlock();
		return elt;
	}
	
	public DefaultListElt insertLast(Object data)
	{
		lock.lock();
		DefaultListElt last = list.getLast();
		DefaultListElt elt = new DefaultListElt(this, last, null, data);
		last.setNext(elt);
		list.addLast(elt);
		lock.unlock();
		return elt;		
	}
	
	public DefaultListElt insertBefore(DefaultListElt ref, Object data)
	{
		lock.lock();
		DefaultListElt elt;
		int i = list.indexOf(ref);
		if (i == 0)
		{
			elt = new DefaultListElt(this, null, ref, data);			
		}
		else
		{
			elt = new DefaultListElt(this, ref.getPrev(), ref, data);
			ref.getPrev().setNext(elt);
		}
		ref.setPrev(elt);
		list.add(i, elt);
		lock.unlock();
		return elt;
	}
	
	public DefaultListElt insertAfter(DefaultListElt ref, Object data)
	{
		lock.lock();
		DefaultListElt elt;
		int i = list.indexOf(ref);
		if (i + 1 == list.size())
		{
			elt = new DefaultListElt(this, ref, null, data);
		}
		else
		{
			elt = new DefaultListElt(this, ref, ref.getNext(), data);
			ref.getNext().setPrev(elt);
		}
		ref.setNext(elt);
		list.add(i + 1, elt);
		lock.unlock();
		return elt;
	}
	
	public void delete(DefaultListElt elt)
	{
		lock.lock();
		if (elt.getNext() != null)
			elt.getNext().setPrev(elt.getPrev());
		if (elt.getPrev() != null)
			elt.getPrev().setNext(elt.getNext());
		list.remove(elt);
		elt.destroy();
		lock.unlock();
	}
}
