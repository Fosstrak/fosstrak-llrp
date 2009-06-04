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
