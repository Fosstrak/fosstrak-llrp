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

package org.fosstrak.llrp.adaptor.queue.test;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.fosstrak.llrp.adaptor.queue.QueueEntry;
import org.junit.Test;
import org.llrp.ltk.types.LLRPMessage;

/**
 * test the queue entry. 
 * 
 * @author swieland
 *
 */
public class QueueEntryTest {
	
	@Test
	public void testQueueEntry() {
		final LLRPMessage message = EasyMock.createMock(LLRPMessage.class);
		final String readerName = "readerName";
		final String adaptorName = "adaptorName";
		
		QueueEntry e = new QueueEntry(message, readerName, adaptorName);
		Assert.assertEquals(message, e.getMessage());
		Assert.assertEquals(readerName, e.getReaderName());
		Assert.assertEquals(adaptorName, e.getAdaptorName());
		
		final String readerName2 = "readerName";
		final String adaptorName2 = "adaptorName";		
		e.setAdaptorName(adaptorName2);
		e.setReaderName(readerName2);
		e.setMessage(null);

		Assert.assertNull(e.getMessage());
		Assert.assertEquals(readerName2, e.getReaderName());
		Assert.assertEquals(adaptorName2, e.getAdaptorName());		
	}
}
