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

package org.fosstrak.llrp.commander.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.fosstrak.llrp.commander.LLRPPlugin;

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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}
	
}