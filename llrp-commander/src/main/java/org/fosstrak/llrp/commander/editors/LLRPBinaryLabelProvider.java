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
import org.eclipse.swt.graphics.*;

public class LLRPBinaryLabelProvider extends LabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object aElement, int aColumnIndex) {
		return null;
	}

	public String getColumnText(Object aElement, int aColumnIndex) {
		
		if (aElement instanceof BinarySingleValue) {
			BinarySingleValue value = (BinarySingleValue) aElement;
			if (0 == aColumnIndex) {
				return value.getKey();
			} else {
				return value.getValue();
			}
		}
		
		if (aElement instanceof BinaryMessage) {
			if (0 == aColumnIndex) {
				return "Message";
			} 
		}
		return "";
	}

}
