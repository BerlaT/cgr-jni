/*
 * init_global.c
 *
 *  Created on: 23 nov 2015
 *      Author: michele
 */


#include "init_global.h"
#include "jni_thread.h"
#include "shared.h"

#include <pthread.h>
#include <time.h>
#include <jni.h>
#include <locale.h>

#include "psm.h"
#include "utils.h"

#define WM_PSM_PARTITION 0
#define SDR_PSM_PARTITION 1

JavaVM *javaVM = NULL;
static time_t ONEreferenceTime = 0;
pthread_key_t nodeNum_key;
pthread_key_t jniEnv_key;
int initialized = 0;



int init_global()
{
	if (initialized == 0)
	{
		setlocale(LC_ALL, "en_US.UTF-8");
		pthread_key_create(&nodeNum_key, NULL);
		pthread_key_create(&jniEnv_key, NULL);
		pthread_key_create(&interfaceInfo_key, NULL);
		ONEreferenceTime = time(NULL);
		initialized = 1;
		return 1;
	}
	return 0;
}

void finalize_global()
{
	destroyIonPartitions();
	pthread_key_delete(nodeNum_key);
	pthread_key_delete(jniEnv_key);
	pthread_key_delete(interfaceInfo_key);
	initialized = 0;
}

void init_node()
{
	init_global();
	initIonWm();
	initIonSdr();
}

void destroy_node()
{
	destroyIonWm();
	destroyIonSdr();
}

jlong getNodeNum()
{
	jlong * result = (jlong *) pthread_getspecific(nodeNum_key);
	return * result;
}

void setNodeNum(jlong nodeNum_new)
{
	jlong * nodeNumPtr;
	if ((nodeNumPtr = pthread_getspecific(nodeNum_key)) == NULL)
	{
		nodeNumPtr = (jlong *) malloc(sizeof(jlong));
		pthread_setspecific(nodeNum_key, nodeNumPtr);
	}
	*nodeNumPtr = nodeNum_new;
}

time_t getONEReferenceTime()
{
	return ONEreferenceTime;
}

void setONEReferenceTime(time_t ref)
{
	if (ref == 0)
		ONEreferenceTime = time(NULL);
	else
		ONEreferenceTime = ref;
}
int getTimeFromONE()
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass oneClockClass = (*jniEnv)->FindClass(jniEnv, ONEClockClass);
	if (oneClockClass == NULL) //not using ONE environment
	{
		(*jniEnv)->ExceptionClear(jniEnv);
		return -1;
	}
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, oneClockClass, "getIntTime","()I");
	if (method == NULL) //not using ONE environment
	{
		(*jniEnv)->ExceptionDescribe(jniEnv);
		(*jniEnv)->ExceptionClear(jniEnv);

		return -1;
	}
	jint result = (*jniEnv)->CallStaticIntMethod(jniEnv, oneClockClass, method);
	return result;
}

time_t getSimulatedUTCTime()
{
	int timeFromOne = getTimeFromONE();
	if (timeFromOne < 0) // not using ONE environment
		return time(NULL);
	return (time_t) getONEReferenceTime() + timeFromOne;
}

time_t convertIonTimeToOne(time_t ionTime)
{
	if (getTimeFromONE() < 0) // not using ONE environment
		return ionTime;
	return ionTime - getONEReferenceTime();
}
