/*
 * Copyright (C) 2007 ETH Zurich
 *
 * This file is part of Fosstrak (www.fosstrak.org).
 *
 * Fosstrak is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * Fosstrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Fosstrak; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */
package org.fosstrak.llrp.adaptor;

import java.io.FileReader;
import java.rmi.RemoteException;

import junit.framework.TestCase;

import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.adaptor.util.AsynchronousNotifiableList;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.types.LLRPMessage;


/**
 * tests {@link AsynchronousNotifiableList}.
 * @author sawielan
 *
 */
public class AsynchronousNotifiableListTest extends TestCase {	
	
	public static final byte[][] msgs = new byte[10][];
	public static final String[] names = new String[10];
	
	/**
	 * tests if registration and deregistration works as expected.
	 * @throws Exception upon error...
	 */
	public void testRegister() throws Exception {
		AsynchronousNotifiableList an = new AsynchronousNotifiableList();
		
		// test 1
		assertEquals(0, an.getAll().size());
		
		// test 2
		AsynchronousNotifiable asynchronousNotifiable = new AsynchronousNotifiable() {

			public void notify(byte[] message, String readerName)
					throws RemoteException {
			}

			public void notifyError(LLRPRuntimeException e, String readerName)
					throws RemoteException {
			}
			
		};
		
		an.add(asynchronousNotifiable);
		assertEquals(1, an.getAll().size());
		an.remove(asynchronousNotifiable);
		assertEquals(0, an.getAll().size());
		
		// test 3
		AsynchronousNotifiable a1 = new AsynchronousNotifiable() {

			public void notify(byte[] message, String readerName)
					throws RemoteException {
			}

			public void notifyError(LLRPRuntimeException e, String readerName)
					throws RemoteException {
			}
			
		};
		
		AsynchronousNotifiable a2 = new AsynchronousNotifiable() {

			public void notify(byte[] message, String readerName)
					throws RemoteException {
			}

			public void notifyError(LLRPRuntimeException e, String readerName)
					throws RemoteException {
			}
			
		};
		an.add(asynchronousNotifiable);
		an.add(a1);
		an.add(a2);
		assertEquals(3, an.getAll().size());
		an.remove(a1);
		assertEquals(2, an.getAll().size());
		
	}
	
	public void testNotify() throws Exception {
		AsynchronousNotifiableList an = new AsynchronousNotifiableList();
		AsynchronousNotifiable a1 = new AsynchronousNotifiable() {

			public void notify(byte[] message, String readerName)
					throws RemoteException {
				msgs[0] = message;
				names[0] = readerName;
			}

			public void notifyError(LLRPRuntimeException e, String readerName)
					throws RemoteException {
			}
			
		};
		
		AsynchronousNotifiable a2 = new AsynchronousNotifiable() {

			public void notify(byte[] message, String readerName)
					throws RemoteException {
				msgs[1] = message;
				names[1] = readerName;
			}

			public void notifyError(LLRPRuntimeException e, String readerName)
					throws RemoteException {
			}
			
		};
		
		an.add(a1);
		an.add(a2);
		
		// just use some dummy method for test of the callback.
		Document document = new SAXBuilder().build(new FileReader("src/test/config/getCapabilities.xml"));
		
		LLRPMessage message = LLRPMessageFactory.createLLRPMessage(document);
		String readerName = "reader";
		
		msgs[0] = msgs[1] = null;
		names[0] = names[1] = null;
		
		an.notify(message.encodeBinary(), readerName);
		
		assertNotNull(msgs[0]);
		assertEquals(readerName, names[0]);
		assertNotNull(msgs[1]);
		assertEquals(readerName, names[1]);
		
		msgs[0] = msgs[1] = null;
		names[0] = names[1] = null;
		
		an.remove(a2);
		an.notify(message.encodeBinary(), readerName);
		assertNotNull(msgs[0]);
		assertEquals(readerName, names[0]);
		assertNull(msgs[1]);
		assertNull(names[1]);
		
		msgs[0] = msgs[1] = null;
		names[0] = names[1] = null;
		an.remove(a1);
		an.notify(message.encodeBinary(), readerName);
		assertNull(msgs[0]);
		assertNull(names[0]);
		assertNull(msgs[1]);
		assertNull(names[1]);
	}
}
