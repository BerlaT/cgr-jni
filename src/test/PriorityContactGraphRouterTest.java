/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

package test;

//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//import cgr_jni.Utils;
//import core.DTNHost;
//import core.Message;
//import core.MessageListener;
//import core.NetworkInterface;
//import core.PriorityMessage;
//import core.SimClock;
//import core.SimScenario;
//import routing.MessageRouter;
//import routing.PriorityContactGraphRouter;
//import routing.PriorityContactGraphRouter.PriorityOutduct;

public class PriorityContactGraphRouterTest extends AbstractRouterTest {
	
	/**
	 * do not use this test class as it has not been updated to the new code
	 * */

//	private static final String CONTACT_PLAN_ASMS_FIG4 = "resources/contact_plan_ASMS14_Fig4.txt";
//	private static final String CONTACT_PLAN_ASMS_FIG6 = "resources/contact_plan_ASMS14_2.txt";
//
//	private static final int NROF_HOSTS = 6;
//	private PriorityContactGraphRouter r1, r2, r3, r4;
//	protected static final int TRANSMIT_SPEED = 16000;
//
//	@Override
//	public void setUp() throws Exception {
//		ts.putSetting(SimScenario.SCENARIO_NS + "." + SimScenario.NROF_GROUPS_S, "1");
//		ts.putSetting(SimScenario.GROUP_NS + "." + core.SimScenario.NROF_HOSTS_S, "" + NROF_HOSTS);
//		// ts.putSetting(Message.TTL_SECONDS_S, "true");
//		ts.putSetting(MessageRouter.MSG_TTL_S, "3600");
//
//		// Primary network interface settings
//		ts.putSetting(TestUtilsForCGR.IFACE1_NS + "." + NetworkInterface.TRANSMIT_RANGE_S, "1");
//		ts.putSetting(TestUtilsForCGR.IFACE1_NS + "." + NetworkInterface.TRANSMIT_SPEED_S, "" + TRANSMIT_SPEED);
//
//		// Secondary network interface settings
//		ts.putSetting(TestUtilsForCGR.IFACE2_NS + "." + NetworkInterface.TRANSMIT_RANGE_S, "1");
//		ts.putSetting(TestUtilsForCGR.IFACE2_NS + "." + NetworkInterface.TRANSMIT_SPEED_S, "" + TRANSMIT_SPEED * 4);
//		PriorityContactGraphRouter routerProto = new PriorityContactGraphRouter(ts);
//		setRouterProto(routerProto);
//		this.mc = new MessageChecker();
//		mc.reset();
//		this.clock = SimClock.getInstance();
//		clock.setTime(0);
//
//		List<MessageListener> ml = new ArrayList<MessageListener>();
//		ml.add(mc);
//		this.utils = new TestUtilsForCGR(null, ml, ts);
//		this.utils.setMessageRouterProto(routerProto);
//		core.NetworkInterface.reset();
//		core.DTNHost.reset();
//		this.h1 = utils.createHost(c0, "h1");
//		this.h2 = utils.createHost(c0, "h2");
//		this.h3 = utils.createHost(c0, "h3");
//		this.h4 = utils.createHost(c0, "h4");
//		this.h5 = utils.createHost(c0, "h5");
//		this.h6 = utils.createHost(c0, "h6");
//		Utils.init(utils.getAllHosts());
//		for (DTNHost h : utils.getAllHosts()) {
//			disconnect(h);
//		}
//
//		r1 = (PriorityContactGraphRouter) h1.getRouter();
//		r2 = (PriorityContactGraphRouter) h2.getRouter();
//		r3 = (PriorityContactGraphRouter) h3.getRouter();
//		r4 = (PriorityContactGraphRouter) h4.getRouter();
//	}
//
//	public void testASMS_fig4_allBulk() {
//		String cp_path = (new File(CONTACT_PLAN_ASMS_FIG4)).getAbsolutePath();
//		r1.readContactPlan(cp_path);
//		r2.readContactPlan(cp_path);
//		r3.readContactPlan(cp_path);
//		r4.readContactPlan(cp_path);
//
//		PriorityMessage m1 = new PriorityMessage(h1, h4, "Messaggio 1", 100000, 0);
//		h1.createNewMessage(m1);
//		PriorityMessage m2 = new PriorityMessage(h1, h4, "Messaggio 2", 100000, 0);
//		h1.createNewMessage(m2);
//		PriorityMessage m3 = new PriorityMessage(h1, h4, "Messaggio 3", 100000, 0);
//		h1.createNewMessage(m3);
//		PriorityMessage m4 = new PriorityMessage(h1, h4, "Messaggio 4", 100000, 0);
//		h1.createNewMessage(m4);
//		PriorityMessage m5 = new PriorityMessage(h1, h4, "Messaggio 5", 100000, 0);
//		h1.createNewMessage(m5);
//		PriorityMessage m6 = new PriorityMessage(h1, h4, "Messaggio 6", 100000, 0);
//		h1.createNewMessage(m6);
//		PriorityMessage m7 = new PriorityMessage(h1, h4, "Messaggio 7", 100000, 0);
//		h1.createNewMessage(m7);
//		PriorityMessage m8 = new PriorityMessage(h1, h4, "Messaggio 8", 100000, 0);
//		h1.createNewMessage(m8);
//		PriorityMessage m9 = new PriorityMessage(h1, h4, "Messaggio 9", 100000, 0);
//		h1.createNewMessage(m9);
//		PriorityMessage m10 = new PriorityMessage(h1, h4, "Messaggio 10", 100000, 0);
//		h1.createNewMessage(m10);
//		PriorityMessage m11 = new PriorityMessage(h1, h4, "Messaggio 11", 100000, 0);
//		h1.createNewMessage(m11);
//		PriorityMessage m12 = new PriorityMessage(h1, h4, "Messaggio 12", 100000, 0);
//		h1.createNewMessage(m12);
//		PriorityMessage m13 = new PriorityMessage(h1, h4, "Messaggio 13", 100000, 0);
//		h1.createNewMessage(m13);
//		PriorityMessage m14 = new PriorityMessage(h1, h4, "Messaggio 14", 100000, 0);
//		h1.createNewMessage(m14);
//		PriorityMessage m15 = new PriorityMessage(h1, h4, "Messaggio 15", 100000, 0);
//		h1.createNewMessage(m15);
//		PriorityMessage m16 = new PriorityMessage(h1, h4, "Messaggio 16", 100000, 0);
//		h1.createNewMessage(m16);
//
//		checkCreates(16);
//
//		updateAllNodes();
//		h2.forceConnection(h4, null, true);
//		h3.forceConnection(h4, null, true);
//		assertEquals(((PriorityOutduct) r1.getOutducts()[h2.getAddress()]).getBulkQueue().size(), 8);
//		assertEquals(((PriorityOutduct) r1.getOutducts()[h3.getAddress()]).getBulkQueue().size(), 8);
//		System.out.println("VIA H3");
//		for (Message m : ((PriorityOutduct) r1.getOutducts()[h3.getAddress()]).getBulkQueue())
//			System.out.println(m.getId());
//		System.out.println("VIA H2");
//		for (Message m : ((PriorityOutduct) r1.getOutducts()[h2.getAddress()]).getBulkQueue())
//			System.out.println(m.getId());
//		clock.advance(29);
//
//		h1.forceConnection(h2, null, true);
//		updateAllNodes();
//
//		clock.advance(1);
//
//		h1.forceConnection(h3, null, true);
//
//		for (int i = 0; i < 436; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		clock.advance(1);
//
//		clock.advance(1);
//
//		disconnect(h1);
//		disconnect(h2);
//		disconnect(h3);
//		disconnect(h4);
//
//		assertEquals(true, r4.isDeliveredMessage(m1));
//
//		assertEquals(true, r4.isDeliveredMessage(m2));
//
//		assertEquals(true, r4.isDeliveredMessage(m3));
//
//		assertEquals(true, r4.isDeliveredMessage(m4));
//
//		assertEquals(true, r4.isDeliveredMessage(m5));
//
//		assertEquals(true, r4.isDeliveredMessage(m6));
//
//		assertEquals(true, r4.isDeliveredMessage(m7));
//
//		assertEquals(true, r4.isDeliveredMessage(m8));
//
//		assertEquals(true, r4.isDeliveredMessage(m9));
//
//		assertEquals(true, r4.isDeliveredMessage(m10));
//
//		assertEquals(true, r4.isDeliveredMessage(m11));
//
//		assertEquals(true, r4.isDeliveredMessage(m12));
//
//		assertEquals(true, r4.isDeliveredMessage(m13));
//
//		assertEquals(true, r4.isDeliveredMessage(m14));
//
//		assertEquals(true, r4.isDeliveredMessage(m15));
//
//		assertEquals(true, r4.isDeliveredMessage(m16));
//
//	}
//
//	public void testASMS_fig6_allBulk() {
//		String cp_path = (new File(CONTACT_PLAN_ASMS_FIG6)).getAbsolutePath();
//		r1.readContactPlan(cp_path);
//		r2.readContactPlan(cp_path);
//		r3.readContactPlan(cp_path);
//		r4.readContactPlan(cp_path);
//
//		PriorityMessage m1 = new PriorityMessage(h1, h4, "Messaggio 1", 100000, 0);
//		h1.createNewMessage(m1);
//		PriorityMessage m2 = new PriorityMessage(h1, h4, "Messaggio 2", 100000, 0);
//		h1.createNewMessage(m2);
//		PriorityMessage m3 = new PriorityMessage(h1, h4, "Messaggio 3", 100000, 0);
//		h1.createNewMessage(m3);
//		PriorityMessage m4 = new PriorityMessage(h1, h4, "Messaggio 4", 100000, 0);
//		h1.createNewMessage(m4);
//		PriorityMessage m5 = new PriorityMessage(h1, h4, "Messaggio 5", 100000, 0);
//		h1.createNewMessage(m5);
//		PriorityMessage m6 = new PriorityMessage(h1, h4, "Messaggio 6", 100000, 0);
//		h1.createNewMessage(m6);
//		PriorityMessage m7 = new PriorityMessage(h1, h4, "Messaggio 7", 100000, 0);
//		h1.createNewMessage(m7);
//		PriorityMessage m8 = new PriorityMessage(h1, h4, "Messaggio 8", 100000, 0);
//		h1.createNewMessage(m8);
//		PriorityMessage m9 = new PriorityMessage(h1, h4, "Messaggio 9", 100000, 0);
//		h1.createNewMessage(m9);
//		PriorityMessage m10 = new PriorityMessage(h1, h4, "Messaggio 10", 100000, 0);
//		h1.createNewMessage(m10);
//		PriorityMessage m11 = new PriorityMessage(h1, h4, "Messaggio 11", 100000, 0);
//		h1.createNewMessage(m11);
//		PriorityMessage m12 = new PriorityMessage(h1, h4, "Messaggio 12", 100000, 0);
//		h1.createNewMessage(m12);
//		PriorityMessage m13 = new PriorityMessage(h1, h4, "Messaggio 13", 100000, 0);
//		h1.createNewMessage(m13);
//		PriorityMessage m14 = new PriorityMessage(h1, h4, "Messaggio 14", 100000, 0);
//		h1.createNewMessage(m14);
//		PriorityMessage m15 = new PriorityMessage(h1, h4, "Messaggio 15", 100000, 0);
//		h1.createNewMessage(m15);
//
//		checkCreates(15);
//
//		updateAllNodes();
//		h2.forceConnection(h4, TestUtilsForCGR.IFACE2_NS, true);
//		h3.forceConnection(h4, TestUtilsForCGR.IFACE2_NS, true);
//		assertEquals(((PriorityOutduct) r1.getOutducts()[h2.getAddress()]).getBulkQueue().size(), 8);
//		assertEquals(((PriorityOutduct) r1.getOutducts()[h3.getAddress()]).getBulkQueue().size(), 7);
//		System.out.println("VIA H3");
//		for (Message m : ((PriorityOutduct) r1.getOutducts()[h3.getAddress()]).getBulkQueue())
//			System.out.println(m.getId());
//		System.out.println("VIA H2");
//		for (Message m : ((PriorityOutduct) r1.getOutducts()[h2.getAddress()]).getBulkQueue())
//			System.out.println(m.getId());
//
//		clock.advance(30);
//
//		h1.forceConnection(h3, TestUtilsForCGR.IFACE1_NS, true);
//		updateAllNodes();
//
//		for (int i = 0; i < 120; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		h1.forceConnection(h2, TestUtilsForCGR.IFACE2_NS, true);
//
//		for (int i = 0; i < 80; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		h1.forceConnection(h2, TestUtilsForCGR.IFACE2_NS, false);
//
//		for (int i = 0; i < 40; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		h1.forceConnection(h3, TestUtilsForCGR.IFACE1_NS, false);
//
//		for (int i = 0; i < 60; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		h1.forceConnection(h3, TestUtilsForCGR.IFACE1_NS, true);
//
//		for (int i = 0; i < 120; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		h1.forceConnection(h3, TestUtilsForCGR.IFACE1_NS, false);
//
//		disconnect(h1);
//		disconnect(h2);
//		disconnect(h3);
//		disconnect(h4);
//
//		assertEquals(true, r4.isDeliveredMessage(m1));
//
//		assertEquals(true, r4.isDeliveredMessage(m2));
//
//		assertEquals(true, r4.isDeliveredMessage(m3));
//
//		assertEquals(true, r4.isDeliveredMessage(m4));
//
//		assertEquals(true, r4.isDeliveredMessage(m5));
//
//		assertEquals(true, r4.isDeliveredMessage(m6));
//
//		assertEquals(true, r4.isDeliveredMessage(m7));
//
//		assertEquals(true, r4.isDeliveredMessage(m8));
//
//		assertEquals(true, r4.isDeliveredMessage(m9));
//
//		assertEquals(true, r4.isDeliveredMessage(m10));
//
//		assertEquals(true, r4.isDeliveredMessage(m11));
//
//		assertEquals(true, r4.isDeliveredMessage(m12));
//
//		assertEquals(true, r4.isDeliveredMessage(m13));
//
//		assertEquals(true, r4.isDeliveredMessage(m14));
//
//		assertEquals(true, r4.isDeliveredMessage(m15));
//	}
//
//	public void testASMS_fig6_last4Exp() {
//		String cp_path = (new File(CONTACT_PLAN_ASMS_FIG6)).getAbsolutePath();
//		r1.readContactPlan(cp_path);
//		r2.readContactPlan(cp_path);
//		r3.readContactPlan(cp_path);
//		r4.readContactPlan(cp_path);
//
//		PriorityMessage m1 = new PriorityMessage(h1, h4, "Messaggio 1", 100000, 0);
//		h1.createNewMessage(m1);
//		PriorityMessage m2 = new PriorityMessage(h1, h4, "Messaggio 2", 100000, 0);
//		h1.createNewMessage(m2);
//		PriorityMessage m3 = new PriorityMessage(h1, h4, "Messaggio 3", 100000, 0);
//		h1.createNewMessage(m3);
//		PriorityMessage m4 = new PriorityMessage(h1, h4, "Messaggio 4", 100000, 0);
//		h1.createNewMessage(m4);
//		PriorityMessage m5 = new PriorityMessage(h1, h4, "Messaggio 5", 100000, 0);
//		h1.createNewMessage(m5);
//		PriorityMessage m6 = new PriorityMessage(h1, h4, "Messaggio 6", 100000, 0);
//		h1.createNewMessage(m6);
//		PriorityMessage m7 = new PriorityMessage(h1, h4, "Messaggio 7", 100000, 0);
//		h1.createNewMessage(m7);
//		PriorityMessage m8 = new PriorityMessage(h1, h4, "Messaggio 8", 100000, 0);
//		h1.createNewMessage(m8);
//		PriorityMessage m9 = new PriorityMessage(h1, h4, "Messaggio 9", 100000, 0);
//		h1.createNewMessage(m9);
//		PriorityMessage m10 = new PriorityMessage(h1, h4, "Messaggio 10", 100000, 0);
//		h1.createNewMessage(m10);
//		PriorityMessage m11 = new PriorityMessage(h1, h4, "Messaggio 11", 100000, 0);
//		h1.createNewMessage(m11);
//		PriorityMessage m12 = new PriorityMessage(h1, h4, "Messaggio 12", 100000, 2);
//		h1.createNewMessage(m12);
//		PriorityMessage m13 = new PriorityMessage(h1, h4, "Messaggio 13", 100000, 2);
//		h1.createNewMessage(m13);
//		PriorityMessage m14 = new PriorityMessage(h1, h4, "Messaggio 14", 100000, 2);
//		h1.createNewMessage(m14);
//		PriorityMessage m15 = new PriorityMessage(h1, h4, "Messaggio 15", 100000, 2);
//		h1.createNewMessage(m15);
//
//		checkCreates(15);
//
//		updateAllNodes();
//		h2.forceConnection(h4, TestUtilsForCGR.IFACE2_NS, true);
//		h3.forceConnection(h4, TestUtilsForCGR.IFACE2_NS, true);
//		/*
//		 * assertEquals(((PriorityOutduct)r1.getOutducts().get(h2)).getBulkQueue
//		 * ().size(), 5);
//		 * assertEquals(((PriorityOutduct)r1.getOutducts().get(h3)).getBulkQueue
//		 * ().size(), 6);
//		 * assertEquals(((PriorityOutduct)r1.getOutducts().get(h3)).
//		 * getExpeditedQueue().size(), 4);
//		 */
//
//		System.out.println("VIA H3");
//		for (Message m : ((PriorityOutduct) r1.getOutducts()[h3.getAddress()]).getBulkQueue())
//			System.out.println(m.getId());
//		System.out.println("VIA H2");
//		for (Message m : ((PriorityOutduct) r1.getOutducts()[h2.getAddress()]).getBulkQueue())
//			System.out.println(m.getId());
//
//		clock.advance(30);
//
//		h1.forceConnection(h3, TestUtilsForCGR.IFACE1_NS, true);
//		updateAllNodes();
//
//		for (int i = 0; i < 120; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		assertEquals(true, r4.isDeliveredMessage(m12));
//
//		assertEquals(true, r4.isDeliveredMessage(m13));
//
//		assertEquals(true, r4.isDeliveredMessage(m14));
//
//		assertEquals(true, r4.isDeliveredMessage(m15));
//
//		h1.forceConnection(h2, TestUtilsForCGR.IFACE2_NS, true);
//
//		for (int i = 0; i < 80; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		h1.forceConnection(h2, TestUtilsForCGR.IFACE2_NS, false);
//
//		for (int i = 0; i < 40; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		h1.forceConnection(h3, TestUtilsForCGR.IFACE1_NS, false);
//
//		for (int i = 0; i < 60; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		h1.forceConnection(h3, TestUtilsForCGR.IFACE1_NS, true);
//
//		for (int i = 0; i < 120; i++) {
//			updateAllNodes();
//			clock.advance(0.25);
//		}
//
//		h1.forceConnection(h3, TestUtilsForCGR.IFACE1_NS, false);
//
//		disconnect(h1);
//		disconnect(h2);
//		disconnect(h3);
//		disconnect(h4);
//
//		assertEquals(true, r4.isDeliveredMessage(m1));
//
//		assertEquals(true, r4.isDeliveredMessage(m2));
//
//		assertEquals(true, r4.isDeliveredMessage(m3));
//
//		assertEquals(true, r4.isDeliveredMessage(m4));
//
//		assertEquals(true, r4.isDeliveredMessage(m5));
//
//		assertEquals(true, r4.isDeliveredMessage(m6));
//
//		assertEquals(true, r4.isDeliveredMessage(m7));
//
//		assertEquals(true, r4.isDeliveredMessage(m8));
//
//		assertEquals(true, r4.isDeliveredMessage(m9));
//
//		assertEquals(true, r4.isDeliveredMessage(m10));
//
//		assertEquals(true, r4.isDeliveredMessage(m11));
//
//	}
}
