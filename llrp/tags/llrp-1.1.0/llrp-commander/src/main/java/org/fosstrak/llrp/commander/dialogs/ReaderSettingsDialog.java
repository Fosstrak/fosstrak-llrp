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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
	 * @param adaptor a name of the adapter where the reader belongs to.
	 * @param reader the name of the reader to display the settings of.
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
		layout.numColumns = 2;
		
		parent.getShell().setLayout(layout);
		parent.getShell().setText("Messagebox View Options");
		
		GridData gridAll = new GridData(GridData.FILL_BOTH);
		gridAll.verticalSpan = 1;
		gridAll.horizontalSpan = 2;
		
		final Button logKAMsg = new Button(parent, SWT.CHECK);
		logKAMsg.setText("Log Keep-Alive Messages");
		// we need to create a special grid data object for the check-box 
		// without width-hint as otherwise the check-box will not be displayed 
		// in *nix ...
		GridData gridDataKAMsg = new GridData();
		gridDataKAMsg.horizontalSpan = 2;
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
			
			final Text[] txts = new Text[14];
			final Label[] lbls = new Label[14];
			int i=0;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Reader Name:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(metaData.getReaderName());
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Reader Address:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(metaData.getReaderAddress());
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Port:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%d", metaData.getPort()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Is Alive:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%b", metaData.isAlive()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Connected to Reader:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%b", metaData.isConnected()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Connect immediately:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%b", metaData.isConnectImmediately()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Client initiated:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%b", metaData.isClientInitiated()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Report Keepalive:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%b", metaData.isReportKeepAlive()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Packages sent:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%d", metaData.getPackagesSent()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Packages received:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%d", metaData.getPackagesReceived()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Packages Current Session sent:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%d", metaData.getPackagesCurrentSessionSent()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Packages Current Session received:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%d", metaData.getPackagesCurrentSessionReceived()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Allowed Keepalive misses:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%d", metaData.getAllowNKeepAliveMisses()));
			i++;
			
			lbls[i] = new Label(parent, SWT.NONE);
			lbls[i].setText("Keepalive Period:");
			txts[i] = new Text(parent, SWT.NONE);
			txts[i].setText(String.format("%d", metaData.getKeepAlivePeriod()));
			
			if (null != txts) {
				Color color = getShell().getDisplay().getSystemColor(
						SWT.COLOR_WHITE);
				for (int j=0; j<txts.length; j++) {
					if (null != txts[j]) {
						txts[j].setEditable(false);
						txts[j].setFont(parent.getFont());
						txts[j].setBackground(color);
					}
				}
			}
			if (null != lbls) {
				for (int j=0; j<lbls.length; j++) {
					if (null != lbls[j]) {
						lbls[j].setFont(parent.getFont());
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Composite buttonGroup = new Composite(parent, SWT.NONE);
		buttonGroup.setFont(parent.getFont());
		buttonGroup.setLayout(layout);

		final Button btnOK = new Button(buttonGroup, SWT.PUSH);
		btnOK.setText("OK");
		// set the keyboard focus
		btnOK.setFocus();
		btnOK.setFont(parent.getFont());
		GridData gd = new GridData();
		gd.widthHint = 75;
		btnOK.setLayoutData(gd);
		
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

		
		final Button btnCancel = new Button(buttonGroup, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  setReturnCode(Window.CANCEL);
		    	  close();
		      }
		    });
		btnCancel.setFont(parent.getFont());
		btnCancel.setLayoutData(gd);
		
		parent.pack();
		return parent;
	}
}
