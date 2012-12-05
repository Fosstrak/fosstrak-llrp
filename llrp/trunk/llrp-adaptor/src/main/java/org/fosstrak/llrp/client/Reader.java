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
	 * @return a string holding a unique reader name.
	 */
	public static String getUniqueReaderId(String aAdapterName, String aReaderName) {
		return aAdapterName + "-" + aReaderName;
	}
	
    /**
     * Get the description of the Reader.
     */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Name:");
		sb.append(getName());
		sb.append(" Adapter:");
		sb.append(getAdapterName());
		sb.append(" IP:");
		sb.append(getIp());
		sb.append(":");
		sb.append(getPort());
		return sb.toString();
	}
}
