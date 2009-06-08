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

import org.fosstrak.llrp.client.LLRPMessageItem;

import java.util.ArrayList;

/**
 * This single access point for Reader Management module to access the message
 * repository. The instance class will be initiated in ResourceCenter.
 *
 * @author Haoning Zhang
 * @version 1.0
 */

public interface Repository {

	/** flag to set the retrieval of messages to all messages in the repo. */
	public static final int RETRIEVE_ALL = -1;
	
	/**
	 * How the resource layer (File System, Database, and etc.) initiates
	 * its resources. This function will be called when the client startup.
	 */
	public void open();
	
	/**
	 * How the resource layer (File System, Database, and etc.) close and destroy
	 * related resource handlers. This function will be called when the client exits.
	 */
	public void close();
	
	/**
	 * Get the LLRP Message Item from repository according to the unique Message ID.
	 * 
	 * @param aMsgSysId The unique message ID
	 * @return LLRP Message Wrapper Item
	 */
	public LLRPMessageItem get(String aMsgSysId);
	
	/**
	 * Put the LLRP Message Item to the repository
	 * 
	 * @param aMessage LLRP Message Wrapper Item
	 */
	public void put(LLRPMessageItem aMessage);
	
	/**
	 * returns all the messages from the specified adaptor and the reader 
	 * limited by num. if you set num to RETRIEVE_ALL all messages get returned.
	 * if you set readerName to null, all the messages of all the readers with 
	 * adaptor adaptorName will be returned.
	 * @param adaptorName the name of the adaptor.
	 * @param readerName the name of the reader.
	 * @param num how many messages to retrieve.
	 * @param content if true retrieve the message content, false no content.
	 * @return a list of messages.
	 */
	public ArrayList<LLRPMessageItem> get(
			String adaptorName, String readerName, int num, boolean content);
	
	/**
	 * returns the number of messages in the repository to a given filter.
	 * @param adaptor the name of the adaptor to filter. if null all the 
	 * messages in the repository get return.
	 * @param reader the name of the reader to filter. if null all the 
	 * messages of the given adaptor will be returned.
	 * @return the number of messages in the repository.
	 */
	public int count(String adaptor, String reader);
	
	/**
	 * Clear all the items in repository.
	 */
	public void clearAll();
	
	/**
	 * clear all the items that belong to a given adapter.
	 * @param adapter the name of the adapter to clear.
	 */
	public void clearAdapter(String adapter);
	
	/**
	 * clear all the items that belong to a given reader on a given adapter.
	 * @param adapter the name of the adapter where the reader belongs to.
	 * @param reader the name of the reader to clear.
	 */
	public void clearReader(String adapter, String reader);
}
