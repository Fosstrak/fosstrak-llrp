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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.adaptor.ReaderMetaData;

/**
 * models a dialog to set the options for the message box view.
 * @author sawielan
 *
 */
public class ReaderSettingsDialog extends Dialog {
	
	private String reader;
	private String adaptor;

	/**
	 * create a new options dialog. 
	 * @param aShell Shell instance.
	 * @param mbv a link to the surrounding message box view.
	 */
	public ReaderSettingsDialog(Shell aShell, String adaptor, String reader) {
		super(aShell);
		this.adaptor = adaptor;
		this.reader = reader;
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
		gridLabel.heightHint=20;
		gridLabel.widthHint = 250;
		
		GridData gridText = new GridData(GridData.FILL_BOTH);
		gridText.verticalSpan = 1;
		gridText.horizontalSpan = 2;
		gridText.heightHint=20;
		gridText.widthHint = 150;
		
		parent.getShell().setLayout(layout);
		parent.getShell().setText("Messagebox View Options");
		
		GridData gridAll = new GridData(GridData.FILL_BOTH);
		gridAll.verticalSpan = 1;
		gridAll.horizontalSpan = 3;
		gridAll.widthHint = 400;
		gridAll.heightHint=20;
			
		final Button logKAMsg = new Button(parent, SWT.CHECK);
		logKAMsg.setText("Log Keep-Alive Messages");
		// we need to create a special grid data object for the check-box 
		// without width-hint as otherwise the check-box will not be displayed 
		// in *nix ...
		GridData gridDataKAMsg = new GridData();
		gridDataKAMsg.horizontalSpan = 3;
		logKAMsg.setLayoutData(gridDataKAMsg);
		
		try {
			logKAMsg.setSelection(
					AdaptorManagement.getInstance().getAdaptor(adaptor).
						getReader(reader).isReportKeepAlive());
		} catch (Exception e) {
			e.printStackTrace();
			logKAMsg.setSelection(false);
		} 
		

		try {
			ReaderMetaData metaData = AdaptorManagement.getInstance().
				getAdaptor(adaptor).getReader(reader).getMetaData();
			
			Label lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Reader Name:");
			Label txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(metaData.getReaderName());
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Reader Address:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(metaData.getReaderAddress());
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Port:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%d", metaData.getPort()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Is Alive:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%b", metaData.isAlive()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Connected to Reader:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%b", metaData.isConnected()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Connect immediately:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%b", metaData.isConnectImmediately()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Client initiated:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%b", metaData.isClientInitiated()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Report Keepalive:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%b", metaData.isReportKeepAlive()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Packages sent:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%d", metaData.getPackagesSent()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Packages received:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%d", metaData.getPackagesReceived()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Packages Current Session sent:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%d", metaData.getPackagesCurrentSessionSent()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Packages Current Session received:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%d", metaData.getPackagesCurrentSessionReceived()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Allowed Keepalive misses:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%d", metaData.getAllowNKeepAliveMisses()));
			
			lbl = new Label(parent, SWT.NONE);
			lbl.setLayoutData(gridLabel);
			lbl.setText("Keepalive Period:");
			txt = new Label(parent, SWT.NONE);
			txt.setLayoutData(gridText);
			txt.setText(String.format("%d", metaData.getKeepAlivePeriod()));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		

		final Button btnOK = new Button(parent, SWT.PUSH);
		btnOK.setText("OK");
		btnOK.setLayoutData(gridLabel);
		
		btnOK.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
	    			try {
	    				AdaptorManagement.getInstance().getAdaptor(adaptor).
	    					getReader(reader).setReportKeepAlive(logKAMsg.getSelection());
	    			} catch (Exception e1) {
	    				e1.printStackTrace();
	    				logKAMsg.setSelection(false);
	    			}
					setReturnCode(Window.OK);
					close();
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
		
		parent.pack();
		return parent;
	}
}
