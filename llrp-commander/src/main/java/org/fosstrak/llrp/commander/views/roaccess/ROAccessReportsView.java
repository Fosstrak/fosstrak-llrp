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

package org.fosstrak.llrp.commander.views.roaccess;

import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.client.MessageHandler;
import org.fosstrak.llrp.client.ROAccessReportsRepository;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.client.repository.sql.roaccess.DerbyROAccessReportsRepository;
import org.fosstrak.llrp.client.repository.sql.roaccess.ROAccessItem;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.views.TableViewPart;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.types.LLRPMessage;

/**
 * View displaying the contents of RO_ACCESS_REPORT. The different fields of 
 * the report get expanded into columns. The messages are sorted according 
 * the arrival time.
 * @author sawielan
 *
 */
public class ROAccessReportsView extends TableViewPart implements MessageHandler {
	
	// the column headers.
	private String columnHeaders[];
	
	// the layout for the columns
	private ColumnLayoutData columnLayouts[];
	
	// the display
	private Display display;
	
	// action to enable/disable logging.
	private Action actionEnable;
	
	// allow to load the entries from the database.
	private Action actionLoadFromDatabase;
	
	// clear the viewer from the messages.
	private Action actionClearViewer;
	
	// clear the database from the messages.
	private Action actionClearDB;
	
	// export the content to a CSV file.
	private Action actionExportAsCSV;
	
	// whether to log or not.
	private boolean enabled = false;
	
	// the log4j logger.
	private static Logger log = Logger.getLogger(ROAccessReportsView.class);

	public ROAccessReportsView() {
		super();
		
		String[][] cnt = DerbyROAccessReportsRepository.COLUMN_NAMES_AND_TYPES;
		columnHeaders = new String[cnt.length];
		columnLayouts = new ColumnLayoutData[cnt.length];
		for (int i=0; i<cnt.length; i++) {
			columnHeaders[i] = cnt[i][0];
			columnLayouts[i] = new ColumnWeightData(20, true);
		}
		
		setColumnHeaders(columnHeaders);
		setColumnLayouts(columnLayouts);
	}

	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		
		display = parent.getDisplay();
		
		TableViewer viewer = getViewer();
		viewer.setLabelProvider(new ROAccessReportsLabelProvider());
		viewer.setComparator(new ROAccessItemComparator());

		final MessageHandler h = this;
		
		actionEnable = new Action() {
			@Override
			public void run() {
				if (enabled) {
					// turn off logging.
					AdaptorManagement.getInstance().deregisterPartialHandler(
							h, RO_ACCESS_REPORT.class);
					enabled = false;
				} else {
					AdaptorManagement.getInstance().registerPartialHandler(
							h, RO_ACCESS_REPORT.class);
					enabled = true;
				}
			}
		};
		actionEnable.setText("Enable/Disable logging.");
		actionEnable.setToolTipText("Enable/Disable logging.");
		actionEnable.setImageDescriptor(
				ResourceCenter.getInstance().getImageDescriptor("filter.gif"));
		actionEnable.setChecked(false);
		
		actionClearViewer = new Action() {
			@Override
			public void run() {
				getViewer().getTable().removeAll();
			}
		};
		actionClearViewer.setText("Clear the table");
		actionClearViewer.setToolTipText("Remove all the messages from the " +
				"Viewer");
		
		actionLoadFromDatabase = new Action() {
			@Override
			public void run() {
				Repository repo = ResourceCenter.getInstance().getRepository();
				if (null == repo) return;
				ROAccessReportsRepository rorepo = repo.getROAccessRepository();
				if (null == rorepo) return;
				
				try {
					List<ROAccessItem> items = rorepo.getAll();
					for (ROAccessItem item : items) {
						getViewer().add(item);
					}
				} catch (Exception e) {
					log.error(
							String.format("could not load messages from db: %s",
									e.getMessage()));
				}
				
			}
		};
		Repository repo = ResourceCenter.getInstance().getRepository();
		if ((null != repo) && (null != repo.getROAccessRepository())) {
			actionLoadFromDatabase.setEnabled(true);
		} else {
			actionLoadFromDatabase.setEnabled(false);
		}
		actionLoadFromDatabase.setText("Load from DB");
		actionLoadFromDatabase.setToolTipText(
				"Loads the messages from the RO_ACCESS_REPORTS database in " +
				"the backend. This action is only available, if you have " +
				"a running instance of the backend.");
		
		actionClearDB = new Action() {
			@Override
			public void run() {
				Repository repo = ResourceCenter.getInstance().getRepository();
				if (null == repo) return;
				ROAccessReportsRepository rorepo = repo.getROAccessRepository();
				if (null == rorepo) return;
				
				try {
					rorepo.clear();
					getViewer().getTable().removeAll();
				} catch (Exception e) {
					log.error(
							String.format("could not clear db: %s",
									e.getMessage()));
				}
				
			}
		};
		if ((null != repo) && (null != repo.getROAccessRepository())) {
			actionClearDB.setEnabled(true);
		} else {
			actionClearDB.setEnabled(false);
		}
		actionClearDB.setText("Clear Database");
		actionClearDB.setToolTipText(
				"Delete all the messages from the database.");
		
		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof ROAccessItem) {
					new DetailsDialog(
							getViewer().getControl().getShell(),
							(ROAccessItem) sel.getFirstElement()
							).open();
				}
			}
		});
		
		
		actionExportAsCSV = new Action() {
			@Override
			public void run() {
				try {
					String folder = "csv";
					String fileName = String.format(
							"%s.csv",	
							new Timestamp(System.currentTimeMillis()
									).toString());
					ResourceCenter.getInstance().writeMessageToFile(
									folder, 
									fileName, 
									asCSV());
					
					MessageDialog.openInformation(
							getViewer().getControl().getShell(), 
							"CSV Export", String.format("Exported to file %s/%s", 
									folder, fileName));
				} catch (Exception e) {
					MessageDialog.openWarning(
							getViewer().getControl().getShell(),
							"CSV Export", "Could not write csv");
					e.printStackTrace();
				}
			}
		};
		actionExportAsCSV.setText("Save as CSV");
		actionExportAsCSV.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(
						ISharedImages.IMG_ETOOL_SAVE_EDIT));
		
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager man = bars.getToolBarManager();
		man.add(actionExportAsCSV);
		
		
		viewer.refresh();
	}
	
	/**
	 * @return the content of the table as a comma-separated-values string.
	 */
	public String asCSV() {
		StringBuffer str = new StringBuffer();
		String [][] clm = DerbyROAccessReportsRepository.COLUMN_NAMES_AND_TYPES;
		for (int i=0; i<clm.length; i++) {
			str.append(String.format("%s,", clm[i][0]));
		}
		str.append("\n");
		
		Table table = getViewer().getTable();
		for (TableItem item : table.getItems()) {
			if (item.getData() instanceof ROAccessItem) {
				ROAccessItem ro = (ROAccessItem) item.getData();
				str.append(ro.getAsCSV()); str.append("\n");
			}
		}
		return str.toString();
	}

	public void handle(String adaptorName, String readerName,
			LLRPMessage message) {
		
		if (message instanceof RO_ACCESS_REPORT) {
			final List<ROAccessItem> items = ROAccessItem.parse(
					(RO_ACCESS_REPORT)message, adaptorName, 
					readerName, System.currentTimeMillis());
			
			// execute the update in the thread context of the 
			// window thread.
			synchronized (display) {
				display.syncExec(
						new Runnable() {
							public void run() {
								for (ROAccessItem item : items) {
									
									getViewer().add(item);
								}
							}
						}
					);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.fosstrak.llrp.commander.views.TableViewPart#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	@Override
	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(actionEnable);
		manager.add(actionClearViewer);
		manager.add(actionLoadFromDatabase);
		manager.add(actionClearDB);
	}
	
	
	/**
	 * comparator to compare two llrp message item elements. if 
	 * the timestamp of the second element is smaller than the timestamp 
	 * of the first, then a negative number is returned. otherwise 
	 * a positive value.
	 * @author sawielan
	 *
	 */
	private class ROAccessItemComparator extends ViewerComparator {
		
		/**
		 * compares two elements and returns a negative number if element2 is 
		 * more recent than element1.
		 * @param viewer the viewer.
		 * @param element1 the first element to compare.
		 * @param element2 the second element to compare.
		 */
		public int compare(Viewer viewer, Object element1, Object element2) {
			if ((element1 instanceof ROAccessItem) && 
					(element2 instanceof ROAccessItem)) {
				
				ROAccessItem msg1 = (ROAccessItem) element1;
				ROAccessItem msg2 = (ROAccessItem) element2;

				// compare the two timestamps...
				return (int)(msg2.getLogTime().getTime() - msg1.getLogTime().getTime());				
			}
			return 0;
		}
	}
}
