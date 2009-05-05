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
import org.eclipse.swt.widgets.Text;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.adaptor.ReaderImpl;

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
		
		GridData gridText = new GridData(GridData.FILL_BOTH);
		gridText.verticalSpan = 1;
		gridText.horizontalSpan = 2;
		
		parent.getShell().setLayout(layout);
		parent.getShell().setText("Messagebox View Options");
		parent.setSize(350, 140);
		
		GridData gridAll = new GridData(GridData.FILL_BOTH);
		gridAll.verticalSpan = 1;
		gridAll.horizontalSpan = 3;
	
		final Label lblKAPeriod = new Label(parent, SWT.NONE);
		lblKAPeriod.setText("Keep-Alive Period (Not active yet!)");
		lblKAPeriod.setLayoutData(gridLabel);
		
		// FIXME : IMPLEMENT ME
		final Text txtKAPeriod = new Text(parent, SWT.BORDER);
		try {
			txtKAPeriod.setText(String.format("%d", 
					AdaptorManagement.getInstance().getAdaptor(adaptor).
					getReader(reader).getKeepAlivePeriod()));
		} catch (Exception e2) {
			e2.printStackTrace();
			txtKAPeriod.setText(String.format("%d",
					ReaderImpl.DEFAULT_KEEPALIVE_PERIOD));
		}
		txtKAPeriod.setLayoutData(gridText); 

		// FIXME : IMPLEMENT ME
		final Label lblKAMiss = new Label(parent, SWT.NONE);
		lblKAMiss.setText("Keep-Alive Miss allowed (Not active yet!)");
		lblKAMiss.setLayoutData(gridLabel);
		
		final Text txtKAMiss = new Text(parent, SWT.BORDER);
		txtKAMiss.setText(String.format("%d",
					ReaderImpl.DEFAULT_MISS_KEEPALIVE));
		txtKAMiss.setLayoutData(gridText); 
		
		final Button logKAMsg = new Button(parent, SWT.CHECK);
		logKAMsg.setText("Log Keep-Alive Messages");
		logKAMsg.setLayoutData(gridAll);
		try {
			logKAMsg.setSelection(
					AdaptorManagement.getInstance().getAdaptor(adaptor).
						getReader(reader).isReportKeepAlive());
		} catch (Exception e) {
			e.printStackTrace();
			logKAMsg.setSelection(false);
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
		
		return parent;
	}
}
