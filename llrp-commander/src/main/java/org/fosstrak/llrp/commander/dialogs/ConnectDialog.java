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
import org.eclipse.swt.widgets.Listener;
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
public abstract class ConnectDialog extends org.eclipse.jface.dialogs.Dialog {
	
	/** the label of the fields. */
	public String [] FIELDS;
	
	/** the default values for the fields. */
	public String [] DEFAULTS;
	
	/** the values collected from the fields. */
	public String [] values;
	
	/** the text fields. */
	protected Text []txts;
	
	/** the caption. */
	protected final String caption;
	
	/** the grid settings for the label fields. */
	protected GridData gridLabel = new GridData(GridData.FILL_BOTH);
	
	/** the grid settings for the text fields. */
	protected GridData gridText = new GridData(GridData.FILL_BOTH);
	
	/** the grid settings for a horizontal filler. */
	protected GridData gridAll = new GridData(GridData.FILL_BOTH);
	
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
	 * sets the layout for the dialog.
	 * @param parent the parent where to set the layout.
	 */
	protected void setLayout(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		
		gridLabel.verticalSpan = 1;
		gridLabel.horizontalSpan = 1;
		gridLabel.widthHint=100;
		gridLabel.heightHint = 20;

		gridText.verticalSpan = 1;
		gridText.horizontalSpan = 2;
		gridText.widthHint=200;	
		gridText.heightHint = 20;

		gridAll.verticalSpan = 1;
		gridAll.horizontalSpan = 3;
		gridAll.heightHint = 20;
		
		parent.getShell().setLayout(layout);
		parent.getShell().setText(caption);
	}
	
	/**
	 * registers listeners to the text fields with the OK button as the parent.
	 * @param btnOK the ok button to use as parent.
	 */
	protected void registerTextFieldListeners(Button btnOK) {
		// add the selection listeners.
		for (int i=0; i<DEFAULTS.length; i++) {
			Listener listener = getListener(txts[i], i, btnOK);
			if (null != listener) {
				// add a listener
				txts[i].addListener(SWT.Modify, listener);
			}
		}
	}
	
	/**
	 * adds the text fields.
	 * @param parent the parent where to add.
	 */
	protected void addTextFields(Composite parent) {
		values = new String[DEFAULTS.length];
		txts = new Text[DEFAULTS.length];
		for (int i=0; i<FIELDS.length; i++) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(FIELDS[i]);
			label.setLayoutData(gridLabel);
			
			txts[i] = new Text(parent, SWT.BORDER);
			txts[i].setText(DEFAULTS[i]);
			txts[i].setLayoutData(gridText);
		}
	}
	
	/**
	 * adds a Cancel button.
	 * @param parent the parent where to add.
	 */
	protected void addCancelButton(Composite parent) {		
		final Button btnCancel = new Button(parent, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(gridLabel);
		btnCancel.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  setReturnCode(Window.CANCEL);
		    	  close();
		      }
		    });
	}
	
	/**
	 * adds a OK button and installs the necessary listeners.
	 * @param parent the parent where to add.
	 */
	protected void addOKButton(Composite parent) {
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
		
		// add the selection listeners.
		for (int i=0; i<DEFAULTS.length; i++) {
			Listener listener = getListener(txts[i], i, btnOK);
			if (null != listener) {
				// add a listener
				txts[i].addListener(SWT.Modify, listener);
			}
		}
	}
	
	/**
	 * adds an invisible button to the grid to keep the alignment.
	 * @param parent the parent where to add.
	 */
	protected void addInvisibleButton(Composite parent) {
		// empty invisible button to make nice alignment
		final Button none = new Button(parent, SWT.NONE);
		none.setVisible(false);
		none.setLayoutData(gridLabel);
	}
	
	/**
	 * Create GUI elements in the dialog.
	 */
	protected Control createContents(Composite parent) {
		
		setLayout(parent);
		
		addTextFields(parent);
		addInvisibleButton(parent);
		addOKButton(parent);
		addCancelButton(parent);

		parent.pack();	
		return parent;
	}
	
	/**
	 * this method allows the subclasses to put constraints via listeners 
	 * on the content of the value fields. you can use the offset to determine 
	 * the field.
	 * @param txt the field holding the changed text.
	 * @param offset the offset of the field. 
	 * @param ok the OK button.
	 * @return null if no constraint, otherwise the listener.
	 */
	public abstract Listener getListener(final Text txt, int offset, final Button ok);
}
