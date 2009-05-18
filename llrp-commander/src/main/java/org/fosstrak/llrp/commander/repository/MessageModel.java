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
