/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */package cgr_jni;

import java.util.HashSet;
import java.util.List;

import core.DTNHost;
import core.Message;
import core.PriorityMessage;
import routing.ContactGraphRouter;
import routing.ContactGraphRouter.MessageStatus;
import routing.ContactGraphRouter.Outduct;
import routing.OpportunisticContactGraphRouter;
import routing.PriorityContactGraphRouter;
import routing.PriorityContactGraphRouter.PriorityOutduct;

/**
 * In this class are implemented the static methods accessed from
 * the Java Native Interface, i.e. the methods used by the native C libraries.
 * 
 * @author Simone Pozza
 *
 */
public class IONInterface {

	private static DTNHost getNodeFromNbr(long nodeNbr) {
		return Utils.getHostFromNumber(nodeNbr);
	}

	//// STATIC METHODS ACCESSED FROM JNI /////

	static long getMessageSenderNbr(Message message) {
		int size = message.getHops().size();
		if(size==1) return message.getHops().get(0).getAddress();
		return message.getHops().get(size-2).getAddress();
	}

	static long getMessageDestinationNbr(Message message) {
		return message.getTo().getAddress();
	}

	static long getMessageCreationTime(Message message) {
		return (int) message.getCreationTime();
	}

	static long getMessageTTL(Message message) {
		long result = (long) message.getTtl();
		return result;
	}

	static long getMessageSize(Message message) {
		return message.getSize();
	}

	static void updateMessageForfeitTime(Message message, long forfeitTime) {
		// message.updateProperty(ContactGraphRouter.ROUTE_FORWARD_TIMELIMIT_PROP,
		// forfeitTime);
		MessageStatus status = (MessageStatus) message.getProperty(ContactGraphRouter.MESSAGE_STATUS_PROP);
		status.updateRouteTimelimit(forfeitTime);
	}

	static boolean isOutductBlocked(Outduct jOutduct) {
		return false;
	}

	static int getMaxPayloadLen(Outduct jOutduct) {
		return 1024 * 1024 * 1024;
	}

	static String getOutductName(Outduct jOutduct) {
		return "" + jOutduct.getHost().getAddress();
	}

	static long getOutductTotalEnququedBytes(Outduct jOutduct) {
		return jOutduct.getTotalEnqueuedBytes();
	}

	static Outduct getONEOutductToNode(long localNodeNbr, long toNodeNbr) {
		DTNHost local = getNodeFromNbr(localNodeNbr);
		DTNHost to = getNodeFromNbr(toNodeNbr);

		ContactGraphRouter localRouter = (ContactGraphRouter) local.getRouter();
		// Outduct result = localRouter.getOutducts().get(to);
		Outduct result = localRouter.getOutducts()[to.getAddress()];
		return result;

	}

	static int insertBundleIntoOutduct(long localNodeNbr, Message message, long toNodeNbr) {
		DTNHost local = getNodeFromNbr(localNodeNbr);
		DTNHost to = getNodeFromNbr(toNodeNbr);
		ContactGraphRouter localRouter = (ContactGraphRouter) local.getRouter();
		/*
		 * if(localRouter.getOutducts().containsKey(to)){
		 * localRouter.getOutducts().get(to).insertMessageIntoOutduct(message,
		 * true); return 0; }
		 */

		return localRouter.putMessageIntoOutduct(to, message, true);
	}

	static int insertBundleIntoLimbo(long localNodeNbr, Message message) {
		DTNHost local = getNodeFromNbr(localNodeNbr);
		ContactGraphRouter localRouter = (ContactGraphRouter) local.getRouter();
		localRouter.putMessageIntoLimbo(message);
		return 0;
	}

	static void cloneMessage(long localNodeNbr, Message message) {
		DTNHost local = getNodeFromNbr(localNodeNbr);
		ContactGraphRouter localRouter = (ContactGraphRouter) local.getRouter();
		// Message newMessage = message.replicate();
		/* xmitCopies array must be deep copied */
		/*
		 * int[] xmitCopies = (int[]) message.getProperty(
		 * ContactGraphRouter.XMIT_COPIES_PROP);
		 * newMessage.updateProperty(ContactGraphRouter.XMIT_COPIES_PROP,
		 * xmitCopies.clone());
		 */
		localRouter.putMessageIntoLimbo(message);
	}

	/*
	 * METHODS USED BY OPPORTUNISTIC CGR
	 */

	static MessageStatus getMessageStatus(Message message) {
		return ContactGraphRouter.getMessageStatus(message);
	}

	static int getMessageXmitCopiesCount(Message message) {
		HashSet<Integer> result;
		MessageStatus status = getMessageStatus(message);
		result = status.getXmitCopies();
		if (result != null)
			return result.size();
		return -1;
	}

	static int[] getMessageXmitCopies(Message message) {
		HashSet<Integer> result;
		MessageStatus status = getMessageStatus(message);
		result = status.getXmitCopies();
		if (result != null) {
			if (result.size() > 0) {
				return result.stream().mapToInt(i -> i).toArray();
			} else
				return new int[0];
		}
		return null;
	}
	
	static int getMessagePathCount(Message message) {
		return message.getHops().size();
	}

	static int[] getMessagePath (Message message) {
		int[] out = new int[message.getHops().size()];
		List<DTNHost> list = message.getHops();
		for(int i=0; i<out.length; i++) {
			out[i] = list.get(i).getAddress();
		}
		return out;
	}
	static double getMessageDlvConfidence(Message message) {
		double result;
		MessageStatus status = getMessageStatus(message);
		result = status.getDlvConfidence();
		return result;
	}

	static void setMessageXmitCopies(Message message, int[] copies) {
		MessageStatus status = getMessageStatus(message);
		int copiesCount = getMessageXmitCopiesCount(message);
		if (copies.length == copiesCount) {
			// did not change
			return;
		}
		HashSet<Integer> javaCopies = status.getXmitCopies();
		for (int c : copies) {
			javaCopies.add(c);
		}
	}

	static void setMessageDlvConfidence(Message message, double conf) {
		MessageStatus status = getMessageStatus(message);
		status.setDlvConfidence(conf);
	}

	static void sendDiscoveryInfo(long destinationNode, long fromNode, long toNode, long fromTime, long toTime,
			int xmitSpeed) {
		DTNHost local = getNodeFromNbr(destinationNode);
		OpportunisticContactGraphRouter localRouter = (OpportunisticContactGraphRouter) local.getRouter();
		localRouter.addDiscoveryInfo(fromNode, toNode, fromTime, toTime, xmitSpeed);
	}

	/*
	 * METHODS USED BY PRIORITY CGR
	 */

	static int getMessagePriority(Message message) {
		if (message instanceof PriorityMessage)
			return ((PriorityMessage) message).getPriority();

		return 1;
	}

	static long getOutductBulkBacklog(Outduct jOuduct) {
		if (jOuduct instanceof PriorityOutduct)
			return ((PriorityOutduct) jOuduct).getBulkBacklog();

		return 0;
	}

	static long getOutductNormalBacklog(Outduct jOuduct) {
		if (jOuduct instanceof PriorityOutduct)
			return ((PriorityOutduct) jOuduct).getNormalBacklog();

		return jOuduct.getTotalEnqueuedBytes();
	}

	static long getOutductExpeditedBacklog(Outduct jOuduct) {
		if (jOuduct instanceof PriorityOutduct)
			return ((PriorityOutduct) jOuduct).getExpeditedBacklog();

		return 0;
	}

	static int manageOverbooking(long localNodeNbr, long proximateNodeNbr, double overbooked, double protect) {
		DTNHost local = getNodeFromNbr(localNodeNbr);
		DTNHost to = getNodeFromNbr(proximateNodeNbr);
		PriorityContactGraphRouter localRouter;
		if (local.getRouter() instanceof PriorityContactGraphRouter) {
			localRouter = (PriorityContactGraphRouter) local.getRouter();
			localRouter.setManageOverbooking(to, overbooked, protect);
		}

		return 0;
	}

}
