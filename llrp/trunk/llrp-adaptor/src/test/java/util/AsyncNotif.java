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

package util;

import java.rmi.RemoteException;
import java.rmi.StubNotFoundException;
import java.rmi.server.UnicastRemoteObject;

import org.fosstrak.llrp.adaptor.AsynchronousNotifiable;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.types.LLRPMessage;

/** 
 * helper class to test the asynchronous notification interface.
 * @author sawielan
 *
 */
public class AsyncNotif extends UnicastRemoteObject implements AsynchronousNotifiable {
	
	/**
	 * serial version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * creates a callback instance that retrieves asynchronous messages.
	 * @param remote true if run remotely.
	 */
	public AsyncNotif(boolean remote) throws RemoteException {
		if (remote) {
			try {
				UnicastRemoteObject.exportObject(this);
			} catch (StubNotFoundException e) {
				// this exception is normal as exportObject is backwards compatible to 
				// java 1.4. since java 1.5 the stub gets auto-generated and so 
				// there is no stub available -> exception. we can safely 
				// ignore this exception.
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
	
	public LLRPMessage asyncNotifMessage = null;
	public String asyncNotifReaderName = null;
	
	public LLRPRuntimeException asyncNotifError = null;
	public String asyncNotifErrorReader = null;
	
	public void notify(byte[] message, String readerName)
			throws RemoteException {
		
		try {
			asyncNotifMessage = LLRPMessageFactory.createLLRPMessage(message);
		} catch (InvalidLLRPMessageException e) {
			e.printStackTrace();
		}
		asyncNotifReaderName = readerName;
	}

	public void notifyError(LLRPRuntimeException e, String readerName) {
		asyncNotifError = e;
		asyncNotifErrorReader = readerName;
	}
	
}
