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

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.StubNotFoundException;
import java.rmi.server.UnicastRemoteObject;

import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.LLRPExceptionHandlerTypeMap;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.generated.parameters.LLRPStatus;
import org.llrp.ltk.types.LLRPMessage;

/**
 * creates a callback instance that retrieves asynchronous messages.
 * @author sawielan
 *
 */
public class AdaptorCallback extends UnicastRemoteObject implements AsynchronousNotifiable {
	
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 1L;
	
	/** the worker that holds this callback. */
	private AdaptorWorker worker = null;
	
	/**
	 * creates a callback instance that retrieves asynchronous messages.
	 * @param remote true if run remotely.
	 * @throws RemoteException when there is an rmi exception.
	 */
	public AdaptorCallback(boolean remote) throws RemoteException {
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
	

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.AsynchronousNotifiable#notify(byte[], java.lang.String)
	 */
	public void notify(byte[] message, String readerName) throws RemoteException {
		try {
			// create the llrp message 
			LLRPMessage llrpMessage = LLRPMessageFactory.createLLRPMessage(message);
			
			LLRPMessageItem item = new LLRPMessageItem();
			item.setAdapter(worker.getAdaptor().getAdaptorName());
			item.setReader(readerName);
			
			// set the message type
			Integer typeNum = llrpMessage.getTypeNum().toInteger();
			String msgName = llrpMessage.getName();
			item.setMessageType(msgName);
			
			// if the message contains a "LLRPStatus" parameter, set the status code (otherwise use empty string)
			String statusCode = "";
			try {
				Method getLLRPStatusMethod = llrpMessage.getClass().getMethod("getLLRPStatus", new Class[0]);
				LLRPStatus status = (LLRPStatus) getLLRPStatusMethod.invoke(llrpMessage, new Object[0]);
				statusCode = status.getStatusCode().toString();
			} catch (Exception e) {
				// do nothing
			} 
			item.setStatusCode(statusCode);
			
			// store the xml string to the repository
			item.setContent(llrpMessage.toXMLString());
			
			// store to the repository
			AdaptorManagement.getInstance().postLLRPMessageToRepo(item);
		} catch (InvalidLLRPMessageException e) {
			AdaptorManagement.getInstance().postException(new LLRPRuntimeException(e.getMessage()), 
					LLRPExceptionHandlerTypeMap.EXCEPTION_MSG_SENDING_ERROR, 
					worker.getAdaptor().getAdaptorName(), readerName);
		}
	}
	
	/**
	 * sets the worker that holds this callback.
	 * @param worker the worker that holds this callback.
	 */
	public void setWorker(AdaptorWorker worker) {
		this.worker = worker;
	}

	public void notifyError(LLRPRuntimeException e, String readerName) throws RemoteException {
		AdaptorManagement.getInstance().postException(e, e.getExceptionType(),
				worker.getAdaptor().getAdaptorName(),
				readerName);
	}
}