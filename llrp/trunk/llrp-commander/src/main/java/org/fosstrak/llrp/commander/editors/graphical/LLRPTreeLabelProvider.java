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

package org.fosstrak.llrp.commander.editors.graphical;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.util.LLRPTreeMaintainer;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.LLRPParameter;

/**
 * Label Provider for the message tree.
 *
 * @author Ulrich Etter, ETHZ
 *
 */
class LLRPTreeLabelProvider extends LabelProvider {
	
	private LLRPTreeMaintainer treeMaintainer;

	public LLRPTreeLabelProvider(LLRPTreeMaintainer treeMaintainer){
		this.treeMaintainer = treeMaintainer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
	 */
	public String getText(Object object) {
		return treeMaintainer.getName(object);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object object) {
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		if (treeMaintainer.isValid(object)){
			if (object instanceof LLRPMessage){
			   return ResourceCenter.getInstance().getImage("Message.gif");
			}
			else if (object instanceof LLRPParameter){
				imageKey = ISharedImages.IMG_OBJ_ELEMENT;
			}
			else if (object instanceof java.util.List){
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			}
		}
		else{
			imageKey = ISharedImages.IMG_OBJS_ERROR_TSK;
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}
