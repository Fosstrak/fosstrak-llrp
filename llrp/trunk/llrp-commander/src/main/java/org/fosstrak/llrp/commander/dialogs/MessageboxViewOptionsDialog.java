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

import org.eclipse.jface.dialogs.Dialog;
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
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.views.MessageboxView;

/**
 * models a dialog to set the options for the message box view.
 * @author sawielan
 *
 */
public class MessageboxViewOptionsDialog extends Dialog {

	/** the refresh time. */
	private long refreshTime;
	
	/** the number of messages to be displayed. */
	private int numberOfMessages;
	
	/**
	 * create a new options dialog. 
	 * @param aShell Shell instance.
	 * @param mbv a link to the surrounding message box view.
	 */
	public MessageboxViewOptionsDialog(Shell aShell, MessageboxView mbv) {
		super(aShell);
		numberOfMessages = mbv.getDisplayNumMessages();
		refreshTime = ResourceCenter.getInstance().getMessageBoxRefresh().getRefreshTime();
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
		parent.getShell().setText("Messagebox View Options");
		parent.setSize(300, 150);
		
		Label lblRefreshTime = new Label(parent, SWT.NONE);
		lblRefreshTime.setText("Refresh Time (ms):");
		lblRefreshTime.setLayoutData(gridLabel);
		
		final Text txtRefreshTime = new Text(parent, SWT.BORDER);
		txtRefreshTime.setText(String.format("%d", getRefreshTime()));
		txtRefreshTime.setLayoutData(gridText);
		
		
		Label lblNMsg = new Label(parent, SWT.NONE);
		lblNMsg.setText("Number of Messages:");
		lblNMsg.setLayoutData(gridLabel);
		
		final Text txtNMsg = new Text(parent, SWT.BORDER);
		txtNMsg.setText(String.format("%d", getNumberOfMessages()));
		txtNMsg.setLayoutData(gridText);
		
		final Button btnOK = new Button(parent, SWT.PUSH);
		btnOK.setText("OK");
		btnOK.setLayoutData(gridLabel);
		
		btnOK.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		        setRefreshTime(Long.parseLong(txtRefreshTime.getText()));
		        setNumberOfMessages(Integer.parseInt(txtNMsg.getText()));
		        
		        setReturnCode(Window.OK);
		        close();
		      }
		    });
		
		
		txtRefreshTime.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				try {
					if ((txtRefreshTime.getText() == null) || (txtRefreshTime.getText().length() < 3)) {
						btnOK.setEnabled(false);
					} else {
						btnOK.setEnabled(true);
						setRefreshTime(Long.parseLong(txtRefreshTime.getText()));
					}
				} catch (Exception e) {
					btnOK.setEnabled(false);
				}
			}
		});
		
		txtNMsg.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event event) {
				try {
					final int n = (new Integer(txtNMsg.getText())).intValue();
					btnOK.setEnabled(true);
					setNumberOfMessages(n);
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

	/**
	 * @param refreshTime the refreshTime to set
	 */
	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
	}

	/**
	 * @return the refreshTime
	 */
	public long getRefreshTime() {
		return refreshTime;
	}

	/**
	 * @param numberOfMessages the numberOfMessages to set
	 */
	public void setNumberOfMessages(int numberOfMessages) {
		this.numberOfMessages = numberOfMessages;
	}

	/**
	 * @return the numberOfMessages
	 */
	public int getNumberOfMessages() {
		return numberOfMessages;
	}
}
