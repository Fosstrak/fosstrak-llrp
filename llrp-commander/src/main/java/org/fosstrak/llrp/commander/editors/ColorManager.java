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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * A helper class for supporting the color table used in XML Editor.
 * 
 * @author Haoning Zhang
 * @version 1.0
 */
public class ColorManager {
	
	/**
	 * Max different colors used in the editor.
	 */
	private final static int MAX_COLORS = 10;
	
	/**
	 * Color table.
	 */
	protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(MAX_COLORS);

	/**
	 * Dispose all the <code>Color</code> in the Color Table.
	 */
	public void dispose() {
		Iterator<Color> e = fColorTable.values().iterator();
		while (e.hasNext())
			 e.next().dispose();
	}
	
	/**
	 * Get the <code>Color</code> instance by <code>RGB</code>.
	 * The color will be created when first uses.
	 * 
	 * @param aRGB RGB instance
	 * @return Color instance from Color Table
	 */
	public Color getColor(RGB aRGB) {
		Color color = fColorTable.get(aRGB);
		if (color == null) {
			color = new Color(Display.getCurrent(), aRGB);
			fColorTable.put(aRGB, color);
		}
		return color;
	}
}
