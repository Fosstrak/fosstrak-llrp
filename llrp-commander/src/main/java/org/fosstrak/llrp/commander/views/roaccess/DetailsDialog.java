package org.fosstrak.llrp.commander.views.roaccess;

import java.util.HashMap;

import org.apache.log4j.Logger;
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
import org.epcglobalinc.tdt.LevelTypeList;
import org.fosstrak.llrp.client.repository.sql.roaccess.AbstractSQLROAccessReportsRepository;
import org.fosstrak.llrp.client.repository.sql.roaccess.DerbyROAccessReportsRepository;
import org.fosstrak.llrp.client.repository.sql.roaccess.ROAccessItem;
import org.fosstrak.llrp.commander.views.ReaderExplorerView;
import org.fosstrak.tdt.TDTEngine;

public class DetailsDialog extends Dialog {
	
	final protected ROAccessItem item;
	
	// the log4j logger.
	private static Logger log = Logger.getLogger(DetailsDialog.class);

	public DetailsDialog(Shell parent, ROAccessItem item) {
		super(parent);
		this.item = item;
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		
		parent.getShell().setLayout(layout);
		parent.getShell().setText("Details page:");
		
		GridData gridAll = new GridData();
		gridAll.horizontalSpan = 2;
		
		String[][] cnt = DerbyROAccessReportsRepository.COLUMN_NAMES_AND_TYPES;
		Color color = getShell().getDisplay().getSystemColor(
				SWT.COLOR_WHITE);
		for (int i=0; i<cnt.length; i++) {
			final Label lbl = new Label(parent, SWT.NONE);
			lbl.setText(cnt[i][0] + ": ");
			lbl.setFont(parent.getFont());
			
			final Text txt = new Text(parent, SWT.NONE);
			String s = item.getAsString(i + 1);
			if (null == s) s = "";
			txt.setText(s);
			txt.setEditable(false);
			txt.setBackground(color);
			
			if (!s.equalsIgnoreCase("") && 
					(AbstractSQLROAccessReportsRepository.CINDEX_EPC == i+1)) {
				// try to add the tdt stuff...
				try {
					TDTEngine tdt = new TDTEngine();
					
					String binary = tdt.hex2bin(s);
					if (binary.startsWith("1")) binary = "00" + binary;
					
					// try to translate to tag and epc
					String asEPC = tdt.convert(
							binary, 
							new HashMap<String, String> (), 
							LevelTypeList.PURE_IDENTITY);
					
					final Label lblEPC = new Label(parent, SWT.NONE);
					lblEPC.setText("   decoded as epc-pure: ");
					lblEPC.setFont(parent.getFont());
					
					final Text txtEPC = new Text(parent, SWT.NONE);
					txtEPC.setText(asEPC);
					txtEPC.setFont(parent.getFont());
					txtEPC.setEditable(false);
					txtEPC.setBackground(color);
					
					String asTag = tdt.convert(
							binary, 
							new HashMap<String, String> (), 
							LevelTypeList.TAG_ENCODING);
					
					final Label lblTag = new Label(parent, SWT.NONE);
					lblTag.setText("   decoded as epc-tag: ");
					lblTag.setFont(parent.getFont());
					
					final Text txtTag = new Text(parent, SWT.NONE);
					txtTag.setText(asTag);
					txtTag.setFont(parent.getFont());
					txtTag.setEditable(false);
					txtTag.setBackground(color);
				} catch (Exception e) {
					log.debug(String.format("Could not translate epc: '%s'",
							e.getMessage()));
				}
			}
		}
		
		// add CSV (for now not active as it destroys the layout)
//		final Label lbl = new Label(parent, SWT.NONE);
//		lbl.setText("as CSV");
//		lbl.setFont(parent.getFont());
//		
//		final Text txt = new Text(parent, SWT.NONE);
//		txt.setFont(parent.getFont());
//		txt.setText(item.getAsCSV());
//		
//		
//		final Button btnOk = new Button(parent, SWT.PUSH);
//		btnOk.setText("Close");
//		btnOk.setLayoutData(gridAll);
//		btnOk.addSelectionListener(new SelectionAdapter() {
//		      public void widgetSelected(SelectionEvent e) {
//		    	  setReturnCode(Window.OK);
//		    	  close();
//		      }
//		    });
//		
		parent.pack();
		return parent;
	}
}
