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

import java.util.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.ui.*;
import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.commander.*;
import org.fosstrak.llrp.commander.repository.JavaDBRepository;
import org.apache.log4j.Logger;

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
	
	/**
	 * Column ID for Message Type.
	 */
	public static final int COL_MSG_READER = 2;
	
	/**
	 * Column ID for Message Type.
	 */
	public static final int COL_MSG_TYPE = 3;
	
	/**
	 * Column ID for Status Code.
	 */
	public static final int COL_STATUS_CODE = 4;

	/**
	 * Column ID for Message Issue Time.
	 */
	public static final int COL_MSG_TIME = 5;

	/**
	 * Column ID for Message Comments.
	 */
	public static final int COL_MSG_COMMENT = 6;

	private String columnHeaders[] = { "", "ID", "Reader", "Message Type", "Status Code",
			"Time", "Comment" };
	private ColumnLayoutData columnLayouts[] = { new ColumnWeightData(12),
			new ColumnWeightData(80), new ColumnWeightData(60),
			new ColumnWeightData(100), new ColumnWeightData(80), 
			new ColumnWeightData(80), new ColumnWeightData(100) };

	private Action deleteAction;
	
	private Action rOAccessReportFilterAction;

	private MessageFilter filter;
	
	private ROAccessReportFilter rOAccessReportFilter;
	
	private String selectedAdapter, selectedReader;
	
	/** 
	 * access to the display is needed by the asyncExec/syncExec API 
	 * to allow multi-threaded access to the SWT widget
	 */
	protected Display display = null;
	
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
				if (item.getMessageType().equals("RO_ACCESS_REPORT")) {
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
		manager.add(new Separator("Additions"));
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
		super.fillLocalToolBar(manager);
		manager.add(rOAccessReportFilterAction);
		manager.add(deleteAction);
	}

	protected void createActions() {
		super.createActions();
		deleteAction = new Action() {
			public void run() {
				ResourceCenter.getInstance().getRepository().clearAll();
				ResourceCenter.getInstance().clearMessageMetadataList();
				
				getTable().setRedraw(false);
				// model.clear();
				getViewer().refresh(true);
				getTable().setRedraw(true);
			}
		};
		String deleteText = "Delete all messages";
		deleteAction.setText(deleteText);
		deleteAction.setToolTipText(deleteText);
		deleteAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_TOOL_DELETE));
		
		rOAccessReportFilterAction = new Action() {
			public void run() {
				if (rOAccessReportFilterAction.isChecked()){
					getViewer().addFilter(rOAccessReportFilter);
					updateViewer(false);
				}
				else{
					getViewer().removeFilter(rOAccessReportFilter);
					updateViewer(false);
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
		synchronized (this) {
			filter.setCondition(selectedAdapter, selectedReader);
			if (reload) {
	
				// first load all the messages from the database backend.
				Repository repo = ResourceCenter.getInstance().getRepository();
				ArrayList<LLRPMessageItem> msgs = repo.get(
						selectedAdapter, 
						selectedReader, 
						ResourceCenter.GET_MAX_MESSAGES,
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
				}
			}
			
			// inform all the other threads about the free lock.
			notifyAll();
		}
	}

}