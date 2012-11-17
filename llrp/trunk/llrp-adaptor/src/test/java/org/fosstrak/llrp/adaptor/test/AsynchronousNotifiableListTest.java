/*
 *  
 *  Fosstrak LLRP Commander (www.fosstrak.org)
 * 
 *  Copyright (C) 2008 ETH Zurich
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/> 
 *
 */

package org.fosstrak.llrp.adaptor.test;

import java.io.FileReader;
import java.rmi.RemoteException;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.fosstrak.llrp.adaptor.AsynchronousNotifiable;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.adaptor.util.AsynchronousNotifiableList;
import org.fosstrak.llrp.adaptor.util.type.NotifiableFailureCounter;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.junit.Test;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.types.LLRPMessage;


/**
 * tests {@link AsynchronousNotifiableList}.
 * @author swieland
 *
 */
public class AsynchronousNotifiableListTest {	
	
	/**
	 * tests if registration and deregistration works as expected.
	 * @throws Exception upon error...
	 */
	@Test
	public void testRegister() throws Exception {
		AsynchronousNotifiableList an = new AsynchronousNotifiableList();
		
		// test 1
		Assert.assertEquals(0, an.getAll().size());
		
		// test 2
		AsynchronousNotifiable asynchronousNotifiable = EasyMock.createMock(AsynchronousNotifiable.class);
		
		EasyMock.replay(asynchronousNotifiable);
		
		an.add(asynchronousNotifiable);
		Assert.assertEquals(1, an.getAll().size());
		an.remove(asynchronousNotifiable);
		Assert.assertEquals(0, an.getAll().size());
		EasyMock.verify(asynchronousNotifiable);
		
		// test 3
		AsynchronousNotifiable a1 = EasyMock.createMock(AsynchronousNotifiable.class);
		AsynchronousNotifiable a2 = EasyMock.createMock(AsynchronousNotifiable.class);
		EasyMock.replay(a1);
		EasyMock.replay(a2);
		
		an.add(asynchronousNotifiable);
		an.add(a1);
		an.add(a2);
		Assert.assertEquals(3, an.getAll().size());
		an.remove(a1);
		Assert.assertEquals(2, an.getAll().size());
		
		EasyMock.verify(a1);
		EasyMock.verify(a2);
	}
	
	@Test
	public void testNotifyAndRemove() throws Exception {
		// just use some dummy method for test of the callback.
		Document document = new SAXBuilder().build(new FileReader("src/test/config/getCapabilities.xml"));

		final LLRPRuntimeException ex = new LLRPRuntimeException("MOCK EXCEPTION");
		final LLRPMessage message = LLRPMessageFactory.createLLRPMessage(document);
		byte[] messageEncodedBinary = message.encodeBinary();
		final String readerName = "reader";
		AsynchronousNotifiableList an = new AsynchronousNotifiableList();
		
		AsynchronousNotifiable a1 = EasyMock.createMock(AsynchronousNotifiable.class);
		a1.notify(messageEncodedBinary, readerName);
		EasyMock.expectLastCall();
		a1.notifyError(ex, readerName);
		EasyMock.expectLastCall().andThrow(new RemoteException("MOCK EXCEPTION"));
		AsynchronousNotifiable a2 = EasyMock.createMock(AsynchronousNotifiable.class);
		a2.notify(messageEncodedBinary, readerName);
		EasyMock.expectLastCall();
		a2.notifyError(ex, readerName);
		EasyMock.expectLastCall().andThrow(new RemoteException("MOCK EXCEPTION"));
		
		EasyMock.replay(a1);
		EasyMock.replay(a2);
		an.add(a1);
		an.add(a2);
		
		an.notify(messageEncodedBinary, readerName);
		an.notifyError(ex, readerName);
		
		EasyMock.verify(a1);
		EasyMock.verify(a2);
		
		an.remove(a2);
		// reset a1 as only a1 is allowed to get the notify (a2 is removed)
		EasyMock.reset(a1);
		a1.notify(messageEncodedBinary, readerName);
		EasyMock.expectLastCall();
		a1.notifyError(ex, readerName);
		EasyMock.expectLastCall();
		
		EasyMock.replay(a1);
		
		an.notify(messageEncodedBinary, readerName);
		an.notifyError(ex, readerName);
		
		EasyMock.verify(a1);
	}
	
	@Test
	public void testNotifyWithException() throws Exception {
		// just use some dummy method for test of the callback.
		Document document = new SAXBuilder().build(new FileReader("src/test/config/getCapabilities.xml"));
		
		final LLRPRuntimeException ex = new LLRPRuntimeException("MOCK EXCEPTION");
		final LLRPMessage message = LLRPMessageFactory.createLLRPMessage(document);
		byte[] messageEncodedBinary = message.encodeBinary();
		final String readerName = "reader";
		AsynchronousNotifiableList an = new AsynchronousNotifiableList();
		
		AsynchronousNotifiable a1 = EasyMock.createMock(AsynchronousNotifiable.class);
		a1.notify(messageEncodedBinary, readerName);
		EasyMock.expectLastCall().andThrow(new RemoteException("MOCK EXCEPTION"));
		a1.notifyError(ex, readerName);
		EasyMock.expectLastCall().andThrow(new RemoteException("MOCK EXCEPTION"));
		
		EasyMock.replay(a1);
		
		an.add(a1);
		Assert.assertFalse(an.getAll().toArray(new NotifiableFailureCounter[0])[0].hasError());
		
		an.notify(messageEncodedBinary, readerName);
		Assert.assertTrue(an.getAll().toArray(new NotifiableFailureCounter[0])[0].hasError());
		
		an.getAll().toArray(new NotifiableFailureCounter[0])[0].clean();
		Assert.assertFalse(an.getAll().toArray(new NotifiableFailureCounter[0])[0].hasError());
		
		an.notifyError(ex, readerName);
		Assert.assertTrue(an.getAll().toArray(new NotifiableFailureCounter[0])[0].hasError());
		
		EasyMock.verify(a1);
	}
	
	@Test
	public void testCleanup() throws Exception {
		final LLRPRuntimeException ex = new LLRPRuntimeException("MOCK EXCEPTION");
		final String readerName = "reader";
		AsynchronousNotifiableList an = new AsynchronousNotifiableList();
		
		AsynchronousNotifiable a1 = EasyMock.createMock(AsynchronousNotifiable.class);
		a1.notifyError(ex, readerName);
		EasyMock.expectLastCall().andThrow(new RemoteException("MOCK EXCEPTION")).atLeastOnce();
		
		EasyMock.replay(a1);
		
		an.add(a1);
		for (int i = 0; i<NotifiableFailureCounter.NUM_NON_RECHABLE_ALLOWED + 1; i++) {
			an.notifyError(ex, readerName);
		}
		Assert.assertEquals(0, an.getAll().size());
		
		EasyMock.verify(a1);
	}
}
