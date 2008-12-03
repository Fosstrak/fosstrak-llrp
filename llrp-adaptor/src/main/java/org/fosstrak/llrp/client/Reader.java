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

package org.fosstrak.llrp.client;

/**
 * This wrapper class for Reader. Some extended attributes added for
 * Editor use.
 *
 * @author Haoning Zhang
 * @version 1.0
 */
public class Reader {
	
    /**
     * The default Adapter Name.
     * For those Readers, which are directly connected form Client side.
     */
	public final static String LOCAL_ADAPTER_NAME = "Local";
	
	private String adapterName;
	private String name;
	private String ip;
	private int port;
	
    /**
     * Default Constructor.
     */
	public Reader() {
		setAdapterName(LOCAL_ADAPTER_NAME);
	}
	
    /**
     * Get the Unique Id of this Reader.
     */
	public String getId() {
		return getUniqueReaderId(getAdapterName(), getName());
	}
	
	/**
	 * Get IP address.
	 * 
	 * @return Reader IP Address
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Set IP Address
	 * 
	 * @param ip Reader IP Address
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * Get Port number
	 * 
	 * @return Reader Port Number
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set Port Number
	 * 
	 * @param port Reader Port Number
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Get the logical name of the Reader.
	 * 
	 * @return Reader Logical Name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the logical name of the Reader.
	 * 
	 * @param name Reader Logical Name
	 */
	public void setName(String name) {
		this.name = name;
	}

    /**
     * Get the name of LLRP Adapter, which hold this Reader.
     */
	public String getAdapterName() {
		return adapterName;
	}

    /**
     * Set the name of LLRP Adapter, which hold this Reader.
     *
     * @param aAdapterName String the name of Adapter.
     */
	public void setAdapterName(String aAdapterName) {
		adapterName = aAdapterName;
	}
	
	public boolean isAttachedTo(String aAdapterName) {
		return aAdapterName.trim().equals(getAdapterName())? true : false;
	}
	
	/**
	 * Static function to get system wide unique reader name.
	 * It just combine the AdapterName and the ReaderName.
	 *  
	 * @param aAdapterName Adapter Logical Name
	 * @param aReaderName Reader Logical Name
	 * @return
	 */
	public static String getUniqueReaderId(String aAdapterName, String aReaderName) {
		return aAdapterName + "-" + aReaderName;
	}
	
    /**
     * Get the description of the Reader.
     */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Name:");
		sb.append(getName());
		sb.append(" Adater:");
		sb.append(getAdapterName());
		sb.append(" IP:");
		sb.append(getIp());
		sb.append(":");
		sb.append(getPort());
		return sb.toString();
	}
}
