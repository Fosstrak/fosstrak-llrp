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

	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(XMLEditor.class);
	
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
