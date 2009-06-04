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

package org.fosstrak.llrp.commander.editors.graphical;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.*;
import org.fosstrak.llrp.commander.LLRPPlugin;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.editors.graphical.actions.AddParameterAction;
import org.fosstrak.llrp.commander.editors.graphical.actions.DeleteParameterAction;
import org.fosstrak.llrp.commander.util.LLRP;
import org.fosstrak.llrp.commander.util.LLRPTreeMaintainer;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.LLRPParameter;
import org.llrp.ltkGenerator.generated.*;


/**
 * The LLRPMasterDetailsBlock has two parts: a master part that shows the message tree
 * and a details part that shows details about the currently selected tree element.
 *
 * @author Ulrich Etter, ETHZ
 *
 */
public class LLRPMasterDetailsBlock extends MasterDetailsBlock {
	
	private static String DUMMY_ACTION_TEXT = "<No Applicable Parameter>";
	
	private TreeViewer treeViewer;
	private GraphicalEditorPage page;
	private LLRPTreeMaintainer treeMaintainer;

	private List<Object> invalidParameters;
	
	public LLRPMasterDetailsBlock(GraphicalEditorPage page, LLRPTreeMaintainer treeMaintainer) {
		this.page = page;
		this.treeMaintainer = treeMaintainer;
	}
	
	/**
	 * Updates the error message field, refreshes the TreeViewer and selects the given element in the TreeViewer.
	 * 
	 * @param elementToSelect the element to select in the TreeViewer
	 */
	public void refresh(Object elementToSelect){
		updateErrorMessage();
		treeViewer.refresh();
		if(elementToSelect != null){
			treeViewer.setSelection(new StructuredSelection(elementToSelect), true);
		}
		treeViewer.expandAll();
	}

	protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
		final FormToolkit toolkit = managedForm.getToolkit();
		Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.DESCRIPTION);
		section.setText("Message Tree");
		section.setDescription("Right-click on a tree element to add or delete a parameter.");
		section.marginWidth = 10;
		section.marginHeight = 5;
		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 2;
		layout.marginTop = 5;
		client.setLayout(layout);
		
		Tree t = toolkit.createTree(client, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 20;
		gd.widthHint = 100;
		t.setLayoutData(gd);
		toolkit.paintBordersFor(client);
		section.setClient(client);
		final SectionPart spart = new SectionPart(section);
		managedForm.addPart(spart);
		treeViewer = new TreeViewer(t);
		page.getSite().setSelectionProvider(treeViewer);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				managedForm.getMessageManager().removeAllMessages();
				managedForm.fireSelectionChanged(spart, event.getSelection());
			}
		});

		treeViewer.setContentProvider(new LLRPTreeContentProvider(treeMaintainer));
		treeViewer.setLabelProvider(new LLRPTreeLabelProvider(treeMaintainer));
		treeViewer.setInput(treeMaintainer);
		treeViewer.expandAll();
		
		createSectionToolbar(section, toolkit);
		createContextMenu();		
		
		final ScrolledForm form = managedForm.getForm();	
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				Point hl = ((Control) e.widget).toDisplay(0, 0);
				hl.x += 10;
				hl.y += 10;
				Shell shell = new Shell(form.getShell(), SWT.ON_TOP | SWT.TOOL);
				shell.setLayout(new FillLayout());
				FormText text = toolkit.createFormText(shell, true);
				text.addFocusListener(new FocusListener(){

					public void focusGained(FocusEvent e) {
						// do nothing
					}

					public void focusLost(FocusEvent e) {
						((FormText) e.widget).getShell().dispose();
					}
					
				});
				configureFormText(text);
				text.setText(createFormTextContent(), true, false);
				shell.setLocation(hl);
				shell.pack();
				shell.open();
			}
		});

	}
	
	private void createContextMenu() {
		DeleteParameterAction deleteParameterAction = new DeleteParameterAction(page.getSite().getWorkbenchWindow(), treeViewer, treeMaintainer, this);
		ImageDescriptor deleteImageDescriptor = ResourceCenter.getInstance().getImageDescriptor("delete.gif");
		deleteParameterAction.setImageDescriptor(deleteImageDescriptor);
		
		MenuManager contextMenuManager = new MenuManager(null);
		final MenuManager addMenuManager = new MenuManager("Add", "add");

		final Action a = new Action(DUMMY_ACTION_TEXT) {};
		a.setEnabled(false);
		addMenuManager.add(a); // add dummy action so that eclipse shows the menu
		addMenuManager.setRemoveAllWhenShown(true);
		addMenuManager.addMenuListener(new IMenuListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.jface.action.IMenuListener#menuAboutToShow(org.eclipse.jface.action.IMenuManager)
			 */
			public void menuAboutToShow(IMenuManager manager) {
				createAddParameterActions(addMenuManager);
			}

		});
		
		contextMenuManager.add(addMenuManager);
		contextMenuManager.add(deleteParameterAction);
		Menu menu = contextMenuManager.createContextMenu(treeViewer.getControl());
		treeViewer.getControl().setMenu(menu);
	}
	
	private void createAddParameterActions(MenuManager addMenuManager) {
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		if (!selection.isEmpty()) {
			Object treeElement = selection.getFirstElement();
			if (treeElement instanceof LLRPMessage || treeElement instanceof LLRPParameter){
				Object messageOrParameterDefinition = treeMaintainer.getDefinition(treeElement);
				java.util.List<Object> parameterOrChoiceList = new LinkedList<Object>();
				if (messageOrParameterDefinition instanceof MessageDefinition){
					parameterOrChoiceList = ((MessageDefinition) messageOrParameterDefinition).getParameterOrChoice();
				}
				else if (messageOrParameterDefinition instanceof ParameterDefinition){
					parameterOrChoiceList = ((ParameterDefinition) messageOrParameterDefinition).getParameterOrChoice();
				}
				for (Object o : parameterOrChoiceList){
					String childName = "";
					if (o instanceof ParameterReference){
						childName = ((ParameterReference) o).getType(); 
					}
					else if (o instanceof ChoiceReference){
						childName = ((ChoiceReference) o).getType(); 
					}
					if (LLRP.canOccurMultipleTimes(messageOrParameterDefinition, childName)){
						// lists can't be added or removed, therefore don't create actions for lists
					}
					else {
						if (LLRP.isChoice(messageOrParameterDefinition, childName)){
							MenuManager choiceMenuManager = new MenuManager(childName);
							addMenuManager.add(choiceMenuManager);
							ChoiceDefinition choiceDefinition = LLRP.getChoiceDefinition(childName);
							List<ChoiceParameterReference> choiceParameterReferences = choiceDefinition.getParameter();
							for (ChoiceParameterReference cpr : choiceParameterReferences){
								String parameterName = cpr.getType();
								AddParameterAction addParameterAction = createAddParameterAction(treeElement, childName, parameterName);
								choiceMenuManager.add(addParameterAction);
								if (treeMaintainer.getChild(treeElement, childName) != null){
									// parameter does already exist and therefore can't be added anymore
									addParameterAction.setEnabled(false);
								}
							}
						}
						else{
							AddParameterAction addParameterAction = createAddParameterAction(treeElement, childName, childName);
							addMenuManager.add(addParameterAction);
							if (treeMaintainer.getChild(treeElement, childName) != null){
								// parameter does already exist and therefore can't be added anymore
								addParameterAction.setEnabled(false);
							}
						}
					}
				}
			}
			else if (treeElement instanceof java.util.List){
				
				String name = treeMaintainer.getName(treeElement);
				LlrpDefinition llrpDefinition = LLRP.getLlrpDefintion();
				List<Object> list = llrpDefinition.getMessageDefinitionOrParameterDefinitionOrChoiceDefinition();
				for (Object o : list){
					if (o instanceof ParameterDefinition){
						if (((ParameterDefinition) o).getName().equals(name)){
							AddParameterAction addParameterAction = createAddParameterAction(treeElement, null, name);
							addMenuManager.add(addParameterAction);
							break;
						}
					}
					if (o instanceof ChoiceDefinition){
						if (((ChoiceDefinition) o).getName().equals(name)){
							ChoiceDefinition choiceDefinition = LLRP.getChoiceDefinition(name);
							for (ChoiceParameterReference cpr : choiceDefinition.getParameter()){
								AddParameterAction addParameterAction = createAddParameterAction(treeElement, null, cpr.getType());
								addMenuManager.add(addParameterAction);
							}
							break;
						}
					}
				}
			}

			if (addMenuManager.getSize() == 0){
				// add dummy action so that eclipse keeps showing the menu
				Action a = new Action(DUMMY_ACTION_TEXT) {};
				a.setEnabled(false);
				addMenuManager.add(a);
			}
		}
	}

	private AddParameterAction createAddParameterAction(Object treeElement, String childName, String parameterName) {
		AddParameterAction result = 
			new AddParameterAction(treeViewer, treeMaintainer, treeElement, childName, parameterName);
		ImageDescriptor imageDescriptor = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJ_ELEMENT);
		result.setImageDescriptor(imageDescriptor);
		return result;
	}

	/**
	 * This method is based on the method 'createSectionToolbar' of 
	 * org.eclipse.pde.internal.ui.editor.plugin.ExtensionsSection
	 * 
	 * @param section
	 * @param toolkit
	 */
	private void createSectionToolbar(Section section, FormToolkit toolkit) {
		ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
		ToolBar toolbar = toolBarManager.createControl(section);
		final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
		toolbar.setCursor(handCursor);
		// Cursor needs to be explicitly disposed
		toolbar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if ((handCursor != null) &&
						(handCursor.isDisposed() == false)) {
					handCursor.dispose();
				}
			}
		});	
		
		// Add "expand all" action to the tool bar	
		Action expandAllAction = new Action(){
			public void run() {				
				if (treeViewer != null){
					treeViewer.expandAll();
				}			
			}
		};
		expandAllAction.setToolTipText("Expand All");
		ImageDescriptor imageDescriptor = ResourceCenter.getInstance().getImageDescriptor("expand_all.gif");
		expandAllAction.setImageDescriptor(imageDescriptor);
		toolBarManager.add(expandAllAction);
		
		// Add "collapse all" action to the tool bar	
		Action collapseAllAction = new Action(){
			public void run() {				
				if (treeViewer != null){
					treeViewer.collapseAll();
				}			
			}
		};
		collapseAllAction.setToolTipText("Collapse All");
		imageDescriptor = ResourceCenter.getInstance().getImageDescriptor("collapse_all.gif");
		collapseAllAction.setImageDescriptor(imageDescriptor);
		toolBarManager.add(collapseAllAction);
		
		toolBarManager.update(true);

		section.setTextClient(toolbar);
	}
	
	private void updateErrorMessage(){
		final ScrolledForm form = page.getManagedForm().getForm();
		invalidParameters = treeMaintainer.getNonRecursivelyInvalidMessageOrParameterDescendants(treeMaintainer.getRoot());
		if (invalidParameters.size() == 0){
			form.getForm().setMessage(null, 0);
		}
		else if (invalidParameters.size() == 1){
			form.getForm().setMessage(invalidParameters.size() + " parameter contains errors", IMessageProvider.ERROR);
		}
		else{
			form.getForm().setMessage(invalidParameters.size() + " parameters contain errors", IMessageProvider.ERROR);
		}
	}
	
	private void configureFormText(FormText text) {
		text.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				String is = (String) e.getHref();
				int index = Integer.parseInt(is);
				Object invalidParameter = invalidParameters.get(index);
				((FormText) e.widget).getShell().dispose();
				treeViewer.setSelection(new StructuredSelection(invalidParameter), true);
			}
		});
		text.setImage("error", PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
	}
	
	private String createFormTextContent() {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pw.println("<form>");
		for (int i = 0; i < invalidParameters.size(); i++) {
			Object invalidParameter = invalidParameters.get(i);
			pw.print("<li vspace=\"false\" style=\"image\" indent=\"16\" value=\"error");
			pw.print("\"> <a href=\"");
			pw.print(i);
			pw.print("\">");
			pw.print(treeMaintainer.getName(invalidParameter));
			pw.println("</a></li>");
		}
		pw.println("</form>");
		pw.flush();
		return sw.toString();
	}
	
	protected void createToolBarActions(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		Action haction = new Action("hor", Action.AS_RADIO_BUTTON) {
			public void run() {
				sashForm.setOrientation(SWT.HORIZONTAL);
				form.reflow(true);
			}
		};
		haction.setChecked(true);
		haction.setToolTipText("Horizontal orientation");
		haction.setImageDescriptor(LLRPPlugin.getImageDescriptor("icons/th_horizontal.gif"));
		Action vaction = new Action("ver", Action.AS_RADIO_BUTTON) {
			public void run() {
				sashForm.setOrientation(SWT.VERTICAL);
				form.reflow(true);
			}
		};
		vaction.setChecked(false);
		vaction.setToolTipText("Vertical orientation");
		vaction.setImageDescriptor(LLRPPlugin.getImageDescriptor("icons/th_vertical.gif"));
		form.getToolBarManager().add(haction);
		form.getToolBarManager().add(vaction);
	}
	
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.setPageProvider(new LLRPDetailsPageProvider());
		refresh(treeMaintainer.getRoot());
	}
	
	class LLRPDetailsPageProvider implements IDetailsPageProvider{

		public IDetailsPage getPage(Object key) {
			if (key instanceof LLRPMessage || key instanceof LLRPParameter){
				return new LLRPDetailsPage(key, treeViewer, treeMaintainer, LLRPMasterDetailsBlock.this);
			}
			return null;
		}

		public Object getPageKey(Object object) {
			return object;
		}
		
	}
}