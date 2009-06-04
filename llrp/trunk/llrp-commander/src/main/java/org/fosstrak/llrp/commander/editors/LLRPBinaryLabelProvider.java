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

package org.fosstrak.llrp.commander.editors;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.*;

/**
 * Provides the labels to the content of an LLRP message for a tree viewer. 
 * @author Haoning Zhang
 *
 */
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
