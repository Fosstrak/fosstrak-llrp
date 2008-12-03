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
import org.fosstrak.llrp.commander.*;
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
	
	class MessageFilter extends ViewerFilter {
		
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

				if (item.getAdapter().equals(adapterName)
						&& (readerName == null || item.getReader().equals(readerName))) {
					return true;
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
	class ROAccessReportFilter extends ViewerFilter {
		
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
					updateViewer();
				}
				else{
					getViewer().removeFilter(rOAccessReportFilter);
					updateViewer();
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
					
					updateViewer();
				} else {
					selectedAdapter = readerNode.getName();
					selectedReader = null;
					
					updateViewer();
				}
			}
		}
	}
	
	/**
	 * Refresh the message list in the viewer from selected adapter or reader.
	 */
	public void updateViewer() {
		
		getViewer().getTable().removeAll();
		
		// Set the filtering condition
		filter.setCondition(selectedAdapter, selectedReader);
		
		ArrayList<LLRPMessageItem> list = ResourceCenter.getInstance().getMessageMetadataList();
		getViewer().add(list.toArray());
	}

}