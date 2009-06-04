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

import org.eclipse.core.runtime.IAdaptable;


/**
* Entry for the message box view. 
* @author zhanghao
*
*/
public class MessageBoxTreeObject implements IAdaptable {
	
	public final static String INBOX_NAME = "Inbox";
	public final static String OUTBOX_NAME = "Outbox";
	
	private String name;
	private ReaderTreeObject parentReader;
	
	public MessageBoxTreeObject(String aName) {
		name = aName;
	}
	public String getName() {
		return name;
	}
	public void setParent(ReaderTreeObject aParent) {
		parentReader = aParent;
	}
	public ReaderTreeObject getParent() {
		return parentReader;
	}
	public String toString() {
		return getName();
	}
	public Object getAdapter(Class key) {
		return null;
	}
}
