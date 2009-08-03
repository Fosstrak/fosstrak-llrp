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

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;

import java.text.*;
import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.repository.sql.DerbyRepository;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.apache.log4j.Logger;


/**
* label provider for the message box view. Depending on the column the 
* provider returns the message id, the adaptor/reader name etc.
* @author zhanghao
* @author sawielan
*
*/
public class MessageboxViewLabelProvider extends LabelProvider implements
		ITableLabelProvider {
	
	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(DerbyRepository.class);
	
	private static final DateFormat DATE_FORMATTER = new SimpleDateFormat(
			"yyyy-MMM-dd HH:mm:ss.SSS");

	public String getColumnText(Object aObj, int aIndex) {
		LLRPMessageItem msg = (LLRPMessageItem) aObj;
		
		switch (aIndex) {
			case MessageboxView.COL_MSG_ID:
				return msg.getId();
			case MessageboxView.COL_MSG_ADAPTER:
				return (msg.getAdapter()).trim();
			case MessageboxView.COL_MSG_READER:
				return (msg.getReader().trim());
			case MessageboxView.COL_MSG_TYPE:
				return msg.getMessageType();
			case MessageboxView.COL_STATUS_CODE:
				return msg.getStatusCode();
			case MessageboxView.COL_MSG_COMMENT:
				return msg.getComment();
			case MessageboxView.COL_MSG_TIME:
				return DATE_FORMATTER.format(msg.getTime());
		}
		
		return "";
	}

	public Image getColumnImage(Object aObj, int aIndex) {
		
		LLRPMessageItem msg = (LLRPMessageItem) aObj;
		
		if (aIndex == MessageboxView.COL_MSG_MARK) {
			
			ISharedImages sharedImages =
		         PlatformUI.getWorkbench().getSharedImages();
			
			log.debug("Mark value is " + msg.getMark());
			
			if (msg.getMark() == LLRPMessageItem.MARK_INCOMING) {
				return ResourceCenter.getInstance().getImage("incomingMsg.gif");
			} else {
				return ResourceCenter.getInstance().getImage("outgoingMsg.gif");
			}
			
		}
		
		return null;
	}
}
