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

package org.fosstrak.llrp.commander.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * Wizard Page that lets the user enter the folder, the file name and the 
 * message type of the new LLRP message.
 *
 * @author Ulrich Etter, ETHZ
 *
 */
public class NewLLRPMessageWizardPage extends WizardPage {
	
	private final String WIZARD_PAGE_TITLE = "LLRP Message";
	private final String WIZARD_PAGE_DESCRIPTION = "Create a new LLRP message.";
	private final String FILE_EXTENSION = "llrp";
	
	private final String DEFAULT_MESSAGE_TYPE = "ADD_ROSPEC";
	
	private final String[] MESSAGE_TYPES = {
			"GET_READER_CAPABILITIES",
			"GET_READER_CONFIG",
			"SET_READER_CONFIG",
			
			"GET_ROSPECS",
			"ADD_ROSPEC",
			"ENABLE_ROSPEC",
			"DISABLE_ROSPEC",
			"START_ROSPEC",
			"STOP_ROSPEC",
			"DELETE_ROSPEC",
			
			"GET_ACCESSSPECS",
			"ADD_ACCESSSPEC",
			"ENABLE_ACCESSSPEC",
			"DISABLE_ACCESSSPEC",
			"DELETE_ACCESSSPEC",
			
			"GET_REPORT",
			"ENABLE_EVENTS_AND_REPORTS",
			"CLOSE_CONNECTION",
			"CUSTOM_MESSAGE",
	};
	
	private Text folderText;
	private Text fileText;
	private Combo messageTypeCombo;

	private ISelection selection;

	/**
	 * Constructor for NewLLRPMessageWizardPage.
	 * 
	 * @param pageName
	 */
	public NewLLRPMessageWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle(WIZARD_PAGE_TITLE);
		setDescription(WIZARD_PAGE_DESCRIPTION);
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		
		Label label = new Label(container, SWT.NULL);
		label.setText("&Folder:");

		folderText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		folderText.setLayoutData(gd);
		folderText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		
		label = new Label(container, SWT.NULL);
		label.setText("&File name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		
		label = new Label(container, SWT.NULL);
		label.setText("." + FILE_EXTENSION);
		
		
		label = new Label(container, SWT.NULL);
		label.setText("&Message type:");
		
		messageTypeCombo = new Combo(container, SWT.READ_ONLY);
		for (int i = 0; i < MESSAGE_TYPES.length; i++){
			messageTypeCombo.add(MESSAGE_TYPES[i]);
		}
		gd = new GridData(GridData.FILL_HORIZONTAL);
		messageTypeCombo.setLayoutData(gd);
		messageTypeCombo.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				fileText.setText(messageTypeCombo.getText().toLowerCase());
			}
			
		});
		
		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Initializes the input fields.
	 */
	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() == 1){
				Object obj = ssel.getFirstElement();
				if (obj instanceof IResource) {
					IContainer container;
					if (obj instanceof IContainer)
						container = (IContainer) obj;
					else
						container = ((IResource) obj).getParent();
					folderText.setText(container.getFullPath().toString());
				}
			}
		}
		fileText.setText(DEFAULT_MESSAGE_TYPE.toLowerCase());
		messageTypeCombo.setText(DEFAULT_MESSAGE_TYPE);
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the folder field.
	 */
	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Please select a folder.");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				folderText.setText(((Path) result[0]).toString());
			}
		}
	}

	/**
	 * Checks whether the input is valid. If not, an error message is shown.
	 */
	private void dialogChanged() {
		IContainer container = (IContainer) ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getFolderName()));
		String fileName = getFileName();

		if (getFolderName().length() == 0) {
			updateStatus("Folder must be specified.");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("Folder must exist.");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable.");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified.");
			return;
		}
		if (fileName.trim().equals("." + FILE_EXTENSION)) {
			updateStatus("File name must not be empty.");
			return;
		}
		IFile file = container.getFile(new Path(fileName));
		if (file.exists()){
			updateStatus("File '" + fileName + "' does already exist.");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getFolderName() {
		return folderText.getText();
	}

	public String getFileName() {
		return fileText.getText() + "." + FILE_EXTENSION;
	}
	
	public String getLLRPMessageType() {
		return messageTypeCombo.getText();
	}
}