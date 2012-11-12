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

import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.adaptor.queue.QueueEntry;
import org.fosstrak.llrp.client.LLRPExceptionHandlerTypeMap;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;

/**
 * an LLRPAdaptorWorker holds an Adaptor and a callback. the worker
 * enqueues messages and dispatches them to the corresponding reader.
 * through the callback it can retrieve messages that then will be 
 * dispatched to the MessageRepository.
 * @author sawielan
 *
 */
public class AdaptorWorker implements Runnable {
	
	/** the worker does not accept more messages in the queue than this threshold. */
	public static final int QUEUE_THRESHOLD = 100;
	
	/** the callback for asynchronous message retrieval. */
	private AdaptorCallback callback = null;
	
	/** the adaptor holding the connection to the readers. */
	private Adaptor adaptor = null;	
	
	/** the queue holding messages to be sent to readers. */
	private LinkedList<QueueEntry> outQueue = new LinkedList<QueueEntry> ();
	
	/** as long as this value is set to true the worker will accept and process messages. */
	private boolean isRunning = true;
	
	/** the ip address of this adaptor. if its the local adaptor it returns null. */
	private String adaptorIpAddress = null;
	
	/** 
	 * the number of connection failures. The initial value is chosen in 
	 * such a way that upon startup erroneous adaptors get cleaned out. 
	 * */
	private int connFailures = 2;

	/** the logger. */
	private static Logger log = Logger.getLogger(AdaptorWorker.class);
	
	/** the number of allowed connection failures between adaptor and client. */
	public static final int MAX_CONN_FAILURES = 3;
	
	/**
	 * creates a new LLRPAdaptorWorker. 
	 * @param callback the callback for asynchronous message retrieval. 
	 * @param adaptor the adaptor holding the connection to the readers. 
	 */
	public AdaptorWorker(AdaptorCallback callback, Adaptor adaptor) {
		this.callback = callback;
		callback.setWorker(this);
		
		this.adaptor = adaptor;
	}
	public void run() {
		while (isRunning) {
			try {
				QueueEntry entry = null;
				synchronized (outQueue) {
					// if the outQueue is empty we put this thread to sleep. 
					// when someone posts a new message to the queue to 
					// poster has to call the notifyAll() method and by 
					// that awake this thread.
					while (outQueue.isEmpty()) outQueue.wait();
					// the thread has been awakened and is now able to 
					// process the first element in the queue.
					entry = outQueue.removeFirst();
					
					process(entry);
				}
			} catch (InterruptedException e) {
				log.info("got interrupted", e);
			}
		}
		
	}
	
	/**
	 * call this method if you want to stop the worker thread.
	 */
	public void tearDown() {
		// set isRunning to false.
		isRunning = false;
		
		// wakeup the thread to let it stop.
		synchronized (outQueue) {
			outQueue.notifyAll();
		}
	}
	
	/**
	 * send a queued element through the adaptor to the reader.
	 * @param entry the queued element to be sent.
	 */
	private void process(QueueEntry entry) {
		try {
			getAdaptor().sendLLRPMessage(entry.getReaderName(), entry.getMessage().encodeBinary());
		} catch (LLRPRuntimeException e) {
			AdaptorManagement.getInstance().postException(new LLRPRuntimeException(e.getMessage()),
					LLRPExceptionHandlerTypeMap.EXCEPTION_MSG_SENDING_ERROR,
					entry.getAdaptorName(),
					entry.getReaderName());
		} catch (InvalidLLRPMessageException e) {
			AdaptorManagement.getInstance().postException(new LLRPRuntimeException(e.getMessage()),
					LLRPExceptionHandlerTypeMap.EXCEPTION_MSG_SYNTAX_ERROR,
					entry.getAdaptorName(),
					entry.getReaderName());
					
		} catch (Exception e) {
			AdaptorManagement.getInstance().postException(new LLRPRuntimeException(e.getMessage()),
					LLRPExceptionHandlerTypeMap.EXCEPTION_MSG_SENDING_ERROR,
					entry.getAdaptorName(),
					entry.getReaderName());
		}
	}

	/**
	 * returns the callback for asynchronous message retrieval. 
	 * @return the callback for asynchronous message retrieval. 
	 */
	public AdaptorCallback getCallback() {
		return callback;
	}

	/**
	 * sets the callback for asynchronous message retrieval. 
	 * @param callback the callback for asynchronous message retrieval. 
	 */
	public void setCallback(AdaptorCallback callback) {
		this.callback = callback;
	}

	/**
	 * the adaptor holding the connection to the readers. 
	 * @return the adaptor holding the connection to the readers.
	 */
	public Adaptor getAdaptor() {
		return adaptor;
	}

	/**
	 * sets the adaptor holding the connection to the readers.
	 * @param adaptor the adaptor holding the connection to the readers.
	 */
	public void setAdaptor(Adaptor adaptor) {
		this.adaptor = adaptor;
	}
	
	/**
	 * signals whether this worker is ready to accept messages.
	 * @return true if ok, else otherwise.
	 */
	public boolean isReady() {
		if (outQueue.size() >= QUEUE_THRESHOLD) {
			return false;
		}
		return true;
	}
	
	/**
	 * enqueues a message to be sent.
	 * @param e the queue element holding the message.
	 * @throws LLRPRuntimeException when worker is not ready or queue is full
	 */
	public void enqueue(QueueEntry e) throws LLRPRuntimeException {
		if (!isReady()) {
			throw new LLRPRuntimeException("Queue is full or worker not ready.");
		}
		synchronized (outQueue) {
			outQueue.add(e);
			outQueue.notifyAll();
		}
	}

	/**
	 * returns the ip address of this adaptor. if its the local adaptor it returns null.
	 * @return null if local adaptor else the address of the adaptor.
	 */
	public String getAdaptorIpAddress() {
		return adaptorIpAddress;
	}

	/**
	 * sets the address of this adaptor. if its the local adaptor set null.
	 * @param adaptorIpAddress the address of the adaptor.
	 */
	public void setAdaptorIpAddress(String adaptorIpAddress) {
		this.adaptorIpAddress = adaptorIpAddress;
	}
	
	/**
	 * increases the connection failure counter by one.
	 */
	public void reportConnFailure() {
		connFailures++;
	}
	
	/**
	 * resets the connection failure counter to zero.
	 */
	public void cleanConnFailure() {
		connFailures = 0;
	}
	
	/**
	 * @return true if connection to adaptor is alright, false otherwise.
	 */
	public boolean ok() {
		return (connFailures < MAX_CONN_FAILURES);
	}
}
