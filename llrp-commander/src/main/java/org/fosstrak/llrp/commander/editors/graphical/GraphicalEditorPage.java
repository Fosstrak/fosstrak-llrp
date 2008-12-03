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

import java.util.Observable;
import java.util.Observer;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.*;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.fosstrak.llrp.commander.editors.LLRPEditor;
import org.fosstrak.llrp.commander.util.LLRPTreeMaintainer;
import org.llrp.ltk.types.LLRPMessage;

/**
 * The GraphicalEditorPage is the root class of the graphical editor. It has a 
 * LLRPMasterDetailsBlock.
 *
 * @author Ulrich Etter, ETHZ
 *
 */
public class GraphicalEditorPage extends FormPage implements Observer {
		
	private final static String GRAPHICAL_EDITOR_TITLE = "Graphical Editor";
	private LLRPEditor editor;
	private LLRPMasterDetailsBlock masterDetailsBlock;
	private LLRPTreeMaintainer treeMaintainer;
	private boolean masterDetailsBlockCreated;
	private boolean dirty;
	
	/**
	 * Creates a new GraphicalEditorPage.
	 * 
	 * @param editor the editor this editor page belongs to
	 */
	public GraphicalEditorPage(LLRPEditor editor) {
		super(editor, "graphicalEditor", GRAPHICAL_EDITOR_TITLE);
		this.editor = editor;
		treeMaintainer = new LLRPTreeMaintainer(null);		
		treeMaintainer.addObserver(this);
		masterDetailsBlock = new LLRPMasterDetailsBlock(this, this.treeMaintainer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	protected void createFormContent(final IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		form.setText(GRAPHICAL_EDITOR_TITLE);
		masterDetailsBlock.createContent(managedForm);
		masterDetailsBlockCreated = true;
	}
	
	/**
	 * Returns the LLRP message that is currently displayed in the graphical editor.
	 * 
	 * @return the LLRP message that is currently displayed in the graphical editor
	 */
	public LLRPMessage getLLRPMessage(){
		return treeMaintainer.getRoot();
	}

	/**
	 * Changes the message currently displayed in the graphical editor to the given message.
	 * 
	 * @param lLRPMessage the message that should be displayed in the graphical editor
	 */
	public void setLLRPMessage(LLRPMessage lLRPMessage){
		this.treeMaintainer.setRoot(lLRPMessage);
		dirty = false;
		if (masterDetailsBlockCreated){
			masterDetailsBlock.refresh(treeMaintainer.getRoot());
		}
	}

	/**
	 * @return <code>true</code> if the graphical editor is dirty, and <code>false</code> otherwise.
	 */
	public boolean isGraphicalEditorDirty() {
		return dirty;
	}

	/**
	 * Sets the dirty flag to the given value.
	 */
	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

	public void update(Observable arg0, Object arg1) {
		if (arg0 == treeMaintainer){
			if (dirty == false){
				dirty = true;
				editor.markXMLEditorAsDirty();
			}
		}
		
	}
	
}