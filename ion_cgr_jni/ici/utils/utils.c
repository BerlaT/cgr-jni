/*
 * utils.c
 *
 *  Created on: 04 nov 2015
 *      Author: Michele Rodolfi University of Bologna michirod@gmail
 */

#include "utils.h"

#include "ion.h"
#include "sdr.h"
#include "rfx.h"
#include "shared.h"
#include "init_global.h"
#include "jni_thread.h"
#include <pthread.h>

#define PsmPartitionManagerClass "cgr_jni/psm/PsmPartitionManager"

pthread_key_t ionPartitions_key = -1;
typedef struct
{
	PsmPartition partition[2];
	uvast nodeNbr;
} IonPartitions;

static IonPartitions * setIonPartitions(IonPartitions * ionPartitions)
{
	if ((pthread_getspecific(ionPartitions_key)) == NULL)
	{
		pthread_setspecific(ionPartitions_key, ionPartitions);
	}
	return ionPartitions;
}
static IonPartitions * getIonPartitions()
{
	IonPartitions * ionP;
	ionP = pthread_getspecific(ionPartitions_key);
	if (ionP == NULL)
	{
		ionP = malloc(sizeof(IonPartitions));
		memset(ionP, 0, sizeof(IonPartitions));
		setIonPartitions(ionP);
	}
/*	char buf[256];
	sprintf(buf, "nodeNum = %d\nionPartition_key = %lu\nionPartition = %lx\n",
			getNodeNum(), ionPartitions_key, ionP);
	writeMemo(buf);
	*/return ionP;
}
static IonPartitions * initIonPartitions()
{
	IonPartitions * ionP = NULL;
	if (ionPartitions_key == -1)
		pthread_key_create(&ionPartitions_key, NULL);
	else
		ionP = getIonPartitions();
	if (ionP == NULL)
	{
		ionP = malloc(sizeof(IonPartitions));
		memset(ionP, 0, sizeof(IonPartitions));
		setIonPartitions(ionP);
	}
	return ionP;
}
static IonPartitions * updateIonPartitions()
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	IonPartitions * ionP = getIonPartitions();
	if (ionP == NULL)
	{
		putErrmsg("Cannot retrieve ion partitions", NULL);
		return NULL;
	}
	if (ionP->nodeNbr != getNodeNum())
	{
		ionP->nodeNbr = getNodeNum();
		(*jniEnv)->DeleteGlobalRef(jniEnv, ionP->partition[WM_PSM_PARTITION]);
		(*jniEnv)->DeleteGlobalRef(jniEnv, ionP->partition[SDR_PSM_PARTITION]);
		ionP->partition[WM_PSM_PARTITION] =
				(*jniEnv)->NewGlobalRef(jniEnv,
				getIonPsmPartition((jlong) ionP->nodeNbr, WM_PSM_PARTITION));
		ionP->partition[SDR_PSM_PARTITION] =
				(*jniEnv)->NewGlobalRef(jniEnv,
				getIonPsmPartition((jlong) ionP->nodeNbr, SDR_PSM_PARTITION));
		setIonPartitions(ionP);
	}
	return ionP;
}
void destroyIonPartitions()
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	IonPartitions * ionP = getIonPartitions();
	(*jniEnv)->DeleteGlobalRef(jniEnv, ionP->partition[WM_PSM_PARTITION]);
	(*jniEnv)->DeleteGlobalRef(jniEnv, ionP->partition[SDR_PSM_PARTITION]);
	free(ionP);
	pthread_key_delete(ionPartitions_key);
	ionPartitions_key = -1;
}

char * getIonvdbName();

PsmPartition getIonPsmPartition(long nodeNum, int partNum)
{
	//cached for optimization
	static jclass psmPartitionManagerClass = 0;
	static jmethodID method = 0;
	JNIEnv * jniEnv = getThreadLocalEnv();
	if (method == 0)
	{
		psmPartitionManagerClass =
				(*jniEnv)->FindClass(jniEnv, PsmPartitionManagerClass);
		method =
				(*jniEnv)->GetStaticMethodID(jniEnv, psmPartitionManagerClass,
						"getPartition","(JI)Lcgr_jni/psm/PsmPartition;");
	}
	jobject partition =
			(*jniEnv)->CallStaticObjectMethod(jniEnv,
					psmPartitionManagerClass, method, (jlong) nodeNum, partNum);
	if (partition == NULL)
	{
		method = (*jniEnv)->GetStaticMethodID(jniEnv,
				psmPartitionManagerClass, "newPartition",
				"(JI)Lcgr_jni/psm/PsmPartition;");
		partition = (*jniEnv)->CallStaticObjectMethod(jniEnv,
				psmPartitionManagerClass, method, (jlong) nodeNum, partNum);
	}
	return (PsmPartition) partition;
}

PsmPartition newIonPsmPartition(long nodeNum, int partNum)
{
	//cached for optimization
	static jclass psmPartitionManagerClass = 0;
	static jmethodID method = 0;
	JNIEnv * jniEnv = getThreadLocalEnv();
	if (method == 0)
	{
		psmPartitionManagerClass =
				(*jniEnv)->FindClass(jniEnv, PsmPartitionManagerClass);
		method = (*jniEnv)->GetStaticMethodID(jniEnv,
				psmPartitionManagerClass, "newPartition",
				"(JI)Lcgr_jni/psm/PsmPartition;");
	}
	jobject partition = (*jniEnv)->CallStaticObjectMethod(jniEnv,
			psmPartitionManagerClass, method, (jlong) nodeNum, partNum);
	return (PsmPartition) partition;
}

void eraseIonPsmPartition(long nodeNum, int partNum)
{
	//cached for optimization
	static jclass psmPartitionManagerClass = 0;
	static jmethodID method = 0;
	JNIEnv * jniEnv = getThreadLocalEnv();
	if (method == 0)
	{
		psmPartitionManagerClass =
				(*jniEnv)->FindClass(jniEnv, PsmPartitionManagerClass);
		method = (*jniEnv)->GetStaticMethodID(jniEnv,
				psmPartitionManagerClass, "erasePartition","(JI)V");
	}
	(*jniEnv)->CallStaticVoidMethod(jniEnv, psmPartitionManagerClass,
			method, (jlong) nodeNum, partNum);
}

void initIonWm()
{
	newIonPsmPartition((jlong) getNodeNum(), WM_PSM_PARTITION);
	initIonPartitions();
}
void destroyIonWm()
{
	eraseIonPsmPartition((jlong) getNodeNum(), WM_PSM_PARTITION);
}
PsmPartition getIonWm()
{
	//return getIonPsmPartition(getNodeNum(), WM_PSM_PARTITION);
	return updateIonPartitions()->partition[WM_PSM_PARTITION];
}

void initIonSdr()
{
	newIonPsmPartition((jlong) getNodeNum(), SDR_PSM_PARTITION);
	initIonPartitions();
}
void destroyIonSdr()
{
	eraseIonPsmPartition((jlong) getNodeNum(), SDR_PSM_PARTITION);
}
Sdr	getIonSdr()
{
	//return getIonPsmPartition(getNodeNum(), SDR_PSM_PARTITION);
	return updateIonPartitions()->partition[SDR_PSM_PARTITION];
}

IonDB * createIonDb(Sdr ionsdr, IonDB * iondbPtr)
{
	if (iondbPtr == NULL)
		iondbPtr = (IonDB*) malloc(sizeof(IonDB));
	memset((char *) iondbPtr, 0, sizeof(IonDB));
	iondbPtr->ownNodeNbr = getNodeNum();
	iondbPtr->productionRate = -1;	/*	Unknown.	*/
	iondbPtr->consumptionRate = -1;	/*	Unknown.	*/
	//limit = (sdr_heap_size(ionsdr) / 100) * (100 - ION_SEQUESTERED);

	/*	By default, let outbound ZCOs occupy up to
	 *	half of the available heap space, leaving
	 *	the other half for inbound ZCO acquisition.	*/

	//zco_set_max_heap_occupancy(ionsdr, limit/2, ZcoInbound);
	//zco_set_max_heap_occupancy(ionsdr, limit/2, ZcoOutbound);

	/*	By default, the occupancy ceiling is 50% more
	 *	than the outbound ZCO allocation.		*/

	//iondbBuf.occupancyCeiling = zco_get_max_file_occupancy(ionsdr, ZcoOutbound);
	//iondbBuf.occupancyCeiling += (limit/4);
	iondbPtr->contacts = sdr_list_create(ionsdr);
	iondbPtr->ranges = sdr_list_create(ionsdr);
	iondbPtr->maxClockError = 0;
	iondbPtr->clockIsSynchronized = 1;
	iondbPtr->contactLog[SENDER_NODE] = sdr_list_create(ionsdr);
	iondbPtr->contactLog[RECEIVER_NODE] = sdr_list_create(ionsdr);
    //memcpy(&iondbBuf.parmcopy, parms, sizeof(IonParms));
	//fprintf(stderr, "ionDb created: %d\n", getOwnNodeNbr());
	return iondbPtr;
}

void destroyIonDb(char *iondbName)
{
	Sdr sdr = getIonsdr();
	IonDB iondbBuf;
	Object iondbObj;
	iondbObj = sdr_find(sdr, iondbName, NULL);
	if (iondbObj == NULL)
		return;
	sdr_read(sdr, (char *) &iondbBuf, iondbObj, sizeof(IonDB));
	sdr_list_destroy(sdr, iondbBuf.contacts, NULL, NULL);
	sdr_list_destroy(sdr, iondbBuf.ranges, NULL, NULL);
	sdr_free(sdr, iondbObj);
	//fprintf(stderr, "ionDb destroyed: %d\n", getOwnNodeNbr());
}

IonVdb * createIonVdb(char * ionvdbName)
{
	IonVdb	*vdb = NULL;
	PsmAddress	vdbAddress;
	PsmAddress	elt;
	Sdr		sdr;
	PsmPartition	ionwm;
	IonDB		iondb;
	char * name = ionvdbName;

	/*	Attaching to volatile database.			*/

	ionwm = getIonwm();
	if (psm_locate(ionwm, name, &vdbAddress, &elt) < 0)
	{
		putErrmsg("Failed searching for vdb.", name);
		return NULL;
	}

	if (elt)
	{
		vdb = (IonVdb *) psp(ionwm, vdbAddress);
	}

	if (vdb != NULL)
		return vdb;
	/*	ION volatile database doesn't exist yet.	*/

	sdr = getIonsdr();
	CHKNULL(sdr_begin_xn(sdr));	/*	To lock memory.	*/
	vdbAddress = psm_zalloc(ionwm, sizeof(IonVdb));
	if (vdbAddress == 0)
	{
		sdr_exit_xn(sdr);
		putErrmsg("No space for volatile database.", name);
		return NULL;
	}

	vdb = (IonVdb *) psp(ionwm, vdbAddress);
	memset((char *) vdb, 0, sizeof(IonVdb));
	if ((vdb->nodes = sm_rbt_create(ionwm)) == 0
			|| (vdb->neighbors = sm_rbt_create(ionwm)) == 0
			|| (vdb->contactIndex = sm_rbt_create(ionwm)) == 0
			|| (vdb->rangeIndex = sm_rbt_create(ionwm)) == 0
			|| (vdb->timeline = sm_rbt_create(ionwm)) == 0
			|| (vdb->probes = sm_list_create(ionwm)) == 0
			|| (vdb->requisitions[0] = sm_list_create(ionwm)) == 0
			|| (vdb->requisitions[1] = sm_list_create(ionwm)) == 0
			|| psm_catlg(ionwm, name, vdbAddress) < 0)
	{
		sdr_exit_xn(sdr);
		putErrmsg("Can't initialize volatile database.", name);
		return NULL;
	}

	vdb->clockPid = ERROR;	/*	None yet.		*/
	sdr_read(sdr, (char *) &iondb, getIonDbObject(), sizeof(IonDB));
	vdb->deltaFromUTC = iondb.deltaFromUTC;
	sdr_exit_xn(sdr);	/*	Unlock memory.		*/

	//fprintf(stderr, "ionVdb created: %d\n", getOwnNodeNbr());
	return vdb;
}

static void	destroyIonNode(PsmPartition partition, PsmAddress eltData,
			void *argument)
{
	IonNode	*node = (IonNode *) psp(partition, eltData);

	sm_list_destroy(partition, node->embargoes, rfx_erase_data, NULL);
	psm_free(partition, eltData);
}

static void	dropVdb(PsmPartition wm, PsmAddress vdbAddress)
{
	IonVdb		*vdb;
	int		i;
	PsmAddress	elt;
	PsmAddress	nextElt;
	PsmAddress	addr;
	Requisition	*req;

	vdb = (IonVdb *) psp(wm, vdbAddress);

	/*	Time-ordered list of probes can simply be destroyed.	*/

	sm_list_destroy(wm, vdb->probes, rfx_erase_data, NULL);

	/*	Three of the red-black tables in the Vdb are
	 *	emptied and recreated by rfx_stop().  Destroy them.	*/

	sm_rbt_destroy(wm, vdb->contactIndex, NULL, NULL);
	sm_rbt_destroy(wm, vdb->rangeIndex, NULL, NULL);
	sm_rbt_destroy(wm, vdb->timeline, NULL, NULL);

	/*	cgr_stop clears all routing objects, so nodes and
	 *	neighbors themselves can now be deleted.		*/

	sm_rbt_destroy(wm, vdb->nodes, destroyIonNode, NULL);
	sm_rbt_destroy(wm, vdb->neighbors, rfx_erase_data, NULL);

	/*	Safely shut down the ZCO flow control system.		*/

	for (i = 0; i < 1; i++)
	{
		for (elt = sm_list_first(wm, vdb->requisitions[i]); elt;
				elt = nextElt)
		{
			nextElt = sm_list_next(wm, elt);
			addr = sm_list_data(wm, elt);
			req = (Requisition *) psp(wm, addr);
			sm_SemEnd(req->semaphore);
			psm_free(wm, addr);
			sm_list_delete(wm, elt, NULL, NULL);
		}
	}

	//zco_unregister_callback();
}

static void	ionDropVdb(char * vdbName)
{
	PsmPartition	wm = getIonwm();
	char		*ionvdbName = vdbName;
	PsmAddress	vdbAddress;
	PsmAddress	elt;

	if (psm_locate(wm, ionvdbName, &vdbAddress, &elt) < 0)
	{
		putErrmsg("Failed searching for vdb.", NULL);
		return;
	}

	if (elt)
	{
		dropVdb(wm, vdbAddress);	/*	Destroy Vdb.	*/
		if (psm_uncatlg(wm, ionvdbName) < 0)
		{
			putErrmsg("Failed uncataloging vdb.", NULL);
		}
		psm_free(wm, vdbAddress);
	}

}

void destroyIonVdb(char * ionvdbName)
{
	ionDropVdb(ionvdbName);
	//fprintf(stderr, "ionVdb destroyed: %d\n", getOwnNodeNbr());
}
