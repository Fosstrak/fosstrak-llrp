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

package org.fosstrak.llrp.commander.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.*;
import org.eclipse.ui.actions.*;
import org.fosstrak.llrp.commander.dialogs.SendMessageDialog;

/**
 * The SendMessageAction implements workbench action delegate.
 * The action proxy will be created by the workbench and
 * shown in the UI. When the user tries to use the action,
 * this delegate will be created and execution will be 
 * delegated to it.
 * @see IEditorActionDelegate
 */
public class SendMessageAction extends ActionDelegate
	implements IEditorActionDelegate {
	
	/**
	 * @see ActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		SendMessageDialog.getInstance(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).open();
	}

	/**
	 * @see IEditorActionDelegate#setActiveEditor(IAction, IEditorPart)
	 */
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
	}

}
