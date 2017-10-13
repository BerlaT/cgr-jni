/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

package test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cgr_jni.Utils;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.NetworkInterface;
import core.SimClock;
import core.SimScenario;
import routing.ContactGraphRouter;
import routing.MessageRouter;
import routing.OpportunisticContactGraphRouter;


public class OpportunisticContactGraphRouterTest extends ContactGraphRouterTest {
	private static final int NROF_HOSTS = 6;
	protected static final int TRANSMIT_SPEED = 100000;
	public static String MsgIdString = "MSG_";
	protected OpportunisticContactGraphRouter r1, r2, r3, r4, r5, r6;

	private int msgCounter = 0;

	@Override
	public void setUp() throws Exception {
		ts.putSetting(SimScenario.SCENARIO_NS + "." + SimScenario.NROF_GROUPS_S, "1");
		ts.putSetting(SimScenario.GROUP_NS + "." + core.SimScenario.NROF_HOSTS_S, "" + NROF_HOSTS);
		// ts.putSetting(Message.TTL_SECONDS_S, "true");
		ts.putSetting(MessageRouter.MSG_TTL_S, "3600");

		// Primary network interface settings
		ts.putSetting(TestUtilsForCGR.IFACE1_NS + "." + NetworkInterface.TRANSMIT_RANGE_S, "1");
		ts.putSetting(TestUtilsForCGR.IFACE1_NS + "." + NetworkInterface.TRANSMIT_SPEED_S, "" + TRANSMIT_SPEED);

		// Secondary network interface settings
		ts.putSetting(TestUtilsForCGR.IFACE2_NS + "." + NetworkInterface.TRANSMIT_RANGE_S, "1");
		ts.putSetting(TestUtilsForCGR.IFACE2_NS + "." + NetworkInterface.TRANSMIT_SPEED_S, "" + TRANSMIT_SPEED / 1000);
		// ContactGraphRouter debug active
		ts.putSetting(ContactGraphRouter.CGR_NS + "." + ContactGraphRouter.DEBUG_S, "true");
		OpportunisticContactGraphRouter routerProto = new OpportunisticContactGraphRouter(ts);
		setRouterProto(routerProto);
		this.mc = new MessageChecker();
		mc.reset();
		this.clock = SimClock.getInstance();
		clock.setTime(0);
		this.msgCounter = 0;

		List<MessageListener> ml = new ArrayList<MessageListener>();
		ml.add(mc);
		this.utils = new TestUtilsForCGR(null, ml, ts);
		this.utils.setMessageRouterProto(routerProto);
		core.NetworkInterface.reset();
		core.DTNHost.reset();
		this.h1 = utils.createHost(c0, "h1");
		this.h2 = utils.createHost(c0, "h2");
		this.h3 = utils.createHost(c0, "h3");
		this.h4 = utils.createHost(c0, "h4");
		this.h5 = utils.createHost(c0, "h5");
		this.h6 = utils.createHost(c0, "h6");
		Utils.init(utils.getAllHosts());
		for (DTNHost h : utils.getAllHosts()) {
			disconnect(h);
		}

		r1 = (OpportunisticContactGraphRouter) h1.getRouter();
		r2 = (OpportunisticContactGraphRouter) h2.getRouter();
		r3 = (OpportunisticContactGraphRouter) h3.getRouter();
		r4 = (OpportunisticContactGraphRouter) h4.getRouter();
		r5 = (OpportunisticContactGraphRouter) h5.getRouter();
		r6 = (OpportunisticContactGraphRouter) h6.getRouter();
		super.r1 = r1;
		super.r2 = r2;
		super.r3 = r3;
		super.r4 = r4;
		super.r5 = r5;
		super.r6 = r6;
	}

	/**
	 * Opportunistic version: message should be forwarded. {@inheritDoc}
	 */
	@Override
	public void testRouting5() {
		/*
		 * NO CONTACT PLAN PROVIDED Same connections as testRouting3 Messages
		 * should remain into limbo
		 */

		Message m1 = new Message(h1, h3, msgId1, 10);
		h1.createNewMessage(m1);
		Message m2 = new Message(h2, h4, msgId2, 10);
		h2.createNewMessage(m2);
		Message m3 = new Message(h3, h5, msgId3, 10);
		h3.createNewMessage(m3);
		Message m4 = new Message(h4, h6, msgId4, 10);
		h4.createNewMessage(m4);
		Message m5 = new Message(h5, h6, msgId5, 10);
		h5.createNewMessage(m5);
		Message m6 = new Message(h6, h1, "pippo", 10);
		h6.createNewMessage(m6);
		checkCreates(6);

		updateAllNodes();

		// check all messages are into limbo
		assertEquals(1, r1.getLimboSize());
		assertEquals(1, r2.getLimboSize());
		assertEquals(1, r3.getLimboSize());
		assertEquals(1, r4.getLimboSize());
		assertEquals(1, r5.getLimboSize());
		assertEquals(1, r6.getLimboSize());

		// 1st round, contact 10-30
		clock.advance(10);
		h1.forceConnection(h2, null, true);

		for (int i = 0; i < 20; i++) {
			clock.advance(1);
			updateAllNodes();
		}
		// inserisco la disconnect

		disconnect(h1);
		disconnect(h2);

		if (!r1.isEpidemicDropBack()) {
			// check all message are into limbo
			assertEquals(1, r1.getLimboSize());
			assertEquals(1, r2.getLimboSize());
			assertEquals(1, r3.getLimboSize());
			assertEquals(1, r4.getLimboSize());
			assertEquals(1, r5.getLimboSize());
			assertEquals(1, r6.getLimboSize());
		}
		// no message delivered, 1st deliver h3 no contact available

		// 2nd round contact 40-80
		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h2.forceConnection(h3, null, true);

		for (int i = 0; i < 40; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h2);
		disconnect(h3);

		if (!r1.isEpidemicDropBack()) {
			// check all message are into limbo
			assertEquals(1, r1.getLimboSize());
			assertEquals(1, r2.getLimboSize());
			assertEquals(1, r3.getLimboSize());
			assertEquals(1, r4.getLimboSize());
			assertEquals(1, r5.getLimboSize());
			assertEquals(1, r6.getLimboSize());
		}
		// 3rd round contact 100-150

		for (int i = 0; i < 20; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h3.forceConnection(h4, null, true);

		for (int i = 0; i < 50; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h3);
		disconnect(h4);

		if (!r1.isEpidemicDropBack()) {
			// check all message are into limbo
			assertEquals(1, r1.getLimboSize());
			assertEquals(1, r2.getLimboSize());
			assertEquals(1, r3.getLimboSize());
			assertEquals(1, r4.getLimboSize());
			assertEquals(1, r5.getLimboSize());
			assertEquals(1, r6.getLimboSize());
		}
		// 4th round contact 170-200

		for (int i = 0; i < 20; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h4.forceConnection(h5, null, true);

		for (int i = 0; i < 30; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h4);
		disconnect(h5);

		if (!r1.isEpidemicDropBack()) {
			// check all message are into limbo
			assertEquals(1, r1.getLimboSize());
			assertEquals(1, r2.getLimboSize());
			assertEquals(1, r3.getLimboSize());
			assertEquals(1, r4.getLimboSize());
			assertEquals(1, r5.getLimboSize());
			assertEquals(1, r6.getLimboSize());
		}
		// 5th round contact 250-300

		for (int i = 0; i < 50; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h5.forceConnection(h6, null, true);

		for (int i = 0; i < 50; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h5);
		disconnect(h6);

		if (!r1.isEpidemicDropBack()) {
			// check all message are into limbo
			assertEquals(1, r1.getLimboSize());
			assertEquals(1, r2.getLimboSize());
			assertEquals(1, r3.getLimboSize());
			assertEquals(1, r4.getLimboSize());
			// m5 should have been forwarded
			assertEquals(0, r5.getLimboSize());
			assertEquals(1, r6.getLimboSize());
		}
		// 6th round

		for (int i = 0; i < 20; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h6.forceConnection(h1, null, true);

		for (int i = 0; i < 60; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h1);
		disconnect(h6);

		if (!r1.isEpidemicDropBack()) {
			// check all message are into limbo
			assertEquals(1, r1.getLimboSize());
			assertEquals(1, r2.getLimboSize());
			assertEquals(1, r3.getLimboSize());
			assertEquals(1, r4.getLimboSize());
			assertEquals(0, r5.getLimboSize());
			// m6 should have been forwarded
			assertEquals(0, r6.getLimboSize());
		} else {
			int deliveredCount = 0;
			for (DTNHost h : Utils.getAllNodes()) {
				ContactGraphRouter r = (ContactGraphRouter) h.getRouter();
				deliveredCount += r.getDeliveredCount();
			}

			assertEquals(6, deliveredCount);
		}
	}

	/**
	 * TEST 1 Each node has a contact with his previous and next nodeNbr All
	 * contacts happen simultaneously. Each node create a message for his next
	 * nodeNbr. Messages should be delivered through a single hop route.
	 */
	public void testOppRouting1() {
		// test visuale
		r3.processLine("l range");
		r3.processLine("l contact");

		Message m1 = new Message(h1, h2, msgId1, 100);
		h1.createNewMessage(m1);
		Message m2 = new Message(h2, h3, msgId2, 100);
		h2.createNewMessage(m2);
		Message m3 = new Message(h3, h4, msgId3, 100);
		h3.createNewMessage(m3);
		Message m4 = new Message(h4, h5, msgId4, 100);
		h4.createNewMessage(m4);
		Message m5 = new Message(h5, h6, msgId5, 100);
		h5.createNewMessage(m5);
		Message m6 = new Message(h6, h1, "pippo", 100);
		h6.createNewMessage(m6);
		checkCreates(6);

		updateAllNodes();

		assertEquals(1, r1.getLimboSize());
		assertEquals(1, r2.getLimboSize());
		assertEquals(1, r3.getLimboSize());
		assertEquals(1, r4.getLimboSize());
		assertEquals(1, r5.getLimboSize());
		assertEquals(1, r6.getLimboSize());

		clock.advance(11);
		h1.forceConnection(h2, null, true);
		h2.forceConnection(h3, null, true);
		h3.forceConnection(h4, null, true);
		h4.forceConnection(h5, null, true);
		h5.forceConnection(h6, null, true);
		h6.forceConnection(h1, null, true);

		for (int i = 0; i < 2000; i++) {
			clock.advance(0.1);
			updateAllNodes();
		}

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 0);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 0);

		assertEquals(true, r2.isDeliveredMessage(m1));
		assertEquals(true, r3.isDeliveredMessage(m2));
		assertEquals(true, r4.isDeliveredMessage(m3));
		assertEquals(true, r5.isDeliveredMessage(m4));
		assertEquals(true, r6.isDeliveredMessage(m5));
		assertEquals(true, r1.isDeliveredMessage(m6));
	}

	/**
	 * TEST 2 Each node has a contact with his previous and next nodeNbr All
	 * contacts happen simultaneously. Node 1 creates 20 Messages whose
	 * destination are all the simulated nodes (node 1 included)
	 */
	public void testOppRouting2() {
		/*
		 * String cp_path = ""; cp_path = (new
		 * File("resources/contact_plan_test_discovery.txt"))
		 * .getAbsolutePath(); r1.readContactPlan(cp_path);
		 * r2.readContactPlan(cp_path); r3.readContactPlan(cp_path);
		 * r4.readContactPlan(cp_path); r5.readContactPlan(cp_path);
		 */
		Message m;
		int i;

		m = createNewMessage(h1, h3, 100);
		assertEquals(true, r1.isMessageIntoLimbo(m));
		updateAllNodes();
		clock.advance(10);
		h1.forceConnection(h2, null, true);
		r1.processLine("l contact");
		testWait(1, 0.1);
		h2.forceConnection(h3, null, true);
		r2.processLine("l contact");
		testWait(1, 0.1);
		h3.forceConnection(h4, null, true);
		r3.processLine("l contact");
		testWait(1, 0.1);
		h4.forceConnection(h5, null, true);
		r4.processLine("l contact");
		testWait(1, 0.1);
		h5.forceConnection(h1, null, true);
		r1.processLine("l contact");
		r2.processLine("l contact");
		r3.processLine("l contact");
		r4.processLine("l contact");
		r5.processLine("l contact");

		updateAllNodes();

		for (i = 0; i < 20; i++) {
			m = createNewMessage(h1, Utils.getHostFromNumber((i % 5) + 1), 10);
		}
		// assertEquals(16, r1.getLimboSize());
		testWait(20, 0.1);
		int deliveredCount = 0;
		for (DTNHost h : Utils.getAllNodes()) {
			OpportunisticContactGraphRouter r = (OpportunisticContactGraphRouter) h.getRouter();
			deliveredCount += r.getDeliveredCount();
		}
		assertEquals(21, deliveredCount);
		h1.forceConnection(h2, null, false);
		testWait(1, 0.1);
		h2.forceConnection(h3, null, false);
		testWait(1, 0.1);
		h3.forceConnection(h4, null, false);
		testWait(1, 0.1);
		h4.forceConnection(h5, null, false);
		testWait(1, 0.1);
		h5.forceConnection(h1, null, false);
		testWait(1, 0.1);
		r1.processLine("l contact");
		r2.processLine("l contact");
		r3.processLine("l contact");
		r4.processLine("l contact");
		r5.processLine("l contact");
		h1.forceConnection(h2, null, true);
		testWait(1, 0.1);
		r1.processLine("l contact");
		r2.processLine("l contact");

	}

	public void testOppRoutingSimple() {
		Message m1 = new Message(h1, h2, "MSG_1", 100);
		Message m2 = new Message(h2, h1, "MSG_2", 100);
		Message m3 = new Message(h1, h2, "MSG_3", 100);
		Message m4 = new Message(h2, h1, "MSG_4", 100);
		Message m5 = new Message(h1, h2, "MSG_5", 100);
		Message m6 = new Message(h2, h1, "MSG_6", 100);
		Message m7 = new Message(h1, h2, "MSG_7", 100);
		Message m8 = new Message(h2, h1, "MSG_8", 100);

		h1.createNewMessage(m1);
		h2.createNewMessage(m2);
		assertEquals(true, r1.isMessageIntoLimbo(m1));
		assertEquals(true, r2.isMessageIntoLimbo(m2));
		testWait(30, 0.1);
		r1.processLine("l range");
		r1.processLine("l contact");
		r2.processLine("l range");
		r2.processLine("l contact");
		System.out.println();
		System.out.println("" + SimClock.getTime() + ": Node 1 and Node2 connected");
		h1.forceConnection(h2, null, true);
		testWait(10, 0.1);
		r1.processLine("l range");
		r1.processLine("l contact");
		r2.processLine("l range");
		r2.processLine("l contact");
		System.out.println();
		System.out.println("" + SimClock.getTime() + ": Node 1 and Node2 disconnected");
		disconnect(h1);
		r1.processLine("l range");
		r1.processLine("l contact");
		r2.processLine("l range");
		r2.processLine("l contact");
		assertEquals(true, r1.isDeliveredMessage(m2));
		assertEquals(true, r2.isDeliveredMessage(m1));
		h1.createNewMessage(m3);
		h2.createNewMessage(m4);
		assertEquals(true, r1.isMessageIntoLimbo(m3));
		assertEquals(true, r2.isMessageIntoLimbo(m4));
		testWait(30, 0.1);
		System.out.println();
		System.out.println("" + SimClock.getTime() + ": Node 1 and Node2 connected");
		h1.forceConnection(h2, null, true);
		testWait(10, 0.1);
		r1.processLine("l range");
		r1.processLine("l contact");
		r2.processLine("l range");
		r2.processLine("l contact");
		System.out.println();
		System.out.println("" + SimClock.getTime() + ": Node 1 and Node2 disconnected");
		h1.forceConnection(h2, null, false);
		r1.processLine("l range");
		r1.processLine("l contact");
		r2.processLine("l range");
		r2.processLine("l contact");
		assertEquals(true, r1.isDeliveredMessage(m4));
		assertEquals(true, r2.isDeliveredMessage(m3));
		h1.createNewMessage(m5);
		h2.createNewMessage(m6);
		/*
		 * with last libcgr.c review, messages are not enqueued to any outduct
		 * unless the first route contact has confidence 1.0. So messages remain
		 * in limbo if no discovered contacts are active.
		 *
		 * assertEquals(true, r1.isMessageIntoOutduct(h2, m5));
		 * assertEquals(true, r2.isMessageIntoOutduct(h1, m6));
		 */
		assertEquals(true, r1.isMessageIntoLimbo(m5));
		assertEquals(true, r2.isMessageIntoLimbo(m6));
		testWait(30, 0.1);
		/*
		 * with last libcgr.c review, messages are not enqueued to any outduct
		 * unless the first route contact has confidence 1.0. So messages remain
		 * in limbo if no discovered contacts are active.
		 *
		 * assertEquals(1, r1.getOutductSize(h2)); assertEquals(1,
		 * r2.getOutductSize(h1));
		 */
		System.out.println();
		System.out.println("" + SimClock.getTime() + ": Node 1 and Node2 connected");
		h1.forceConnection(h2, null, true);
		testWait(10, 0.1);
		r1.processLine("l range");
		r1.processLine("l contact");
		r2.processLine("l range");
		r2.processLine("l contact");
		System.out.println();
		System.out.println("" + SimClock.getTime() + ": Node 1 and Node2 disconnected");
		disconnect(h1);
		r1.processLine("l range");
		r1.processLine("l contact");
		r2.processLine("l range");
		r2.processLine("l contact");
		testWait(30, 0.1);
		assertEquals(true, r1.isDeliveredMessage(m6));
		assertEquals(true, r2.isDeliveredMessage(m5));
		h1.createNewMessage(m7);
		h2.createNewMessage(m8);
		/*
		 * with last libcgr.c review, messages are not enqueued to any outduct
		 * unless the first route contact has confidence 1.0. So messages remain
		 * in limbo if no discovered contacts are active.
		 *
		 * assertEquals(true, r1.isMessageIntoOutduct(h2, m7));
		 * assertEquals(true, r2.isMessageIntoOutduct(h1, m8));
		 */
		assertEquals(true, r1.isMessageIntoLimbo(m7));
		assertEquals(true, r2.isMessageIntoLimbo(m8));

	}

	public void testOppRouting3() {
		DTNHost h, hp;
		ContactGraphRouter r, rp;
		Message m0 = createNewMessage(h1, h5, 1000);
		assertEquals(true, r1.isMessageIntoLimbo(m0));
		for (int j = 0; j < 3; j++) {
			for (int i = 1; i <= 5; i++) {
				h = getNodeFromNbr(i);
				hp = getNodeFromNbr(i != 5 ? i + 1 : 1);
				r = (ContactGraphRouter) h.getRouter();
				rp = (ContactGraphRouter) hp.getRouter();
				h.forceConnection(hp, null, true);
				r.processLine("l contact");
				rp.processLine("l contact");
				testWait(30, 0.1);
				h.forceConnection(hp, null, false);
				r.processLine("l contact");
				rp.processLine("l contact");
				testWait(30, 0.1);
			}
		}
		assertEquals(true, r5.isDeliveredMessage(m0));
		Message m1 = createNewMessage(h1, h2, 100);
		testWait(1, 0.1);
		h1.forceConnection(h2, null, true);
		r1.processLine("l contact");
		assertEquals(true, r1.isMessageIntoLimbo(m1));
		/*
		 * with last libcgr.c review, messages are not enqueued to any outduct
		 * unless the first route contact has confidence 1.0. So messages remain
		 * in limbo if no discovered contacts are active.
		 *
		 * assertEquals(true, r1.isMessageIntoOutduct(h2, m1));
		 */
		testWait(10, 0.1);
		h1.forceConnection(h2, null, false);
		r1.processLine("l contact");
		assertEquals(true, r2.isDeliveredMessage(m1));
		Message m2 = createNewMessage(h1, h5, 100);
		testWait(1, 0.1);
		assertEquals(true, r1.isMessageIntoLimbo(m2));
		h1.forceConnection(h2, null, true);
		// clock.advance(1);
		r1.update();
		assertEquals(true, r1.isMessageIntoOutduct(h2, m2));

	}

	public void testReq_0033_prob_cgr() {
		String cp_path = (new File("resources/cp_req-0033-prob-CGR.txt")).getAbsolutePath();
		r1.readContactPlan(cp_path);
		r2.readContactPlan(cp_path);
		r3.readContactPlan(cp_path);
		r4.readContactPlan(cp_path);
		r5.readContactPlan(cp_path);
		r6.readContactPlan(cp_path);
		// visual test
		r3.processLine("l range");
		r3.processLine("l contact");

		Message m1 = new Message(h1, h6, "MSG_0", 10);
		Message m2 = new Message(h1, h4, "MSG_1", 10);
		Message m3 = new Message(h1, h5, "MSG_2", 10);

		updateAllNodes();
		clock.advance(5);
		/*
		 * Create message to node 6. Node 1 doesn't have any information about
		 * node 6. Bundle should be put in limbo
		 */
		h1.createNewMessage(m1);
		assertEquals(true, r1.isMessageIntoLimbo(m1));

		testWait(6, 0.1);
		h1.forceConnection(h2, null, true);
		testWait(3, 0.1);
		/*
		 * Create message to node 4. Delivery confidence is higher than the
		 * threshold, so bundle should be enqueued to node 2 but not duplicated
		 * 
		 * r1.processLine("l contact"); h1.createNewMessage(m2);
		 * assertEquals(false, r1.isMessageIntoLimbo(m2)); assertEquals(true,
		 * r1.isMessageIntoOutduct(h2, m2));
		 */

		/*
		 * With last libcgr.c review, bundles are not enqueued into outducts
		 * unless the first contact has confidence 1.0. So message to node 4
		 * will not be enqueued but it'll remain in limbo
		 */
		r1.processLine("l contact");
		h1.createNewMessage(m2);
		assertEquals(true, r1.isMessageIntoLimbo(m2));
		assertEquals(false, r1.isMessageIntoOutduct(h2, m2));

		testWait(6, 0.1);
		disconnect(h1);
		disconnect(h2);
		testWait(5, 0.1);
		h1.forceConnection(h3, null, true);
		testWait(3, 0.1);
		/*
		 * Create message to node 5. Delivery confidence is less than the
		 * threshold, so bundle should be enqueued to node 3 AND duplicated into
		 * limbo
		 *
		 * h1.createNewMessage(m3); assertEquals(true,
		 * r1.isMessageIntoLimbo(m3)); assertEquals(true,
		 * r1.isMessageIntoOutduct(h3, m3));
		 */
		/*
		 * With last libcgr.c review, bundles are not enqueued into outducts
		 * unless the first contact has confidence 1.0. So messageto node 5
		 * should not be enqueued to any outduct.
		 */
		h1.createNewMessage(m3);
		assertEquals(true, r1.isMessageIntoLimbo(m3));
		assertEquals(false, r1.isMessageIntoOutduct(h3, m3));

		testWait(6, 0.1);
		disconnect(h1);
		disconnect(h3);
		testWait(5, 0.1);
		h2.forceConnection(h4, null, true);
		testWait(8, 0.1);
		disconnect(h2);
		disconnect(h4);
		testWait(5, 0.1);
		h3.forceConnection(h5, null, true);
		testWait(8, 0.1);
		disconnect(h3);
		disconnect(h5);

		/*
		 * Both messages should have been delivered, m3 should still be in node
		 * 1 limbo
		 */
		assertEquals(true, r4.isDeliveredMessage(m2));
		assertEquals(true, r5.isDeliveredMessage(m3));
		assertEquals(true, r1.isMessageIntoLimbo(m3));
	}

	protected void testWait(double sec, double gran) {
		sec = sec / gran;
		for (int i = 0; i < sec; i++) {
			clock.advance(gran);
			updateAllNodes();
		}
	}

	protected String getNextMsgId() {
		return MsgIdString + msgCounter++;
	}

	protected Message createNewMessage(DTNHost from, DTNHost to, int size) {
		Message m = new Message(from, to, getNextMsgId(), size);
		from.getRouter().createNewMessage(m);
		return m;
	}

	protected void disconnectAll() {
		for (DTNHost h : utils.getAllHosts()) {
			disconnect(h);
		}
		updateAllNodes();
	}
}
