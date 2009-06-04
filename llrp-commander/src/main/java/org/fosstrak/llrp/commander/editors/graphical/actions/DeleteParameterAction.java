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

package org.fosstrak.llrp.commander.editors.graphical.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.fosstrak.llrp.commander.editors.graphical.LLRPMasterDetailsBlock;
import org.fosstrak.llrp.commander.util.LLRP;
import org.fosstrak.llrp.commander.util.LLRPTreeMaintainer;
import org.llrp.ltk.types.*;


/**
 * This class represents the action to delete an LLRP parameter.
 *
 * @author Ulrich Etter, ETHZ
 *
 */
public class DeleteParameterAction extends Action implements ISelectionChangedListener, IWorkbenchAction {
	
	public final static String ID = "org.eclipse.ui.forms.article.deleteParameter";
	private IStructuredSelection selection;
	private TreeViewer viewer;
	private LLRPTreeMaintainer treeMaintainer;
	private LLRPMasterDetailsBlock block;
	
	public DeleteParameterAction(IWorkbenchWindow window, TreeViewer viewer, LLRPTreeMaintainer treeMaintainer, LLRPMasterDetailsBlock block){
		setId(ID);
		setText("&Delete");
		setToolTipText("Delete this llrp parameter.");

		viewer.addSelectionChangedListener(this);
		
		this.viewer = viewer;
		this.treeMaintainer = treeMaintainer;
		this.block = block;
	}
	
	@Override
	public void run(){
		LLRPParameter parameterToDelete = (LLRPParameter) selection.getFirstElement();
		Object parent = treeMaintainer.getParent(parameterToDelete);
		if (parent instanceof LLRPMessage || parent instanceof LLRPParameter){
			for (String childName : LLRP.getParameterAndChoiceNames(treeMaintainer.getDefinition(parent))){
				if (treeMaintainer.getChild(parent, childName) == parameterToDelete){
					treeMaintainer.setChild(parent, childName, null);
				}
			}
		}
		else if (parent instanceof List){
			treeMaintainer.removeChild((List<LLRPParameter>)parent, parameterToDelete);
		}
		block.refresh(parent);
	}

	public void dispose() {
		viewer.removeSelectionChangedListener(this);
	}

	public void selectionChanged(SelectionChangedEvent event) {
		boolean enable = false;
		ISelection incoming = event.getSelection();
		if (incoming instanceof IStructuredSelection){
			selection = (IStructuredSelection) incoming;
			if (selection.size() == 1){
				if(selection.getFirstElement() instanceof LLRPParameter){
					enable = true;
				}
			}
		}
		setEnabled(enable);
	}

}
