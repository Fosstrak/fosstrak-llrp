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

package org.fosstrak.llrp.commander.dialogs;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	
	/** flag whether client initiated or not. */
	private boolean ci = true;
	
	/** handle to the client initiated connection button. */
	protected Button cICon;
	
	/** flag whether connect immediately or not. */
	private boolean connectImmediate = true;
	
	/** handle to the connect immediately button. */
	protected Button conImmed;
	
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
	
	@Override
	protected void addOKButton(Composite parent) {
		final Button btnOK = new Button(parent, SWT.PUSH);
		btnOK.setText("OK");
		btnOK.setLayoutData(gridLabel);
		
		btnOK.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  for (int i=0; i<DEFAULTS.length; i++) {
		    		  values[i] = txts[i].getText();
		    	  }
				
		    	  ci = cICon.getSelection();
		    	  connectImmediate = conImmed.getSelection();
		    	  
		    	  setReturnCode(Window.OK);
		    	  close();
		      }
		    });
		
		registerTextFieldListeners(btnOK);
	}
	
	/**
	 * Create GUI elements in the dialog.
	 */
	protected Control createContents(Composite parent) {
		setLayout(parent);
		
		addTextFields(parent);
		
		// we need to create a special grid data object for the check-box 
		// without width-hint as otherwise the check-box will not be displayed 
		// in *nix ...
		GridData gridNoWidthHint = new GridData();
		gridNoWidthHint.horizontalSpan = 3;
		
		conImmed = new Button(parent, SWT.CHECK);
		conImmed.setText("Connect immediately");
		conImmed.setLayoutData(gridNoWidthHint);
		conImmed.setSelection(true);
		
		cICon = new Button(parent, SWT.CHECK);
		cICon.setText("Connector mode (initiate connection to the reader)");
		cICon.setLayoutData(gridNoWidthHint);
		cICon.setSelection(true);
		
		addInvisibleButton(parent);
		addOKButton(parent);
		addCancelButton(parent);

		parent.pack();
		return parent;
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
	
	/**
	 * @return true if client initiates connection, false otherwise.
	 */
	public boolean isClientInitiated() {
		return ci;
	}
	
	/**
	 * @return true if connect immediately, false otherwise.
	 */
	public boolean isConnectImmediately() {
		return connectImmediate;
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
