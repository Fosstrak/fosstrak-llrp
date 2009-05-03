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

package org.fosstrak.llrp.commander.editors;

import java.io.*;

import org.apache.log4j.Logger;

import org.jdom.Document;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.types.LLRPBitList;
import org.llrp.ltk.types.LLRPMessage;


/**
 * This is a helper class for LLRP Binary Message, which transform the XML message
 * to binary bit list by calling LTK Java APIs.
 * 
 * @author Haoning Zhang
 * @version 1.0
 */
public class BinaryMessageHelper {

	/**
	 * Log4J instance.
	 */
	private static Logger log = Logger.getLogger(BinaryMessageHelper.class);
	
	private LLRPBitList bitList;
	
	/**
	 * Constructor, initialize the valid Message in binary format
	 * 
	 * @param aXMLContent XML Message Format
	 * @throws Exception if message is not valid.
	 */
	public BinaryMessageHelper(String aXMLContent) throws Exception {
		log.debug("Start tranforming.");
		Document doc = new org.jdom.input.SAXBuilder()
				.build(new StringReader(aXMLContent));
		//XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		//log.debug("Input XML Message: " + outputter.outputString(doc));
		LLRPMessage message = LLRPMessageFactory.createLLRPMessage(doc);

		bitList = new LLRPBitList(message.toBinaryString());
		log.debug("Finish tranforming.");
	}
	
	/**
	 * Get the binary list.
	 * 
	 * @return Binary List
	 */
	public String getBinaryString() {
		return bitList.toString();
	}
	
	/**
	 * Get the reversed value of the message, in binary format.
	 * 
	 * @return Reserved Value
	 */
	public String getReserved() {
		return bitList.subList(0, 3).toString();
	}
	
	/**
	 * Get the version value of the message, in binary format.
	 * 
	 * @return Version Value
	 */
	public String getVersion() {
		return bitList.subList(3, 3).toString();
	}
	
	/**
	 * Get the message type value of the message, in binary format.
	 * 
	 * @return Message Type Value
	 */
	public String getMessageType() {
		return bitList.subList(6, 10).toString();
	}
	
	/**
	 * Get the length value of the message, in binary format.
	 * 
	 * @return Message Length Value
	 */
	public String getLength() {
		return bitList.subList(16, 32).toString();
	}
	
	/**
	 * Get the message ID  of the message, in binary format.
	 * 
	 * @return Message ID Value
	 */
	public String getMessageID() {
		return bitList.subList(48, 32).toString();
	}
	
	/**
	 * Get the ALL parameter values of the message, in binary format.
	 * 
	 * @return ALL parameter values
	 */
	public String getParameters() {
		int length = bitList.length() - 64;
		
		return (length > 0) ? bitList.subList(64, length).toString() : "";
	}
}
