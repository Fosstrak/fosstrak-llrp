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

import java.rmi.RemoteException;
import java.util.*;
import org.apache.log4j.Logger;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.llrp.ltk.types.LLRPMessage;
import org.fosstrak.llrp.adaptor.Adaptor;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.commander.*;

/**
 * This Dialog will be triggered by the context menu from <code>LLRPEditor</code>
 * User pick up the readers to issue the LLRP messages.
 *
 * @author Haoning Zhang
 * @version 1.0
 */
public class SendMessageDialog extends org.eclipse.jface.dialogs.Dialog {
	
	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(SendMessageDialog.class);

	/**
	 * Record the selected items from the dialog.
	 */
	private int[] selectedList;
	
	private ProgressBar progressBar;
	
	private List outputList;
	
	private Table tblReaders;
	
	private Text txtComment;
	
	private static SendMessageDialog instance;
	
	/**
	 * Default Constructor
	 * 
	 * @param aShell
	 */
	private SendMessageDialog(Shell aShell) {
		super(aShell);
	}
	
	public static SendMessageDialog getInstance(Shell aShell) {
		if (null == instance) {
			instance = new SendMessageDialog(aShell);
		} 

		return instance;
	}
	

	/**
	 * Initialize the GUI components.
	 */
	protected Control createContents(Composite parent) {
		
		String fileName = ResourceCenter.getInstance().getCurrentFileName();
		
		parent.getShell().setLayout(new GridLayout(2, false));
		parent.getShell().setText("Sending " + fileName);
		
		GridData gridText = new GridData(GridData.FILL_HORIZONTAL);
		gridText.horizontalSpan = 2;
		gridText.heightHint = 15;
		gridText.widthHint = 400;
		
		GridData gridList = new GridData(GridData.FILL_HORIZONTAL);
		gridList.horizontalSpan = 2;
		gridList.heightHint = 180;
		
		GridData gridComment = new GridData(GridData.FILL_HORIZONTAL);
		gridComment.horizontalSpan = 2;
		gridComment.heightHint = 18;
		
		GridData gridOutput = new GridData(GridData.FILL_HORIZONTAL);
		gridOutput.horizontalSpan = 2;
		gridOutput.heightHint = 100;
		
		GridData gridBar = new GridData(GridData.FILL_HORIZONTAL);
		gridBar.horizontalSpan = 2;
		gridBar.heightHint = 15;
		
		GridData gridButton = new GridData(GridData.FILL_BOTH);
		gridButton.horizontalSpan = 1;
		gridButton.heightHint = 25;
		
		Rectangle shellBounds = parent.getShell().getShell().getBounds();
        Point dialogSize = parent.getShell().getSize();

        this.getShell().setLocation(shellBounds.x + (shellBounds.width - dialogSize.x) / 2,
          shellBounds.y + (shellBounds.height - dialogSize.y) / 2);
        
        
        final Label lblReaders = new Label(parent, SWT.NONE);
        lblReaders.setLayoutData(gridText);
        lblReaders.setText("Select Readers:");
        
        tblReaders = new Table (parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.FULL_SELECTION);
		tblReaders.setLayoutData(gridList);
		tblReaders.setBounds (0, 0, 300, 200);
		tblReaders.setLinesVisible (true);
		tblReaders.setHeaderVisible (true);
		
		TableColumn columnAdapter = new TableColumn (tblReaders, SWT.NONE);
		columnAdapter.setText ("Adapter");
		TableColumn columnReader = new TableColumn (tblReaders, SWT.NONE);
		columnReader.setText ("Reader");
		
		try {
			Iterator<String> i = AdaptorManagement.getInstance().getAdaptorNames().iterator();
			while (i.hasNext()) {
				Adaptor adaptor = AdaptorManagement.getInstance().getAdaptor(i.next());

				Iterator<String> j = adaptor.getReaderNames().iterator();
				while (j.hasNext()) {
					String readerName = j.next();
					if (adaptor.getReader(readerName).isConnected()) {
						
						TableItem item = new TableItem (tblReaders, 0);
						item.setText(0, adaptor.getAdaptorName());
						item.setText(1, readerName);
					}
				}
			}
		} catch (LLRPRuntimeException llrpe) {
			llrpe.printStackTrace();
		} catch (RemoteException re) {
			re.printStackTrace();
		}
		
		columnAdapter.pack();
		columnReader.pack();
		
		if (selectedList == null) {
			tblReaders.selectAll();
		} else {
			tblReaders.setSelection(selectedList);
		}
		
		
		final Label lblComment = new Label(parent, SWT.NONE);
		lblComment.setLayoutData(gridText);
		lblComment.setText("Comment:");
		
		txtComment = new Text(parent, SWT.BORDER);
		txtComment.setTextLimit(16);
		txtComment.setLayoutData(gridComment);
		
		final Label lblOutput = new Label(parent, SWT.NONE);
		lblOutput.setLayoutData(gridText);
		lblOutput.setText("Progress:");
        
		outputList = new List(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        outputList.setLayoutData(gridOutput);
        outputList.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
        outputList.setForeground(new Color(parent.getDisplay(), 0, 0, 0));
        
		progressBar = new ProgressBar (parent, SWT.SMOOTH);
		progressBar.setLayoutData(gridBar);
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSelection(0);

		
		final Button btnOK = new Button(parent, SWT.PUSH);
		btnOK.setText("Send");
		btnOK.setLayoutData(gridButton);
		btnOK.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  
		    	  btnOK.setEnabled(false);
		    	  outputList.removeAll();
		    	  
	    		  if (sendMessage()) {
	    			  close();
	    		  }
	    		  
		    	  btnOK.setText("Send Again");
		    	  btnOK.setEnabled(true);
		      }
		    });
		
		
		final Button btnCancel = new Button(parent, SWT.PUSH);
		btnCancel.setText("Close");
		btnCancel.setLayoutData(gridButton);
		btnCancel.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  close();
		      }
		    });
		
		if (tblReaders.getSelectionIndices().length == 0) {
			btnOK.setEnabled(false);
			btnCancel.setFocus();
		} else {
			btnOK.setEnabled(true);
			btnOK.setFocus();
			selectedList = tblReaders.getSelectionIndices();
		}
		
		tblReaders.addListener(SWT.Selection, new Listener () {
			public void handleEvent (Event e) {
				selectedList = tblReaders.getSelectionIndices();
				
				if ((selectedList == null) || (selectedList.length == 0)) {
					btnOK.setEnabled(false);
				} else {
					btnOK.setEnabled(true);
				}
			}
		});
		
		tblReaders.addListener(SWT.DefaultSelection, new Listener () {
			public void handleEvent (Event e) {
				selectedList = tblReaders.getSelectionIndices();
				if ((selectedList == null) || (selectedList.length == 0)) {
					btnOK.setEnabled(false);
				} else {
					btnOK.setEnabled(true);
				}
			}
		});
		
		parent.pack();
		return parent;
		
	}
	
	private void output(String aLine) {
		if (null != outputList) {
			outputList.add(aLine);
		}
	}
	
	private void setProgress(int aPercent) {
		if (aPercent < 0) {
			aPercent = 0;
		}
		
		if (aPercent > 100) {
			aPercent = 100;
		}
		
		if (null != progressBar) {
			progressBar.setSelection(aPercent);
		}
	}
	
	private boolean sendMessage() {
		
		String fileName = ResourceCenter.getInstance().getCurrentFileName();
		output("Reading " + fileName + "...");
		setProgress(10);
		
		String xmlContent = ResourceCenter.getInstance().getCurrentFile();
		
		if ((null == xmlContent) || (xmlContent.equals(""))) {
			output("Reading " + fileName + "failed!");
			return false;
		} else {
			output(fileName + " loaded.");
			setProgress(30);
		}
		
		LLRPMessage message = ResourceCenter.getInstance().generateLLRPMessage(xmlContent);
		if (null == message) {
			output("Generate LLRPMessage object failed!");
			return false;
		} else {
			output("LLRPMessage object generated successfully.");
			setProgress(60);
		}
		
		int step = progressBar.getMaximum() - progressBar.getSelection();
		
		if (selectedList.length > 0) {
			step = step / selectedList.length;
		}
		
		String comment = "";
		
		if ((txtComment != null) && (!txtComment.getText().equals(""))) {
			comment = txtComment.getText();
		}
		
		for (int i = 0; i < selectedList.length; i++) {
			
			TableItem item = tblReaders.getItem(selectedList[i]);
			ResourceCenter.getInstance().sendMessage(item.getText(0),
					item.getText(1), message, comment);
			output("Sending to " + item.getText(1) + " @ " + item.getText(0) + "...");
			setProgress(progressBar.getSelection() + (i + 1) * step);
		}
		
		return true;
	}
}
