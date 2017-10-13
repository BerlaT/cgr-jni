/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

import core.DTNHost;
import core.DTNSim;
import core.SimScenario;

public class Utils {
	
	protected static Utils instance = null;
	protected Collection<DTNHost> hostsReference;
	protected DTNHost[] hostsArray;
	
	static {
		DTNSim.registerForReset(Utils
				.class.getCanonicalName());
		}
	public static void reset()
	{
		instance.hostsReference.clear();
		instance.hostsArray = null;
		instance = null;
	}
	
	/**
	 * Default constructor.
	 * Get hosts from SimScenario
	 */
	protected Utils()
	{
		this(null);
	}
	protected Utils(Collection<DTNHost> hosts)
	{
		if (hosts == null) // Default value
		{
			hostsReference = SimScenario.getInstance().getHosts();
		}
		else
		{
			hostsReference = hosts;
		}
		hostsArray = new DTNHost[hostsReference.size()];
		hostsArray = hostsReference.toArray(hostsArray);
		Arrays.sort(hostsArray, new Comparator<DTNHost>() {

			@Override
			public int compare(DTNHost o1, DTNHost o2) {
				return o1.getAddress() - o2.getAddress();
			}
		});
	}
	
	public static Utils init(Collection<DTNHost> hosts)
	{
		instance = new Utils(hosts);
		return instance;
	}
	
	public static Utils init()
	{
		instance = new Utils();
		return instance;
	}
	
	public static Utils getInstance()
	{
		if (instance == null)
			instance = new Utils();
		return instance;
	}
	
	protected Collection<DTNHost> getAllNodesFromEnv()
	{
		return hostsReference;
	}
	
	public static Collection<DTNHost> getAllNodes()
	{
		return getInstance().getAllNodesFromEnv();
	}

	public static DTNHost getHostFromNumber(long nodeNbr)
	{
		/*
		for(DTNHost host : getAllNodes()){
			if(host.getAddress() == nodeNbr){
				return host;
			}
		}
		return null;
		*/
		return getInstance().hostsArray[(int) (nodeNbr -1)];
	}

}