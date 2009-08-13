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

import org.apache.log4j.Logger;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * The XML Editor for LLRP in XML Format.
 * 
 * The editor extends Eclipse <code>TextEditor</code> by providing XML format
 * recognition.
 * 
 * @author Haoning Zhang
 * @version 1.0
 */
public class XMLEditor extends TextEditor {
	
	private ColorManager colorManager;

	/**
	 * Constructor, initial the ColorManager.
	 */
	public XMLEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}
	
	/**
	 * Dispose the ColorManager before disposing it's parent.
	 */
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
