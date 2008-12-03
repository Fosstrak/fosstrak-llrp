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

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class ConnectDialog extends org.eclipse.jface.dialogs.Dialog {
	
	
	/**
	 * Default Logical Name
	 */
	public final static String DEFAULT_NAME = "Untitled";
	
	/**
	 * Default IP
	 */
	public final static String DEFAULT_IP = "127.0.0.1";
	
	/**
	 * Default Port.
	 */
	public final static int DEFAULT_PORT = 5084;
	
	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(ConnectDialog.class);
	
	private String name;
	private String ip;
	private int port;
	
	private String caption;
	/**
	 * Constructor.
	 * 
	 * @param aShell Shell instance.
	 */
	public ConnectDialog(Shell aShell, String aCaption) {
		super(aShell);
		caption = aCaption;
		setName(DEFAULT_NAME);
		setIP(DEFAULT_IP);
		setPort(DEFAULT_PORT);
	}
	
	/**
	 * Get the logical name of connection resource
	 * @return Logical Name of connection resource
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the logical name of connection resource
	 * @param aName Logical Name of connection resource
	 */
	public void setName(String aName) {
		name = aName;
	}

	/**
	 * Get the IP Address of connection resource
	 * @return IP Address of connection resource
	 */
	public String getIP() {
		return ip;
	}

	/**
	 * Set the IP Address of connection resource
	 * @param aIP IP Address of connection resource
	 */
	public void setIP(String aIP) {
		ip = aIP;
	}

	/**
	 * Get the IP Port of connection resource
	 * @return IP Port of connection resource
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set the IP Port of connection resource
	 * @param aPort IP Port of connection resource
	 */
	public void setPort(int aPort) {
		port = aPort;
	}
	
	/**
	 * Create GUI elements in the dialog.
	 */
	protected Control createContents(Composite parent) {
	
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		
		GridData gridLabel = new GridData(GridData.FILL_BOTH);
		gridLabel.verticalSpan = 1;
		gridLabel.horizontalSpan = 1;
		
		GridData gridText = new GridData(GridData.FILL_BOTH);
		gridText.verticalSpan = 1;
		gridText.horizontalSpan = 2;
		
		parent.getShell().setLayout(layout);
		parent.getShell().setText(caption);
		parent.setSize(300, 150);
		
		
		
		Label lblName = new Label(parent, SWT.NONE);
		lblName.setText("Logical Name:");
		lblName.setLayoutData(gridLabel);
		
		final Text txtName = new Text(parent, SWT.BORDER);
		txtName.setText(getName());
		txtName.setLayoutData(gridText);
		
		
		Label lblIP = new Label(parent, SWT.NONE);
		lblIP.setText("IP Address:");
		lblIP.setLayoutData(gridLabel);
		
		final Text txtIP = new Text(parent, SWT.BORDER);
		txtIP.setText(getIP());
		txtIP.setLayoutData(gridText);
		
		Label lblPort = new Label(parent, SWT.NONE);
		lblPort.setText("Port:");
		lblPort.setLayoutData(gridLabel);
		
		final Text txtPort = new Text(parent, SWT.BORDER);
		txtPort.setLayoutData(gridText);
		txtPort.setText("" + getPort());
		
		final Button btnOK = new Button(parent, SWT.PUSH);
		btnOK.setText("OK");
		btnOK.setLayoutData(gridLabel);
		
		btnOK.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		        setName(txtName.getText());
		        setIP(txtIP.getText());
		        
		        setReturnCode(Window.OK);
		        close();
		      }
		    });
		
		
		txtName.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				try {
					if ((txtName.getText() == null) || (txtName.getText().length() < 3)) {
						btnOK.setEnabled(false);
					} else {
						btnOK.setEnabled(true);
						setName(txtName.getText());
					}
				} catch (Exception e) {
					btnOK.setEnabled(false);
				}
			}
		});
		
		txtPort.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				try {
					int port = (new Integer(txtPort.getText())).intValue();
					btnOK.setEnabled(true);
					setPort(port);
				} catch (Exception e) {
					btnOK.setEnabled(false);
				}
			}
		});
		

		
		final Button btnCancel = new Button(parent, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(gridLabel);
		btnCancel.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  setReturnCode(Window.CANCEL);
		    	  close();
		      }
		    });
		
		return parent;
	}
}
