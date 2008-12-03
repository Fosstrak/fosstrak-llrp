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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This wrapper class for LLRP Message. Some extended attributes added for
 * Repository use.
 *
 * @author Haoning Zhang
 * @version 1.0
 */
public class LLRPMessageItem {
	
	// Default values when it is empty
	private final static String EMPTY_READER_ID = "Unknown Reader";
	private final static String EMPTY_MESSAGE_TYPE = "Unknown Type";
	
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(
	"yyyy-MMM-dd-HH-mm-ss-SSS");
	
	/**
	 * Static DB field value for Imcoming Message.
	 */
	public final static int MARK_INCOMING = 1;
	
	/**
	 * Static DB field value for Outgoing Message
	 */
	public final static int MARK_OUTGOING = 2;
	
	private String msgId;
	private String adapterId;
	private String readerId;
	private String messageType;
	private String statusCode;
	private String comment;
	private Date issueTime;
	private String content;
	private int mark;
	
	/**
	 * Default Constructor
	 */
	public LLRPMessageItem() {
		setId(DATE_FORMATTER.format(new Date()));
		setMessageType(EMPTY_MESSAGE_TYPE);
		setStatusCode("");
		setTime(new Date());
		setReader(EMPTY_READER_ID);
		setContent("");
		setComment("");
		setMark(MARK_INCOMING);
		setAdapter(Reader.LOCAL_ADAPTER_NAME);
	}
	
	/**
	 * Get the unique reader name (Adapter Name + Reader Name).
	 * 
	 * @return Unique Reader Name
	 */
	public String getUniqueName() {
		return Reader.getUniqueReaderId(getAdapter(), getReader());
	}
	
	/**
	 * Get the Message Id
	 * 
	 * @return Message Id
	 */
	public String getId() {
		return msgId;
	}
	
	/**
	 * Set the Message Id
	 * 
	 * @param aId Message Id
	 */
	public void setId(String aId) {
		msgId = aId;
	}
	
	/**
	 * Get the Message Content as String
	 * 
	 * @return Message Content
	 */
	public String getContent() {
		return content;
	}
	
	/**
	 * Set the Message Content
	 * 
	 * @param content Message Content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Get the Reader Logical Name, without Adapter Logical Name.
	 * 
	 * @return Reader Logical Name
	 */
	public String getReader() {
		return readerId;
	}
	
	/**
	 * Set the Reader Logical Name, without Adapter Logical Name.
	 * 
	 * @param aReader Reader Logical Name
	 */
	public void setReader(String aReader) {
		readerId = aReader;
	}
	
	/**
	 * Get the Message Type.
	 * 
	 * @return Message Type
	 */
	public String getMessageType() {
		return messageType;
	}
	
	/**
	 * Set the Message Type.
	 * 
	 * @param messageType Message Type
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	/**
	 * Get the Status Code.
	 * 
	 * @return Status Code
	 */
	public String getStatusCode() {
		return statusCode;
	}
	
	/**
	 * Set the Status Code.
	 * 
	 * @param statusCode Status Code
	 */
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	
	/**
	 * Get the Message comments
	 * 
	 * @return Message comments
	 */
	public String getComment() {
		return comment;
	}
	
	/**
	 * Set the Message comments
	 * 
	 * @param comment Message comments
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	/**
	 * Get the Message issue time
	 * 
	 * @return Issue Time
	 */
	public Date getTime() {
		return issueTime;
	}
	
	/**
	 * Set the Message issue time
	 * 
	 * @param aTime Issue Time
	 */
	public void setTime(Date aTime) {
		issueTime = aTime;
	}

	/**
	 * Get the mark of the Message
	 * 
	 * Static value MARK_INCOMING for incoming messages
	 * Static value MARK_OUTGOING for outgoing messages
	 * 
	 * @return Message Mark
	 */
	public int getMark() {
		return mark;
	}

	/**
	 * Set the mark of the Message
	 * 
	 * Static value MARK_INCOMING for incoming messages
	 * Static value MARK_OUTGOING for outgoing messages
	 * 
	 * @param aMark Message Mark
	 */
	public void setMark(int aMark) {
		mark = aMark;
	}

	/**
	 * Get Adapter Logic Name
	 * 
	 * @return Adapter Logic Name
	 */
	public String getAdapter() {
		return adapterId;
	}

	/**
	 * Set Adapter Logic Name
	 * 
	 * @param aAdapterId Adapter Logic Name
	 */
	public void setAdapter(String aAdapterId) {
		adapterId = aAdapterId;
	}
	
}
