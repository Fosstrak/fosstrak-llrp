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
	 * Get the Top N LLRP Message Wrapper items (desc by issue time). Please notice 
	 * that the return objects only for the MessageList view, there is NO message 
	 * content in those objects because of performance concern.
	 * 
	 * @param aRowNum The number N for Top N query.
	 * @return
	 */
	public ArrayList<LLRPMessageItem> getTopN(int aRowNum);
	
	/**
	 * Clear all the items in repository.
	 */
	public void clearAll();
}
