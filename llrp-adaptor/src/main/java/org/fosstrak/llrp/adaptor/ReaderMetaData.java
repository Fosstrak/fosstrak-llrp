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

import java.io.Serializable;
import java.rmi.Remote;

/**
 * Meta data that contains all information about the reader. this is for 
 * example the reader name, the address, the port, etc. ...
 * 
 * @author sawielan
 *
 */
public class ReaderMetaData implements Remote, Serializable  {
	
	// serial version id.
	private static final long serialVersionUID = 6184673996217995724L;

	// whether the reader is alive.
	private boolean alive;

	// the number of sent packages in total.
	private int packagesSent;
	
	// the number of received packages in total.
	private int packagesReceived;
	
	// the number of sent packages in the current session.
	private int packagesCurrentSessionSent;
	
	// the number of received packages in the current session.
	private int packagesCurrentSessionReceived;
	
	// whether keep alive messages get logged or not.
	private boolean reportKeepAlive;
	
	// whether the connection is initiated by the reader or from the physical reader.
	private boolean clientInitiated;
	
	// flags whether the reader is connected or not.
	private boolean connected;
	
	// tells whether this reader connects directly after creation.
	private boolean connectImmediately = true;
	
	// the name of this logical reader.
	private String readerName = null;
		
	// the address of the physical reader.
	private String readerAddress = null;

	// the port where to connect.
	private int port = -1;
	
	// how many times a keep-alive can be missed.
	private int allowNKeepAliveMisses;
	
	// the interval for the reader to send a keep-alive message. 
	private int keepAlivePeriod;
	
	/**
	 * default constructor.
	 */
	public ReaderMetaData() {
	}
	
	/**
	 * copy constructor that creates a complete copy of the meta data.
	 * @param other the meta data object to clone;
	 */
	public ReaderMetaData(ReaderMetaData other) {
		readerName = other.getReaderName();
		readerAddress = other.getReaderAddress();
		port = other.getPort();
		
		alive = other.isAlive();
		
		packagesCurrentSessionReceived = other.getPackagesCurrentSessionReceived();
		packagesCurrentSessionSent = other.getPackagesCurrentSessionSent();
		packagesReceived = other.getPackagesReceived();
		packagesSent = other.getPackagesSent();
		
		reportKeepAlive = other.isReportKeepAlive();
		
		clientInitiated = other.isClientInitiated();
		connected = other.isConnected();
		connectImmediately = other.isConnectImmediately();
		
		allowNKeepAliveMisses = other.getAllowNKeepAliveMisses();
		keepAlivePeriod = other.getKeepAlivePeriod();
	}

	/**
	 * starts a new session.
	 */
	public void newSession() {
		packagesCurrentSessionReceived = 0;
		packagesCurrentSessionSent = 0;
	}
	
	/**
	 * increases the number of received packages by one.
	 */
	public void packageReceived() {
		packagesReceived++;
		packagesCurrentSessionReceived++;
	}
	
	/**
	 * increases the number of sent packages by one.
	 */
	public void packageSent() {
		packagesSent++;
		packagesCurrentSessionSent++;
	}
	
	private String prep(String label, String value) {
		return String.format("%s:\t%s\n", label, value);
	}
	
	private String prep(String label, boolean value) {
		return prep(label, String.format("%b", value));
	}
	
	private String prep(String label, int value) {
		return prep(label, String.format("%d", value));
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(prep("name", getReaderName()));
		sb.append(prep("address", getReaderAddress()));
		sb.append(prep("port", getPort()));
		
		sb.append(prep("alive", isAlive()));
		sb.append(prep("connected", isConnected()));
		sb.append(prep("report keepAlive", isReportKeepAlive()));
		sb.append(prep("client initiated", isClientInitiated()));
		sb.append(prep("connect immediate", isConnectImmediately()));
		
		sb.append(prep("nSent", getPackagesSent()));
		sb.append(prep("nRecv", getPackagesReceived()));
		sb.append(prep("currentSession nSent", getPackagesCurrentSessionSent()));
		sb.append(prep("currentSession nRecv", getPackagesCurrentSessionReceived()));
		
		sb.append(prep("nAllowedKeepAliveMisses", getAllowNKeepAliveMisses()));
		sb.append(prep("keepAlive period", getKeepAlivePeriod()));
		return sb.toString();
	}
	
	/**
	 * @param keepAlivePeriod the interval for the reader to send a keep-alive message. 
	 */
	public void setKeepAlivePeriod(int keepAlivePeriod) {
		this.keepAlivePeriod = keepAlivePeriod;
	}

	/**
	 * @return the interval for the reader to send a keep-alive message. 
	 */
	public int getKeepAlivePeriod() {
		return keepAlivePeriod;
	}

	/**
	 * @param allowNKeepAliveMisses how many times a keep-alive can be missed.
	 */
	public void setAllowNKeepAliveMisses(int allowNKeepAliveMisses) {
		this.allowNKeepAliveMisses = allowNKeepAliveMisses;
	}

	/**
	 * @return how many times a keep-alive can be missed.
	 */
	public int getAllowNKeepAliveMisses() {
		return allowNKeepAliveMisses;
	}

	/**
	 * @param port the port where to connect.
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @return the port where to connect.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param readerAddress the address of the physical reader.
	 */
	public void setReaderAddress(String readerAddress) {
		this.readerAddress = readerAddress;
	}

	/**
	 * @return the address of the physical reader.
	 */
	public String getReaderAddress() {
		return readerAddress;
	}

	/**
	 * @param readerName the name of this logical reader.
	 */
	public void setReaderName(String readerName) {
		this.readerName = readerName;
	}

	/**
	 * @return the name of this logical reader.
	 */
	public String getReaderName() {
		return readerName;
	}
	
	/**
	 * @param alive whether the reader is alive.
	 */
	public void setAlive(boolean alive) {
		this.alive = alive;
	}

	/**
	 * @return whether the reader is alive.
	 */
	public boolean isAlive() {
		return alive;
	}

	/**
	 * @param packagesSent the number of sent packages in total.
	 */
	public void setPackagesSent(int packagesSent) {
		this.packagesSent = packagesSent;
	}

	/**
	 * @return the number of sent packages in total.
	 */
	public int getPackagesSent() {
		return packagesSent;
	}

	/**
	 * @param packagesReceived the number of received packages in total.
	 */
	public void setPackagesReceived(int packagesReceived) {
		this.packagesReceived = packagesReceived;
	}

	/**
	 * @return the number of received packages in total.
	 */
	public int getPackagesReceived() {
		return packagesReceived;
	}

	/**
	 * @param packagesCurrentSessionSent the number of sent packages in the current session.
	 */
	public void setPackagesCurrentSessionSent(int packagesCurrentSessionSent) {
		this.packagesCurrentSessionSent = packagesCurrentSessionSent;
	}

	/**
	 * @return the number of sent packages in the current session.
	 */
	public int getPackagesCurrentSessionSent() {
		return packagesCurrentSessionSent;
	}

	/**
	 * @param packagesCurrentSessionReceived the number of received packages in the current session.
	 */
	public void setPackagesCurrentSessionReceived(int packagesCurrentSessionReceived) {
		this.packagesCurrentSessionReceived = packagesCurrentSessionReceived;
	}

	/**
	 * @return the number of received packages in the current session.
	 */
	public int getPackagesCurrentSessionReceived() {
		return packagesCurrentSessionReceived;
	}

	/**
	 * @param reportKeepAlive whether keep alive messages get logged or not.
	 */
	public void setReportKeepAlive(boolean reportKeepAlive) {
		this.reportKeepAlive = reportKeepAlive;
	}

	/**
	 * @return whether keep alive messages get logged or not.
	 */
	public boolean isReportKeepAlive() {
		return reportKeepAlive;
	}

	/**
	 * @param clientInitiated the clientInitiated to set
	 */
	public void setClientInitiated(boolean clientInitiated) {
		this.clientInitiated = clientInitiated;
	}

	/**
	 * @return the clientInitiated
	 */
	public boolean isClientInitiated() {
		return clientInitiated;
	}

	/**
	 * @param connected flags whether the reader is connected or not.
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	/**
	 * @return whether the reader is connected or not.
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @param connectImmediately tells whether this reader connects directly after creation.
	 */
	public void setConnectImmediately(boolean connectImmediately) {
		this.connectImmediately = connectImmediately;
	}

	/**
	 * @return tells whether this reader connects directly after creation.
	 */
	public boolean isConnectImmediately() {
		return connectImmediately;
	}
}
