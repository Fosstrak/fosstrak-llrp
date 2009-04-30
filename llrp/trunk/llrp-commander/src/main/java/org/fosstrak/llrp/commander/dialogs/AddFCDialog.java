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

/**
 * dialog to add a new adaptor to the reader explorer.
 * @author sawielan
 *
 */
public class AddFCDialog extends ConnectDialog {
	
	/** the index for the adaptor name in the values array. */
	private static final int VALUE_NAME = 0;
	
	/** the index for the adaptor ip in the values array. */
	private static final int VALUE_IP = 1;

	/**
	 * create a new add adaptor dialog.
	 * @param aShell the parent shell.
	 */
	public AddFCDialog(Shell aShell) {
		super(aShell, "Add Adapter");
		FIELDS = new String[] { "Adapter Name", "IP" };
		DEFAULTS = new String [] { "AdapterName", "127.0.0.1" };
	}

	/**
	 * @return Logical Name of connection resource
	 */
	public String getName() {
		return values[VALUE_NAME];
	}
	
	/**
	 * @return IP Address of connection resource
	 */
	public String getIP() {
		return values[VALUE_IP];
	}

	@Override
	public Listener getListener(final Text txt, int offset, final Button ok) {
		Listener listener = null;
		switch (offset) {
		case VALUE_NAME:
			listener = new Listener() {
				public void handleEvent(Event event) {
					try {
						if ((txt.getText() == null) || (txt.getText().length() < 3)) {
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
		case VALUE_IP:
			// we don't care about the IP format (hope that user does it right).
			break;
		}
		return listener;
	}
}

