/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni.test;

public class JNITest {
	
	/**
	 * Dichiarazione dei metodi nativi
	 * Carico anche le librerie C che mi servono.
	 * normal: libnormal.so contiene il codice nativo e la libreria che accede a java da C
	 * test: libtest.so contiene le funzioni di accesso a C da java
	 * (vedi Makefile)
	 */
	static {
		 System.loadLibrary("test");
//		 System.loadLibrary("normal");
//		 System.loadLibrary("test");
//		 System.loadLibrary("cgr_jni");
	}

	public static native int doSomething(long node, String string);
	public static native int doSomethingWithLists(long node, String[] strings);
	public static native int doSomethingWithPsm(long node, String[] strings);
}