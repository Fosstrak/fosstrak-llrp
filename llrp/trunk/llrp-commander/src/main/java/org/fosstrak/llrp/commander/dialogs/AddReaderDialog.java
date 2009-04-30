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

package org.fosstrak.llrp.commander.dialogs;

import org.eclipse.swt.widgets.Shell;;

/**
 * dialog to add a new reader to the reader explorer.
 * @author sawielan
 *
 */
public class AddReaderDialog extends ConnectDialog {
	
	/** the index for the reader name in the values array. */
	private static final int VALUE_READER_NAME = 0;
	
	/** the index for the reader ip in the values array. */
	private static final int VALUE_READER_IP = 1;
	
	/** the index for the reader port in the values array. */
	private static final int VALUE_READER_PORT = 2;
	
	/**
	 * create a new add reader dialog.
	 * @param aShell the parent shell.
	 */
	public AddReaderDialog(Shell aShell) {
		super(aShell, "Add Local Reader");
		FIELDS = new String[] { "Reader Name", "IP", "Port" };
		DEFAULTS = new String [] { "ReaderName", "127.0.0.1", "5084" };
	}
	
	/**
	 * @return Logical Name of connection resource
	 */
	public String getName() {
		return values[VALUE_READER_NAME];
	}

	/**
	 * @return IP Address of connection resource
	 */
	public String getIP() {
		return values[VALUE_READER_IP];
	}

	/**
	 * @return IP Port of connection resource
	 */
	public int getPort() {
		return Integer.parseInt(values[VALUE_READER_PORT]);
	}
}
