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

package org.fosstrak.llrp.client.repository.sql.roaccess.test;

import java.sql.Timestamp;

import junit.framework.Assert;

import org.fosstrak.llrp.client.repository.sql.roaccess.AbstractSQLROAccessReportsRepository;
import org.junit.Test;
import org.llrp.ltk.types.UnsignedLong;

/**
 * test abstract ro access reports repository.
 * @author swieland
 *
 */
public class AbstractSQLROAccessReportsRepositoryTest {

	@Test
	public void testExtractTimestampNull() {
		Assert.assertNull(AbstractSQLROAccessReportsRepository.extractTimestamp(null));
	}

	@Test
	public void testExtractTimestamp() {
		long millis = 1354L;
		long nanos = 555555;
		UnsignedLong ul = new UnsignedLong(1000000*millis + nanos);
		Timestamp ts = AbstractSQLROAccessReportsRepository.extractTimestamp(ul);
		Assert.assertNotNull(ts);
		Assert.assertEquals(555555000, ts.getNanos());
		Assert.assertEquals(34, ts.getSeconds());
		Assert.assertEquals(22, ts.getMinutes());
		Assert.assertEquals(01, ts.getHours());
	}
}
