/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.psm;

import java.util.HashMap;

public class PsmPartitionManager {
	
	private static HashMap<Long, PsmPartitionNodeManager> nodes = 
			new HashMap<>();
	
	public static PsmPartition newPartition(long nodeNum, int partNum)
	{
		PsmPartitionNodeManager nodeMan;
		if (! nodes.keySet().contains(nodeNum))
		{
			nodeMan = new PsmPartitionNodeManager(nodeNum);
			nodes.put(nodeNum, nodeMan);
		}
		else
		{
			nodeMan = nodes.get(nodeNum);
		}
		return nodeMan.newPartition(partNum);
	}
	
	public static PsmPartition getPartition(long nodeNum, int partNum)
	{
		PsmPartitionNodeManager nodeMan = nodes.get(nodeNum);
		if (nodeMan != null)
			return nodeMan.getPartition(partNum);
		else
			return null;
	}
	
	public static void erasePartition(long nodeNum, int partNum)
	{
		PsmPartitionNodeManager nodeMan = nodes.get(nodeNum);
		if (nodeMan != null)
		{
			nodeMan.removePartition(partNum);
		}
	}

}