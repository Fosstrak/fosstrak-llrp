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

package org.fosstrak.llrp.adaptor.util;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import org.fosstrak.llrp.adaptor.AsynchronousNotifiable;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.adaptor.util.type.NotifiableFailureCounter;

/**
 * helper class to maintain a list of asynchronous message receivers. the 
 * helper checks whether there are transmission exception, and if so then the 
 * respective receivers get dropped after a certain number of erroneous 
 * transmissions.
 * <br/>
 * <br/>
 * the implementation is thread safe.
 * @author swieland
 *
 */
public class AsynchronousNotifiableList implements AsynchronousNotifiable {
	
	/**
	 * SID.
	 */
	private static final long serialVersionUID = 1L;

	/** a list with all the receivers of asynchronous messages. */
	private Set<NotifiableFailureCounter> receivers = new ConcurrentSkipListSet<NotifiableFailureCounter>();
	
	/**
	 * add a new receiver to the list.
	 * @param entry the new receiver to be stored in the list.
	 */
	public void add(AsynchronousNotifiable entry) {
		receivers.add(new NotifiableFailureCounter(entry));
	}
	
	/**
	 * removes a receiver from the list.
	 * @param entry the receiver to be removed.
	 */
	public void remove(AsynchronousNotifiable entry) {
		for (NotifiableFailureCounter r : receivers) {
			if (r.getReceiver().equals(entry)) {
				receivers.remove(r);
			}
		}
	}
	
	/**
	 * removes all the erroneous receivers from the list.
	 */
	private synchronized void cleanup() {
		List<NotifiableFailureCounter> aboveThreshold = new LinkedList<NotifiableFailureCounter> ();
		for (NotifiableFailureCounter counter : receivers) {
			if (counter.isAboveThreshold()) {
				aboveThreshold.add(counter);
			}
		}
		
		for (NotifiableFailureCounter counter : aboveThreshold) {
			receivers.remove(counter);
		}
	}

	/**
	 * notify all the receivers with a new message.
	 * @param message the LLRP message.
	 * @param readerName the reader that delivered the message.
	 * @throws RemoteException when there is an RMI exception.
	 */
	public void notify(byte[] message, String readerName) throws RemoteException {
		
		for (NotifiableFailureCounter receiver : receivers) {
			try {
				receiver.getReceiver().notify(message, readerName);
				// if notified successfully, clean the error counter.
				receiver.clean();
			} catch (RemoteException e) {
				receiver.error();
			}
		}
		// run the cleanup routine.
		cleanup();
	}

	/**
	 * notify all the receivers about an exception in the reader module.
	 * @param e the exception.
	 * @param readerName the reader that delivered the exception.
	 * @throws RemoteException when there is an RMI exception.
	 */
	public void notifyError(LLRPRuntimeException e, String readerName) throws RemoteException {
		for (NotifiableFailureCounter receiver : receivers) {
			try {
				receiver.getReceiver().notifyError(e, readerName);
				// if notified successfully, clean the error counter.
				receiver.clean();
			} catch (RemoteException ex) {
				receiver.error();
			}
		}
		// run the cleanup routine.
		cleanup();
	}

	/**
	 * @return a set holding all the registered receivers.
	 */
	public Set<NotifiableFailureCounter> getAll() {
		return receivers;
	}
}
