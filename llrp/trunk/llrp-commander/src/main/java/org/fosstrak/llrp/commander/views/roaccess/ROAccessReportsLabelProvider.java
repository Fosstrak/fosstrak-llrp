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

package org.fosstrak.llrp.commander.views.roaccess;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.fosstrak.llrp.client.repository.sql.roaccess.ROAccessItem;

/**
 * Provides the labels for the table in {@link ROAccessReportsView}.
 * @author sawielan
 *
 */
public class ROAccessReportsLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object arg0, int index) {
		return null;
	}

	public String getColumnText(Object arg0, int index) {
		
		if (arg0 instanceof ROAccessItem) {
			ROAccessItem item = (ROAccessItem) arg0;

			// increase by one to fit to derby index.
			String str = item.getAsString(index + 1);
			if (null != str) {
				return str;
			}
		}
		return "";
	}
}
