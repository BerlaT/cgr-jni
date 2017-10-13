/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.test;

import cgr_jni.Libcgr;
import core.Message;

public class CGRouter {
	
	private int nodeNum;
	
	public CGRouter()
	{
		
	}
	
	public int getNodeNum()
	{
		return this.nodeNum;
	}

	public void init(int host)
	{
		this.nodeNum = host;
		Libcgr.initializeNode(host);
	}
	
	public void readContactPlan(String fileName)
	{
		Libcgr.readContactPlan(nodeNum, fileName);
	}
	
	public void processLine(String line)
	{
		Libcgr.processLine(nodeNum, line);
	}
	
	public void testMessage()
	{
		Message message = new Message(null, null, "pippo", 100);
		int result = Libcgr.genericTest(nodeNum, message);
		assert(result == 0);
	}
}
