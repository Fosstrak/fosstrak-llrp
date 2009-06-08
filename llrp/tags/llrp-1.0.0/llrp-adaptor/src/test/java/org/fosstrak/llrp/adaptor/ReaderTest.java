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
	private final int readerPort = 49212;
	
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
		
		Reader reader = new ReaderImpl(adaptor, readerName, 
				readerAddress, readerPort);
		// to enable the queue-worker, we need to connect
		reader.connect(false);
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
		// we need to wait a few milliseconds to allow the worker to process
		// the message
		Thread.sleep(1000);
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
		reader.disconnect();
	}
}
