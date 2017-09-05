/*
 * init_global.h
 *
 *  Created on: 23 nov 2015
 *      Author: Michele Rodolfi University of Bologna michirod@gmail.com
 */

#ifndef JNI_INCLUDE_INIT_GLOBAL_H_
#define JNI_INCLUDE_INIT_GLOBAL_H_

#include <time.h>
#include <pthread.h>
#include <jni.h>

#define ONEClockClass "core/SimClock"

int init_global();
void finalize_global();
void init_node();
void destroy_node();
jlong getNodeNum();
void setNodeNum(jlong nodeNum_new);
time_t getONEReferenceTime();
void setONEReferenceTime(time_t time);
int getTimeFromONE();
time_t getSimulatedUTCTime();
time_t convertIonTimeToOne(time_t ionTime);

#endif /* JNI_INCLUDE_INIT_GLOBAL_H_ */
