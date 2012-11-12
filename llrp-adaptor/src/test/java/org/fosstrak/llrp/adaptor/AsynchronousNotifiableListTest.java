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

package org.fosstrak.llrp.adaptor;

import java.io.FileReader;
import java.rmi.RemoteException;

import junit.framework.Assert;

import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.adaptor.util.AsynchronousNotifiableList;
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
	
	public static final byte[][] msgs = new byte[10][];
	public static final String[] names = new String[10];
	
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
		AsynchronousNotifiable asynchronousNotifiable = new AsynchronousNotifiable() {

			public void notify(byte[] message, String readerName)
					throws RemoteException {
			}

			public void notifyError(LLRPRuntimeException e, String readerName)
					throws RemoteException {
			}
			
		};
		
		an.add(asynchronousNotifiable);
		Assert.assertEquals(1, an.getAll().size());
		an.remove(asynchronousNotifiable);
		Assert.assertEquals(0, an.getAll().size());
		
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
		Assert.assertEquals(3, an.getAll().size());
		an.remove(a1);
		Assert.assertEquals(2, an.getAll().size());
		
	}
	
	@Test
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
		
		Assert.assertNotNull(msgs[0]);
		Assert.assertEquals(readerName, names[0]);
		Assert.assertNotNull(msgs[1]);
		Assert.assertEquals(readerName, names[1]);
		
		msgs[0] = msgs[1] = null;
		names[0] = names[1] = null;
		
		an.remove(a2);
		an.notify(message.encodeBinary(), readerName);
		Assert.assertNotNull(msgs[0]);
		Assert.assertEquals(readerName, names[0]);
		Assert.assertNull(msgs[1]);
		Assert.assertNull(names[1]);
		
		msgs[0] = msgs[1] = null;
		names[0] = names[1] = null;
		an.remove(a1);
		an.notify(message.encodeBinary(), readerName);
		Assert.assertNull(msgs[0]);
		Assert.assertNull(names[0]);
		Assert.assertNull(msgs[1]);
		Assert.assertNull(names[1]);
	}
}
