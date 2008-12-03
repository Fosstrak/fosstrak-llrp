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

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;

/**
 * if you want to receive asynchronous message notifications 
 * you can implement this interface.
 * @author sawielan
 *
 */
public interface AsynchronousNotifiable extends Remote {
	/**
	 * when an asynchronous message arrived, this method will be invoked.
	 * @param message the LLRPMessage arrived asynchronously.
	 * @param readerName the nam eof the reader that read the message.
	 * @throws RemoteException when there has been an error in the communication.
	 */
	public void notify(byte[] message, String readerName) throws RemoteException;
	
	/**
	 * this method can be called asynchronously when 
	 * there occurs an asynchronous error or exception.
	 * @param e the exception that was triggered.
	 * @param readerName the name of the reader that triggered the error.
	 * @throws RemoteException when there has been an error in the communication.
	 */
	public void notifyError(LLRPRuntimeException e, String readerName) throws RemoteException;
}
