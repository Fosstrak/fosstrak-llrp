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

import org.eclipse.jface.viewers.*;

public class LLRPBinaryContentProvider implements ITreeContentProvider {

	private BinaryMessage message;
	
	public void inputChanged (Viewer aViewer, Object aOldInput, Object aNewInput) {
		message = (BinaryMessage) aNewInput;
	}
	
	public Object[] getElements(Object aElement) {
		return getChildren(aElement);
	}
	
	public Object[] getChildren(Object aElement) {
		if (aElement instanceof BinaryMessage) {
			BinaryMessage msg = (BinaryMessage) aElement;
			BinarySingleValue[] values = new BinarySingleValue[6];
			values[0] = msg.getReserved();
			values[1] = msg.getVersion();
			values[2] = msg.getMsgType();
			values[3] = msg.getMsgID();
			values[4] = msg.getMsgLength();
			values[5] = msg.getParameters();
			
			return values;
		}
		
		return null;
	}
	
	public Object getParent(Object aElement) {
		if (aElement instanceof BinarySingleValue) {
			BinarySingleValue value = (BinarySingleValue) aElement;
			return value.getParent();
		}
		return null;
	}
	
	public boolean hasChildren(Object aElement) {
		if (aElement instanceof BinaryMessage) {
			return true;
		}
		
		return false;
	}
	
	public void dispose() {
		
	}
}
