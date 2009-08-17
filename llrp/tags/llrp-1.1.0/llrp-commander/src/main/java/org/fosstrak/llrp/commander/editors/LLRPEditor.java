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

package org.fosstrak.llrp.commander.editors;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormEditor;
import org.fosstrak.llrp.commander.LLRPPlugin;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.dialogs.SendMessageDialog;
import org.fosstrak.llrp.commander.editors.graphical.GraphicalEditorPage;
import org.fosstrak.llrp.commander.preferences.PreferenceConstants;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.types.LLRPMessage;

/**
 * This editor extended Eclipse FormEditor, which contains multiple pages.
 * 
 * It provides one XML Editor page, one Graphics Editor page, and one Binary
 * format viewer.
 * 
 * In this multiple page editor, those three page works on the same message
 * file, but only XML editor touch the file system. Once the message changed,
 * XML Editor pass the LLRPBitList to Binary Viewer to update the binary format.
 * 
 * The target file will be stored under one Eclipse project.
 * 
 * @author Haoning Zhang
 * @version 1.0
 */
public class LLRPEditor extends FormEditor {

	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(LLRPEditor.class);

	/**
	 * Page Caption of XML Editor.
	 */
	private final static String SOURCE_PAGE_TITLE = "XML Editor";

	/**
	 * Page Caption of Binary Viewer.
	 */
	private final static String BINARY_PAGE_TITLE = "Binary Viewer";

	private TreeViewer treeViewer;
	private TreeColumn keyColumn, valueColumn;
	private XMLEditor textEditor;
	private LLRPBinaryContentProvider provider;
	
	private GraphicalEditorPage graphicsPage;
	
	private final static int PAGE_GRAPHICS 	= 0;
	private final static int PAGE_XML 		= 1;
	private final static int PAGE_BINARY 	= 2;
	
	private int oldPageIndex = PAGE_XML;
	
	private Action actionSend;

	/**
	 * Extends <code>FormEditor.addPages()</code> Initial three pages involved
	 * in this FormEditor
	 */
	protected void addPages() {

		createActions();
		
		// Page No.0
		try {
			log.debug("Creating Graphics Editor Page...");
			graphicsPage = new GraphicalEditorPage(this);
			addPage(graphicsPage);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		// Page No.1
		log.debug("Creating XML Editor Page...");
		createSourcePage();
		
		// Page No.2
		log.debug("Creating Binary Viewer Page...");
		createBinaryPage();
		
		// initialize graphical editor and binary viewer with content from xml editor
		boolean graphicalEditorSuccessfullyInitialized = updateGraphicalEditor();
		boolean binaryViewerSuccessfullyInitialized = updateBinaryViewer();
		
		IPreferenceStore store = LLRPPlugin.getDefault().getPreferenceStore();
		String defaultEditor = store.getString(PreferenceConstants.P_DEFAULT_EDITOR);
		
		if (defaultEditor.equals(PreferenceConstants.P_DEFAULT_EDITOR_GRAPHICAL)
				&& graphicalEditorSuccessfullyInitialized){
			setActivePage(PAGE_GRAPHICS);
		}
		else if (defaultEditor.equals(PreferenceConstants.P_DEFAULT_EDITOR_BINARY)
				&& binaryViewerSuccessfullyInitialized){
			setActivePage(PAGE_BINARY);
		}
		else{
			setActivePage(PAGE_XML);
		}
		
		updateTitle();
	}

	/**
	 * Create related Eclipse action classes.
	 */
	private void createActions() {
		actionSend = new Action() {
			public void run() {
				SendMessageDialog.getInstance(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).open();
			}
		};
		actionSend.setText("Send LLRP Message");
		actionSend.setToolTipText("Send LLRP Message");
		actionSend.setImageDescriptor(ResourceCenter.getInstance().getImageDescriptor("Send_LLRP.gif"));
	}

	/**
	 * Create the Binary Message Viewer
	 */
	private void createBinaryPage() {
		treeViewer = new TreeViewer(getContainer(), SWT.MULTI
				| SWT.FULL_SELECTION);
		Tree tree = treeViewer.getTree();
		tree.setHeaderVisible(true);

		keyColumn = new TreeColumn(tree, SWT.LEFT);
		keyColumn.setText("Key");
		keyColumn.setWidth(150);
		valueColumn = new TreeColumn(tree, SWT.LEFT);
		valueColumn.setText("Value");
		valueColumn.setWidth(420);
		valueColumn.setResizable(false);

		int index = addPage(treeViewer.getControl());
		setPageText(index, BINARY_PAGE_TITLE);
		
		
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				manager.add(actionSend);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeViewer);
		

		initTreeContent();
	}

	/**
	 * Initialize the tree structure in Binary Viewer.
	 */
	private void initTreeContent() {
		provider = new LLRPBinaryContentProvider();
		treeViewer.setContentProvider(provider);
		treeViewer.setLabelProvider(new LLRPBinaryLabelProvider());

		treeViewer.setInput(new BinaryMessage());
		treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
	}

	/**
	 * Create XML Message Editor
	 */
	private void createSourcePage() {
		try {

			textEditor = new XMLEditor();

			int index = addPage(textEditor, getEditorInput());
			setPageText(index, SOURCE_PAGE_TITLE);

		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	/**
	 * When user change the page, the content should be synchronized here.
	 */
	protected void pageChange(int aNewPageIndex) {
		
		if (oldPageIndex == aNewPageIndex){
			return;
		}
		
		// commit any changes of the old page to the xml editor
		if (oldPageIndex == PAGE_GRAPHICS){
			if (graphicsPage.isGraphicalEditorDirty()){
				boolean success = commitGraphicalEditor();
				if (!success){
					setActivePage(PAGE_GRAPHICS);
					showSwitchErrorMessage();
					return;
				}
			}
		}
		
		// update the new page with the content from the xml editor
		if (aNewPageIndex == PAGE_GRAPHICS){
			boolean success = updateGraphicalEditor();
			if (!success){
				setActivePage(oldPageIndex);
				showSwitchErrorMessage();
				return;
			}
		}
		else if (aNewPageIndex == PAGE_BINARY){
			boolean success = updateBinaryViewer();
			if (!success){
				setActivePage(oldPageIndex);
				showSwitchErrorMessage();
				return;
			}
		}
		oldPageIndex = aNewPageIndex;
		
		super.pageChange(aNewPageIndex);
	}

	private void updateTitle() {
		IEditorInput input = getEditorInput();
		// use setPartName instead of old setTitle
		setPartName(input.getName());
		
		// we could use setContentDescription(String) to set a nice 
		// description on top of the pane...
		
		setTitleToolTip(input.getToolTipText());
	}

	public void setFocus() {
		switch (getActivePage()) {
		case 1:
			treeViewer.getTree().setFocus();
			break;
		case 0:
			textEditor.setFocus();
			break;
		}
	}

	/**
	 * Here the XML Editor Page save the content.
	 */
	public void doSave(IProgressMonitor aMonitor) {
		if ((getActivePage() == PAGE_GRAPHICS) && (!commitGraphicalEditor())){
			showSaveErrorMessage();
			return;
		}
		textEditor.doSave(aMonitor);
	}

	/**
	 * Always true.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}

	/**
	 * Here the XML Editor Page save as the content.
	 */
	public void doSaveAs() {
		if ((getActivePage() == PAGE_GRAPHICS) && (!commitGraphicalEditor())){
			showSaveErrorMessage();
			return;
		}
		textEditor.doSaveAs();
		setInput(textEditor.getEditorInput());
		updateTitle();
	}
	
	/**
	 * Marks the XML Editor as dirty.
	 */
	public void markXMLEditorAsDirty(){
		String xmlContent = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).get();
		textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).set(xmlContent);
	}
	
	/**
	 * Commits any changes made in the graphical editor to the xml editor.
	 * 
	 * @return <code>true</code> if the commit operation was successful, and <code>false</code> otherwise
	 * (e.g. if the message object could not be serialized to xml)
	 */
	private boolean commitGraphicalEditor() {
		LLRPMessage message = graphicsPage.getLLRPMessage();
		if (message == null) {
			return false;
		}
		
		String xmlContent = "";
		boolean success;
		try {
			xmlContent = message.toXMLString();
			textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).set(xmlContent);
			graphicsPage.setDirty(false);
			success = true;
		} catch (InvalidLLRPMessageException e) {
			success = false;
		}
		return success;
	}
	
	/**
	 * Updates the graphical editor with the content of the xml editor.
	 * 
	 * @return  <code>true</code> if the update operation was successful, and <code>false</code> otherwise
	 * (e.g. if the xml string could not be deserialized to a message object).
	 */
	private boolean updateGraphicalEditor(){
		String xmlContent = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).get();
		LLRPMessage message = ResourceCenter.getInstance().generateLLRPMessage(xmlContent);
		
		boolean success;
		if (message == null){
			success = false;
		}
		else{
			graphicsPage.setLLRPMessage(message);
			success = true;
		}
		return success;
	}
	
	/**
	 * Updates the binary viewer with the content of the xml editor.
	 * 
	 * @return  <code>true</code> if the update operation was successful, and <code>false</code> otherwise
	 * (e.g. if the xml string could not be deserialized to a message object).
	 */
	private boolean updateBinaryViewer(){
		String xmlContent = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).get();
		LLRPMessage message = ResourceCenter.getInstance().generateLLRPMessage(xmlContent);
		
		boolean success;
		if (message == null){
			success = false;
		}
		else{
			String content = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput()).get();
			BinaryMessage msg;
			try {
				msg = new BinaryMessage(content);
			} catch (Exception e) {
				return false;
			}
			treeViewer.setInput(msg);
			success = true;
		}
		return success;
	}
	
	/**
	 * Shows an error message saying that the message cannot be saved.
	 */
	private void showSaveErrorMessage(){
		String title = "Cannot save the message";
		String message = "The message cannot be saved because it is not valid.";
		MessageDialog.openError(this.getContainer().getShell(), title, message);
	}
	
	/**
	 * Shows an error message saying that the page cannot be switched.
	 */
	private void showSwitchErrorMessage(){
		String title = "Cannot switch the page";
		String message = "The page cannot be switched because the message is not valid.";
		MessageDialog.openError(this.getContainer().getShell(), title, message);
	}

}
