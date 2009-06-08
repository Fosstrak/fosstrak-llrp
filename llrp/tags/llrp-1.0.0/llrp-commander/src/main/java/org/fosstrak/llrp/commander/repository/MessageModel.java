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

package org.fosstrak.llrp.commander.repository;

import org.eclipse.jface.viewers.*;
import org.fosstrak.llrp.commander.*;
import org.apache.log4j.Logger;


/**
* ...
* @author zhanghao
*
*/
public class MessageModel implements IStructuredContentProvider {
	
	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(MessageModel.class);
	
	/**
	 * Get All Message Meta Data
	 */
	public Object[] getElements(Object aInputElement) {
		log.debug("Load existing Messages Metadata List...");
		return ResourceCenter.getInstance().getMessageMetadataList().toArray();
	}

	public void inputChanged(Viewer aViewer, Object aOldInput, Object aNewInput) {
	}

	public void dispose() {
	}
}
