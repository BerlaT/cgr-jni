/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.psm;

import java.util.LinkedHashMap;
import java.util.TreeSet;

public class PsmPartition {
	private int id;
	private TreeSet<Long> addresses;
	private LinkedHashMap<String, Long> catalog;
	
	public PsmPartition(int id)
	{
		this.id = id;
		this.catalog = new LinkedHashMap<>(1);
		this.addresses = new TreeSet<>();
	}
	
	public int getId()
	{
		return this.id;
	}
	/**
	 * 
	 * @param name
	 * @return the address of the object on success, 
	 * -1 if @name is not found in catalog.
	 */
	public long psmLocate(String name)
	{
		Long add = catalog.get(name);
		if (add == null)
		{
			return -1;
		}
		else
		{
			return add.longValue();
		}
	}
	
	public int psmCatlg(String name, long objLocation)
	{
		if (catalog.containsKey(name))
		{
			return -1;
		}
		catalog.put(name, objLocation);
		return 0;
	}
	
	public int psmUncatlg(String name)
	{
		if (! catalog.containsKey(name))
		{
			return -1;
		}
		catalog.remove(name);
		return 0;
	}
	
	public long psmAlloc(long pointer)
	{
		if (addresses.add(pointer) == false)
			return -1;
		return pointer;
	}
	
	public void psmFree(long address)
	{
		String key = null;
		addresses.remove(address);
		for (String cur : catalog.keySet())
		{
			if (catalog.get(cur) == address)
			{
				key = cur;
				break;
			}
		}
		if (key != null)
			catalog.remove(key);
	}
	
	public long getAddress(long pointer)
	{
		if (addresses.contains(pointer))
				return pointer;
		return -1;
	}

}