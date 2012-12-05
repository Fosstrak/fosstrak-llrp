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

package util.test;

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
