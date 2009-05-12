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

/**
 * This is a wrapper class for LLRP Binary Message.
 * 
 * @author Haoning Zhang
 * @author sawielan
 * @version 1.0
 */
public class BinaryMessage {

	private BinarySingleValue reserved, version, msgType, msgLength, msgID;
	private BinarySingleValue parameters;
	private BinarySingleValue[] paramArr; 
	
	private final static String INVALID_VALUE = "Invalid";
	private final static String CAPTION_RSV = "RSV";
	private final static String CAPTION_VERSION = "Version";
	private final static String CAPTION_MSG_TYP = "Message Type";
	private final static String CAPTION_LENGTH = "Message Length in Bits";
	private final static String CAPTION_MSG_ID = "Message ID";
	private final static String CAPTION_PARAM = "Fields and Parameters";
	
	/**
	 * Constructor, to set the default values.
	 */
	public BinaryMessage() {
		reserved = initValue(CAPTION_RSV, INVALID_VALUE);
		version = initValue(CAPTION_VERSION, INVALID_VALUE);
		msgType = initValue(CAPTION_MSG_TYP, INVALID_VALUE);
		msgLength = initValue(CAPTION_LENGTH, INVALID_VALUE);
		msgID = initValue(CAPTION_MSG_ID, INVALID_VALUE);
		parameters = initValue(CAPTION_PARAM, INVALID_VALUE);
		paramArr = initArrValue(CAPTION_PARAM, null);
	}
	
	/**
	 * Constructor, with XML Message as input.
	 * The constructor initialize the message from LLRP XML Message.
	 * 
	 * @param aContent LLRP XML Message
	 * @throws Exception if message is not valid.
	 */
	public BinaryMessage(String aContent) throws Exception {
		
		BinaryMessageHelper helper = new BinaryMessageHelper(aContent);
		reserved = initValue(CAPTION_RSV, helper.getReserved());
		version = initValue(CAPTION_VERSION, helper.getVersion());
		msgType = initValue(CAPTION_MSG_TYP, helper.getMessageType());
		msgLength = initValue(CAPTION_LENGTH, helper.getLength());
		msgID = initValue(CAPTION_MSG_ID, helper.getMessageID());
		parameters = initValue(CAPTION_PARAM, helper.getParameters());
		paramArr = initArrValue(CAPTION_PARAM, helper.getArrParameters(
				BinaryMessageHelper.DEFAULT_LINE_LENGTH,
				BinaryMessageHelper.DEFAULT_CHUNK_LENGTH,
				BinaryMessageHelper.DEFAULT_CHUNK_DELIMITER));
	}
	
	/**
	 * Generate one <code>BinarySingleValue</code> instance.
	 * 
	 * @param aName Name of the Binary Item
	 * @param aValue Value of the Binary Item
	 * @return BinarySingleValue instance
	 */
	private BinarySingleValue initValue(String aName, String aValue) {
		BinarySingleValue aEntry = new BinarySingleValue(this);
		aEntry.setKey(aName);
		aEntry.setValue(aValue);
		
		return aEntry;
	}
	
	/**
	 * parse the parameters chunk into an array of equal length.
	 * @param name the name of the parameter array.
	 * @param value the value for the resulting binary single value.
	 * @return a binary value holding the parameters.
	 */
	private BinarySingleValue[] initArrValue(String name, String[] value) {
		if (null == value) {
			BinarySingleValue v = new BinarySingleValue(this);
			v.setKey(name);
			v.setValue(INVALID_VALUE);
			return new BinarySingleValue[] { v };
		}
		
		BinarySingleValue[] arr = new BinarySingleValue[value.length];
		for (int i=0; i<value.length; i++) {
			arr[i] = new BinarySingleValue(this);
			arr[i].setValue(value[i]);
		}
		arr[0].setKey(name);
		return arr;
	}
	
	/**
	 * Get <strong>Reserved</strong> value from LLRP message.
	 * 
	 * @return Reserved value
	 */
	public BinarySingleValue getReserved() {
		return reserved;
	}

	/**
	 * Get <strong>Version</strong> value from LLRP message.
	 * 
	 * @return Version value
	 */
	public BinarySingleValue getVersion() {
		return version;
	}

	/**
	 * Get <strong>MessageType</strong> value from LLRP message.
	 * 
	 * @return Message Type value
	 */
	public BinarySingleValue getMsgType() {
		return msgType;
	}

	/**
	 * Get <strong>MessageLength</strong> value from LLRP message.
	 * 
	 * @return Message Length value
	 */
	public BinarySingleValue getMsgLength() {
		return msgLength;
	}

	/**
	 * Get <strong>MessageID</strong> value from LLRP message.
	 * 
	 * @return Message ID value
	 */
	public BinarySingleValue getMsgID() {
		return msgID;
	}

	/**
	 * Get <strong>Parameters</strong> value from LLRP message.
	 * 
	 * @return All parameters value
	 */
	public BinarySingleValue getParameters() {
		return parameters;
	}
	
	/**
	 * get the parameters value as an array split. This is a quick fix to 
	 * circumvent the eclipse table view in-capability to display line feeds.
	 * @return an array holding the parameters as an array.
	 */
	public BinarySingleValue[] getSplitParameters() {
		return paramArr;
	}
}
