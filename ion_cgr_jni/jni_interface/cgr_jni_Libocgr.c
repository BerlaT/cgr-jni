/*
 * cgr_jni_Libocgr.c
 *
 *  Created on: 08 feb 2016
 *      Author: michele
 */



#include <jni.h>
#include <stdio.h>

#include "cgr_jni_Libocgr.h"
#include "platform.h"
#include "shared.h"
#include "init_global.h"
#include "jni_thread.h"
#include "chsim.h"


JNIEXPORT jint JNICALL Java_cgr_1jni_Libocgr_predictContacts
(JNIEnv *env, jclass thisObj, jint nodeNum)
{
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum = (uvast) nodeNum;
	setNodeNum(unodeNum);
	predictContacts();
	fflush(stdout);
	return 0;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libocgr_exchangeCurrentDiscoveredContatcs
(JNIEnv *env, jclass thisObj, jint nodeNum1, jint nodeNum2)
{
	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum1 = (uvast) nodeNum1;
	uvast unodeNum2 = (uvast) nodeNum2;
	setNodeNum(unodeNum1);
	exchangeCurrentDiscoveredContacts(unodeNum1,unodeNum2);
	fflush(stdout);
	return 0;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libocgr_exchangeContactHistory
(JNIEnv *env, jclass thisObj, jint nodeNum1, jint nodeNum2)
{
	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum1 = (uvast) nodeNum1;
	uvast unodeNum2 = (uvast) nodeNum2;
	setNodeNum(unodeNum1);
	exchangeContactHistory(unodeNum1,unodeNum2);
	fflush(stdout);
	return 0;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libocgr_contactDiscoveryAquired
  (JNIEnv *env, jclass thisObj, jint nodeNum, jint neighborNum, jint rate)
{
	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum = (uvast) nodeNum;
	uvast uneighborNum = (uvast) neighborNum;
	setNodeNum(unodeNum);
	insertNewDiscoveredContact(uneighborNum, (unsigned int) rate);
	fflush(stdout);
	return 0;
}

JNIEXPORT jint JNICALL Java_cgr_1jni_Libocgr_contactDiscoveryLost
(JNIEnv *env, jclass thisObj, jint nodeNum, jint neighborNum)
{
	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum = (uvast) nodeNum;
	uvast uneighborNum = (uvast) neighborNum;
	setNodeNum(unodeNum);
	contactLost(uneighborNum);
	fflush(stdout);
	return 0;
}
JNIEXPORT jint JNICALL Java_cgr_1jni_Libocgr_applyDiscoveryInfos
  (JNIEnv *env, jclass thisObj, jint nodeNum, jlong fromNode,
		  jlong toNode, jlong fromTime, jlong toTime, jint xmitRate)
{

	jint result;
	if (javaVM == NULL)
		(*env)->GetJavaVM(env, &javaVM);
	init_global();
	setThreadLocalEnv(env);
	uvast unodeNum = (uvast) nodeNum;
	setNodeNum(unodeNum);
	applyDiscoveryInfo((uvast) fromNode, (uvast) toNode,
			(uvast) fromTime, (uvast) toTime, (int) xmitRate);
	fflush(stdout);
	return 0;
}

