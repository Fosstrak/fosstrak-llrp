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

package org.fosstrak.llrp.commander;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Factory that creates the LLRP Commander perspective 
 * for the eclipse view space.
 * @author zhanghao
 * @author sawielan
 *
 */
public class PerspectiveFactory implements IPerspectiveFactory {

	/** the unique id of the message box view. */
	private static final String MESSAGEBOX_VIEW_ID = "org.fosstrak.llrp.commander.views.MessageboxView";
	
	/** the unique id of the reader view. */
	private static final String READER_EXP_VIEW_ID = "org.fosstrak.llrp.commander.views.ReaderExplorerView";
	
	public void createInitialLayout(IPageLayout aLayout) {
		
		IFolderLayout left = aLayout.createFolder("Left", IPageLayout.LEFT, 0.26f, aLayout.getEditorArea());
		left.addView(IPageLayout.ID_RES_NAV);
		    
		IFolderLayout bottom = aLayout.createFolder("Bottom",IPageLayout.BOTTOM,0.76f, aLayout.getEditorArea());
		bottom.addView(MESSAGEBOX_VIEW_ID);
		
		IFolderLayout right = aLayout.createFolder("Right",IPageLayout.RIGHT,0.2f, aLayout.getEditorArea());
		right.addView(READER_EXP_VIEW_ID);
	}

}
