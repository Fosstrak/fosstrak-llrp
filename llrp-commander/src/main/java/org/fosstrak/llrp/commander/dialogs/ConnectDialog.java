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

/**
 * superclass for all the connect dialogs. all subclasses have to instantiate 
 * the two members FIELDS and DEFAULTS as arrays providing the labels and the 
 * default values for the fields available. <br/>
 * <br/>
 * <code>FIELDS = new String[]{ "test", "me" };</code><br/>
 * <code>DEFAULTS = new String [] { "myTestDefault", "memuuDefault" };</code><br/>
 * will create two fields with labels "test" and "me" with the respective 
 * default values.
 * 
 * @author sawielan
 *
 */
public class ConnectDialog extends org.eclipse.jface.dialogs.Dialog {
	
	/** the label of the fields. */
	public String [] FIELDS;
	
	/** the default values for the fields. */
	public String [] DEFAULTS;
	
	/** the values collected from the fields. */
	public String [] values;
	
	/** the caption. */
	private final String caption;
	
	/**
	 * create a new connect dialog.
	 * @param shell the parent shell.
	 * @param caption the caption for the dialog.
	 */
	public ConnectDialog(Shell shell, String caption) {
		super(shell);
		this.caption = caption;
	}
	
	/**
	 * Create GUI elements in the dialog.
	 */
	protected Control createContents(Composite parent) {
		values = new String[DEFAULTS.length];
		final Text []txts = new Text[DEFAULTS.length];
		
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
		parent.setSize(300, 50 + DEFAULTS.length * 30);	
		
		for (int i=0; i<FIELDS.length; i++) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(FIELDS[i]);
			label.setLayoutData(gridLabel);
			
			txts[i] = new Text(parent, SWT.BORDER);
			txts[i].setText(DEFAULTS[i]);
			txts[i].setLayoutData(gridText);
		}
		
		final Button btnOK = new Button(parent, SWT.PUSH);
		btnOK.setText("OK");
		btnOK.setLayoutData(gridLabel);
		
		btnOK.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  for (int i=0; i<DEFAULTS.length; i++) {
		    		  values[i] = txts[i].getText();
		    	  }
				
		    	  setReturnCode(Window.OK);
		    	  close();
		      }
		    });
		
		/*
		 // TODO: reintroduce this behavior for the tests
		   
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
		

		*/
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
