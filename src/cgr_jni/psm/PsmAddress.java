/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.psm;

public class PsmAddress implements Comparable<PsmAddress>{
	private long value;		// the psm address
	private long pointer;	// the C pointer
	
	public PsmAddress(long pointer)
	{
		this.pointer = pointer;
	}
	public PsmAddress()
	{
		this(0);
	}
	
	public long getPointer() {
		return pointer;
	}

	public void setPointer(long pointer) {
		this.pointer = pointer;
	}
	
	public boolean equals(PsmAddress o)
	{
		return this.value == o.pointer;
	}
	
	@Override
	public int compareTo(PsmAddress o) {
		// TODO Auto-generated method stub
		long result =  this.pointer - o.pointer;
		if (result > 0)
			return 1;
		if (result < 0)
			return -1;
		return 0;
	}
	
}
