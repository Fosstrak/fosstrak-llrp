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

package org.fosstrak.llrp.adaptor.util.type;

import java.io.Serializable;

import org.fosstrak.llrp.adaptor.AsynchronousNotifiable;

/**
 * wrapper class that helps counting the errors in the asynchronous notifiable list.
 * @author swieland
 *
 */
public class NotifiableFailureCounter implements Serializable, Comparable<NotifiableFailureCounter> {
	
	/** remove the receiver after this number of unsuccessful connection attempts. */
	public static final int NUM_NON_RECHABLE_ALLOWED = 3;
	
	/**
	 * SID.
	 */
	private static final long serialVersionUID = 1L;

	// the number of errors occurred.
	private int errors = 0;
	
	// the receiver.
	private final AsynchronousNotifiable receiver;
	
	/**
	 * creates a wrapper class.
	 * @param receiver the receiver.
	 */
	public NotifiableFailureCounter(AsynchronousNotifiable receiver) {
		this.receiver = receiver;
	}
	
	/**
	 * sets the error-count to zero.
	 */
	public void clean() {
		errors = 0;
	}
	
	/**
	 * increases the error-count by one.
	 */
	public void error() {
		errors++;
	}
	
	/**
	 * true if errors have been registered.
	 * @return true if yes, false otherwise.
	 */
	public boolean hasError() {
		return errors > 0;
	}
	
	/**
	 * checks if the receiver held by this wrapper has been called more than the defined threshold.
	 * @return true if yes, false otherwise.
	 */
	public boolean isAboveThreshold() {
		return errors > NUM_NON_RECHABLE_ALLOWED;
	}

	/**
	 * @return the receiver of this helper.
	 */
	public AsynchronousNotifiable getReceiver() {
		return receiver;
	}

	@Override
	public int compareTo(NotifiableFailureCounter other) {
		if (receiver == other.receiver) {
			return 0;
		}
		return 1;
	}
}
