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

package org.fosstrak.llrp.commander.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.fosstrak.llrp.commander.LLRPPlugin;
import org.fosstrak.llrp.commander.ResourceCenter;

/**
 * Class used to initialize default preference values.
 *
 * @author Haoning Zhang
 * @version 1.0
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/**
	 * Initialize the Preference Values
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = LLRPPlugin.getDefault().getPreferenceStore();
		
		store.setDefault(PreferenceConstants.P_READER_FROM, "fromXML");
		store.setDefault(PreferenceConstants.P_PROJECT, ResourceCenter.DEFAULT_ECLIPSE_PROJECT);
		store.setDefault(PreferenceConstants.P_STRING, "Default value");		
		store.setDefault(PreferenceConstants.P_DEFAULT_EDITOR, PreferenceConstants.P_DEFAULT_EDITOR_GRAPHICAL);
	}

}
