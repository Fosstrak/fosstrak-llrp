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

import java.io.StringReader;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.types.LLRPBitList;
import org.llrp.ltk.types.LLRPMessage;


/**
 * This is a helper class for LLRP Binary Message, which transform the XML message
 * to binary bit list by calling LTK Java APIs.
 * 
 * @author Haoning Zhang
 * @author sawielan
 * @version 1.0
 */
public class BinaryMessageHelper {

	/**
	 * Log4J instance.
	 */
	private static Logger log = Logger.getLogger(BinaryMessageHelper.class);
	
	private LLRPBitList bitList;
	
	/** denotes the number of characters used to split the parameters into several lines. */
	public static final int DEFAULT_LINE_LENGTH = 64;
	
	/** denotes the length of a chunk within a line. */
	public static final int DEFAULT_CHUNK_LENGTH = 8;
	
	/** the default separator between two chunks. */
	public static final String DEFAULT_CHUNK_DELIMITER = " ";
	
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
	
	/**
	 * create a new string that contains the delimiter ins every n characters.
	 * @param orig the original string.
	 * @param ins the delimiter to insert.
	 * @param n the number of characters to use between two delimiters.
	 * @return the resulting string.
	 */
	public String insert(String orig, String ins, int n) {
		StringBuffer copy = new StringBuffer();
		
		final int length = orig.length();
		for (int i=0; i<length; i+=n) {
			int up = (i+n<length-1) ? i+n : length-1;
			copy.append(orig.subSequence(i, up));
			if (up < length-1) {
				copy.append(ins);
			}
		}
		
		return copy.toString();
	}
	
	/**
	 * splits the parameters into an array of several strings with sub-chunks.
	 * @param lineLength the length of the resulting line.
	 * @param chunkLength the length of one chunk within the line.
	 * @param delimiter the delimiter between the chunks.
	 * @return an array encoding the parameters into a chunk of several lines.
	 */
	public String[] getArrParameters(final int lineLength, final int chunkLength, final String delimiter) {
		
		int length = bitList.length() - 64;
		if (0 >= length) {
			return new String[] { "" };
		}
		
		ArrayList<String> arr = new ArrayList<String>();
		int i=0;
		for (;i<length-lineLength; i+=lineLength) {
			arr.add(insert(bitList.subList(i+64, lineLength).toString(), delimiter, chunkLength));
		}
		if (i<length) {
			arr.add(insert(bitList.subList(i+64, length-i).toString(), delimiter, chunkLength));
		}
		String[] a = new String[arr.size()];
		arr.toArray(a);
		return a;
	}
}
