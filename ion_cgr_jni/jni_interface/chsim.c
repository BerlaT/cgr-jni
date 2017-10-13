/*
 * chsim.c
 *
 *  Created on: 08 feb 2016
 *      Author: michele
 */

#include <sdr.h>
#include <rfx.h>
#include <utils.h>
#include <lyst.h>

#include <init_global.h>
#include <ONEtoION_interface.h>

static void	freeLystContents(LystElt elt, void *userdata)
{
	free(lyst_data(elt));
}

static int findDiscoveredContacts(uvast localNodeNbr, Lyst list)
{
	uvast currentNodeNum = getNodeNum();
	setNodeNum(localNodeNbr);
	Sdr sdr = getIonsdr();
	Object iondbObj = getIonDbObject();
	IonDB iondb;
	IonContact buf;
	IonContact * contact;
	Object elt = NULL;
	int count = 0;
char buf1[255], buf2[255];
	sdr_read(sdr, (char*) &iondb, iondbObj, sizeof(IonDB));
	elt = sdr_list_first(sdr, iondb.contacts);
	while (elt != NULL)
	{
		sdr_read(sdr, (char*) &buf,
				sdr_list_data(sdr, elt), sizeof(IonContact));
		if (buf.discovered == 1)
		{
			contact = malloc(sizeof(IonContact));
			memcpy(contact, &buf, sizeof(IonContact));
			count++;
			lyst_insert_last(list, contact);
#ifdef DEBUG_PRINTS
writeTimestampLocal(contact->fromTime, buf1);
writeTimestampLocal(contact->toTime, buf2);
printf ("Found discovered contact on %d, from %d to %d start %s end "
"%s.\n", localNodeNbr, contact->fromNode, contact->toNode, buf1, buf2);
#endif
		}
		elt = sdr_list_next(sdr, elt);
	}
	setNodeNum(currentNodeNum);
	return count;
}

static void copyDiscoveredContacts(Lyst from, int toNode)
{
	uvast currentNodeNum = getNodeNum();
	IonContact * contact;
	setNodeNum(toNode);
	LystElt elt = lyst_first(from);
	PsmAddress xaddr;
char buf1[255], buf2[255];
	while (elt != NULL)
	{
		contact = lyst_data(elt);
		rfx_insert_contact(contact->fromTime, 0, contact->fromNode,
				contact->toNode, contact->xmitRate,
				contact->confidence, &xaddr);
		elt = lyst_next(elt);
#ifdef DEBUG_PRINTS
writeTimestampLocal(contact->fromTime, buf1);
writeTimestampLocal(contact->toTime, buf2);
printf ("Inserting range and contact, from %d to %d "
		"start %s end %s.\n", contact->fromNode, contact->toNode, buf1, buf2);
#endif
	}
	lyst_clear(from);
	setNodeNum(currentNodeNum);
}

void exchangeCurrentDiscoveredContacts(uvast node1, uvast node2)
{
	Lyst discoveredContacts[2];
	int found[2];
	discoveredContacts[0] = lyst_create();
	discoveredContacts[1] = lyst_create();
	found[0] = findDiscoveredContacts(node1, discoveredContacts[0]);
	found[1] = findDiscoveredContacts(node2, discoveredContacts[1]);
	// copy current discovered contact from node1 to node2
#ifdef DEBUG_PRINTS
printf("COPYING CURRENT DISCOVERED CONTACTS FROM NODE %d TO NODE %d\n",
		node1, node2);
#endif
	copyDiscoveredContacts(discoveredContacts[0], node2);
	// copy current discovered contact from node2 to node1
#ifdef DEBUG_PRINTS
printf("COPYING CURRENT DISCOVERED CONTACTS FROM NODE %d TO NODE %d\n",
		node2, node1);
#endif
	copyDiscoveredContacts(discoveredContacts[1], node1);
	lyst_destroy(discoveredContacts[0]);
	lyst_destroy(discoveredContacts[1]);
	fflush(stdout);
}

void insertNewDiscoveredContact(uvast neighborNode, unsigned int xmitRate)
{
	uvast localNode = getNodeNum();
	time_t currentTime = getUTCTime();
	PsmAddress xaddr;
	rfx_insert_contact(currentTime, 0, localNode, neighborNode,
			xmitRate, 1, &xaddr);
	rfx_insert_contact(currentTime, 0, neighborNode, localNode,
				xmitRate, 1, &xaddr);
	fflush(stdout);
}

void contactLost(uvast neighborNode)
{
	rfx_remove_discovered_contacts(neighborNode);
	fflush(stdout);
}
static void getContactLog(uvast localNodeNbr, int idx, Lyst log)
{
	uvast currentNodeNum = getNodeNum();
	setNodeNum(localNodeNbr);
	Sdr sdr = getIonsdr();
	Object iondbObj = getIonDbObject();
	IonDB iondb;
	PastContact * contact;
	Object elt;
	sdr_read(sdr, (char*) &iondb, iondbObj, sizeof(IonDB));
	elt = sdr_list_first(sdr, iondb.contactLog[idx]);
	while (elt != NULL)
	{
		contact = malloc(sizeof(PastContact));
		sdr_read(sdr, (char*) contact,
				sdr_list_data(sdr, elt), sizeof(PastContact));
		lyst_insert_last(log, contact);
		elt = sdr_list_next(sdr, elt);
	}
	setNodeNum(currentNodeNum);
}

static void copyContactHistory(Lyst from, uvast toNode, int idx)
{
	uvast currentNodeNum = getNodeNum();
	PastContact * contact;
	setNodeNum(toNode);
	LystElt elt = lyst_first(from);
	while (elt != NULL)
	{
		contact = lyst_data(elt);
		rfx_log_discovered_contact(contact->fromTime, contact->toTime,
				contact->fromNode, contact->toNode, contact->xmitRate,
				idx);
		elt = lyst_next(elt);
	}
	lyst_clear(from);
	setNodeNum(currentNodeNum);
}

void exchangeContactHistory(uvast node1, uvast node2)
{
	Lyst node1Log[2], node2Log[2];
	node1Log[SENDER_NODE] = lyst_create();
	node1Log[RECEIVER_NODE] = lyst_create();
	node2Log[SENDER_NODE] = lyst_create();
	node2Log[RECEIVER_NODE] = lyst_create();

	lyst_delete_set(node1Log[SENDER_NODE], freeLystContents, NULL);
	lyst_delete_set(node1Log[RECEIVER_NODE], freeLystContents, NULL);
	lyst_delete_set(node2Log[SENDER_NODE], freeLystContents, NULL);
	lyst_delete_set(node2Log[RECEIVER_NODE], freeLystContents, NULL);

	getContactLog(node1, SENDER_NODE, node1Log[SENDER_NODE]);
	getContactLog(node1, RECEIVER_NODE, node1Log[RECEIVER_NODE]);
	getContactLog(node2, SENDER_NODE, node2Log[SENDER_NODE]);
	getContactLog(node2, RECEIVER_NODE, node2Log[RECEIVER_NODE]);

#ifdef DEBUG_PRINTS
printf("COPYING CONTACT HISTORY FROM NODE %d TO NODE %d\n", node1, node2);
#endif
	copyContactHistory(node1Log[SENDER_NODE], node2, SENDER_NODE);
	copyContactHistory(node1Log[RECEIVER_NODE], node2, RECEIVER_NODE);
#ifdef DEBUG_PRINTS
printf("COPYING CONTACT HISTORY FROM NODE %d TO NODE %d\n", node2, node1);
#endif
	copyContactHistory(node2Log[SENDER_NODE], node1, SENDER_NODE);
	copyContactHistory(node2Log[RECEIVER_NODE], node1, RECEIVER_NODE);
	fflush(stdout);

	lyst_destroy(node1Log[SENDER_NODE]);
	lyst_destroy(node1Log[RECEIVER_NODE]);
	lyst_destroy(node2Log[SENDER_NODE]);
	lyst_destroy(node2Log[RECEIVER_NODE]);
}

void predictContacts()
{
	rfx_predict_all_contacts();
	fflush(stdout);
}

int	notifyNeighbor(uvast neighborNode, uvast fromNode, uvast toNode,
			time_t fromTime, time_t toTime, unsigned int xmitRate)
{
	/**
	PsmAddress cxaddr;
	uvast currentNodeNum = getNodeNum();
	setNodeNum(neighborNode);
	if (toTime == MAX_POSIX_TIME)
	{
		// discovered contact acquired
		rfx_insert_contact(fromTime, 0, fromNode, toNode,
				xmitRate, 1, &cxaddr);
		rfx_insert_contact(fromTime, 0, toNode, fromNode,
				xmitRate, 1, &cxaddr);
	}
	else
	{
		// discovered contact lost
		rfx_remove_contact(fromTime, fromNode, toNode);
		rfx_remove_contact(fromTime, toNode, fromNode);
	}
	setNodeNum(currentNodeNum);
	return 0;
	*/
	return sendDiscoveryInfoToNeighbor(neighborNode,
			fromNode, toNode, fromTime, toTime, xmitRate);
}

int applyDiscoveryInfo(uvast fromNode, uvast toNode,
			time_t fromTime, time_t toTime, unsigned int xmitRate)
{
	PsmAddress cxaddr;
	if (toTime == MAX_POSIX_TIME)
	{
		// discovered contact acquired
		rfx_insert_contact(fromTime, 0, fromNode, toNode,
				xmitRate, 1, &cxaddr);
		rfx_insert_contact(fromTime, 0, toNode, fromNode,
				xmitRate, 1, &cxaddr);
	}
	else
	{
		// discovered contact lost
		rfx_remove_contact(fromTime, fromNode, toNode);
		rfx_remove_contact(fromTime, toNode, fromNode);
	}
	return 0;
}
