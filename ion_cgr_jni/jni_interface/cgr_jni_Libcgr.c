/*
 *  cgr_Libcgr.c
 *
 *  Created on: 07 dic 2015
 *      Author: michele
 */

#include <jni.h>
#include <stdio.h>

#include "cgr.h"
#include "ion.h"
#include "ionadmin.h"
#include "platform.h"
#include "shared.h"
#include "init_global.h"
#include "jni_thread.h"
#include "ONEtoION_interface.h"

//JavaVM *javaVM = NULL;

JNIEXPORT jint JNICALL Java_cgr_1jni_Libcgr_initializeNode
	(JNIEnv *env, jclass thisObj, jint nodeNum)
{
	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	setNodeNum(nodeNum);
	init_node();
	result = ionInitialize(NULL, (uvast) nodeNum);
	if (result == 0)
		cgr_start();
	else result = -10;
	fflush(stdout);
	return result;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libcgr_finalizeNode
	(JNIEnv *env, jclass thisObj, jint nodeNum)
{
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	setThreadLocalEnv(env);
	setNodeNum(nodeNum);
	cgr_stop();
	ionTerminate();
	destroy_node();
	return 0;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libcgr_readContactPlan
	(JNIEnv *env, jclass thisObj, jint nodeNum, jstring fileName)
{
	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum = (uvast) nodeNum;
	setNodeNum(unodeNum);
	const char *nativeString = (*env)->GetStringUTFChars(env, fileName, 0);
	result = runIonadmin(nativeString);
	fflush(stdout);
	return result;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libcgr_processLine
	(JNIEnv *env, jclass thisObj, jint nodeNum, jstring line)
{
	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum = (uvast) nodeNum;
	setNodeNum(unodeNum);
	const char *nativeString = (*env)->GetStringUTFChars(env, line, 0);
	result = processLine(nativeString, strlen(nativeString));
	fflush(stdout);
	return result;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libcgr_cgrForward
	(JNIEnv *env, jclass thisObj, jint nodeNum, jobject message, jlong terminusNodeNbr)
{
	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum = (uvast) nodeNum;
	setNodeNum(unodeNum);
	result = cgrForwardONE(message, terminusNodeNbr);
	fflush(stdout);
	return result;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libcgr_genericTest
	(JNIEnv *env, jclass thisObj, jint nodeNum, jobject message)
{
	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum = (uvast) nodeNum;
	setNodeNum(unodeNum);
	result = testMessage(message);
	return result;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libcgr_finalizeGlobalMem
  (JNIEnv *env, jclass thisObj)
{
	setThreadLocalEnv(env);
	finalize_global();
	return 0;
}
