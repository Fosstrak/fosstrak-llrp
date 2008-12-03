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

package org.fosstrak.llrp.adaptor.queue;

import org.llrp.ltk.types.LLRPMessage;

/**
 * data structure to store enqueue an LLRPMessage.
 * @author sawielan
 *
 */
public class QueueEntry {
	/** the llrp message. */
	private LLRPMessage message;
	
	/** the reader that will receive or read the message. */
	private String readerName;
	
	/** the adaptor name that will receive or read the message. */
	private String adaptorName;
	
	/**
	 * creates a new queue item for an llrp message.
	 * @param message the llrp message.
	 * @param readerName the reader that will receive or read the message.
	 * @param adaptorName the adaptor name that will receive or read the message.
	 */
	public QueueEntry(LLRPMessage message, String readerName, String adaptorName) {
		super();
		this.message = message;
		this.readerName = readerName;
		this.adaptorName = adaptorName;
	}
	
	/**
	 * returns the stored message.
	 * @return the stored message.
	 */
	public LLRPMessage getMessage() {
		return message;
	}

	/**
	 * sets the message in the datastructure.
	 * @param message an LLRPMessage to set.
	 */
	public void setMessage(LLRPMessage message) {
		this.message = message;
	}

	/**
	 * returns the reader name that either read or will receive the message.
	 * @return the reader name that either read or will receive the message.
	 */
	public String getReaderName() {
		return readerName;
	}

	/**
	 * sets the name of the reader that will either receive or read the message.
	 * @param readerName the name of the reader that will either receive or read the message.
	 */
	public void setReaderName(String readerName) {
		this.readerName = readerName;
	}

	/**
	 * returns the name of the adaptor that will either receive or read the message.
	 * @return the name of the adaptor that will either receive or read the message.
	 */
	public String getAdaptorName() {
		return adaptorName;
	}

	/**
	 *  sets the name of the adaptor that will either receive or read the message.
	 * @param adaptorName the name of the adaptor that will either receive or read the message.
	 */
	public void setAdaptorName(String adaptorName) {
		this.adaptorName = adaptorName;
	}
}
