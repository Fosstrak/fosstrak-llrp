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

package org.fosstrak.llrp.commander.views;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;


/**
* Prepares the ViewPart for the table viewer.
* @author zhanghao
*
*/
public class TableViewPart extends ViewPart {

	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(TableViewPart.class);
	
	protected static final String TAG_COLUMN = "column";
	protected static final String TAG_NUMBER = "number";
	protected static final String TAG_WIDTH = "width";

	private String columnHeaders[];
	private ColumnLayoutData columnLayouts[];
	private IAction doubleClickAction;
	private IMemento memento;

	private Table table;
	private TableViewer viewer;

	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.FULL_SELECTION
				| SWT.H_SCROLL | SWT.V_SCROLL);

		table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumns();
		createActions();
		hookMenus();
		hookEvents();
		contributeToActionBars();
	}

	protected void createColumns() {
		if (memento != null) {
			restoreColumnWidths(memento);
		}

		TableLayout layout = new TableLayout();
		table.setLayout(layout);

		for (int i = 0; i < columnHeaders.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.NONE, i);
			
			log.debug("Creating Column " + columnHeaders[i]);
			
			tc.setText(columnHeaders[i]);
			tc.setResizable(columnLayouts[i].resizable);
			layout.addColumnData(columnLayouts[i]);
		}
		
		//table.pack();
	}

	protected void restoreColumnWidths(IMemento memento) {
		IMemento children[] = memento.getChildren(TAG_COLUMN);
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				Integer val = children[i].getInteger(TAG_NUMBER);
				if (val != null) {
					int index = val.intValue();
					val = children[i].getInteger(TAG_WIDTH);
					if (val != null) {
						columnLayouts[index] = new ColumnPixelData(val
								.intValue(), true);
					}
				}
			}
		}
	}

	protected void saveColumnWidths(IMemento memento) {
		Table table = viewer.getTable();
		TableColumn columns[] = table.getColumns();

		for (int i = 0; i < columns.length; i++) {
			if (columnLayouts[i].resizable) {
				IMemento child = memento.createChild(TAG_COLUMN);
				child.putInteger(TAG_NUMBER, i);
				child.putInteger(TAG_WIDTH, columns[i].getWidth());
			}
		}
	}

	protected void hookMenus() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TableViewPart.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	protected void hookEvents() {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() != null)
					TableViewPart.this.selectionChanged(event);
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				try {
					doubleClickAction.run();
				} catch (Exception e) {
					
				}
			}
		});
	}

	protected void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	public void saveState(IMemento memento) {
		if (viewer == null) {
			if (this.memento != null) // Keep the old state;
				memento.putMemento(this.memento);
			return;
		}

		saveColumnWidths(memento);
	}

	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		this.memento = memento;
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public Table getTable() {
		return table;
	}

	public TableViewer getViewer() {
		return viewer;
	}

	public void setColumnHeaders(String[] strings) {
		columnHeaders = strings;
	}

	public void setColumnLayouts(ColumnLayoutData[] data) {
		columnLayouts = data;
	}

	public void setDoubleClickAction(IAction action) {
		doubleClickAction = action;
	}

	protected void fillContextMenu(IMenuManager manager) {
	}

	protected void fillLocalPullDown(IMenuManager manager) {
	}

	protected void fillLocalToolBar(IToolBarManager manager) {
	}

	protected void selectionChanged(SelectionChangedEvent event) {
	}

	protected void createActions() {
	}
}
