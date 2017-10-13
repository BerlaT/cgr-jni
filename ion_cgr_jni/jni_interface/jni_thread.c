/*
 * jni_threads.c
 *
 *  Created on: 24 nov 2015
 *      Author: michele
 */
#include "jni_thread.h"

#include <pthread.h>
#include <jni.h>
#include <stdlib.h>
#include <string.h>

#include "shared.h"

pthread_key_t jniEnv_key;

static void * __jni_thread_run(void * arg)
{
	JNIEnv * localEnv;
	(*javaVM)->AttachCurrentThread(javaVM, &localEnv, NULL);
	setThreadLocalEnv(localEnv);
	Passing * passing = (Passing *)arg;
	void * res = passing->function(passing->args);
	free(passing);
	(*javaVM)->DetachCurrentThread(javaVM);
	return res;
}

int jni_thread_create(pthread_t *newthread, pthread_attr_t *attr, void *(*__start_routine) (void *), void * arg)
{
	Passing * passing = (Passing *) malloc(sizeof(Passing));
	passing->function = __start_routine;
	passing->args = arg;
	pthread_create(newthread, attr, __jni_thread_run, passing);
	return 0;
}
int jni_thread_join(pthread_t __th, void **__thread_return)
{
	return pthread_join(__th, __thread_return);
}


JNIEnv * getThreadLocalEnv()
{
	JNIEnv * result = pthread_getspecific(jniEnv_key);
	return result;
}

void setThreadLocalEnv(JNIEnv * localEnv)
{
	pthread_setspecific(jniEnv_key, localEnv);
}
