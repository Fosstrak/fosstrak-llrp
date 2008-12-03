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

package org.fosstrak.llrp.commander.editors.graphical;

import java.util.LinkedList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.*;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.util.LLRP;
import org.fosstrak.llrp.commander.util.LLRPFactory;
import org.fosstrak.llrp.commander.util.LLRPTreeMaintainer;
import org.llrp.ltk.types.*;
import org.llrp.ltkGenerator.generated.*;

/**
 * The LLRPDetailsPage shows details about the message/parameter currently selected in the
 * master part.
 *
 * @author Ulrich Etter, ETHZ
 *
 */
public class LLRPDetailsPage implements IDetailsPage {
	private IManagedForm mform;
	private Object input;
	private LLRPTreeMaintainer treeMaintainer;
	private Section section;
	
	private java.util.List<ModifyListener> textModifyListeners = new LinkedList<ModifyListener>();
	private java.util.List<VerifyListener> textVerifyListeners = new LinkedList<VerifyListener>();
	private java.util.List<Text> texts = new LinkedList<Text>();
	private java.util.List<ControlDecoration> textControlDecorations = new LinkedList<ControlDecoration>();
	
	private java.util.List<Combo> combos = new LinkedList<Combo>();
	private java.util.List<ControlDecoration> comboControlDecorations = new LinkedList<ControlDecoration>();
	
	private java.util.List<TableViewer> tableViewers = new LinkedList<TableViewer>();
	private java.util.List<ControlDecoration> tableViewerControlDecorations = new LinkedList<ControlDecoration>();
	
	private java.util.List<Hyperlink> hyperlinks = new LinkedList<Hyperlink>();
	private java.util.List<ControlDecoration> hyperlinkControlDecorations = new LinkedList<ControlDecoration>();
	
	private TreeViewer treeViewer;
	private LLRPMasterDetailsBlock block;
	
	/**
	 * Creates a new LLRPDetailsPage.
	 * 
	 * @param input the object for which this page should show details (either a LLRPMessage or a LLRPParameter)
	 * @param treeViewer the tree viewer of the master part
	 * @param treeMaintainer the TreeMaintainer associated with the input object
	 * @param block the LLRPMasterDetailsBlock
	 */
	public LLRPDetailsPage(Object input, TreeViewer treeViewer, 
			LLRPTreeMaintainer treeMaintainer, LLRPMasterDetailsBlock block) {
		this.input = input;
		this.treeViewer = treeViewer;
		this.treeMaintainer = treeMaintainer;
		this.block = block;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	public void initialize(IManagedForm mform) {
		this.mform = mform;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public void createContents(Composite parent) {

		TableWrapLayout layout = new TableWrapLayout();
		layout.topMargin = 5;
		layout.leftMargin = 5;
		layout.rightMargin = 2;
		layout.bottomMargin = 2;
		parent.setLayout(layout);
		
		FormToolkit toolkit = mform.getToolkit();
		section = toolkit.createSection(parent, Section.TITLE_BAR);
		section.marginWidth = 10;
		section.setText(treeMaintainer.getName(input));
		TableWrapData td = new TableWrapData(TableWrapData.FILL, TableWrapData.TOP);
		td.grabHorizontal = true;
		section.setLayoutData(td);
		createSectionToolbar(toolkit);
		
		Composite client = toolkit.createComposite(section);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 10;
		gridLayout.numColumns = 2;
		client.setLayout(gridLayout);
		
		createFieldControls(client, toolkit);
		createListAndParameterControls(client, toolkit);
		
		toolkit.paintBordersFor(section);
		section.setClient(client);
	}
	
	private void createSectionToolbar(FormToolkit toolkit) {
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
		Action descriptionAction = createDescriptionAction();
		if (descriptionAction != null){
			toolBarManager.add(descriptionAction);
			toolBarManager.update(true);
			section.setTextClient(toolbar);
		}
	}

	/**
	 * Creates an action that opens a pop-up window and displays the description of the current message/parameter.
	 * 
	 * @return the action or <code>null</code> if there is no description for the current message/parameter or if 
	 * the SWT browser widget is not supported on this platform
	 */
	private Action createDescriptionAction() {
		final String description = LLRP.getDescription(treeMaintainer.getDefinition(input));
		if (description == null || description.equals("")){
			return null;
		}
		try {
			// try to create a browser widget
			Shell popUp = new Shell(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SHELL_TRIM);
			Browser browser = new Browser(popUp, SWT.NONE);
		} catch (SWTError e) {
			// browser widget is not available on this platform -> don't provide "description" action
			return null;
		}
		Action descriptionAction = new Action(){
			public void run() {				
				final Shell popUp = new Shell(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SHELL_TRIM);
				popUp.setText(treeMaintainer.getName(input));
				popUp.setSize(800, 400);
				final Browser browser = new Browser(popUp, SWT.NONE);
				int fontHeight = section.getFont().getFontData()[0].getHeight();
				String fontName = section.getFont().getFontData()[0].getName();
				String css = "overflow:auto;font-family:" + fontName + ";font-size:" + fontHeight + "pt";
				String html = "<html><body style='" + css + "'>" + description + "</body></html>";
				browser.setText(html);
				
				// disable browser context menu
				browser.addListener(SWT.MenuDetect, new Listener() {
					public void handleEvent(Event event) {
						event.doit = false;
					}
				});
				
				browser.setSize(popUp.getClientArea().width, popUp.getClientArea().height);
				popUp.addControlListener(new ControlAdapter() {
					public void controlResized(ControlEvent e) {
						browser.setSize(popUp.getClientArea().width, popUp.getClientArea().height);
					}
				});
				popUp.open();
			}
		};
		descriptionAction.setToolTipText("Show Description");
		ImageDescriptor imageDescriptor = ResourceCenter.getInstance().getImageDescriptor("information.gif");
		descriptionAction.setImageDescriptor(imageDescriptor);
		return descriptionAction;
	}

	private void createFieldControls(Composite client, FormToolkit toolkit) {
		java.util.List<FieldDefinition> fieldDefinitions = LLRP.getFieldDefinitions(treeMaintainer.getDefinition(input));
		for (int i = 0; i < fieldDefinitions.size(); i++){
			final FieldDefinition fieldDefinition = fieldDefinitions.get(i);
			toolkit.createLabel(client, fieldDefinition.getName() + ":");
			if (LLRP.isEnumeration(fieldDefinition)){
				Combo combo = createFieldCombo(client, toolkit, fieldDefinition);
				combos.add(combo);
			}
			else{
				Text text = createFieldText(client, toolkit, fieldDefinition);
				text.setToolTipText(LLRP.getFieldType(fieldDefinition));
				texts.add(text);
			}
		}
	}
	
	private Combo createFieldCombo(Composite client, FormToolkit toolkit, final FieldDefinition fieldDefinition) {
		final Combo combo = new Combo(client, SWT.READ_ONLY);
		final ControlDecoration controlDecoration = createControlDecoration(combo);
		comboControlDecorations.add(controlDecoration);
		EnumerationDefinition enumerationDefinition = LLRP.getEnumerationDefinition(fieldDefinition.getEnumeration());
		if (enumerationDefinition != null){
			java.util.List<EnumerationEntryDefinition> list = enumerationDefinition.getEntry();
			for (EnumerationEntryDefinition e : list){
				combo.add(e.getName());
			}
		}
		combo.addSelectionListener(new SelectionListener(){
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			
			public void widgetSelected(SelectionEvent e) {
				if (input != null){
					LLRPEnumeration field = (LLRPEnumeration) treeMaintainer.getField(input, fieldDefinition.getName());
					Class fieldClass;
					try {
						fieldClass = Class.forName("org.llrp.ltk.generated.enumerations." + fieldDefinition.getEnumeration());
						field = (LLRPEnumeration) fieldClass.getConstructor(new Class[0]).newInstance(new Object[0]);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					field.set(combo.getText());
					treeMaintainer.setField(input, fieldDefinition.getName(), (LLRPType) field);
					block.refresh(input);
				}
			}
			
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		combo.setLayoutData(gd);
		return combo;
	}
	
	private Text createFieldText(Composite client, FormToolkit toolkit, final FieldDefinition fieldDefinition) {
		final Text text = toolkit.createText(client, "", SWT.SINGLE);
		final ControlDecoration controlDecoration = createControlDecoration(text);
		textControlDecorations.add(controlDecoration);
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (input != null){
					
					LLRPType fieldValue = null;
					String fieldType = LLRP.getFieldType(fieldDefinition);
					
					try {
						fieldValue = LLRPFactory.createLLRPType(fieldType, text.getText());
						treeMaintainer.setField(input, fieldDefinition.getName(), fieldValue);
					} catch (Exception e1) {
						// do nothing
					}
					
					// validation
					String errorMessage = treeMaintainer.validateField(input, fieldDefinition.getName());
					updateControlDecoration(controlDecoration, errorMessage);
					block.refresh(null);
				}
			}
		};
		textModifyListeners.add(modifyListener);
		text.addModifyListener(modifyListener);
		VerifyListener verifyListener = new VerifyListener(){

			public void verifyText(VerifyEvent e) {
				String fieldType = LLRP.getFieldType(fieldDefinition);
				try {
					String oldTextValue = text.getText();
					String newTextValue = 
						oldTextValue.substring(0, e.start) + 
						e.text + 
						oldTextValue.substring(e.end);
					LLRPFactory.createLLRPType(fieldType, newTextValue);
					e.doit = true;
				} catch (Exception exception) {
					e.doit = false;
				}
			}
			
		};
		textVerifyListeners.add(verifyListener);
		text.addVerifyListener(verifyListener);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		text.setLayoutData(gd);
		return text;
	}

	private void createListAndParameterControls(Composite client, FormToolkit toolkit) {
		GridData gd;
		Object messageOrParameterDefinition = treeMaintainer.getDefinition(input);
		java.util.List<Object> parameterOrChoiceList = new LinkedList<Object>();
		if (messageOrParameterDefinition instanceof MessageDefinition){
			parameterOrChoiceList = ((MessageDefinition) messageOrParameterDefinition).getParameterOrChoice();
		}
		else if (messageOrParameterDefinition instanceof ParameterDefinition){
			parameterOrChoiceList = ((ParameterDefinition) messageOrParameterDefinition).getParameterOrChoice();
		}
		for (Object o : parameterOrChoiceList){
			String childName = "";
			if (o instanceof ChoiceReference){
				ChoiceReference choiceReference = (ChoiceReference) o;
				childName = choiceReference.getType();
			}
			else if (o instanceof ParameterReference){
				ParameterReference parameterReference = (ParameterReference) o;
				childName = parameterReference.getType();
			}
			Label label = toolkit.createLabel(client, childName + ":");
			gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
			label.setLayoutData(gd);
			if (LLRP.canOccurMultipleTimes(treeMaintainer.getDefinition(input), childName)){
				TableViewer tableViewer = createListTableViewer(client, toolkit);
				tableViewers.add(tableViewer);
			}
			else{
				Hyperlink hyperlink = createParameterHyperlink(client, toolkit);
				hyperlinks.add(hyperlink);
			}
		}
	}

	private TableViewer createListTableViewer(Composite client, FormToolkit toolkit) {
		Table table = toolkit.createTable(client, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
		table.setLayoutData(gd);
		
		ControlDecoration controlDecoration = createControlDecoration(table);
		tableViewerControlDecorations.add(controlDecoration);
		
		final TableViewer tableViewer = new TableViewer(table);
		
		tableViewer.setContentProvider(new IStructuredContentProvider(){
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof java.util.List){
					return ((java.util.List<LLRPParameter>) inputElement).toArray(new Object[0]);
				}
				return null;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		
		tableViewer.setLabelProvider(new LLRPTreeLabelProvider(treeMaintainer));
		
		tableViewer.addDoubleClickListener(new IDoubleClickListener(){
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (selection.size() == 1 && selection.getFirstElement() instanceof LLRPParameter){
					treeViewer.setSelection(new StructuredSelection(selection.getFirstElement()), true);
				}
			}
		});
		
		return tableViewer;
	}
	
	private Hyperlink createParameterHyperlink(Composite client, FormToolkit toolkit) {
		final Hyperlink hyperlink = toolkit.createHyperlink(client, "", SWT.NULL);
		ControlDecoration controlDecoration = createControlDecoration(hyperlink);
		hyperlinkControlDecorations.add(controlDecoration);
		
		hyperlink.addHyperlinkListener(new HyperlinkAdapter(){
			public void linkActivated(HyperlinkEvent e) {
				treeViewer.setSelection(new StructuredSelection(hyperlink.getData()), true);
			}
		});
		return hyperlink;
	}

	private ControlDecoration createControlDecoration(Control control){
		final ControlDecoration controlDecoration = new ControlDecoration(control, SWT.LEFT | SWT.BOTTOM);
		Image errorImage = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
		controlDecoration.setImage(errorImage);
		controlDecoration.hide();
		return controlDecoration;
	}
	
	private void update() {
		if (input != null){
			section.setText(treeMaintainer.getName(input));			
			updateFieldControls();
			updateListAndParameterControls();	
		}
	}
	
	private void updateFieldControls() {
		int textIndex = 0;
		int comboIndex = 0;
		java.util.List<FieldDefinition> fieldDefinitions = LLRP.getFieldDefinitions(treeMaintainer.getDefinition(input));
		for (int i = 0; i < fieldDefinitions.size(); i++){
			FieldDefinition fieldDefinition = fieldDefinitions.get(i);
			String fieldName = fieldDefinition.getName();
			LLRPType fieldValue = treeMaintainer.getField(input, fieldName);
			fieldDefinition.getType().value();
			if (LLRP.isEnumeration(fieldDefinition)){
				if (fieldValue != null){
					combos.get(comboIndex).setText(fieldValue.toString());
				}
				else{
					combos.get(comboIndex).clearSelection();
				}
				
				//validation
				String errorMessage = treeMaintainer.validateField(input, fieldName);
				updateControlDecoration(comboControlDecorations.get(comboIndex), errorMessage);
				
				comboIndex++;
			}
			else{
				String fieldValueAsString = "";
				if (fieldValue != null){
					fieldValueAsString = fieldValue.toString();
				}
				texts.get(textIndex).removeVerifyListener(textVerifyListeners.get(textIndex));
				texts.get(textIndex).removeModifyListener(textModifyListeners.get(textIndex));
				texts.get(textIndex).setText(fieldValueAsString);
				texts.get(textIndex).addVerifyListener(textVerifyListeners.get(textIndex));
				texts.get(textIndex).addModifyListener(textModifyListeners.get(textIndex));
				
				//validation
				String errorMessage = treeMaintainer.validateField(input, fieldName);
				updateControlDecoration(textControlDecorations.get(textIndex), errorMessage);
				
				textIndex++;
			}
		}
	}
	
	private void updateListAndParameterControls() {
		int tableViewerIndex = 0;
		int hyperlinkIndex = 0;
		java.util.List<String> childrenNames = LLRP.getParameterAndChoiceNames(treeMaintainer.getDefinition(input));
		for (String childName : childrenNames){
			Object o = treeMaintainer.getChild(input, childName);
			if (LLRP.canOccurMultipleTimes(treeMaintainer.getDefinition(input), childName)){
				// fill table
				java.util.List<LLRPParameter> parameterList = (java.util.List<LLRPParameter>) o;
				tableViewers.get(tableViewerIndex).setInput(parameterList);
				
				//validation
				String errorMessage = treeMaintainer.validateEmptiness(parameterList);
				updateControlDecoration(tableViewerControlDecorations.get(tableViewerIndex), errorMessage);
				
				tableViewerIndex++;
			}
			else {
				final LLRPParameter parameter = (LLRPParameter) o;
				// "fill" link
				if (parameter == null){
					hyperlinks.get(hyperlinkIndex).setText(childName);
					hyperlinks.get(hyperlinkIndex).setEnabled(false);
					hyperlinks.get(hyperlinkIndex).setUnderlined(false);
				}
				else{
					hyperlinks.get(hyperlinkIndex).setText(treeMaintainer.getName(parameter));
					hyperlinks.get(hyperlinkIndex).setEnabled(true);
					hyperlinks.get(hyperlinkIndex).setUnderlined(true);
					hyperlinks.get(hyperlinkIndex).setData(parameter);
				}
				
				// validation
				String errorMessage = treeMaintainer.validateChildPresence(input, childName);
				updateControlDecoration(hyperlinkControlDecorations.get(hyperlinkIndex), errorMessage);
				
				hyperlinkIndex++;
			}
		}	
	}

	private void updateControlDecoration(ControlDecoration controlDecoration, String errorMessage){
		if (! errorMessage.equals("")){
			// add error flag
			controlDecoration.setDescriptionText(errorMessage);
			controlDecoration.show();
		}
		else{
			// remove error flag
			controlDecoration.hide();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#inputChanged(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection) selection;
		if (ssel.size() == 1) {
			input = ssel.getFirstElement();
		}
		else{
			input = null;
		}
		update();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#commit()
	 */
	public void commit(boolean onSave) {
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#setFocus()
	 */
	public void setFocus() {
		if (!texts.isEmpty()){
			texts.get(0).setFocus();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#dispose()
	 */
	public void dispose() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#isDirty()
	 */
	public boolean isDirty() {
		return false;
	}
	
	public boolean isStale() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.IDetailsPage#refresh()
	 */
	public void refresh() {
		update();
	}
	
	public boolean setFormInput(Object input) {
		return false;
	}
}

