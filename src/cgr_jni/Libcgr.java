/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni;

import core.Message;

/**
 * This class defines static JNI methods implemented in the C file
 * jni_interface/cgr_jni_Libcgr.c.
 * 
 * @author Simone Pozza
 *
 */
public class Libcgr {

	static {
		System.loadLibrary("cgr_jni");
	}

	public static native int initializeNode(int nodeNum);

	public static native int finalizeNode(int nodeNum);

	public static native int readContactPlan(int nodeNum, String fileName);

	public static native int processLine(int nodeNum, String contactLine);

	public static native int cgrForward(int nodeNum, Message bundle, long terminusNodeNbr);

	public static native int genericTest(int nodeNum, Message message);

	public static native int finalizeGlobalMem();

}