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

package org.fosstrak.llrp.client.test;

import junit.framework.Assert;

import org.fosstrak.llrp.client.Reader;
import org.junit.Test;

/**
 * test the client reader wrapper.
 * @author swieland
 *
 */
public class ReaderTest {
	
	@Test
	public void testReaderWrapper() {
		final String adapterName = "adapterName";
		final String readerName = "readerName";
		final String readerIp = "readerIp";
		final int port = 5000;
		
		Reader reader = new Reader();
		reader.setAdapterName(adapterName);
		reader.setIp(readerIp);
		reader.setName(readerName);
		reader.setPort(port);
		
		Assert.assertEquals(adapterName, reader.getAdapterName());
		Assert.assertEquals(readerName, reader.getName());
		Assert.assertEquals(readerIp, reader.getIp());
		Assert.assertEquals(port, reader.getPort());
		Assert.assertEquals(adapterName + "-" + readerName, reader.getId());
		
		Assert.assertTrue(reader.isAttachedTo(adapterName));
		Assert.assertFalse(reader.isAttachedTo(adapterName + "1"));
	}
}
