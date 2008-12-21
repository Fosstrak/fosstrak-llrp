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

package org.fosstrak.llrp.commander.editors.graphical.actions;

import java.util.List;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.fosstrak.llrp.commander.util.LLRPTreeMaintainer;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.LLRPParameter;

/**
 * This class represents the action to add a new LLRP parameter to a message or to a parameter.
 *
 * @author Ulrich Etter, ETHZ
 *
 */
public class AddParameterAction extends Action implements IWorkbenchAction {
	
	public final static String ID = "org.fosstrak.llrp.commander.editors.graphical.actions.AddParameterAction";
	private TreeViewer viewer;
	private LLRPTreeMaintainer treeMaintainer;
	private Object treeElement;
	private String childName;
	private String parameterName;
	
	public AddParameterAction(
			TreeViewer viewer, 
			LLRPTreeMaintainer treeMaintainer, 
			Object treeElement, 
			String childName,
			String parameterName){
		
		setId(ID);
		setText(parameterName);
		
		this.viewer = viewer;
		this.treeMaintainer = treeMaintainer;
		this.treeElement = treeElement;
		this.childName = childName;
		this.parameterName = parameterName;
	}
	
	@Override
	public void run(){
		Class parameterClass;
		try {
			parameterClass = Class.forName("org.llrp.ltk.generated.parameters." + parameterName);
			LLRPParameter subParameter = (LLRPParameter) parameterClass.getConstructor(new Class[0]).newInstance(new Object[0]);
			if (treeElement instanceof LLRPMessage || treeElement instanceof LLRPParameter){
				treeMaintainer.setChild(treeElement, childName, subParameter);
			}
			else if (treeElement instanceof List){
				treeMaintainer.addChild((List<LLRPParameter>) treeElement, subParameter);
			}
			viewer.refresh();
			viewer.setSelection(new StructuredSelection(subParameter), true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dispose() {
		
	}

}
