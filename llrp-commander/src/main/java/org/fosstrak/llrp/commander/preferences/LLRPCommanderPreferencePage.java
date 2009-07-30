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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.fosstrak.llrp.commander.LLRPPlugin;
import org.fosstrak.llrp.commander.preferences.pref.GroupedStringFieldEditor;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * sub-classing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 * 
 * @author Haoning Zhang
 * @author sawielan
 * @version 1.0
 */
public class LLRPCommanderPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	/**
	 * Default Constructor
	 */
	public LLRPCommanderPreferencePage() {
		super(GRID);
		setPreferenceStore(LLRPPlugin.getDefault().getPreferenceStore());
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		
		addField(new StringFieldEditor(
				PreferenceConstants.P_PROJECT, 
				"Eclipse Project Name:", getFieldEditorParent()));

		addField(new RadioGroupFieldEditor(
				PreferenceConstants.P_DEFAULT_EDITOR, 
				"Default Editor/Viewer:", 
				1,
				new String[][] {
					{"Graphical Editor", PreferenceConstants.P_DEFAULT_EDITOR_GRAPHICAL},
					{"XML Editor", PreferenceConstants.P_DEFAULT_EDITOR_XML},
					{"Binary Viewer", PreferenceConstants.P_DEFAULT_EDITOR_BINARY}
				},
				getFieldEditorParent(),
	          true)
		);
		
		addField(
				new BooleanFieldEditor(
						PreferenceConstants.P_WIPE_DB_ON_STARTUP, 
						"Wipe DB on startup", getFieldEditorParent())
				);
		
		addField(
				new BooleanFieldEditor(
						PreferenceConstants.P_LOG_RO_ACCESS_REPORTS,
						"Log RO_ACCESS_REPORTS", getFieldEditorParent()
						)
				);
		addField(
				new BooleanFieldEditor(
						PreferenceConstants.P_WIPE_RO_ACCESS_REPORTS_ON_STARTUP,
						"Wipe RO_ACCESS_REPORTS DB on startup",
						getFieldEditorParent()
						)
				);
		addField(new BooleanFieldEditor(
				PreferenceConstants.P_USE_INTERNAL_DB, 
				"Use internal standalone Derby Database.", getFieldEditorParent())
		);
		addField(new GroupedStringFieldEditor(
				"External Database settings", 
				new String[][] { 
						{"Implementor", PreferenceConstants.P_EXT_DB_IMPLEMENTOR },
						{"JDBC String", PreferenceConstants.P_EXT_DB_JDBC}, 
						{"Username", PreferenceConstants.P_EXT_DB_USERNAME}, 
						{"Password", PreferenceConstants.P_EXT_DB_PWD}
				},
				getFieldEditorParent()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}