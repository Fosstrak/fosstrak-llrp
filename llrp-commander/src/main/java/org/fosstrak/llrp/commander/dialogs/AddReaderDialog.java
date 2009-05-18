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

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.fosstrak.llrp.adaptor.AdaptorManagement;

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
	
	/** increasing number added automatically to the reader name. */
	private static long num = 0;
	
	/**
	 * create a new add reader dialog.
	 * @param aShell the parent shell.
	 */
	public AddReaderDialog(Shell aShell) {
		super(aShell, "Add Local Reader");
		FIELDS = new String[] { "Reader Name", "IP", "Port" };
		
		// make sure, we propose a unique reader name
		String readerName = String.format("ReaderName%d", num++);
		try {
			while (AdaptorManagement.getInstance().getAdaptor(
					AdaptorManagement.DEFAULT_ADAPTOR_NAME).containsReader(
							readerName)) {
				
				readerName = String.format("ReaderName%d", num++);
			}
		} catch (Exception e) {
			readerName = String.format("ReaderName%d", 
					System.currentTimeMillis());
		}
		DEFAULTS = new String [] { 	readerName, "127.0.0.1", "5084" };
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
	
	@Override
	public Listener getListener(final Text txt, int offset, final Button ok) {
		Listener listener = null;
		switch (offset) {
		case VALUE_READER_NAME:
			listener = new Listener() {
				public void handleEvent(Event event) {
					try {
						// do not allow:
						// - empty name
						// - name shorter than 3
						// - name that is already contained
						if ((txt.getText() == null) || (txt.getText().length() < 3) || 
								(AdaptorManagement.getInstance().
										getAdaptor(AdaptorManagement.DEFAULT_ADAPTOR_NAME).
											containsReader(txt.getText()))) {
							ok.setEnabled(false);
							
						} else {
							ok.setEnabled(true);
						}
					} catch (Exception e) {
						ok.setEnabled(false);
					}
				}
			};
			break;
		case VALUE_READER_IP:
			// we don't care about the IP format (hope that user does it right).
			break;		
		case VALUE_READER_PORT:
			listener = new Listener() {
				public void handleEvent(Event event) {
					try {
						// try to parse the port.
						final int port = Integer.parseInt(txt.getText());
						ok.setEnabled(true);
					} catch (Exception e) {
						ok.setEnabled(false);
					}
				}
			};
			break;
		}

		return listener;
	}
}
