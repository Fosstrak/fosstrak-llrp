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

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.client.LLRPExceptionHandlerTypeMap;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.generated.enumerations.KeepaliveTriggerType;
import org.llrp.ltk.generated.messages.KEEPALIVE;
import org.llrp.ltk.generated.messages.SET_READER_CONFIG;
import org.llrp.ltk.generated.parameters.KeepaliveSpec;
import org.llrp.ltk.net.LLRPAcceptor;
import org.llrp.ltk.net.LLRPConnection;
import org.llrp.ltk.net.LLRPConnectionAttemptFailedException;
import org.llrp.ltk.net.LLRPConnector;
import org.llrp.ltk.net.LLRPEndpoint;
import org.llrp.ltk.net.LLRPIoHandlerAdapter;
import org.llrp.ltk.net.LLRPIoHandlerAdapterImpl;
import org.llrp.ltk.types.Bit;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.UnsignedInteger;

/**
 * This class implements the ReaderInterface.
 * @author sawielan
 *
 */
public class ReaderImpl extends UnicastRemoteObject implements LLRPEndpoint, Reader {
	
	/**
	 * serial version.
	 */
	private static final long serialVersionUID = 1L;

	/** the logger. */
	private static Logger log = Logger.getLogger(ReaderImpl.class);
	
	/** the name of this logical reader. */
	private String readerName = null;
		
	/** the address of the physical reader. */
	private String readerAddress = null;
	
	/** the port where to connect. */
	private int port = -1;
	
	/** the llrp connector to the physical reader. */
	private LLRPConnection connector = null;
	
	/** flags whether the reader is connected or not. */
	private boolean connected = false;
	
	/** tells whether this reader connects directly after creation. */
	private boolean connectImmediately = true;
	
	/** the adaptor where the reader belongs to. */
	private Adaptor adaptor = null;

	/** a list with all the receivers of asynchronous messages. */
	private List<AsynchronousNotifiable> toNotify = new LinkedList<AsynchronousNotifiable> ();
	
	private boolean clientInitiated = true;
	
	/** the default keepalive interval for the reader. */
	public static final int DEFAULT_KEEPALIVE_PERIOD = 10000; 
	
	/** the keepalive interval for the reader. */
	private int keepAlivePeriod = DEFAULT_KEEPALIVE_PERIOD;
	
	/** default how many times a keepalive can be missed. */
	public static final int DEFAULT_MISS_KEEPALIVE = 3;
	
	/** how many times a keepalive can be missed.*/
	private int missKeepalive = DEFAULT_MISS_KEEPALIVE;
	
	/** flags whether to report keepalive messages to the repository or not. */
	private boolean reportKeepalive = false;
	
	/** flag whether to throw an exception when a timeout occured. */
	private boolean throwExceptionKeepAlive = true;
	
	/** flags whether keepalives have arrived. */
	private boolean isAlive = true;
	
	private LLRPIoHandlerAdapter handler = null;
	
	/**
	 * constructor for a local reader stub. the stub maintains connection
	 * to the llrp reader.
	 * @param adaptor the adaptor responsible for this reader.
	 * @param readerName the name of this reader.
	 * @param readerAddress the address where to connect.
	 * @throws RemoteException whenever there is an rmi exception
	 */
	public ReaderImpl(Adaptor adaptor, String readerName, String readerAddress) throws RemoteException {
		this.adaptor = adaptor;
		this.readerName = readerName;
		this.readerAddress = readerAddress;
	}
	
	/**
	 * constructor for a local reader stub. the stub maintains connection
	 * to the llrp reader.
	 * @param adaptor the adaptor responsible for this reader.
	 * @param readerName the name of this reader.
	 * @param readerAddress the address where to connect.
	 * @param port the port where to connect.
	 * @throws RemoteException whenver there is an rmi exception
	 */
	public ReaderImpl(Adaptor adaptor, String readerName, String readerAddress, int port) throws RemoteException {
		this.adaptor = adaptor;
		this.readerName = readerName;
		this.readerAddress = readerAddress;
		this.port = port;
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#connect(boolean)
	 */
	public void connect(boolean clientInitiatedConnection) throws LLRPRuntimeException, RemoteException {
		String address = readerAddress;
		this.clientInitiated = clientInitiatedConnection;
		
		if (port == -1) {
			port = Constants.DEFAULT_LLRP_PORT;
			log.warn("port for reader '" + readerName + "' not specified. using default port " + port);
		}
		
		if (clientInitiatedConnection) {
			if (address == null) {
				log.error("address for reader '" + readerName + "' is empty!");
				reportException(new LLRPRuntimeException("address for reader '" + readerName + "' is empty!"));
				return;
			}
				
			// run ltk connector.
			LLRPConnector connector = new LLRPConnector(this, address, port);
			connector.getHandler().setKeepAliveAck(true);
			connector.getHandler().setKeepAliveForward(true);
			try {
				connector.connect();
			} catch (LLRPConnectionAttemptFailedException e) {
				log.error("connection attempt to reader " + readerName + " failed");
				reportException(new LLRPRuntimeException("connection attempt to reader " + readerName + " failed"));
			}
			
			this.connector = connector;
		} else {
			// run the ltk acceptor
			LLRPAcceptor acceptor = new LLRPAcceptor(this, port);
			handler = new LLRPIoHandlerAdapterImpl(acceptor);
			handler.setKeepAliveAck(true);
			connector.getHandler().setKeepAliveForward(true);
			acceptor.setHandler(handler);
			try {
				acceptor.bind();
			} catch (LLRPConnectionAttemptFailedException e) {
				log.error("could not bind acceptor for reader " + readerName);
				reportException(new LLRPRuntimeException("could not bind acceptor for reader " + readerName	));
			}
			
			this.connector = acceptor;
		}
		connected = true;

		enableHeartBeat();
		log.info(String.format("reader %s connected.", readerName));
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#disconnect()
	 */
	public void disconnect() throws RemoteException {
		log.debug("disconnecting the reader.");
		if (connector != null) {
			try {
				if (connector instanceof LLRPConnector) {
					// disconnect from the reader.
					((LLRPConnector)connector).disconnect();
				} else if (connector instanceof LLRPAcceptor) {
					// close the acceptor.
					((LLRPAcceptor)connector).close();
				}
			} catch (Exception e) {
				connector = null;
			}
		}
		
		connected = false;
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#reconnect()
	 */
	public void reconnect() throws LLRPRuntimeException, RemoteException  {
		// first try to disconnect
		disconnect();
		connect(isClientInitiated());
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#send(byte[])
	 */
	public void send(byte[] message) throws RemoteException {
		if (!connected || (connector == null)) {
			reportException(new LLRPRuntimeException(String.format("reader %s is not connected", readerName)));
			return;
		}
		
		// try to create the llrp message from the byte array.
		LLRPMessage llrpMessage = null;
		try {
			llrpMessage = LLRPMessageFactory.createLLRPMessage(message);
		} catch (InvalidLLRPMessageException e) {
			reportException(new LLRPRuntimeException(e.getMessage()));
		}
		
		if (llrpMessage == null) {
			log.warn(String.format("do not send empty llrp message on reader %s", readerName));
			return;
		}
		
		try {
			// send the message asynchronous.
			connector.send(llrpMessage);
		} catch (NullPointerException npe) {
			// a nullpointer exception occurs when the reader is no more connected.
			// we therefor report the exception to the gui.
			disconnect();
			reportException(new LLRPRuntimeException(String.format("reader %s is not connected", readerName),
					LLRPExceptionHandlerTypeMap.EXCEPTION_READER_LOST));
		} catch (Exception e) {
			// just to be sure...
			disconnect();			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#isConnected()
	 */
	public boolean isConnected() throws RemoteException {
		return connected;
	}

	/**
	 * when there is an error, the ltk will call this method.
	 * @param message the error message from ltk.
	 */
	public void errorOccured(String message) {
		reportException(new LLRPRuntimeException(message));
	}

	/**
	 * when a message arrives through ltk, this method is called.
	 * @param message the llrp message delivered by ltk.
	 */
	public void messageReceived(LLRPMessage message) {
		if (message == null) {
			return;
		}
		byte[] binaryEncoded;
		try {
			binaryEncoded = message.encodeBinary();
		} catch (InvalidLLRPMessageException e1) {
			reportException(new LLRPRuntimeException(e1.getMessage()));
			return;
		}
		
		if (message instanceof KEEPALIVE) {
			
			isAlive = true;
			log.debug("received keepalive message from the reader:" + readerName);
			if (!reportKeepalive) {
				return;
			}
		}
		
		try {
			adaptor.messageReceivedCallback(binaryEncoded, readerName);
		} catch (RemoteException e) {
			reportException(new LLRPRuntimeException(e.getMessage()));
		}
		
		// also notify all the registered notifyables.
		for (AsynchronousNotifiable receiver : toNotify) {
			try {
				receiver.notify(binaryEncoded, readerName);
			} catch (RemoteException e) {
				reportException(new LLRPRuntimeException(e.getMessage()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#getReaderAddress()
	 */
	public String getReaderAddress() throws RemoteException {
		return readerAddress;
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#getPort()
	 */
	public int getPort() throws RemoteException {
		return port;
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#isClientInitiated()
	 */
	public boolean isClientInitiated() throws RemoteException {
		return clientInitiated;
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#registerForAsynchronous(org.fosstrak.llrp.adaptor.AsynchronousNotifiable)
	 */
	public void registerForAsynchronous(AsynchronousNotifiable receiver) throws RemoteException {
		toNotify.add(receiver);
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#deregisterFromAsynchronous(org.fosstrak.llrp.adaptor.AsynchronousNotifiable)
	 */
	public void deregisterFromAsynchronous(AsynchronousNotifiable receiver) throws RemoteException {
		toNotify.remove(receiver);
	}
	public String getReaderName() throws RemoteException {
		return readerName;
	}

	public boolean isConnectImmediate() throws RemoteException {
		return connectImmediately;
	}

	public void setConnectImmediate(boolean value) throws RemoteException {
		this.connectImmediately = value;
	}

	/**
	 * reports an exception the the adaptor. if the reporting of the 
	 * exception also fails, the stacktrace gets logged but the 
	 * reader continues to work.
	 * @param e the exception to report.
	 */
	private void reportException(LLRPRuntimeException e) {
		if (adaptor == null) {
			log.error("no adaptor to report exception to on reader: " + readerName);
			return;
		}
		try {
			adaptor.errorCallback(e, readerName);
		} catch (RemoteException e1) {
			// print the stacktrace to the console
			log.debug(e1.getStackTrace().toString());
		}
	}
	
	/**
	 * sends a SET_READER_CONFIG message that sets the keepalive value. 
	 */
	private void enableHeartBeat() {		
		// build the keepalive settings
		SET_READER_CONFIG sr = new SET_READER_CONFIG();
		KeepaliveSpec ks = new KeepaliveSpec();
		ks.setKeepaliveTriggerType(new KeepaliveTriggerType(KeepaliveTriggerType.Periodic));
		ks.setPeriodicTriggerValue(new UnsignedInteger(keepAlivePeriod));
		
		sr.setKeepaliveSpec(ks);
		sr.setResetToFactoryDefault(new Bit(0));
		
		log.debug(String.format("using keepalive periode: %d", keepAlivePeriod));		
		try {
			send(sr.encodeBinary());
		} catch (RemoteException e) {
			if (throwExceptionKeepAlive) {
				reportException(new LLRPRuntimeException("Could not install keepalive message: " + e.getMessage()));
			} else {
				e.printStackTrace();
			}
		} catch (InvalidLLRPMessageException e) {
			e.printStackTrace();
		}
		
		// run the watchdog
		new Thread(new ConnectionWatchDog()).start();
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.Reader#getKeepAlivePeriod()
	 */
	public int getKeepAlivePeriod() throws RemoteException {
		return keepAlivePeriod;
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.Reader#setKeepAlivePeriod(int, int, boolean, boolean)
	 */
	public void setKeepAlivePeriod(int keepAlivePeriod, int times,
			boolean report, boolean throwException) throws RemoteException {
		
		this.keepAlivePeriod = keepAlivePeriod;
		this.missKeepalive = times;
		this.reportKeepalive = report;
		this.throwExceptionKeepAlive = throwException;
		
	}
	
	/**
	 * helper class that periodically tests whether the connection is still alive.
	 * @author sawielan
	 *
	 */
	private class ConnectionWatchDog implements Runnable {

		public void run() {
			try {
				while (isConnected()) {
					try {
						Thread.sleep(missKeepalive * keepAlivePeriod);
						if (!isAlive) {
							disconnect();
							if (throwExceptionKeepAlive) {
								reportException(new LLRPRuntimeException("Connection timed out",
										LLRPExceptionHandlerTypeMap.EXCEPTION_READER_LOST));
							}
						}
						isAlive = false;
					} catch (InterruptedException e) {
					}
					
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
	}
}
