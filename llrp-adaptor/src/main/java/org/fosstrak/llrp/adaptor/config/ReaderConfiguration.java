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

package org.fosstrak.llrp.adaptor.config;

/**
 * a prototype holding all the settings needed for a reader.
 * @author sawielan
 *
 */
public class ReaderConfiguration {
	/** the name of the reader. */
	private String readerName = null;
	
	/** the ip of the reader. */
	private String readerIp = null;
	
	/** the port of the reader. */
	private int readerPort = -1;
	
	/** flags whether this reader opens the connection or not. */
	private boolean readerClientInitiated = false;
	
	/** flags whether this reader connects immediately after setup. */
	private boolean connectImmediately = false;
	
	/**
	 * constructor for the prototype.
	 * @param readerName the name of the reader.
	 * @param readerIp the ip of the reader.
	 * @param readerPort the port of the reader.
	 * @param readerClientInitiated flags whether this reader opens the connection or not.
	 * @param connectImmediately flags whether this reader connects immediately after setup.
	 */
	public ReaderConfiguration(String readerName, String readerIp, int readerPort,
			boolean readerClientInitiated, boolean connectImmediately) {
		super();
		this.readerName = readerName;
		this.readerIp = readerIp;
		this.readerPort = readerPort;
		this.readerClientInitiated = readerClientInitiated;
		this.connectImmediately = connectImmediately;
	}
	
	/**
	 * returns the name of the reader.
	 * @return the name of the reader.
	 */
	public String getReaderName() {
		return readerName;
	}
	
	/**
	 * returns the ip of the reader.
	 * @return the ip of the reader.
	 */
	public String getReaderIp() {
		return readerIp;
	}
	
	/**
	 * returns the port of the reader.
	 * @return the port of the reader.
	 */
	public int getReaderPort() {
		return readerPort;
	}
	
	/**
	 * flags whether this reader opens the connection or not.
	 * @return whether this reader opens the connection or not.
	 */
	public boolean isReaderClientInitiated() {
		return readerClientInitiated;
	}
	
	/**
	 * flags whether this reader connects immediately after setup.
	 * @return whether this reader connects immediately after setup.
	 */
	public boolean isConnectImmediately() {
		return connectImmediately;
	}
}
