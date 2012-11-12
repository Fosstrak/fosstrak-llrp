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

package org.fosstrak.llrp.adaptor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.adaptor.util.AsynchronousNotifiableList;
import org.fosstrak.llrp.client.LLRPExceptionHandlerTypeMap;
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
 * This class implements the ReaderInterface. The Reader implementation 
 * maintains two queues to decouple the user interface from the actual message 
 * delivery over the network.<br/>
 * 1. from the user to the LLRP reader: the message to be sent is put into a 
 * queue. a queue watch-dog awakes as soon as there are messages in the queue 
 * and delivers them via LTK.<br/>
 * 2. from the LLRP reader to user: the incoming message from the reader is 
 * stored into a queue. a queue watch-dog awakes as soon as there are messages 
 * in the queue and delivers them to the user.
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
	private LLRPConnection readerConnection = null;
	
	/** the adaptor where the reader belongs to. */
	private Adaptor adaptor = null;

	/** a list with all the receivers of asynchronous messages. */
	private AsynchronousNotifiableList toNotify = new AsynchronousNotifiableList();
	
	/** the default keep-alive interval for the reader. */
	public static final int DEFAULT_KEEPALIVE_PERIOD = 10000; 
	
	/** default how many times a keep-alive can be missed. */
	public static final int DEFAULT_MISS_KEEPALIVE = 3;

	/**
	 * the queue workers periodically wake up in order to check the queues even when not notified.
	 */
	private static final long PERIODICAL_WAKEUP_TIME = 500L;
	
	/** flag whether to throw an exception when a timeout occurred. */
	private boolean throwExceptionKeepAlive = true;
	
	/** meta-data about the reader, if connection is up, number of packages, etc... */
	private ReaderMetaData metaData = new ReaderMetaData();
	
	/** IO handler. */
	private LLRPIoHandlerAdapter handler = null;
	
	/** handle to the connection watch-dog. */
	private Thread watchDog = null;
	
	/** handle to the out queue worker. */
	private Thread outQueueWorker = null;
	
	/** handle to the in queue worker. */
	private Thread inQueueWorker = null;
	
	/** queue to hold the incoming messages.*/
	private final ConcurrentLinkedQueue<byte[]> inqueue = new ConcurrentLinkedQueue<byte[]> ();
	
	/** queue to hold the outgoing messages. */
	private final ConcurrentLinkedQueue<LLRPMessage> outqueue = new ConcurrentLinkedQueue<LLRPMessage> ();
	
	/** queue policies. */
	public enum QueuePolicy {
		DROP_QUEUE_ON_ERROR,
		KEEP_QUEUE_ON_ERROR
	};
	
	/**
	 * constructor for a local reader stub. the stub maintains connection
	 * to the llrp reader.
	 * @param adaptor the adaptor responsible for this reader.
	 * @param readerName the name of this reader.
	 * @param readerAddress the address where to connect.
	 * @throws RemoteException whenever there is an RMI exception
	 */
	public ReaderImpl(Adaptor adaptor, String readerName, String readerAddress) throws RemoteException {
		this(adaptor, readerName, readerAddress, -1);
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
		metaData.setAllowNKeepAliveMisses(DEFAULT_MISS_KEEPALIVE);
		metaData.setKeepAlivePeriod(DEFAULT_KEEPALIVE_PERIOD);
		metaData.setReaderName(readerName);
		metaData.setReaderAddress(readerAddress);
		if (port <= 0) {
			log.warn("port for reader '" + metaData.getReaderName() + "' not specified. using default port " + Constants.DEFAULT_LLRP_PORT);
			port = Constants.DEFAULT_LLRP_PORT;
		}
		metaData.setPort(port);
	}
	
	public ReaderImpl(Adaptor adaptor, String readerName, LLRPConnection connection) throws RemoteException {
		this.readerConnection = connection;
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#connect(boolean)
	 */
	@Override
	public void connect(boolean clientInitiatedConnection) throws LLRPRuntimeException, RemoteException {
		// FIXME: need to handle the case when the reader is already connected.
		try {
			metaData.setClientInitiated(clientInitiatedConnection);
			
			// start a new counter session
			metaData.newSession();
			
			if (readerConnection != null) {
				readerConnection = prepareConnectionAndConnect();
			}
			metaData.setConnected(true);
			
			outQueueWorker = new Thread(getOutQueueWorker());
			outQueueWorker.start();
			
			inQueueWorker = new Thread(getInQueueWorker());
			inQueueWorker.start();
	
			// only do heart beat in client initiated mode.
			if (clientInitiatedConnection) {
				enableHeartBeat();
			}
			log.info(String.format("reader %s connected.", metaData.getReaderName()));
			
		} catch (Exception e) {
			// catch all unexpected errors...
			LLRPRuntimeException ex = new LLRPRuntimeException(String.format("Could not connect to reader %s on adapter %s:\nException: %s", getReaderName(), adaptor.getAdaptorName(), e.getMessage()));
			reportException(ex);
			throw ex;
		}
	}
	
	private LLRPConnection prepareConnectionAndConnect() throws LLRPRuntimeException, RemoteException {
		if (isClientInitiated()) {
			log.debug("preparing connection in connecting mode with an LLRPConnector");
			if (metaData.getReaderAddress() == null) {
				log.error("address for reader '" + metaData.getReaderName() + "' is empty!");
				LLRPRuntimeException ex = new LLRPRuntimeException("address for reader '" + metaData.getReaderName() + "' is empty!"); 
				reportException(ex);
				throw ex;
			}
				
			// run ltk connector.
			LLRPConnector connector = new LLRPConnector(this, metaData.getReaderAddress(), metaData.getPort());
			connector.getHandler().setKeepAliveAck(true);
			connector.getHandler().setKeepAliveForward(true);
			try {
				connector.connect();
			} catch (LLRPConnectionAttemptFailedException e) {
				log.error("connection attempt to reader " + metaData.getReaderName() + " failed");
				reportException(new LLRPRuntimeException("connection attempt to reader " + metaData.getReaderName() + " failed"));
			}
			
			return connector;
		}
		log.debug("preparing connection in accepting mode with an LLRPAcceptor");
		// run the ltk acceptor
		LLRPAcceptor acceptor = new LLRPAcceptor(this, metaData.getPort());
		handler = new LLRPIoHandlerAdapterImpl(acceptor);
		handler.setKeepAliveAck(true);
		acceptor.getHandler().setKeepAliveForward(true);
		acceptor.setHandler(handler);
		try {
			acceptor.bind();
		} catch (LLRPConnectionAttemptFailedException e) {
			log.error("could not bind acceptor for reader " + metaData.getReaderName());
			reportException(new LLRPRuntimeException("could not bind acceptor for reader " + metaData.getReaderName()	));
		}
		return acceptor;
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#disconnect()
	 */
	@Override
	public void disconnect() throws RemoteException {
		log.debug("disconnecting the reader.");
		setReportKeepAlive(false);
		
		if (readerConnection != null) {
			try {
				if (readerConnection instanceof LLRPConnector) {
					// disconnect from the reader.
					((LLRPConnector)readerConnection).disconnect();
				} else if (readerConnection instanceof LLRPAcceptor) {
					// close the acceptor.
					((LLRPAcceptor)readerConnection).close();
				}
			} catch (Exception e) {
				readerConnection = null;
				log.error("caught exception during disconnect", e);
			}
		}
		
		metaData.setConnected(false);
		
		// stop the outqueue worker
		if (null != outQueueWorker) {
			outQueueWorker.interrupt();
		}
		
		// stop the inqueue worker
		if (null != inQueueWorker) {
			inQueueWorker.interrupt();
		}
		
		// stop the connection watch-dog.
		if (null != watchDog) {
			watchDog.interrupt();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#reconnect()
	 */
	@Override
	public void reconnect() throws LLRPRuntimeException, RemoteException  {
		// first try to disconnect
		disconnect();
		connect(isClientInitiated());
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#send(byte[])
	 */
	@Override
	public void send(byte[] message) throws RemoteException {
		if (!metaData.isConnected() || (readerConnection == null)) {
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
		
		// put the message into the outqueue
		synchronized (outqueue) {
			outqueue.add(llrpMessage);
			outqueue.notifyAll();
		}
	}
	
	/**
	 * performs the actual sending of the LLRP message to the reader.
	 * @param llrpMessage the LLRP message to be sent.
	 * @throws RemoteException at RMI exception.
	 */
	private void sendLLRPMessage(LLRPMessage llrpMessage) throws RemoteException {
		try {
			// send the message.
			readerConnection.send(llrpMessage);
			metaData.packageSent();
		} catch (NullPointerException npe) {
			// a null-pointer exception occurs when the reader is no more connected.
			// we therefore report the exception to the GUI.
			disconnect();
			reportException(new LLRPRuntimeException(String.format("reader %s is not connected", metaData.getReaderName()),	LLRPExceptionHandlerTypeMap.EXCEPTION_READER_LOST));
		} catch (Exception e) {
			// just to be sure...
			disconnect();			
		}		
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#isConnected()
	 */
	@Override
	public boolean isConnected() throws RemoteException {
		return metaData.isConnected();
	}

	/**
	 * when there is an error, the ltk will call this method.
	 * @param message the error message from ltk.
	 */
	@Override
	public void errorOccured(String message) {
		reportException(new LLRPRuntimeException(message));
	}

	/**
	 * when a message arrives through ltk, this method is called.
	 * @param message the llrp message delivered by ltk.
	 */
	@Override
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
		metaData.packageReceived();
		if (message instanceof KEEPALIVE) {
			metaData.setAlive(true);
			log.debug("received keepalive message from the reader:" + metaData.getReaderName());
			if (!metaData.isReportKeepAlive()) {
				return;
			}
		}
		
		// put the message into the inqueue.
		synchronized (inqueue) {
			inqueue.add(binaryEncoded);
			inqueue.notifyAll();
		}
	}

	/**
	 * deliver a received message to the handlers.
	 * @param binaryEncoded the binary encoded LLRP message.
	 */
	private void deliverMessage(byte[] binaryEncoded) {
		try {
			adaptor.messageReceivedCallback(binaryEncoded, metaData.getReaderName());
		} catch (RemoteException e) {
			reportException(new LLRPRuntimeException(e.getMessage()));
		}
		
		// also notify all the registered notifyables.
		try {
			toNotify.notify(binaryEncoded, metaData.getReaderName());
		} catch (RemoteException e) {
			reportException(new LLRPRuntimeException(e.getMessage()));
		}
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#getReaderAddress()
	 */
	@Override
	public String getReaderAddress() throws RemoteException {
		return metaData.getReaderAddress();
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#getPort()
	 */
	@Override
	public int getPort() throws RemoteException {
		return metaData.getPort();
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#isClientInitiated()
	 */
	@Override
	public boolean isClientInitiated() throws RemoteException {
		return metaData.isClientInitiated();
	}

	@Override
	public void setClientInitiated(boolean clientInitiated) throws RemoteException {
		metaData.setClientInitiated(clientInitiated);
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#registerForAsynchronous(org.fosstrak.llrp.adaptor.AsynchronousNotifiable)
	 */
	@Override
	public void registerForAsynchronous(AsynchronousNotifiable receiver) throws RemoteException {
		toNotify.add(receiver);
	}
	
	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.ReaderIface#deregisterFromAsynchronous(org.fosstrak.llrp.adaptor.AsynchronousNotifiable)
	 */
	@Override
	public void deregisterFromAsynchronous(AsynchronousNotifiable receiver) throws RemoteException {
		toNotify.remove(receiver);
	}
	
	@Override
	public String getReaderName() throws RemoteException {
		return metaData.getReaderName();
	}

	@Override
	public boolean isConnectImmediate() throws RemoteException {
		return metaData.isConnectImmediately();
	}

	@Override
	public void setConnectImmediate(boolean value) throws RemoteException {
		metaData.setConnectImmediately(value);
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
			log.debug("Could not send the exception to the error callback.", e1);
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
				log.error("could not report exception to callback.", e);
			}
		} catch (InvalidLLRPMessageException e) {
			log.error("the given heartbeat message is illegal.", e);
		}
		
		// run the watch-dog
		watchDog = new Thread(new Runnable() {
			public void run() {
				log.debug("starting connection watchdog.");
				try {
					while (isConnected()) {
						try {
							Thread.sleep(metaData.getAllowNKeepAliveMisses() * metaData.getKeepAlivePeriod());
							if (!metaData.isAlive()) {
								log.debug("connection timed out...");
								disconnect();
								if (throwExceptionKeepAlive) {
									reportException(new LLRPRuntimeException("Connection timed out", LLRPExceptionHandlerTypeMap.EXCEPTION_READER_LOST));
								}
							}
							metaData.setAlive(false);
						} catch (InterruptedException e) {
							log.debug("received interrupt - stopping watchdog.");
						}
					}
				} catch (RemoteException e) {
					log.error("could not connect to the reader with the watchdog.", e);
				}
				log.debug("connection watchdog stopped.");
			}
		});
		watchDog.start();
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.Reader#getKeepAlivePeriod()
	 */
	@Override
	public int getKeepAlivePeriod() throws RemoteException {
		return metaData.getKeepAlivePeriod();
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.adaptor.Reader#setKeepAlivePeriod(int, int, boolean, boolean)
	 */
	@Override
	public void setKeepAlivePeriod(int keepAlivePeriod, int times, boolean report, boolean throwException) throws RemoteException {		
		metaData.setKeepAlivePeriod(keepAlivePeriod);
		metaData.setAllowNKeepAliveMisses(times);
		metaData.setReportKeepAlive(report);
		this.throwExceptionKeepAlive = throwException;
	}

	@Override
	public void setReportKeepAlive(boolean report) throws RemoteException {
		metaData.setReportKeepAlive(report);
	}

	@Override
	public boolean isReportKeepAlive() throws RemoteException {
		return metaData.isReportKeepAlive();
	}

	@Override
	public final ReaderMetaData getMetaData() throws RemoteException {
		return new ReaderMetaData(metaData);
	}
	
	/**
	 * creates a runnable that watches the out queue for new messages to be 
	 * sent. at arrival of a new message, the message is sent via LTK.
	 * @return a runnable.
	 */
	private Runnable getOutQueueWorker() {
		return new Runnable() {
			public void run() {
				try {
					while (true) {
						synchronized (outqueue) {
							while (outqueue.isEmpty()) outqueue.wait(PERIODICAL_WAKEUP_TIME);
							
							LLRPMessage msg = outqueue.remove();
							try {
								sendLLRPMessage(msg);
							} catch (RemoteException e) {
								log.debug(String.format("Could not send message: %s", e.getMessage()), e);
							}
						}
					}
				} catch (InterruptedException e) {
					log.debug("stopping out queue worker.");
				}
			}
		};
	}
	
	/**
	 * creates a runnable that watches the in queue for new messages and at 
	 * arrival, delivers them to the management.
	 * @return a runnable.
	 */
	private Runnable getInQueueWorker() {
		return new Runnable() {
			public void run() {
				try {
					synchronized (inqueue) {
						while (true) {
							while (inqueue.isEmpty()) inqueue.wait(PERIODICAL_WAKEUP_TIME);
							
							byte[] msg = inqueue.remove();
							deliverMessage(msg);
						}
					}
				} catch (InterruptedException e) {
					log.debug("stopping in queue worker.");
				}
			}			
		};
	}

}
