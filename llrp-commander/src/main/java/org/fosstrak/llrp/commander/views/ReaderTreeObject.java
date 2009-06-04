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

package org.fosstrak.llrp.commander.views;

import java.util.ArrayList;


/**
* Models an object in the reader tree (eg. adapter or reader).
* @author zhanghao
*
*/
public class ReaderTreeObject extends MessageBoxTreeObject {
	
	private ArrayList<MessageBoxTreeObject> children;
	
	private boolean isConnected;
	private boolean isReader;
	private boolean isGetReaderConfig;
	private boolean isGetReaderROSpec;
	
	
	public ReaderTreeObject(String name) {
		super(name);
		children = new ArrayList<MessageBoxTreeObject>();
		setConnected(false);
		setReader(false);
		setGetReaderConfig(false);
		setGetReaderROSpec(false);
	}
	
	public void addChild(MessageBoxTreeObject child) {
		children.add(child);
		child.setParent(this);
	}
	
	public void removeChild(MessageBoxTreeObject child) {
		children.remove(child);
		child.setParent(null);
	}
	public MessageBoxTreeObject [] getChildren() {
		return (MessageBoxTreeObject [])children.toArray(new MessageBoxTreeObject[children.size()]);
	}
	public boolean hasChildren() {
		return children.size()>0;
	}
	
	/**
	 * Return whether the reader is connected.
	 * @return Reader is connected or not.
	 */
	public boolean isConnected() {
		return isConnected;
	}
	
	/**
	 * Set the flag to indicate whether the reader is connected.
	 * @param isConnected Reader is connected or not.
	 */
	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}
	
	/**
	 * Return whether the node represent a reader.
	 * @return This node is a reader or not
	 */
	public boolean isReader() {
		return isReader;
	}
	
	/**
	 * Set the flag to indicate whether the node represent a reader.
	 * @param isReader This node is a reader or not
	 */
	public void setReader(boolean isReader) {
		this.isReader = isReader;
	}

	/**
	 * Return whether the reader get GET_READER_CONFIG_RESPONSE message.
	 * @return Get GET_READER_CONFIG_RESPONSE message or not
	 */
	public boolean isGetReaderConfig() {
		return isGetReaderConfig;
	}

	/**
	 * Set the flag to indicate the reader get GET_READER_CONFIG_RESPONSE message.
	 * @param isGetReaderConfig Get GET_READER_CONFIG_RESPONSE message or not
	 */
	public void setGetReaderConfig(boolean isGetReaderConfig) {
		this.isGetReaderConfig = isGetReaderConfig;
	}
	
	/**
	 * Return whether the reader get GET_ROSPECS_RESPONSE message.
	 * @return Get GET_ROSPECS_RESPONSE message or not
	 */
	public boolean isGetReaderROSpec() {
		return isGetReaderROSpec;
	}

	/**
	 * Set the flag to indicate the reader get GET_ROSPECS_RESPONSE message.
	 * @param isGetReaderROSpec Get GET_ROSPECS_RESPONSE message or not
	 */
	public void setGetReaderROSpec(boolean isGetReaderROSpec) {
		this.isGetReaderROSpec = isGetReaderROSpec;
	}
}
