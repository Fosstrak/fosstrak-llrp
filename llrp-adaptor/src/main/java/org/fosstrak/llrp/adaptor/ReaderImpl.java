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
	
	/** the llrp connector to the physical reader. */
	private LLRPConnection connector = null;
	
	/** the adaptor where the reader belongs to. */
	private Adaptor adaptor = null;

	/** a list with all the receivers of asynchronous messages. */
	private List<AsynchronousNotifiable> toNotify = new LinkedList<AsynchronousNotifiable> ();
	
	/** the default keepalive interval for the reader. */
	public static final int DEFAULT_KEEPALIVE_PERIOD = 10000; 
	
	/** default how many times a keepalive can be missed. */
	public static final int DEFAULT_MISS_KEEPALIVE = 3;
	
	/** flag whether to throw an exception when a timeout occurred. */
	private boolean throwExceptionKeepAlive = true;
	
	/** meta-data about the reader, if connection is up, number of packages, etc... */
	private ReaderMetaData metaData = new ReaderMetaData();
	
	/** io handler. */
	private LLRPIoHandlerAdapter handler = null;
	
	/**
	 * constructor for a local reader stub. the stub maintains connection
	 * to the llrp reader.
	 * @param adaptor the adaptor responsible for this reader.
	 * @param readerName the name of this reader.
	 * @param readerAddress the address where to connect.
	 * @throws RemoteException whenever there is an RMI exception
	 */
	public ReaderImpl(Adaptor adaptor, String readerName, String readerAddress) throws RemoteException {
		this.adaptor = adaptor;
		metaData._setAllowNKeepAliveMisses(DEFAULT_MISS_KEEPALIVE);
		metaData._setKeepAlivePeriod(DEFAULT_KEEPALIVE_PERIOD);
		metaData._setReaderName(readerName);
		metaData._setReaderAddress(readerAddress);
	}
	
	/**
	 * constructor for a local reader stub. the stub maintains connection
	 * to the llrp reader.
	 * @param adaptor the adaptor responsible for this reader.
	 * @param readerName the name of this reader.
	 * @param readerAddress the address where to connect.
	 * @param port the port where to connect.
	 * @throws RemoteException whenever there is an RMI exception
	 */
	public ReaderImpl(Adaptor adaptor, String readerName, String readerAddress, int port) throws RemoteException {
		this.adaptor = adaptor;
		metaData._setAllowNKeepAliveMisses(DEFAULT_MISS_KEEPALIVE);
		metaData._setKeepAlivePeriod(DEFAULT_KEEPALIVE_PERIOD);
		metaData._setReaderName(readerName);
		metaData._setReaderAddress(readerAddress);
		metaData._setPort(port);
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#connect(boolean)
	 */
	public void connect(boolean clientInitiatedConnection) throws LLRPRuntimeException, RemoteException {
		String address = metaData.getReaderAddress();
		metaData._setClientInitiated(clientInitiatedConnection);
		
		// start a new counter session
		metaData._newSession();
		
		if (metaData.getPort() == -1) {
			metaData._setPort(Constants.DEFAULT_LLRP_PORT);
			log.warn("port for reader '" + metaData.getReaderName() + "' not specified. using default port " + metaData.getPort());
		}
		
		if (clientInitiatedConnection) {
			if (address == null) {
				log.error("address for reader '" + metaData.getReaderName() + "' is empty!");
				reportException(new LLRPRuntimeException("address for reader '" + metaData.getReaderName() + "' is empty!"));
				return;
			}
				
			// run ltk connector.
			LLRPConnector connector = new LLRPConnector(this, address, metaData.getPort());
			connector.getHandler().setKeepAliveAck(true);
			connector.getHandler().setKeepAliveForward(true);
			try {
				connector.connect();
			} catch (LLRPConnectionAttemptFailedException e) {
				log.error("connection attempt to reader " + metaData.getReaderName() + " failed");
				reportException(new LLRPRuntimeException("connection attempt to reader " + metaData.getReaderName() + " failed"));
			}
			
			this.connector = connector;
		} else {
			// run the ltk acceptor
			LLRPAcceptor acceptor = new LLRPAcceptor(this, metaData.getPort());
			handler = new LLRPIoHandlerAdapterImpl(acceptor);
			handler.setKeepAliveAck(true);
			connector.getHandler().setKeepAliveForward(true);
			acceptor.setHandler(handler);
			try {
				acceptor.bind();
			} catch (LLRPConnectionAttemptFailedException e) {
				log.error("could not bind acceptor for reader " + metaData.getReaderName());
				reportException(new LLRPRuntimeException("could not bind acceptor for reader " + metaData.getReaderName()	));
			}
			
			this.connector = acceptor;
		}
		metaData._setConnected(true);

		enableHeartBeat();
		log.info(String.format("reader %s connected.", metaData.getReaderName()));
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#disconnect()
	 */
	public void disconnect() throws RemoteException {
		log.debug("disconnecting the reader.");
		setReportKeepAlive(false);
		
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
		
		metaData._setConnected(false);
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
		if (!metaData.isConnected() || (connector == null)) {
			reportException(new LLRPRuntimeException(String.format("reader %s is not connected", metaData.getReaderName())));
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
			log.warn(String.format("do not send empty llrp message on reader %s", metaData.getReaderName()));
			return;
		}
		
		try {
			// send the message asynchronous.
			connector.send(llrpMessage);
			metaData._packageSent();
		} catch (NullPointerException npe) {
			// a nullpointer exception occurs when the reader is no more connected.
			// we therefor report the exception to the gui.
			disconnect();
			reportException(new LLRPRuntimeException(String.format("reader %s is not connected", metaData.getReaderName()),
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
		return metaData.isConnected();
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
		metaData._packageReceived();
		if (message instanceof KEEPALIVE) {
			
			metaData._setAlive(true);
			log.debug("received keepalive message from the reader:" + metaData.getReaderName());
			if (!metaData.isReportKeepAlive()) {
				return;
			}
		}
		
		try {
			adaptor.messageReceivedCallback(binaryEncoded, metaData.getReaderName());
		} catch (RemoteException e) {
			reportException(new LLRPRuntimeException(e.getMessage()));
		}
		
		// also notify all the registered notifyables.
		for (AsynchronousNotifiable receiver : toNotify) {
			try {
				receiver.notify(binaryEncoded, metaData.getReaderName());
			} catch (RemoteException e) {
				reportException(new LLRPRuntimeException(e.getMessage()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#getReaderAddress()
	 */
	public String getReaderAddress() throws RemoteException {
		return metaData.getReaderAddress();
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#getPort()
	 */
	public int getPort() throws RemoteException {
		return metaData.getPort();
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#isClientInitiated()
	 */
	public boolean isClientInitiated() throws RemoteException {
		return metaData.isClientInitiated();
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
		return metaData.getReaderName();
	}

	public boolean isConnectImmediate() throws RemoteException {
		return metaData.isConnectImmediately();
	}

	public void setConnectImmediate(boolean value) throws RemoteException {
		metaData._setConnectImmediately(value);
	}

	/**
	 * reports an exception the the adaptor. if the reporting of the 
	 * exception also fails, the stack-trace gets logged but the 
	 * reader continues to work.
	 * @param e the exception to report.
	 */
	private void reportException(LLRPRuntimeException e) {
		if (adaptor == null) {
			log.error("no adaptor to report exception to on reader: " + metaData.getReaderName());
			return;
		}
		try {
			adaptor.errorCallback(e, metaData.getReaderName());
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
		ks.setPeriodicTriggerValue(new UnsignedInteger(metaData.getKeepAlivePeriod()));
		
		sr.setKeepaliveSpec(ks);
		sr.setResetToFactoryDefault(new Bit(0));
		
		log.debug(String.format("using keepalive periode: %d", metaData.getKeepAlivePeriod()));		
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
		return metaData.getKeepAlivePeriod();
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.Reader#setKeepAlivePeriod(int, int, boolean, boolean)
	 */
	public void setKeepAlivePeriod(int keepAlivePeriod, int times,
			boolean report, boolean throwException) throws RemoteException {
		
		metaData._setKeepAlivePeriod(keepAlivePeriod);
		metaData._setAllowNKeepAliveMisses(times);
		metaData._setReportKeepAlive(report);
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
						Thread.sleep(metaData.getAllowNKeepAliveMisses() * metaData.getKeepAlivePeriod());
						if (!metaData.isAlive()) {
							disconnect();
							if (throwExceptionKeepAlive) {
								reportException(new LLRPRuntimeException("Connection timed out",
										LLRPExceptionHandlerTypeMap.EXCEPTION_READER_LOST));
							}
						}
						metaData._setAlive(false);
					} catch (InterruptedException e) {
					}
					
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
	}

	public void setReportKeepAlive(boolean report) throws RemoteException {
		metaData._setReportKeepAlive(report);
	}

	public boolean isReportKeepAlive() throws RemoteException {
		return metaData.isReportKeepAlive();
	}

	public final ReaderMetaData getMetaData() throws RemoteException {
		return new ReaderMetaData(metaData);
	}
}
