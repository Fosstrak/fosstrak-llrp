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

import java.util.Iterator;

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
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.fosstrak.llrp.commander.check.CheckEclipseProject;
import org.fosstrak.llrp.commander.check.CheckRepository;
import org.fosstrak.llrp.commander.check.HealthCheck;

/**
* Dialog displaying the results of the sanity-checks. 
* @author zhanghao
* @author sawielan
*
*/
public class HealthCheckDialog extends org.eclipse.jface.dialogs.Dialog {
	
	private HealthCheck healthCheck;
	
	/**
	 * Default Constructor
	 * 
	 * @param aShell
	 */
	public HealthCheckDialog(Shell aShell) {
		super(aShell);
	}
	
	/**
	 * Initialize the GUI components.
	 */
	protected Control createContents(Composite parent) {
		
		parent.getShell().setLayout(new GridLayout(3, false));
		parent.getShell().setText("LLRP Client Health Check");
		parent.setSize(600, 230);
		
		GridData gridText = new GridData(GridData.FILL_HORIZONTAL);
		gridText.horizontalSpan = 3;
		gridText.heightHint = 15;
		
		GridData gridOutput = new GridData(GridData.FILL_HORIZONTAL);
		gridOutput.horizontalSpan = 3;
		gridOutput.heightHint = 130;
		
		GridData gridButton = new GridData(GridData.FILL_BOTH);
		gridButton.horizontalSpan = 1;
		gridButton.heightHint = 25;
		
        final Label lblCheck = new Label(parent, SWT.NONE);
        lblCheck.setLayoutData(gridText);
        lblCheck.setText("Check Progress:");
        
        final List outputList = new List(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        outputList.setLayoutData(gridOutput);
        outputList.setBackground(new Color(parent.getDisplay(), 255, 255, 255));
        outputList.setForeground(new Color(parent.getDisplay(), 0, 0, 0));
        
		final Button btnCheck = new Button(parent, SWT.PUSH);
		btnCheck.setText("Check Again");
		btnCheck.setLayoutData(gridButton);
		
		final Button btnFix = new Button(parent, SWT.PUSH);
		btnFix.setText("Fix it!");
		btnFix.setLayoutData(gridButton);
		
		final Button btnClose = new Button(parent, SWT.PUSH);
		btnClose.setText("Close");
		btnClose.setLayoutData(gridButton);
		
		btnCheck.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  
		    	  btnCheck.setEnabled(false);
		    	  btnFix.setEnabled(false);
		    	  btnClose.setEnabled(false);
		    	  
		    	  outputList.removeAll();
		    	  
		    	  if (!getHealthCheck().validate()) {
		    		  Iterator<String> i = getHealthCheck().getReport().iterator();
		    		  while (i.hasNext()) {
		    			  outputList.add(i.next());
		    		  }
		    		  btnFix.setEnabled(true);
		    	  }

		    	  btnCheck.setEnabled(true);
		    	  btnClose.setEnabled(true);
		      }
		    });
		
		
		btnFix.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  
		    	  btnCheck.setEnabled(false);
		    	  btnFix.setEnabled(false);
		    	  btnClose.setEnabled(false);
		    	  
		    	  outputList.removeAll();
		    	  
		    	  if (!getHealthCheck().validate()) {
		    		  
		    		  getHealthCheck().fix();
		    		  
		    		  Iterator<String> i = getHealthCheck().getReport().iterator();
		    		  while (i.hasNext()) {
		    			  outputList.add(i.next());
		    		  }
		    		  btnFix.setEnabled(true);
		    	  }

		    	  btnCheck.setEnabled(true);
		    	  btnFix.setEnabled(false);
		    	  btnClose.setEnabled(true);
		      }
		    });
		
		
		btnClose.addSelectionListener(new SelectionAdapter() {
		      public void widgetSelected(SelectionEvent e) {
		    	  close();
		      }
		    });
		
		return parent;
		
	}
	
	public HealthCheck getHealthCheck() {
		if (null == healthCheck) {
			healthCheck = new HealthCheck();
			healthCheck.registerCheckItem(new CheckEclipseProject());
			healthCheck.registerCheckItem(new CheckRepository());
		}
		return healthCheck;
	}

}
