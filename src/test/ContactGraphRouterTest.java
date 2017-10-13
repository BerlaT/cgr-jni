/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

package test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import cgr_jni.Utils;
import core.DTNHost;
import core.Message;
import core.MessageListener;
import core.NetworkInterface;
import core.SimClock;
import core.SimScenario;
import routing.ContactGraphRouter;
import routing.MessageRouter;

/**
 * To test CGR we have created the ContactGraphRouterTest class, which inherits
 * the AbstractRouter’s methods and adds to them the setup() method and the test
 * functions.
 * 
 * @author Jako Jo Messina
 *
 */
public class ContactGraphRouterTest extends AbstractRouterTest {

	private static final String CONTACT_PLAN_FILE = "resources/contactPlan_prova.txt";
	private static final String CONTACT_PLAN_FILE2 = "resources/cp_prova2.txt";
	private static final String CONTACT_PLAN_TEST4 = "resources/cp_testRouting4.txt";
	private static final String CONTACT_PLAN_TEST7 = "resources/cp_testRouting7.txt";
	private static final String CONTACT_PLAN_ASMS_FIG4 = "resources/contact_plan_ASMS14_Fig4.txt";
	private static final String CONTACT_PLAN_ASMS_FIG6 = "resources/contact_plan_ASMS14_2.txt";

	private static final int NROF_HOSTS = 6;
	protected ContactGraphRouter r1, r2, r3, r4, r5, r6;
	private static ContactGraphRouterTest instance = null;
	protected static final int TRANSMIT_SPEED = 16000;

	@Override
	public void setUp() throws Exception {
		instance = this;
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
		ContactGraphRouter routerProto = new ContactGraphRouter(ts);
		setRouterProto(routerProto);
		this.mc = new MessageChecker();
		mc.reset();
		this.clock = SimClock.getInstance();
		clock.setTime(0);

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

		r1 = (ContactGraphRouter) h1.getRouter();
		r2 = (ContactGraphRouter) h2.getRouter();
		r3 = (ContactGraphRouter) h3.getRouter();
		r4 = (ContactGraphRouter) h4.getRouter();
		r5 = (ContactGraphRouter) h5.getRouter();
		r6 = (ContactGraphRouter) h6.getRouter();
	}

	@Override
	public void tearDown() throws Exception {
		/*
		 * for (DTNHost h : Utils.getAllNodes()) { ContactGraphRouter r =
		 * (ContactGraphRouter) h.getRouter(); r.finalizeCGR(); }
		 * Libcgr.finalizeGlobalMem();
		 */
		ContactGraphRouter.reset();
	}

	/**
	 * TEST 1 Each node has a contact with his previous and next nodeNbr All
	 * contacts happen simultaneously. Each node create a message for his next
	 * nodeNbr. Messages should be delivered through a single hop route.
	 */
	public void testRouting1() {

		String cp_path = (new File(CONTACT_PLAN_FILE)).getAbsolutePath();
		r1.readContactPlan(cp_path);
		r2.readContactPlan(cp_path);
		r3.readContactPlan(cp_path);
		r4.readContactPlan(cp_path);
		r5.readContactPlan(cp_path);
		r6.readContactPlan(cp_path);

		// test visuale
		r3.processLine("l range");
		r3.processLine("l contact");

		Message m1 = new Message(h1, h2, msgId1, 10);
		h1.createNewMessage(m1);
		Message m2 = new Message(h2, h3, msgId2, 10);
		h2.createNewMessage(m2);
		Message m3 = new Message(h3, h4, msgId3, 10);
		h3.createNewMessage(m3);
		Message m4 = new Message(h4, h5, msgId4, 10);
		h4.createNewMessage(m4);
		Message m5 = new Message(h5, h6, msgId5, 10);
		h5.createNewMessage(m5);
		Message m6 = new Message(h6, h1, "pippo", 10);
		h6.createNewMessage(m6);
		checkCreates(6);

		updateAllNodes();

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 1);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 1);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 1);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 1);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 1);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 1);

		clock.advance(11);
		h1.forceConnection(h2, null, true);
		h2.forceConnection(h3, null, true);
		h3.forceConnection(h4, null, true);
		h4.forceConnection(h5, null, true);
		h5.forceConnection(h6, null, true);
		h6.forceConnection(h1, null, true);

		for (int i = 0; i < 2000; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 0);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 0);

		assertEquals(true, r2.isDeliveredMessage(m1));

	}

	/**
	 * TEST 2 Each node has a contact with his previous and next nodeNbr All
	 * contacts happen simultaneously. Node 1 creates 20 Messages whose
	 * destination are all the simulated nodes (node 1 included) All messages
	 * should be delivered within the end of the contacts.
	 */
	public void testRouting2() {
		String cp_path = (new File(CONTACT_PLAN_FILE)).getAbsolutePath();
		r1.readContactPlan(cp_path);
		r2.readContactPlan(cp_path);
		r3.readContactPlan(cp_path);
		r4.readContactPlan(cp_path);
		r5.readContactPlan(cp_path);
		r6.readContactPlan(cp_path);

		Message m;
		int i;
		disconnect(h1);
		for (i = 0; i < 20; i++) {
			m = new Message(h1, getNodeFromNbr((i % 5) + 1), "MSG_" + i, 10);
			h1.createNewMessage(m);
		}
		updateAllNodes();
		clock.advance(11);
		h1.forceConnection(h2, null, true);
		h2.forceConnection(h3, null, true);
		h3.forceConnection(h4, null, true);
		h4.forceConnection(h5, null, true);
		h5.forceConnection(h6, null, true);

		for (int j = 0; j < 2000; j++) {
			clock.advance(1);
			updateAllNodes();
		}

		int deliveredCount = 0;
		for (DTNHost h : Utils.getAllNodes()) {
			ContactGraphRouter r = (ContactGraphRouter) h.getRouter();
			deliveredCount += r.getDeliveredCount();
		}

		assertEquals(20, deliveredCount);
	}

	/**
	 * TEST 3 Each node has a contact with his previous and next nodeNbr
	 * Contacts happen sequentially with disconnected intervals.
	 */
	public void testRouting3() {

		String cp_path = (new File(CONTACT_PLAN_FILE2)).getAbsolutePath();
		r1.readContactPlan(cp_path);
		r2.readContactPlan(cp_path);
		r3.readContactPlan(cp_path);
		r4.readContactPlan(cp_path);
		r5.readContactPlan(cp_path);
		r6.readContactPlan(cp_path);

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

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 1);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 1);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 1);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 1);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 1);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 1);
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

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 2);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 1);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 1);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 1);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 1);

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

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 2);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 1);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 1);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 1);

		// m1 delivered
		assertEquals(true, r3.isDeliveredMessage(m1));

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

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 2);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 1);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 1);

		// m2 delivered

		assertEquals(true, r4.isDeliveredMessage(m2));
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

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 2);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 1);

		// m3 delivered
		assertEquals(true, r5.isDeliveredMessage(m3));
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

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 0);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 1);

		// m4 delivered
		assertEquals(true, r6.isDeliveredMessage(m4));
		// m5 delivered
		assertEquals(true, r6.isDeliveredMessage(m5));
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

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 0);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 0);

		// m6 delivered
		assertEquals(true, r1.isDeliveredMessage(m6));

	}

	/**
	 * TEST 4 Each node has at least a contact, but some messages can't be
	 * delivered because there is no route available for the destination
	 */

	public void testRouting4() {
		String cp_path = (new File(CONTACT_PLAN_TEST4)).getAbsolutePath();
		r1.readContactPlan(cp_path);
		r2.readContactPlan(cp_path);
		r3.readContactPlan(cp_path);
		r4.readContactPlan(cp_path);
		r5.readContactPlan(cp_path);
		r6.readContactPlan(cp_path);

		Message m1 = new Message(h1, h4, msgId1, 10);
		h1.createNewMessage(m1);
		Message m2 = new Message(h1, h5, msgId2, 10);
		h1.createNewMessage(m2);
		Message m3 = new Message(h1, h6, msgId3, 10);
		h1.createNewMessage(m3);
		Message m4 = new Message(h2, h4, msgId4, 10);
		h2.createNewMessage(m4);
		Message m5 = new Message(h5, h6, msgId5, 10);
		h5.createNewMessage(m5);
		Message m6 = new Message(h4, h5, "pippo", 10);
		h4.createNewMessage(m6);
		checkCreates(6);

		updateAllNodes();

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 2);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 1);
		assertEquals(r2.getOutducts()[h4.getAddress()].getQueue().size(), 1);
		assertTrue(r4.isMessageIntoLimbo(m6));
		System.out.println("\nMessage " + m6 + "in r4 limbo: no routes available for its destination\n");
		assertTrue(r5.isMessageIntoLimbo(m5));
		System.out.println("\nMessage " + m5 + "in r5 limbo: no routes available for its destination\n");

		clock.advance(10);
		// 1st round contacts 10-30

		h1.forceConnection(h2, null, true);
		h1.forceConnection(h3, null, true);

		for (int i = 0; i < 20; i++) {
			clock.advance(1);
			updateAllNodes();
		}
		// inserisco la disconnect

		disconnect(h1);
		disconnect(h2);
		disconnect(h3);

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h4.getAddress()].getQueue().size(), 3);
		assertEquals(r3.getOutducts()[h5.getAddress()].getQueue().size(), 1);
		assertTrue(r4.isMessageIntoLimbo(m6));
		System.out.println("\nMessage " + m6 + " in r4 limbo: no routes available for its destination\n");
		assertTrue(r5.isMessageIntoLimbo(m5));
		System.out.println("\nMessage " + m5 + " in r5 limbo: no routes available for its destination\n");

		// no messages delivered
		// 2nd round contacts 70-80 and 60-90

		for (int i = 0; i < 30; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h2.forceConnection(h4, null, true);

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h3.forceConnection(h5, null, true);

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h3);
		disconnect(h5);

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h2);
		disconnect(h4);

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h4.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h6.getAddress()].getQueue().size(), 1);
		assertTrue(r4.isMessageIntoLimbo(m6));
		System.out.println("\nMessage " + m6 + " in r4 limbo: no routes available for its destination\n");
		assertTrue(r5.isMessageIntoLimbo(m5));
		System.out.println("\nMessage " + m5 + " in r5 limbo: no routes available for its destination\n");

		assertEquals(true, r4.isDeliveredMessage(m1));
		assertEquals(true, r4.isDeliveredMessage(m4));
		assertEquals(true, r5.isDeliveredMessage(m2));

		// 3rd round contact 100-130

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h4.forceConnection(h6, null, true);

		for (int i = 0; i < 30; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h4);
		disconnect(h6);

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h4.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h6.getAddress()].getQueue().size(), 0);
		assertTrue(r4.isMessageIntoLimbo(m6));
		System.out.println("\nMessage " + m6 + " in r4 limbo: no routes available for its destination\n");
		assertTrue(r5.isMessageIntoLimbo(m5));
		System.out.println("\nMessage " + m5 + " in r5 limbo: no routes available for its destination\n");

		assertEquals(true, r6.isDeliveredMessage(m3));

	}

	/**
	 * TEST 5 Each node has a contact with his previous and next nodeNbr
	 * Contacts happen sequentially with disconnected intervals. NO CONTACT PLAN
	 * PROVIDED TO THE CGR lib, therefore no messages should be forwarded. All
	 * messages should remain into limbo.
	 * 
	 */
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

		// check all message are into limbo
		assertEquals(1, r1.getLimboSize());
		assertEquals(1, r2.getLimboSize());
		assertEquals(1, r3.getLimboSize());
		assertEquals(1, r4.getLimboSize());
		assertEquals(1, r5.getLimboSize());
		assertEquals(1, r6.getLimboSize());

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

		// check all message are into limbo
		assertEquals(1, r1.getLimboSize());
		assertEquals(1, r2.getLimboSize());
		assertEquals(1, r3.getLimboSize());
		assertEquals(1, r4.getLimboSize());
		assertEquals(1, r5.getLimboSize());
		assertEquals(1, r6.getLimboSize());

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

		// check all message are into limbo
		assertEquals(1, r1.getLimboSize());
		assertEquals(1, r2.getLimboSize());
		assertEquals(1, r3.getLimboSize());
		assertEquals(1, r4.getLimboSize());
		assertEquals(1, r5.getLimboSize());
		assertEquals(1, r6.getLimboSize());

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

		// check all message are into limbo
		assertEquals(1, r1.getLimboSize());
		assertEquals(1, r2.getLimboSize());
		assertEquals(1, r3.getLimboSize());
		assertEquals(1, r4.getLimboSize());
		assertEquals(1, r5.getLimboSize());
		assertEquals(1, r6.getLimboSize());

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

		// check all message are into limbo
		assertEquals(1, r1.getLimboSize());
		assertEquals(1, r2.getLimboSize());
		assertEquals(1, r3.getLimboSize());
		assertEquals(1, r4.getLimboSize());
		assertEquals(1, r5.getLimboSize());
		assertEquals(1, r6.getLimboSize());

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

		// check all message are into limbo
		assertEquals(1, r1.getLimboSize());
		assertEquals(1, r2.getLimboSize());
		assertEquals(1, r3.getLimboSize());
		assertEquals(1, r4.getLimboSize());
		assertEquals(1, r5.getLimboSize());
		assertEquals(1, r6.getLimboSize());

	}

	/**
	 * TEST 6 Contact plan provided same as
	 * {@link ContactGraphRouterTest#testRouting1()}. But no contacts actually
	 * happen. Messages should be moved into the limbo after the route expires
	 */
	public void testRouting6() {

		String cp_path = (new File(CONTACT_PLAN_FILE)).getAbsolutePath();
		r1.readContactPlan(cp_path);
		r2.readContactPlan(cp_path);
		r3.readContactPlan(cp_path);
		r4.readContactPlan(cp_path);
		r5.readContactPlan(cp_path);
		r6.readContactPlan(cp_path);

		// test visuale
		r3.processLine("l range");
		r3.processLine("l contact");

		// message ttl has extended to 4000 sec
		Message m1 = new Message(h1, h2, msgId1, 10);
		h1.createNewMessage(m1);
		m1.setTtl(4000);
		Message m2 = new Message(h2, h3, msgId2, 10);
		h2.createNewMessage(m2);
		m2.setTtl(4000);
		Message m3 = new Message(h3, h4, msgId3, 10);
		h3.createNewMessage(m3);
		m3.setTtl(4000);
		Message m4 = new Message(h4, h5, msgId4, 10);
		h4.createNewMessage(m4);
		m4.setTtl(4000);
		Message m5 = new Message(h5, h6, msgId5, 10);
		h5.createNewMessage(m5);
		m5.setTtl(4000);
		Message m6 = new Message(h6, h1, "pippo", 10);
		h6.createNewMessage(m6);
		m6.setTtl(4000);
		checkCreates(6);

		updateAllNodes();

		// check if messages have been enqueued into the right outducts
		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 1);
		assertEquals(r2.getOutducts()[h3.getAddress()].getQueue().size(), 1);
		assertEquals(r3.getOutducts()[h4.getAddress()].getQueue().size(), 1);
		assertEquals(r4.getOutducts()[h5.getAddress()].getQueue().size(), 1);
		assertEquals(r5.getOutducts()[h6.getAddress()].getQueue().size(), 1);
		assertEquals(r6.getOutducts()[h1.getAddress()].getQueue().size(), 1);

		// nothing happen, fast forward to the end of the presumed contact
		clock.advance(3601);

		updateAllNodes();

		// check if messages have been moved to limbo
		assertEquals(1, r1.getLimboSize());
		assertEquals(1, r2.getLimboSize());
		assertEquals(1, r3.getLimboSize());
		assertEquals(1, r4.getLimboSize());
		assertEquals(1, r5.getLimboSize());
		assertEquals(1, r6.getLimboSize());

		// fast forward to the end of messages lifetime
		clock.advance(400);
		updateAllNodes();

		// check if messages have been discarded
		assertEquals(0, r1.getNrofMessages());
		assertEquals(0, r2.getNrofMessages());
		assertEquals(0, r3.getNrofMessages());
		assertEquals(0, r4.getNrofMessages());
		assertEquals(0, r5.getNrofMessages());
		assertEquals(0, r6.getNrofMessages());

		assertEquals(false, r2.isDeliveredMessage(m1));

	}

	/**
	 * TEST 7 Multiple possible routes for some bundles, cgr should choose the
	 * best one
	 */
	public void testRouting7() {
		String cp_path = (new File(CONTACT_PLAN_TEST7)).getAbsolutePath();
		r1.readContactPlan(cp_path);
		r2.readContactPlan(cp_path);
		r3.readContactPlan(cp_path);
		r4.readContactPlan(cp_path);
		r5.readContactPlan(cp_path);
		r6.readContactPlan(cp_path);

		Message m1 = new Message(h1, h6, msgId1, 10);
		h1.createNewMessage(m1);
		Message m2 = new Message(h1, h6, msgId2, 10);
		h1.createNewMessage(m2);
		Message m3 = new Message(h1, h6, msgId3, 10);
		h1.createNewMessage(m3);
		Message m4 = new Message(h1, h5, msgId4, 10);
		h1.createNewMessage(m4);
		Message m5 = new Message(h3, h6, msgId5, 10);
		h3.createNewMessage(m5);
		Message m6 = new Message(h2, h5, "pippo", 10);
		h2.createNewMessage(m6);
		checkCreates(6);

		updateAllNodes();

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 3);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 1);
		assertEquals(r2.getOutducts()[h1.getAddress()].getQueue().size(), 1);
		assertEquals(r3.getOutducts()[h1.getAddress()].getQueue().size(), 1);

		clock.advance(10);
		// 1st round contacts 10-30

		h1.forceConnection(h2, null, true);
		h1.forceConnection(h3, null, true);

		for (int i = 0; i < 20; i++) {
			clock.advance(1);
			updateAllNodes();
		}
		// inserisco la disconnect

		disconnect(h1);
		disconnect(h2);
		disconnect(h3);

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h5.getAddress()].getQueue().size(), 2);
		assertEquals(r2.getOutducts()[h4.getAddress()].getQueue().size(), 4);

		// no messages delivered
		// 2nd round contacts 40-70 and 50-80

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h2.forceConnection(h4, null, true);

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h3.forceConnection(h5, null, true);

		for (int i = 0; i < 20; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h2);
		disconnect(h4);

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h3);
		disconnect(h5);

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h6.getAddress()].getQueue().size(), 4);

		assertEquals(true, r5.isDeliveredMessage(m6));
		assertEquals(true, r5.isDeliveredMessage(m4));

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h4.forceConnection(h6, null, true);

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h6);
		disconnect(h4);

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h6.getAddress()].getQueue().size(), 0);

		assertEquals(true, r6.isDeliveredMessage(m1));
		assertEquals(true, r6.isDeliveredMessage(m2));
		assertEquals(true, r6.isDeliveredMessage(m3));
		assertEquals(true, r6.isDeliveredMessage(m5));

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h6.forceConnection(h5, null, true);

		for (int i = 0; i < 10; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h6);
		disconnect(h5);

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h6.getAddress()].getQueue().size(), 0);

		for (int i = 0; i < 30; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		h5.forceConnection(h2, null, true);

		for (int i = 0; i < 30; i++) {
			clock.advance(1);
			updateAllNodes();
		}

		disconnect(h5);
		disconnect(h2);

		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 0);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r2.getOutducts()[h1.getAddress()].getQueue().size(), 0);
		assertEquals(r3.getOutducts()[h5.getAddress()].getQueue().size(), 0);
		assertEquals(r4.getOutducts()[h6.getAddress()].getQueue().size(), 0);

	}

	/**
	 * TEST 8 MEMORY TEST No contact plan provided, nodes generate many message.
	 */
	public void testRouting8() {
		Message m;
		int i, r, count = 0;
		DTNHost h;
		Random rand = new Random();
		disconnect(h1);
		int duration = 10000;
		int perc = 0;
		for (i = 0; i < duration; i++) {
			if (i % 10 == 0) // 1 message create every simulated second
			{
				r = Math.abs(rand.nextInt());
				h = Utils.getHostFromNumber((count % 5) + 1);
				m = new Message(h, getNodeFromNbr((r % 5) + 1), "MSG_" + count, 10);
				h.createNewMessage(m);
				count++;
			}
			updateAllNodes();
			clock.advance(0.1);
			if (i % (duration / 100) == 0) {
				System.out.println();
				System.out.print("" + ++perc + "%");
			}
			if (i % (duration / 1000) == 0)
				System.out.print(".");
		}

		// useless assert. I just want to see how the memory is doing
		assertEquals("VACAGHER", "VACAGHER");
	}

	/**
	 * TEST 9 ASMS TESTS CGR - ETO
	 */

	public void testRoutingASMS_Fig4() {
		String cp_path = (new File(CONTACT_PLAN_ASMS_FIG4)).getAbsolutePath();
		r1.readContactPlan(cp_path);
		r2.readContactPlan(cp_path);
		r3.readContactPlan(cp_path);
		r4.readContactPlan(cp_path);

		Message m1 = new Message(h1, h4, "Messaggio 1", 100000);
		h1.createNewMessage(m1);
		Message m2 = new Message(h1, h4, "Messaggio 2", 100000);
		h1.createNewMessage(m2);
		Message m3 = new Message(h1, h4, "Messaggio 3", 100000);
		h1.createNewMessage(m3);
		Message m4 = new Message(h1, h4, "Messaggio 4", 100000);
		h1.createNewMessage(m4);
		Message m5 = new Message(h1, h4, "Messaggio 5", 100000);
		h1.createNewMessage(m5);
		Message m6 = new Message(h1, h4, "Messaggio 6", 100000);
		h1.createNewMessage(m6);
		Message m7 = new Message(h1, h4, "Messaggio 7", 100000);
		h1.createNewMessage(m7);
		Message m8 = new Message(h1, h4, "Messaggio 8", 100000);
		h1.createNewMessage(m8);
		Message m9 = new Message(h1, h4, "Messaggio 9", 100000);
		h1.createNewMessage(m9);
		Message m10 = new Message(h1, h4, "Messaggio 10", 100000);
		h1.createNewMessage(m10);
		Message m11 = new Message(h1, h4, "Messaggio 11", 100000);
		h1.createNewMessage(m11);
		Message m12 = new Message(h1, h4, "Messaggio 12", 100000);
		h1.createNewMessage(m12);
		Message m13 = new Message(h1, h4, "Messaggio 13", 100000);
		h1.createNewMessage(m13);
		Message m14 = new Message(h1, h4, "Messaggio 14", 100000);
		h1.createNewMessage(m14);
		Message m15 = new Message(h1, h4, "Messaggio 15", 100000);
		h1.createNewMessage(m15);
		Message m16 = new Message(h1, h4, "Messaggio 16", 100000);
		h1.createNewMessage(m16);

		checkCreates(16);

		updateAllNodes();
		h2.forceConnection(h4, null, true);
		h3.forceConnection(h4, null, true);
		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 8);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 8);

		clock.advance(29);

		h1.forceConnection(h2, null, true);
		updateAllNodes();

		clock.advance(1);

		h1.forceConnection(h3, null, true);

		for (int i = 0; i < 436; i++) {
			updateAllNodes();
			clock.advance(0.25);
		}

		clock.advance(1);

		clock.advance(1);

		disconnect(h1);
		disconnect(h2);
		disconnect(h3);
		disconnect(h4);

		assertEquals(true, r4.isDeliveredMessage(m1));

		assertEquals(true, r4.isDeliveredMessage(m2));

		assertEquals(true, r4.isDeliveredMessage(m3));

		assertEquals(true, r4.isDeliveredMessage(m4));

		assertEquals(true, r4.isDeliveredMessage(m5));

		assertEquals(true, r4.isDeliveredMessage(m6));

		assertEquals(true, r4.isDeliveredMessage(m7));

		assertEquals(true, r4.isDeliveredMessage(m8));

		assertEquals(true, r4.isDeliveredMessage(m9));

		assertEquals(true, r4.isDeliveredMessage(m10));

		assertEquals(true, r4.isDeliveredMessage(m11));

		assertEquals(true, r4.isDeliveredMessage(m12));

		assertEquals(true, r4.isDeliveredMessage(m13));

		assertEquals(true, r4.isDeliveredMessage(m14));

		assertEquals(true, r4.isDeliveredMessage(m15));

		assertEquals(true, r4.isDeliveredMessage(m16));

	}

	public void testRoutingASMS_Fig6() {
		String cp_path = (new File(CONTACT_PLAN_ASMS_FIG6)).getAbsolutePath();
		r1.readContactPlan(cp_path);
		r2.readContactPlan(cp_path);
		r3.readContactPlan(cp_path);
		r4.readContactPlan(cp_path);

		Message m1 = new Message(h1, h4, "Messaggio 1", 100000);
		h1.createNewMessage(m1);
		Message m2 = new Message(h1, h4, "Messaggio 2", 100000);
		h1.createNewMessage(m2);
		Message m3 = new Message(h1, h4, "Messaggio 3", 100000);
		h1.createNewMessage(m3);
		Message m4 = new Message(h1, h4, "Messaggio 4", 100000);
		h1.createNewMessage(m4);
		Message m5 = new Message(h1, h4, "Messaggio 5", 100000);
		h1.createNewMessage(m5);
		Message m6 = new Message(h1, h4, "Messaggio 6", 100000);
		h1.createNewMessage(m6);
		Message m7 = new Message(h1, h4, "Messaggio 7", 100000);
		h1.createNewMessage(m7);
		Message m8 = new Message(h1, h4, "Messaggio 8", 100000);
		h1.createNewMessage(m8);
		Message m9 = new Message(h1, h4, "Messaggio 9", 100000);
		h1.createNewMessage(m9);
		Message m10 = new Message(h1, h4, "Messaggio 10", 100000);
		h1.createNewMessage(m10);
		Message m11 = new Message(h1, h4, "Messaggio 11", 100000);
		h1.createNewMessage(m11);
		Message m12 = new Message(h1, h4, "Messaggio 12", 100000);
		h1.createNewMessage(m12);
		Message m13 = new Message(h1, h4, "Messaggio 13", 100000);
		h1.createNewMessage(m13);
		Message m14 = new Message(h1, h4, "Messaggio 14", 100000);
		h1.createNewMessage(m14);
		// Message m15 = new Message(h1,h4, "Messaggio 15", 100000);
		// h1.createNewMessage(m15);
		// Message m16 = new Message(h1,h4, "Messaggio 16", 100000);
		// h1.createNewMessage(m16);

		checkCreates(14);

		updateAllNodes();
		h2.forceConnection(h4, null, true);
		h3.forceConnection(h4, null, true);
		assertEquals(r1.getOutducts()[h2.getAddress()].getQueue().size(), 3);
		assertEquals(r1.getOutducts()[h3.getAddress()].getQueue().size(), 11);

		clock.advance(30);

		h1.forceConnection(h3, null, true);
		updateAllNodes();

		for (int i = 0; i < 120; i++) {
			updateAllNodes();
			clock.advance(0.25);
		}

		h1.forceConnection(h2, null, true);

		for (int i = 0; i < 60; i++) {
			updateAllNodes();
			clock.advance(0.25);
		}

		h1.forceConnection(h2, null, false);

		for (int i = 0; i < 60; i++) {
			updateAllNodes();
			clock.advance(0.25);
		}

		h1.forceConnection(h3, null, false);

		for (int i = 0; i < 60; i++) {
			updateAllNodes();
			clock.advance(0.25);
		}

		h1.forceConnection(h3, null, true);

		for (int i = 0; i < 120; i++) {
			updateAllNodes();
			clock.advance(0.25);
		}

		h1.forceConnection(h3, null, false);

		disconnect(h1);
		disconnect(h2);
		disconnect(h3);
		disconnect(h4);

		assertEquals(true, r4.isDeliveredMessage(m1));

		assertEquals(true, r4.isDeliveredMessage(m2));

		assertEquals(true, r4.isDeliveredMessage(m3));

		assertEquals(true, r4.isDeliveredMessage(m4));

		assertEquals(true, r4.isDeliveredMessage(m5));

		assertEquals(true, r4.isDeliveredMessage(m6));

		assertEquals(true, r4.isDeliveredMessage(m7));

		assertEquals(true, r4.isDeliveredMessage(m8));

		assertEquals(true, r4.isDeliveredMessage(m9));

		assertEquals(true, r4.isDeliveredMessage(m10));

		assertEquals(true, r4.isDeliveredMessage(m11));

		assertEquals(true, r4.isDeliveredMessage(m12));

		assertEquals(true, r4.isDeliveredMessage(m13));

		assertEquals(true, r4.isDeliveredMessage(m14));

		// assertEquals(true, r4.isDeliveredMessage(m15));

		// assertEquals(true, r4.isDeliveredMessage(m16));

	}

	protected void testWait(double sec, double gran) {
		sec = sec / gran;
		for (int i = 0; i < sec; i++) {
			clock.advance(gran);
			updateAllNodes();
		}
	}

	public static ContactGraphRouterTest getInstance() {
		return instance;
	}

	public Collection<DTNHost> getHosts() {
		return utils.getAllHosts();
	}

	public DTNHost getNodeFromNbr(long nodeNbr) {
		return Utils.getHostFromNumber(nodeNbr);
	}

}
