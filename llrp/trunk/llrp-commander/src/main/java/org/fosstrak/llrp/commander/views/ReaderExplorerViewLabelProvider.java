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

package org.fosstrak.llrp.commander.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.fosstrak.llrp.commander.ResourceCenter;


/**
* label provider for the reader explorer view. Essentially returns the 
* adaptor name (in case of an adaptor) or the reader name (together with the 
* status of the reader).
* @author zhanghao
* @author sawielan
*
*/
public class ReaderExplorerViewLabelProvider extends LabelProvider {
	
	/**
	 * Get the string value of the object on the tree.
	 */
	public String getText(Object obj) {
		
		if (null == obj) {
			return "";
		} 
		
		if (obj instanceof ReaderTreeObject) {
			ReaderTreeObject rdr = (ReaderTreeObject) obj;
			
			String status = rdr.isConnected() ? "Connected" : "Disconnected";
			
			if (rdr.isReader()) {
				return rdr.toString() + "(" + status + ")";
			}
		}
		
		return obj.toString();
	}

	/**
	 * Get the Image object of the object on the tree.
	 */
	public Image getImage(Object obj) {
		
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		if (obj instanceof ReaderTreeObject) {
			//imageKey = ISharedImages.IMG_OBJ_FOLDER;
			ReaderTreeObject treeObj = (ReaderTreeObject) obj;
			
			if (treeObj.isReader()) {
				if (treeObj.isConnected()) {
					return ResourceCenter.getInstance().getImage("People_069.gif");
				} else {
					return ResourceCenter.getInstance().getImage("People_070.gif");
				}
			}
			
			return ResourceCenter.getInstance().getImage("adapter.gif");
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}

}
