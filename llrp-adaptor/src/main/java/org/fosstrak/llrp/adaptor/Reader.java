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
 * This class models a logical entity of a physical reader. it maintains 
 * the connectivity to the physical reader. 
 * @author sawielan
 *
 */
public interface Reader extends Remote  {

	/**
	 * connects this reader to the real physical llrp reader.
	 * @param clientInitiatedConnection if the connection is initiated by the client then 
	 * you should pass true. if the physical reader initiates the connection then provide false.
	 * @throws LLRPRuntimeException whenever an error occurs.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public void connect(boolean clientInitiatedConnection)
			throws LLRPRuntimeException, RemoteException;

	/**
	 * disconnect the reader stub from the physical reader.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public void disconnect() throws RemoteException;

	/**
	 * try to reconnect the reader.
	 * @throws RemoteException whenever there is an rmi error.
	 * @throws LLRPRuntimeException whenever there is a exception during connection setup.
	 */
	public void reconnect() throws LLRPRuntimeException, RemoteException;

	/**
	 * send a message to the llrp reader.
	 * @param message the message to be sent.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public void send(byte[] message) throws RemoteException;

	/**
	 * tells if the reader is connected or not.
	 * @return true if the reader is connected.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public boolean isConnected() throws RemoteException;

	/**
	 * return the ip address of this reader.
	 * @return the ip address of this reader.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public String getReaderAddress() throws RemoteException;

	/**
	 * return the name of this reader.
	 * @return the name of this reader.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public String getReaderName() throws RemoteException;
	
	/**
	 * return the port of this reader.
	 * @return the port of this reader.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public int getPort() throws RemoteException;

	/**
	 * tell if this reader maintains a client initiated connection or if the 
	 * reader accepts a connection from a llrp reader.
	 * @return <ul><li>true if client initiated connection</li><li>false if llrp reader initiated connection</li></ul>
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public boolean isClientInitiated() throws RemoteException;

	/** 
	 * tells whether this reader connects immediately after creation.
	 * @return whether this reader connects immediately after creation.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public boolean isConnectImmediate() throws RemoteException;
	
	/**
	 * 
	 * tells whether this reader connects immediately after creation.
	 * @param value whether this reader connects immediately after creation.
	 * @throws RemoteException whenever there is an rmi error.	 
	 */
	public void setConnectImmediate(boolean value) throws RemoteException;
	
	/**
	 * register for asynchronous messages from the physical reader.
	 * @param receiver the receiver that shall be notified with the message.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public abstract void registerForAsynchronous(AsynchronousNotifiable receiver) throws RemoteException;

	/**
	 * deregister from the asynchronous messages. the receiver will no more 
	 * receive asynchronous llrp messages.
	 * @param receiver the receiver to deregister.
	 * @throws RemoteException whenever there is an rmi error.
	 */
	public void deregisterFromAsynchronous(
			AsynchronousNotifiable receiver) throws RemoteException;
	
	/**
	 * sets the connection timeout period for the reader. if the times * keepAlivePeriod has 
	 * passed by without a notification from the reader the reader gets disconnected.
	 * @param keepAlivePeriod the reader must send in this period a keepalive message. time in ms.
	 * @param times how many missed keepalive messages are ok.
	 * @param report whether to report the keepalive messages to the repo or not.
	 * @param throwException whether to throw an exception upon disconnection.
	 * @throws RemoteException whenever there is an rmi error. 
	 */
	public void setKeepAlivePeriod(int keepAlivePeriod, int times, boolean report, boolean throwException) throws RemoteException;
	
	/**
	 * returns the keepalive period set for this reader.
	 * @return the keepalive period set for this reader.
	 * @throws RemoteException whenever there is an rmi error. 
	 */
	public int getKeepAlivePeriod() throws RemoteException;
}
