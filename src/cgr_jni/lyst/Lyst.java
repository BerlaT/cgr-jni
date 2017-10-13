/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.lyst;

import java.util.LinkedList;
/**
 * Classe che implementa un gestore di LinkedList
 * 
 * @author michele
 *
 */

public class Lyst {
	
	private LinkedList<LystElt> list;
	private long deleteCallbackFunction = 0;
	private long deleteFunctionUserdata = 0;
		
	public Lyst()
	{
		this.list = new LinkedList<>();
	}
	
	public void clear()
	{
		list.clear();
	}
	
	public LystElt insertFirst(long data)
	{
		LystElt el = new LystElt(data);
		el.setList(this);
		list.addFirst(el);
		return el;
	}
	
	public LystElt insertLast(long data)
	{
		LystElt el = new LystElt(data);
		el.setList(this);
		list.addLast(el);
		return el;
	}
		
	public static Lyst lyst_create_using(int idx)
	{
		return lyst_create();
	}

	public static Lyst lyst_create() {
		return new Lyst();
	}
	
	public static void lyst_clear(Lyst list)
	{
		list.clear();
	}
	
	public static void lyst_destroy(Lyst list)
	{
		list.clear();
		list = null;
	}
	
	public static LystElt lyst_insert_last(Lyst list, long data)
	{
		return list.insertLast(data);
	}
	public static LystElt lyst_insert_before(LystElt elt, long data)
	{
		Lyst list = elt.getList();
		int index = list.list.indexOf(elt);
		LystElt el = new LystElt(data);
		el.setList(list);
		elt.getList().list.add(index, el);
		return el;
	}
	public static void lyst_delete(LystElt el)
	{
		el.getList().list.remove(el);
		el = null;
	}
	
	public static LystElt lyst_first(Lyst list)
	{
		if (list.list.isEmpty())
			return null;
		return list.list.getFirst();
	}
	
	public static LystElt lyst_last(Lyst list)
	{
		if (list.list.isEmpty())
			return null;
		return list.list.getLast();
	}
	
	public static LystElt lyst_next(LystElt el)
	{
		LinkedList<LystElt> list = el.getList().list;
		LystElt result = null;
		int i = list.indexOf(el);
		try {
			result = list.get(i + 1);
		} catch (IndexOutOfBoundsException e)
		{
			return null;
		}
		return result;
	}
	
	public static LystElt lyst_prev(LystElt el)
	{
		LinkedList<LystElt> list = el.getList().list;
		int i = list.indexOf(el);
		if (i == 0)
			return null; // el is the first element of the list
		else
			return list.get(i - 1);
	}
	
	public static long lyst_data(LystElt el)
	{
		return el.getData();
	}

	public static long lyst_data_set(LystElt el, long data)
	{
		long prevData = el.getData();
		el.setData(data);
		return prevData;
	}

	public static void lyst_delete_set(Lyst list, long callbackFunction, long userdata)
	{
		list.deleteCallbackFunction = callbackFunction;
		list.deleteFunctionUserdata = userdata;
	}
	
	public static long getDeleteFunction(Lyst list)
	{
		return list.deleteCallbackFunction;
	}
	public static long getDeleteUserdata(Lyst list)
	{
		return list.deleteFunctionUserdata;
	}
	
	
	public static Lyst getLyst(LystElt elt)
	{
		return elt.getList();
	}
	
}