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

package org.fosstrak.llrp.commander.views;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.dialogs.MessageboxViewOptionsDialog;
import org.fosstrak.llrp.commander.util.MessageBoxRefresh;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 * 
 * 
 * @author zhanghao
 * @author sawielan
 */

public class MessageboxView extends TableViewPart implements ISelectionListener {
	
	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(MessageboxView.class);
	
	/**
	 * Column ID for User Marks.
	 */
	public static final int COL_MSG_MARK = 0;
	
	/**
	 * Column ID for Message ID.
	 */
	public static final int COL_MSG_ID = 1;
	
	/** column for the adapter. */
	public static final int COL_MSG_ADAPTER = 2;
	
	/**
	 * Column ID for Message Type.
	 */
	public static final int COL_MSG_READER = 3;
	
	/**
	 * Column ID for Message Type.
	 */
	public static final int COL_MSG_TYPE = 4;
	
	/**
	 * Column ID for Status Code.
	 */
	public static final int COL_STATUS_CODE = 5;

	/**
	 * Column ID for Message Issue Time.
	 */
	public static final int COL_MSG_TIME = 6;

	/**
	 * Column ID for Message Comments.
	 */
	public static final int COL_MSG_COMMENT = 7;
	
	/** the number of messages to display in the message box. */
	private int displayNumMessages = ResourceCenter.GET_MAX_MESSAGES;

	private String columnHeaders[] = { "", "ID", "Adapter", "Reader", "Message Type", "Status Code",
			"Time", "Comment" };
	private ColumnLayoutData columnLayouts[] = { new ColumnWeightData(12),
			new ColumnWeightData(80), new ColumnWeightData(60), 
			new ColumnWeightData(60), new ColumnWeightData(100), 
			new ColumnWeightData(80), new ColumnWeightData(80), 
			new ColumnWeightData(100) };

	private Action deleteAction;
	
	private Action rOAccessReportFilterAction;

	/** action to enable/disable auto refresh */
	private Action autoRefreshAction;
	
	/** action to set the refresh options. */
	private Action optionsAction;
	
	private MessageFilter filter;
	
	private ROAccessReportFilter rOAccessReportFilter;
	
	private String selectedAdapter, selectedReader;

	/** the name of the auto refresh icon. */
	public static final String ICON_AUTO_REFRESH = "autorefresh.gif";
	
	/** 
	 * access to the display is needed by the asyncExec/syncExec API 
	 * to allow multi-threaded access to the SWT widget
	 */
	protected Display display = null;
	
	/** access to this pointer for inner classes. */
	private MessageboxView mbv = this;
	
	/**
	 * filter according the selected adapter. 
	 * @author sawielan
	 *
	 */
	private class MessageFilter extends ViewerFilter {
		
		private String adapterName, readerName;
		
		public void setCondition(String aAdapterName, String aReaderName) {
			adapterName = aAdapterName;
			readerName = aReaderName;
		}
		
		public boolean select(Viewer aViewer, Object aParentElement, Object aElement) {
			// special filter for the root adapter...
			if ((null == adapterName) || (
					ReaderExplorerViewContentProvider.ROOT_NAME.
						equals(adapterName))) {
				
				return true;
			}
			
			if ((null == adapterName) && (null == readerName)) {
				return true;
			}
			
			if (aElement instanceof LLRPMessageItem) {
				LLRPMessageItem item = (LLRPMessageItem) aElement;
				
				if ((adapterName == null) || (item.getAdapter() == null)) {
					return false;
				} else {
					if (adapterName.trim().equals(item.getAdapter().trim())) {
						
						// compare the reader. if the readerName is null then 
						// the user selected a whole adapter and this means 
						// that we dont care about the readerName.
						if (readerName == null) {
							return true;
						}
						
						if ((item.getReader() != null) && 
								(item.getReader().trim().equals(
										readerName.trim()))) {
							return true;
						}
					}
				}
			}
			return false;
		}
		
	}
	
	/**
	 * This filter can be used to hide RO_ACCESS_REPORT messages in the message box view.
	 * This might be useful for the user, because RO_ACCESS_REPORT messages usually
	 * occur in high numbers and render the message box view overcrowded.
	 *
	 * @author Ulrich Etter, ETHZ
	 *
	 */
	private class ROAccessReportFilter extends ViewerFilter {
		
		public boolean select(Viewer aViewer, Object aParentElement, Object aElement) {
			if (aElement instanceof LLRPMessageItem) {
				LLRPMessageItem item = (LLRPMessageItem) aElement;
				String msg = item.getMessageType();
				if (null == msg) {
					return true;
				}
				if ("RO_ACCESS_REPORT".equals(msg.trim())) {
					return false;
				}
			}
			return true;
		}
		
	}
	
	/**
	 * comparator to compare two llrp message item elements. if 
	 * the timestamp of the second element is smaller than the timestamp 
	 * of the first, then a negative number is returned. otherwise 
	 * a positive value.
	 * @author sawielan
	 *
	 */
	private class NewMessageComparator extends ViewerComparator {
		
		/**
		 * compares two elements and returns a negative number if element2 is 
		 * more recent than element1.
		 * @param viewer the viewer.
		 * @param element1 the first element to compare.
		 * @param element2 the second element to compare.
		 */
		public int compare(Viewer viewer, Object element1, Object element2) {
			if ((element1 instanceof LLRPMessageItem) && 
					(element2 instanceof LLRPMessageItem)) {
				
				LLRPMessageItem msg1 = (LLRPMessageItem) element1;
				LLRPMessageItem msg2 = (LLRPMessageItem) element2;

				// compare the two timestamps...
				return (int)(msg2.getTime().getTime() - msg1.getTime().getTime());				
			}
			return 0;
		}
	}
	
	/**
	 * The constructor.
	 */
	public MessageboxView() {
		super();
		setColumnHeaders(columnHeaders);
		setColumnLayouts(columnLayouts);
	}
	
	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
	
		super.createPartControl(parent);
		
		display = parent.getDisplay();
		
		filter = new MessageFilter();
		rOAccessReportFilter = new ROAccessReportFilter();
		
		TableViewer viewer = getViewer();
		viewer.setLabelProvider(new MessageboxViewLabelProvider());
		viewer.addFilter(filter);
		viewer.setSorter(null);
		viewer.setComparator(new NewMessageComparator());

		viewer.refresh();
		
		getViewSite().getPage().addSelectionListener(this);
		
		ResourceCenter.getInstance().setMessageboxView(this);
	}
	
	/**
	 * return a handle to the display of this widget.
	 * @return the display of this widget.
	 */
	public Display getDisplay() {
		return display;
	}

	protected void fillContextMenu(IMenuManager manager) {
		super.fillContextMenu(manager);
		manager.add(rOAccessReportFilterAction);
		manager.add(deleteAction);
		manager.add(autoRefreshAction);
		manager.add(optionsAction);
		manager.add(new Separator("Additions"));
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(rOAccessReportFilterAction);
		manager.add(autoRefreshAction);
		manager.add(deleteAction);
	}

	protected void createActions() {
		super.createActions();
		deleteAction = new Action() {
			public void run() {
				String adapter = ResourceCenter.getInstance().
					getReaderExplorerView().getSelectedAdapter();
				String reader = ResourceCenter.getInstance().
					getReaderExplorerView().getSelectedReader();
				
				if (ReaderExplorerViewContentProvider.ROOT_NAME.
						equals(adapter) || (null == adapter)) {
					
					ResourceCenter.getInstance().getRepository().clearAll();
				} else if (null == reader) {
					ResourceCenter.getInstance().getRepository().clearAdapter(adapter);
				} else {
					ResourceCenter.getInstance().getRepository().clearReader(
							adapter, reader);
				}
				ResourceCenter.getInstance().clearMessageMetadataList();
				
				getTable().setRedraw(false);
				// model.clear();
				getViewer().refresh(true);
				getTable().setRedraw(true);
			}
		};
		String deleteText = String.format(
				"Remove LLRP messages from the repository:\n" +
				" - Select reader in reader explorer to delete messages of the reader.\n" + 
				" - Select adapter in reader explorer to delete messages of the adapter.\n" + 
				" - Select root in reader explorer to delete all messages."
				);
		deleteAction.setText("Remove Messages");
		deleteAction.setToolTipText(deleteText);
		deleteAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));
		
		rOAccessReportFilterAction = new Action() {
			public void run() {
				if (rOAccessReportFilterAction.isChecked()){
					getViewer().addFilter(rOAccessReportFilter);
					updateViewer(true);
				}
				else{
					getViewer().removeFilter(rOAccessReportFilter);
					updateViewer(true);
				}
			}
		};
		String filterText = "Hide RO_ACCESS_REPORT messages";
		rOAccessReportFilterAction.setText(filterText);
		rOAccessReportFilterAction.setToolTipText(filterText);
		ImageDescriptor imageDescriptor = ResourceCenter.getInstance().getImageDescriptor("filter.gif");
		rOAccessReportFilterAction.setImageDescriptor(imageDescriptor);
		rOAccessReportFilterAction.setChecked(false);

		getViewer().addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				
				// Select the Message Item
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				LLRPMessageItem msg = (LLRPMessageItem) sel.getFirstElement();
				
				// Write to file system within Eclipse Workspace
				if (msg != null) {
					ResourceCenter.getInstance().writeMessageToFile(msg.getId());
				}
			}
		});
		
		autoRefreshAction = new Action() {
			public void run() {
				boolean refresh = false;
				if (autoRefreshAction.isChecked()) {
					refresh = true;
				}
				ResourceCenter.getInstance().getMessageBoxRefresh().setRefresh(refresh);
			}
		};
		final String autoRefreshText = String.format("Auto-Refresh (%d ms)", 
				MessageBoxRefresh.DEFAULT_REFRESH_INTERVAL_MS);
		autoRefreshAction.setText(autoRefreshText);
		autoRefreshAction.setToolTipText(autoRefreshText);
		autoRefreshAction.setChecked(MessageBoxRefresh.DEFAULT_REFRESH_BEHAVIOR);
		autoRefreshAction.setImageDescriptor(
				ResourceCenter.getInstance().getImageDescriptor(ICON_AUTO_REFRESH));
		
		optionsAction = new Action() {
			public void run() {
				MessageboxViewOptionsDialog dlg = new MessageboxViewOptionsDialog(display.getActiveShell(), mbv);
				if (Window.OK == dlg.open()) {
					try {
						final long rt = dlg.getRefreshTime();
						
						ResourceCenter.getInstance().getMessageBoxRefresh().setRefreshTime(rt);
						final String autoRefreshText = String.format("Auto-Refresh (%d ms)", 
								rt);
						autoRefreshAction.setText(autoRefreshText);
						autoRefreshAction.setToolTipText(autoRefreshText);
						
						final int nMsg = dlg.getNumberOfMessages();
						displayNumMessages = nMsg;
						if (nMsg <= 0) {
							displayNumMessages = Repository.RETRIEVE_ALL;
						}
					} catch (Exception e) {
						log.error("could not change the refresh time.");
					}
				}
			}
		};
		final String optionsText = "Options";
		optionsAction.setText(optionsText);
		optionsAction.setToolTipText(optionsText);
	}
	
	/**
	 * Dispose the view.
	 */
	public void dispose() {
		// model.removeListener(modelListener);
		super.dispose();
	}

	/**
	 * Triggered when user change the reader in Reader Management View.
	 * The corresponding messages need to be updated.
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object selectedObj = ((IStructuredSelection) selection).getFirstElement();
			if (selectedObj instanceof ReaderTreeObject) {
				
				ReaderTreeObject readerNode = (ReaderTreeObject) selectedObj;
	
				if (readerNode.isReader()) {
					String aAdapterName = readerNode.getParent().getName();
					
					selectedAdapter = aAdapterName;
					selectedReader = readerNode.getName();
					
					updateViewer(true);
				} else {
					selectedAdapter = readerNode.getName();
					selectedReader = null;
					
					updateViewer(true);
				}
			}
		}
	}
	
	/**
	 * Refresh the message list in the viewer from selected adapter or reader.
	 * @param reload flag whether to reload the whole list of messages. if set 
	 * to true all the messages get loaded from the backend, otherwise just the 
	 * new messages get added to the view.
	 */
	public synchronized void updateViewer(boolean reload) {

		// we want to know how long this takes...
		long st = System.currentTimeMillis();
		
		synchronized (this) {
			filter.setCondition(selectedAdapter, selectedReader);
			if (reload) {
	
				// first load all the messages from the database backend.
				Repository repo = ResourceCenter.getInstance().getRepository();
				ArrayList<LLRPMessageItem> msgs = repo.get(
						selectedAdapter, 
						selectedReader, 
						displayNumMessages,
						false);
				
				getViewer().getTable().removeAll();
				
				getViewer().add(msgs.toArray());
				ResourceCenter.getInstance().clearMessageMetadataList();
				
			} else {
				
				ArrayList<LLRPMessageItem> list = 
					ResourceCenter.getInstance().getMessageMetadataList();
				
				if (list.size() > 0) {
					getViewer().add(list.toArray());
					ResourceCenter.getInstance().clearMessageMetadataList();
					
					//bound the number of displayed messages.
					if (0 < displayNumMessages) {
						getViewer().setItemCount(Math.min(
							getViewer().getTable().getItemCount(), 
							getDisplayNumMessages()));
					} 
				}
			}
			
			// inform all the other threads about the free lock.
			notifyAll();
		}
		
		long et = System.currentTimeMillis();
		log.debug(String.format("Messagebox redraw time: %d", 
				et - st));
	}
	
	/**
	 * @param displayNumMessages the displayNumMessages to set
	 */
	public void setDisplayNumMessages(int displayNumMessages) {
		this.displayNumMessages = displayNumMessages;
	}

	/**
	 * @return the displayNumMessages
	 */
	public int getDisplayNumMessages() {
		return displayNumMessages;
	}
	
	/**
	 * @return the selectedAdapter
	 */
	public final String getSelectedAdapter() {
		return selectedAdapter;
	}

	/**
	 * @return the selectedReader
	 */
	public final String getSelectedReader() {
		return selectedReader;
	}
}