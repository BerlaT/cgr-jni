/*
 * Copyright 2017 University of Bologna
 * Released under GPLv3. See LICENSE.txt for details.
 */

package test;



import core.*;
import routing.MessageRouter;
import routing.PriorityEpidemicRouter;

public class PriorityEpidemicTest extends AbstractRouterTest{

	private static int TTL = 300;
	
	@Override
	public void setUp() throws Exception {
		ts.putSetting(MessageRouter.MSG_TTL_S, ""+TTL);
		ts.putSetting(MessageRouter.B_SIZE_S, ""+BUFFER_SIZE);
		setRouterProto(new PriorityEpidemicRouter(ts));
		super.setUp();
	}
	
	public void testOrder(){
		Message m1 = new PriorityMessage(h1,h2,msgId1,1,0);
		h1.createNewMessage(m1);
		Message m2 = new PriorityMessage(h1,h2,msgId2,1,1);
		h1.createNewMessage(m2);
		Message m3 = new PriorityMessage(h1,h2,msgId3,1,2);
		h1.createNewMessage(m3);
		
		assertTrue(mc.next());
		assertEquals(mc.TYPE_CREATE,mc.getLastType());
		assertTrue(mc.next());
		assertEquals(mc.TYPE_CREATE,mc.getLastType());
		assertTrue(mc.next());
		assertEquals(mc.TYPE_CREATE,mc.getLastType());
	
		h1.connect(h2);
		updateAllNodes();
		
		clock.advance(1);
		updateAllNodes();
		assertTrue(mc.next());
		assertEquals(mc.TYPE_START,mc.getLastType());
		assertEquals(msgId3,mc.getLastMsg().getId());
		
		clock.advance(1);
		updateAllNodes();
		assertTrue(mc.next());
		assertEquals(mc.getLastType(),mc.TYPE_RELAY);
		assertEquals(mc.getLastMsg().getId(),msgId3);
		
		clock.advance(1);
		updateAllNodes();
		assertTrue(mc.next());
		assertEquals(mc.getLastType(),mc.TYPE_START);
		assertEquals(mc.getLastMsg().getId(),msgId2);
		
		clock.advance(1);
		updateAllNodes();
		assertTrue(mc.next());
		assertEquals(mc.getLastType(),mc.TYPE_RELAY);
		assertEquals(mc.getLastMsg().getId(),msgId2);
		
		clock.advance(1);
		updateAllNodes();
		assertTrue(mc.next());
		assertEquals(mc.getLastType(),mc.TYPE_START);
		assertEquals(mc.getLastMsg().getId(),msgId1);
		
		clock.advance(1);
		updateAllNodes();
		assertTrue(mc.next());
		assertEquals(mc.getLastType(),mc.TYPE_RELAY);
		assertEquals(mc.getLastMsg().getId(),msgId1);
		

		clock.advance(1);
		updateAllNodes();
		assertFalse(mc.next());
	}
	
	public void testBufferSize(){
		Message m1 = new PriorityMessage(h1,h2,msgId1,25,2);
		h1.createNewMessage(m1);
		mc.next();
		updateAllNodes();
		
		Message m2 = new PriorityMessage(h1,h2,msgId2,25,1);
		h1.createNewMessage(m2);
		mc.next();
		updateAllNodes();
		
		Message m3 = new PriorityMessage(h1,h2,msgId3,25,0);
		h1.createNewMessage(m3);
		mc.next();
		updateAllNodes();
		
		clock.advance(1);
		Message m4 = new PriorityMessage(h1,h2,msgId4,25,0);
		h1.createNewMessage(m4);
		mc.next();
		updateAllNodes();
		
		Message m5 = new PriorityMessage(h1,h2,msgId5,25,1);
		h1.createNewMessage(m5);
		updateAllNodes();
		assertTrue(mc.next());
		//elimina il meno prioritario da più tempo nel buffer
		assertEquals(mc.TYPE_DELETE,mc.getLastType());
		assertEquals(msgId3,mc.getLastMsg().getId());		
	}

}
