/*
 * ONEtoION_interface.c
 *
 *  Created on: 17 dic 2015
 *      Author: michele
 */

#include <jni.h>

#include "bpP.h"
#include "cgr.h"
#include "shared.h"
#include "utils.h"
#include "init_global.h"
#include "jni_thread.h"

#ifndef CGR_DEBUG
#define CGR_DEBUG	0
#endif

#define jMessageClass "core/Message"
#define jOuductClass "routing/ContactGraphRouting$Outduct"
#define ONEtoION_interfaceClass "cgr_jni/IONInterface"

pthread_key_t interfaceInfo_key;

struct InterfaceInfo_t {
	jobject currentMessage;
	Object outductList;
	Object protocol; // dummy entry to avoid null pointer errors
	int forwardResult;
};
typedef struct InterfaceInfo_t InterfaceInfo;

InterfaceInfo * interfaceInfo;

#if CGR_DEBUG == 1
static void	printCgrTraceLine(void *data, unsigned int lineNbr,
			CgrTraceType traceType, ...)
{
	va_list args;
	const char *text;

	va_start(args, traceType);

	text = cgr_tracepoint_text(traceType);
	printf("NODE %ld: ", getNodeNum());
	vprintf(text, args);
	putchar('\n');
	fflush(stdout);

	va_end(args);
}
#endif


static InterfaceInfo * setInterfaceInfo(InterfaceInfo * interfaceInfo)
{
	if ((pthread_getspecific(interfaceInfo_key)) == NULL)
	{
		pthread_setspecific(interfaceInfo_key, interfaceInfo);
	}
	return interfaceInfo;
}
static InterfaceInfo * getInterfaceInfo()
{
	return pthread_getspecific(interfaceInfo_key);
}

static uvast getMessageSenderNbr(jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "getMessageSenderNbr","(Lcore/Message;)J");
	jlong result = (*jniEnv)->CallStaticLongMethod(jniEnv,
			interfaceClass, method, message);
	return (uvast) result;
}
static uvast getMessageDestinationNbr(jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "getMessageDestinationNbr","(Lcore/Message;)J");
	jlong result = (*jniEnv)->CallStaticLongMethod(jniEnv,
			interfaceClass, method, message);
	return (uvast) result;
}
static unsigned int getMessageCreationTime(jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "getMessageCreationTime","(Lcore/Message;)J");
	jlong result = (*jniEnv)->CallStaticLongMethod(jniEnv,
			interfaceClass, method, message);
	return (unsigned int) (result + getONEReferenceTime());
}
/**
 * return message time to live (sec)
 */
static unsigned int getMessageTTL(jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "getMessageTTL","(Lcore/Message;)J");
	jlong result = (*jniEnv)->CallStaticLongMethod(jniEnv,
			interfaceClass, method, message);
	return (unsigned int) result;
}
/**
 * retrun bundle payload size
 */
static unsigned int getMessageSize(jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "getMessageSize","(Lcore/Message;)J");
	jlong result = (*jniEnv)->CallStaticLongMethod(jniEnv,
			interfaceClass, method, message);
	return (unsigned int) result;
}

static void updateMessageForfeitTime(jobject message, time_t forfeitTime)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	time_t oneTime;
	oneTime = (jlong) convertIonTimeToOne(forfeitTime);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "updateMessageForfeitTime","(Lcore/Message;J)V");
	(*jniEnv)->CallStaticVoidMethod(jniEnv, interfaceClass,
			method, message, (jlong) oneTime);
}

/*
 * FUNCTIONS USED BY OPPORTUNISTIC CGR
 */

static int getMessageXmitCopiesCount(jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass,
			"getMessageXmitCopiesCount","(Lcore/Message;)I");
	jint result = (*jniEnv)->CallStaticIntMethod(jniEnv, interfaceClass,
			method, message);
	return (int) result;
}

static int getMessageXmitCopies(jobject message, int copies[])
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass,	"getMessageXmitCopies","(Lcore/Message;)[I");
	jintArray result = (*jniEnv)->CallStaticObjectMethod(jniEnv,
			interfaceClass,	method, message);
	jsize len = (*jniEnv)->GetArrayLength(jniEnv, result);
	jint * elt = (*jniEnv)->GetIntArrayElements(jniEnv, result, 0);
	int i;
	for (i = 0; i < len; i++)
	{
		copies[i] = elt[i];
	}
	(*jniEnv)->ReleaseIntArrayElements(jniEnv, result, elt, 0);
	return (int) len;
}
/* [MM] */
static int getMessagePathCount(jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass,
			"getMessagePathCount","(Lcore/Message;)I");
	jint result = (*jniEnv)->CallStaticIntMethod(jniEnv, interfaceClass,
			method, message);
	return (int) result;
}

static int getMessagePath(jobject message, int copies[])
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass,	"getMessagePath","(Lcore/Message;)[I");
	jintArray result = (*jniEnv)->CallStaticObjectMethod(jniEnv,
			interfaceClass,	method, message);
	jsize len = (*jniEnv)->GetArrayLength(jniEnv, result);
	jint * elt = (*jniEnv)->GetIntArrayElements(jniEnv, result, 0);
	int i;
	for (i = 0; i < len; i++)
	{
		copies[i] = elt[i];
	}
	(*jniEnv)->ReleaseIntArrayElements(jniEnv, result, elt, 0);
	return (int) len;
}

static jdouble getMessageDlvConfidence(jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass,
			"getMessageDlvConfidence","(Lcore/Message;)D");
	jdouble result = (*jniEnv)->CallStaticIntMethod(jniEnv, interfaceClass,
			method, message);
	return (jdouble) result;
}

static void setMessageXmitCopies(jobject message, int copies[], int len)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass,
			"setMessageXmitCopies","(Lcore/Message;[I)V");
	jintArray array = (*jniEnv)->NewIntArray(jniEnv, len);
	/*
	jint * cur = (*jniEnv)->GetIntArrayElements(jniEnv, array, NULL);
	int i;
	for (i = 0; i < len; i++)
	{
		cur[i] = copies[i];
	}
	(*jniEnv)->ReleaseIntArrayElements(jniEnv, array, cur, 0, copies);
	*/
	(*jniEnv)->SetIntArrayRegion(jniEnv, array, 0, len, copies);
	(*jniEnv)->CallStaticVoidMethod(jniEnv, interfaceClass,
			method, message, array);
	array = 0;
}

static void setMessageDlvConfidence(jobject message, jdouble dlvConf)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass,
			"setMessageDlvConfidence","(Lcore/Message;D)V");
	(*jniEnv)->CallStaticVoidMethod(jniEnv, interfaceClass,
			method, message, (jdouble) dlvConf);
}

static void getXmitCopiesDlvConficence(jobject message, Bundle * bundle)
{
	bundle->dlvConfidence = (jdouble) getMessageDlvConfidence(message);
	bundle->xmitCopiesCount = getMessageXmitCopiesCount(message);
	getMessageXmitCopies(message, bundle->xmitCopies);
}

static void updateXmitCopiesDlvConfidence(jobject message, Bundle * bundle)
{
	setMessageDlvConfidence(message,(jdouble) bundle->dlvConfidence);
	setMessageXmitCopies(message, bundle->xmitCopies,
			bundle->xmitCopiesCount);
}

/**
 * return true if the outduct is blocked
 * (in ONE this should return always false)
 */
static bool_t isOutductBlocked(jobject jOutduct)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "isOutductBlocked",
			"(Lrouting/ContactGraphRouter$Outduct;)Z");
	jboolean result = (*jniEnv)->CallStaticBooleanMethod(jniEnv,
			interfaceClass, method, jOutduct);
	return (bool_t) result;
}
/**
 * return the outduct name
 * in ONE, the outduct name is a property of the class Outduct
 * and the name is the nodeNbr associated to the outduct.
 * outductName must be initialized.
 * Returns a pointer to outductName
 */
static char * getOutductName(jobject jOutduct, char * outductName)
{
	if (outductName == NULL)
		return NULL;
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "getOutductName",
			"(Lrouting/ContactGraphRouter$Outduct;)Ljava/lang/String;");
	jstring result = (*jniEnv)->CallStaticObjectMethod(jniEnv,
			interfaceClass, method, jOutduct);
	const char * nativeString = (*jniEnv)->GetStringUTFChars(jniEnv,
			result, NULL);
	strcpy(outductName, nativeString);
	(*jniEnv)->ReleaseStringUTFChars(jniEnv, result, nativeString);
	return outductName;
}
/**
 * arbitrary defined
 */
static unsigned int getMaxPayloadLen(jobject jOutduct)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "getMaxPayloadLen",
			"(Lrouting/ContactGraphRouter$Outduct;)I");
	jint result = (*jniEnv)->CallStaticIntMethod(jniEnv,
			interfaceClass, method, jOutduct);
	return (unsigned int) result;
}
/**
 * return the java Outduct object of the node localNodeNbr
 * to the node toNodeNbr
 */
static jobject getONEOutductToNode(uvast localNodeNbr, uvast toNodeNbr)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "getONEOutductToNode",
			"(JJ)Lrouting/ContactGraphRouter$Outduct;");
	jobject result = (*jniEnv)->CallStaticObjectMethod(jniEnv,
			interfaceClass, method, (jlong) localNodeNbr, (jlong) toNodeNbr);
	return result;
}

/**
 * Returns the total number of bytes already enqueued on this outduct
 */
static jlong getOutductTotalEnqueuedBytes(jobject jOutduct)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "getOutductTotalEnququedBytes",
			"(Lrouting/ContactGraphRouter$Outduct;)J");
	jlong result = (*jniEnv)->CallStaticLongMethod(jniEnv,
			interfaceClass, method, jOutduct);
	return (jlong) result;
}

static int cloneMessage(uvast localNodeNbr, jobject jMessage)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass,
			"cloneMessage","(JLcore/Message;)V");
	jint result = (*jniEnv)->CallStaticIntMethod(jniEnv, interfaceClass,
			method, (jlong) localNodeNbr, jMessage);
	return result;
}

/**
 * Enqueues a message into an outduct
 */
static int insertBundleIntoOutduct(uvast localNodeNbr,
		jobject message, uvast toNodeNbr)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "insertBundleIntoOutduct","(JLcore/Message;J)I");
	jint result = (*jniEnv)->CallStaticIntMethod(jniEnv,
			interfaceClass, method, (jlong) localNodeNbr, message, (jlong) toNodeNbr);
	return (int) result;
}
/**
 * insert a message into local limbo
 */
static int insertBundleIntoLimbo(uvast localNodeNbr, jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv,
			ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "insertBundleIntoLimbo","(JLcore/Message;J)I");
	jint result = (*jniEnv)->CallStaticIntMethod(jniEnv,
			interfaceClass, method, (jlong) localNodeNbr, message);
	return (int) result;
}

/*
 * FUNCTIOINS USED BY PRIORITY CGR
 */

static int getMessagePriority(jobject message)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass =(*jniEnv)->FindClass(jniEnv,ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass, "getMessagePriority","(Lcore/Message;)I" );
	jint result = (*jniEnv)->CallStaticIntMethod(jniEnv,interfaceClass, method, message);
	return (int) result;
}

static jlong getOutductBulkBacklog(jobject jOutduct)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv, ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass, "getOutductBulkBacklog","(Lrouting/ContactGraphRouter$Outduct;)J");
	jlong result = (*jniEnv)->CallStaticLongMethod(jniEnv, interfaceClass, method, jOutduct);
	return (jlong) result;
}

static jlong getOutductNormBacklog(jobject jOutduct)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv, ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass, "getOutductNormalBacklog","(Lrouting/ContactGraphRouter$Outduct;)J");
	jlong result = (*jniEnv)->CallStaticLongMethod(jniEnv, interfaceClass, method, jOutduct);
	return (jlong) result;
}

static jlong getOutductExpBacklog(jobject jOutduct)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass = (*jniEnv)->FindClass(jniEnv, ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass, "getOutductExpeditedBacklog","(Lrouting/ContactGraphRouter$Outduct;)J");
	jlong result = (*jniEnv)->CallStaticLongMethod(jniEnv, interfaceClass, method, jOutduct);
	return (jlong) result;
}

static int callManageOverbooking(uvast localNodeNbr,uvast proximateNodeNbr,jdouble overbooked,jdouble protect)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass =(*jniEnv)->FindClass(jniEnv,ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv, interfaceClass, "manageOverbooking","(JJDD)I" );
	jint result = (*jniEnv)->CallStaticIntMethod(jniEnv,interfaceClass, method, (jlong) localNodeNbr,(jlong) proximateNodeNbr,overbooked,protect);
	return (int) result;
}

int one_manage_overbooking(double overbooked,double protect,Bundle *lastSent)
{
	uvast localNodeNbr, proximateNodeNbr;
	int priority;

	priority= COS_FLAGS(lastSent->bundleProcFlags) & 0x03;
	if(priority==0 || overbooked == 0.0)
		return 0; //no overbooking

	localNodeNbr = (jlong) getNodeNum();
	proximateNodeNbr = (jlong) interfaceInfo->forwardResult;
	return callManageOverbooking((jlong) localNodeNbr,(jlong) proximateNodeNbr,(jdouble) overbooked, (jdouble) protect);

}

/**
 * Convert a java Message object to an ION Bundle
 */
void ion_bundle(Bundle * bundle, jobject message)
{
	memset(bundle, 0, sizeof(Bundle));
	bundle->returnToSender = 0; /* [MM] set to 0 */
	bundle->clDossier.senderNodeNbr = getMessageSenderNbr(message);
	/*
	bundle->expirationTime =
	 	 	 getMessageCreationTime(message) +getMessageTTL(message);
	 */
	bundle->expirationTime = getSimulatedUTCTime() + getMessageTTL(message);
	bundle->destination.c.nodeNbr = getMessageDestinationNbr(message);
	bundle->destination.c.serviceNbr = 0;
	bundle->destination.cbhe = 1;
	bundle->payload.length = getMessageSize(message);
	bundle->bundleProcFlags = BDL_DOES_NOT_FRAGMENT;

	int pri = getMessagePriority(message);
	if(pri==1)
		 bundle->bundleProcFlags += 128;
	else if(pri==2)
		 bundle->bundleProcFlags += 256;

	bundle->extendedCOS.ordinal = 0;
	bundle->extendedCOS.flags = 0;
	bundle->dictionaryLength = 0;
	bundle->extensionsLength[PRE_PAYLOAD] = 0;
	bundle->extensionsLength[POST_PAYLOAD] = 0;
#ifdef OPPORTUNISTIC_ROUTING

	/* [MM] added */
	//bundle->pathLen = getMessagePathCount(message);
	//bundle->path = (int*) malloc(sizeof(int)*bundle->pathLen);
	//getMessagePath(message, bundle->path);
	/*for(int i=0; i<bundle->pathLen; i++){
		printf("%d\n", bundle->path[i]);
	}*/

	getXmitCopiesDlvConficence(message, bundle);
#endif
}

/**
 * Convert a java Outduct object into an ION Outduct
 */
void ion_outduct(Outduct * duct, jobject jOutduct)
{
	jlong totEnqueued;
	char buf[MAX_CL_DUCT_NAME_LEN];
	ClProtocol prot;
	memset(duct, 0, sizeof(Outduct));
	duct->blocked = isOutductBlocked(jOutduct);
	duct->maxPayloadLen = getMaxPayloadLen(jOutduct);
	totEnqueued = getOutductTotalEnqueuedBytes(jOutduct);
	loadScalar(&(duct->stdBacklog), totEnqueued);
	strncpy(duct->name, getOutductName(jOutduct, buf), MAX_CL_DUCT_NAME_LEN);
	if (interfaceInfo->protocol == NULL)
	{
		interfaceInfo->protocol =
				sdr_malloc(getIonsdr(), sizeof(ClProtocol));
		memset(&prot, 0, sizeof(ClProtocol));
		sdr_write(getIonsdr(), interfaceInfo->protocol,
				(char*) &prot, sizeof(ClProtocol));
	}
	duct->protocol = interfaceInfo->protocol;

	jlong bulkBacklog = (jlong) getOutductBulkBacklog(jOutduct);
	jlong normBacklog = (jlong) getOutductNormBacklog(jOutduct);
	jlong expBacklog = (jlong) getOutductExpBacklog(jOutduct);
	loadScalar(&(duct->bulkBacklog), (jlong) bulkBacklog);
	loadScalar(&(duct->stdBacklog), (jlong) normBacklog);
	loadScalar(&(duct->urgentBacklog), (jlong) expBacklog);
}

void init_ouduct_list()
{
	interfaceInfo->outductList = sdr_list_create(getIonsdr());
}
void wipe_outduct_list()
{
	Sdr sdr = getIonsdr();
	Object outductElt;
	Object outductObj;
	if (interfaceInfo->outductList != NULL)
	{
		outductElt = sdr_list_first(sdr, interfaceInfo->outductList);
		while (outductElt != NULL)
		{
			outductObj = sdr_list_data(sdr, outductElt);
			sdr_free(sdr, outductObj);
			outductElt = sdr_list_next(sdr, outductElt);
		}
		sdr_list_destroy(sdr, interfaceInfo->outductList, NULL, NULL);
	}
	interfaceInfo->outductList = NULL;
}

/**
 * get the outduct to nodeNbr
 * retrieves information from ONE runtime and sets directive->outductElt
 * plans should be NULL.
 * Returns 0 if no directive can be found.
 * Returns 1 if success
 */
int	getONEDirective(uvast nodeNbr, Object plans, Bundle *bundle,
			FwdDirective *directive)
{
	jobject jOutduct;
	Outduct outduct;
	Object outductObj;
	Object outductElt;
	jOutduct = getONEOutductToNode(getNodeNum(), nodeNbr);
	char outductName[MAX_CL_DUCT_NAME_LEN];
	if (jOutduct != NULL)
	{
		// init outduct list if not yet initialized
		if (interfaceInfo->outductList == NULL)
			init_ouduct_list();
		getOutductName(jOutduct, outductName);
		if ((outductElt = sdr_find(getIonsdr(), outductName, NULL)) == 0)
		{
			// convert java outduct object into ION Outduct struct
			ion_outduct(&outduct, jOutduct);
			// init sdr outduct object
			outductObj = sdr_malloc(getIonsdr(), sizeof(Outduct));
			sdr_write(getIonsdr(), outductObj,
					(char*)&outduct, sizeof(Outduct));
			// put outduct into sdr list
			outductElt = sdr_list_insert_first(getIonsdr(),
					interfaceInfo->outductList, outductObj);
			sdr_catlg(getIonsdr(), outductName, 0, outductElt);
		}
		directive->outductElt = outductElt;
		return 1;
	}
	return 0;
}

/**
 * Entry point for Contact Graph Router library
 * Tries to find the best route to terminusNodeNbr using libcgr.
 * If a feasible route is found, the bundle is enqueued into an
 * outduct using bpEnqueONE().
 * If not, no operations are performed.
 * Returns the nodeNbr of the proximate node that the
 * bundle has been enqueued to
 * or 0 if no proximate nodes have been found
 * or -1 in case of any error.
 */
int cgrForwardONE(jobject bundleONE, jlong terminusNodeNbr)
{
	Bundle *bundle;
	Object bundleObj;
	// this value will never be read but it is needed to
	// pass the null check in cgr_forward()
	Object plans = (Object) 42; 	int result = -1;
	CgrTrace * trace = NULL;
#if CGR_DEBUG == 1
	CgrTrace traceBuf;
	traceBuf.fn = printCgrTraceLine;
	traceBuf.data = NULL;
	trace = &traceBuf;
#endif
	interfaceInfo = malloc(sizeof(InterfaceInfo));
	interfaceInfo->forwardResult = 0;
	interfaceInfo->currentMessage = bundleONE;
	interfaceInfo->outductList = NULL;
	interfaceInfo->protocol = NULL;
	setInterfaceInfo(interfaceInfo);
	bundle = malloc(sizeof(Bundle));
	ion_bundle(bundle, bundleONE);
	bundleObj = sdr_malloc(getIonsdr(), sizeof(Bundle));
	sdr_write(getIonsdr(), bundleObj, (char*)bundle, sizeof(Bundle));
	result = cgr_forward(bundle, bundleObj, (uvast) terminusNodeNbr,
			plans, getONEDirective, trace);
	wipe_outduct_list();
	if (result >= 0)
		result = interfaceInfo->forwardResult;
	sdr_free(getIonsdr(), bundleObj);
	sdr_free(getIonsdr(), interfaceInfo->protocol);
	free(interfaceInfo);
	free(bundle);
	return result;
}

/**
 * Enqueues the bundle into an outduct.
 * This also update the bundle forfeit time.
 */
int bpEnqueONE(FwdDirective *directive, Bundle *bundle, Object bundleObj)
{
	uvast localNodeNbr, proximateNodeNbr;
	Object ductAddr;
	Outduct outduct;
	BpEvent forfeitEvent;
	sdr_read(getIonsdr(), (char*) &forfeitEvent,
			bundle->overdueElt, sizeof(BpEvent));
	updateMessageForfeitTime(interfaceInfo->currentMessage,
			forfeitEvent.time);
	localNodeNbr = (jlong) getNodeNum();
	ductAddr = sdr_list_data(getIonsdr(), directive->outductElt);
	sdr_read(getIonsdr(), (char*)&outduct, ductAddr, sizeof(Outduct));
	proximateNodeNbr = (jlong) atol(outduct.name);
	insertBundleIntoOutduct((jlong) localNodeNbr, interfaceInfo->currentMessage,
			(jlong) proximateNodeNbr);
	interfaceInfo->forwardResult = (jlong) proximateNodeNbr;
	updateXmitCopiesDlvConfidence(interfaceInfo->currentMessage, bundle);
	return 0;
}

int bpLimboONE(Bundle *bundle, Object bundleObj)
{
	updateXmitCopiesDlvConfidence(interfaceInfo->currentMessage, bundle);
	return 0;
}

int bpCloneONE(Bundle *oldBundle, Bundle *newBundle)
{
	int result;
	memcpy(newBundle, oldBundle, sizeof(Bundle));
	newBundle->ductXmitElt = NULL;
	result = cloneMessage((jlong) getNodeNum(), interfaceInfo->currentMessage);
	return result;
}

int sendDiscoveryInfoToNeighbor
(uvast neighborNode, uvast fromNode, uvast toNode, time_t fromTime,
		time_t toTime, unsigned int xmitRate)
{
	JNIEnv * jniEnv = getThreadLocalEnv();
	jclass interfaceClass =
			(*jniEnv)->FindClass(jniEnv, ONEtoION_interfaceClass);
	jmethodID method = (*jniEnv)->GetStaticMethodID(jniEnv,
			interfaceClass, "sendDiscoveryInfo","(JJJJJI)V");
	(*jniEnv)->CallStaticObjectMethod(jniEnv, interfaceClass, method,
			(jlong) neighborNode, (jlong) fromNode, (jlong) toNode, (jlong) fromTime, (jlong) toTime, xmitRate);
	return 0;
}

int testMessage(jobject message)
{
	Bundle *bundle;
	Object bundleObj;
	interfaceInfo->currentMessage = message;
	bundle = malloc(sizeof(Bundle));
	ion_bundle(bundle, message);
	bundleObj = sdr_malloc(getIonsdr(), sizeof(Bundle));
	sdr_write(getIonsdr(), bundleObj, (char*) bundle, sizeof(Bundle));
	free(bundle);
	return 0;
}
