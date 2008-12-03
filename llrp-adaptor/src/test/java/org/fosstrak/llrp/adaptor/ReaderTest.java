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

import org.fosstrak.llrp.adaptor.Adaptor;
import org.fosstrak.llrp.adaptor.AdaptorImpl;
import org.fosstrak.llrp.adaptor.Reader;
import org.fosstrak.llrp.adaptor.ReaderImpl;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.types.LLRPMessage;

import util.AsyncNotif;
import junit.framework.TestCase;

public class ReaderTest extends TestCase {
	
	private String readerName = "readerName";
	private String readerAddress = "localhost";
	private String adaptorName = "adaptorName";
	
	public void testSetterAndGetterAndConstructor() throws Exception {
		Reader reader = new ReaderImpl(null, readerName, readerAddress);
		reader.setKeepAlivePeriod(10000, 10, false, false);
		assertEquals(readerName, reader.getReaderName());
		assertEquals(readerAddress, reader.getReaderAddress());
		
		reader = new ReaderImpl(null, readerName, readerAddress, 1234);
		reader.setKeepAlivePeriod(10000, 10, false, false);
		assertEquals(readerName, reader.getReaderName());
		assertEquals(readerAddress, reader.getReaderAddress());
		assertEquals(1234, reader.getPort());
	}
	
	public void testAsynchronousNotification() throws Exception {
		AsyncNotif notif = new AsyncNotif(false);
		Adaptor adaptor = new AdaptorImpl(adaptorName);
		
		Reader reader = new ReaderImpl(adaptor, readerName, readerAddress);
		reader.setKeepAlivePeriod(10000, 10, false, false);
		// register
		reader.registerForAsynchronous(notif);

		// just use some dummy method for test of the callback.
		Document document = new SAXBuilder().build(new FileReader("src/test/config/getCapabilities.xml"));
		LLRPMessage message = LLRPMessageFactory.createLLRPMessage(document);
		
		// run the callback test.
		notif.asyncNotifMessage = null;
		notif.asyncNotifReaderName = null;
		((ReaderImpl)reader).messageReceived(message);
		assertNotNull(notif.asyncNotifMessage);
		assertTrue(notif.asyncNotifMessage instanceof LLRPMessage);
		
		assertNotNull(notif.asyncNotifReaderName);
		assertEquals(readerName, notif.asyncNotifReaderName);
		// deregister
 		reader.deregisterFromAsynchronous(notif);
		
		notif.asyncNotifMessage = null;
		notif.asyncNotifReaderName = null;
		((ReaderImpl)reader).messageReceived(message);
		assertNull(notif.asyncNotifMessage);
		assertNull(notif.asyncNotifReaderName);
	}
}
