/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.psm;

import java.util.HashMap;

public class PsmPartitionNodeManager {
	private long nodeNum;
	private HashMap<Integer, PsmPartition> partitions = new HashMap<>();
	
	public PsmPartitionNodeManager(long nodeNum)
	{
		this.nodeNum = nodeNum;
	}
	
	public long getNodeNum() {
		return nodeNum;
	}
	
	public PsmPartition newPartition(int partNum) {
		PsmPartition partition = new PsmPartition(partNum);
		addPartition(partition);
		return partition;
	}

	public PsmPartition getPartition(int partNum) {
		return partitions.get(partNum);
	}
	
	public void addPartition(PsmPartition partition)
	{
		partitions.put(partition.getId(), partition);
	}
	
	public void removePartition(int partNum)
	{
		partitions.remove(partNum);
	}
}
