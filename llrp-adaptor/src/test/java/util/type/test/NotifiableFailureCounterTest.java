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
package util.type.test;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.fosstrak.llrp.adaptor.AsynchronousNotifiable;
import org.fosstrak.llrp.adaptor.util.type.NotifiableFailureCounter;
import org.junit.Test;

/**
 * test the counting wrapper.
 * @author swieland
 *
 */
public class NotifiableFailureCounterTest {
	
	@Test
	public void testWrapping() {
		AsynchronousNotifiable notif = EasyMock.createMock(AsynchronousNotifiable.class);
		EasyMock.replay(notif);
		
		NotifiableFailureCounter counter = new NotifiableFailureCounter(notif);
		Assert.assertEquals(notif, counter.getReceiver());
		
		EasyMock.verify(notif);
	}
	
	@Test
	public void testCounting() {
		NotifiableFailureCounter counter = new NotifiableFailureCounter(null);
		Assert.assertFalse(counter.isAboveThreshold());
		for (int i = 0; i<NotifiableFailureCounter.NUM_NON_RECHABLE_ALLOWED + 1; i++) {
			counter.error();
		}
		Assert.assertTrue(counter.isAboveThreshold());
		counter.clean();
		Assert.assertFalse(counter.isAboveThreshold());
	}

	@Test
	public void testComparable() {
		AsynchronousNotifiable notif = EasyMock.createMock(AsynchronousNotifiable.class);
		AsynchronousNotifiable notif2 = EasyMock.createMock(AsynchronousNotifiable.class);
		EasyMock.replay(notif);
		EasyMock.replay(notif2);
		
		NotifiableFailureCounter counter = new NotifiableFailureCounter(notif);
		NotifiableFailureCounter counter2 = new NotifiableFailureCounter(notif);
		NotifiableFailureCounter counter3 = new NotifiableFailureCounter(notif2);
		Assert.assertEquals(0, counter.compareTo(counter2));
		Assert.assertEquals(0, counter2.compareTo(counter));

		Assert.assertEquals(1, counter.compareTo(counter3));
		Assert.assertEquals(1, counter3.compareTo(counter));
		
		EasyMock.verify(notif);
		EasyMock.verify(notif2);
	}
}
