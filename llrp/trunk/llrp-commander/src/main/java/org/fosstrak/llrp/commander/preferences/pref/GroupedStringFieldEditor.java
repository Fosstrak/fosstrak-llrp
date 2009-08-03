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

package org.fosstrak.llrp.commander.preferences.pref;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * An extension of the {@link FieldEditor} supporting the grouping of several 
 * preferences into a nice group box. 
 * @author sawielan
 *
 */
public class GroupedStringFieldEditor extends FieldEditor  {
	
	// the top level widget holding the whole graphical representation of the 
	// preferences.
	private Composite groupedFields;
	
	// how many columns to use in the groupedFields widget.
	private int numColumns = 1;
	
	/** index of the text label in the preferencesLabelsAndNames array. */
	public static final int INDEX_LABEL = 0;
	
	/** index of the preference name in the preferencesLabelsAndNames array. */
	public static final int INDEX_PREF_NAME = 1;
	
	// the preferences widgets.
	private Text[] preferences;
	
	// the label widgets.
	private Label[] preferencesLabels;
	
	// the values of the preferences read from the preferences store.
	private String[] preferencesValues;
	
	// a 2D array holding the labels and the names of the preferences.
	private String[][] preferencesLabelsAndNames;
	
	
	/**
	 * A {@link FieldEditor} supporting the grouping of several preferences 
	 * into a nice group box. 
	 * @param labelText the text of the group box (example the title).
	 * @param preferencesLabelsAndNames a 2D array providing the labels for the 
	 * preferences in the first column, and the preferences names in the 
	 * second column.<br/>
	 * <h3>Example</h3>
	 * <code>new String[][] {</code><br/>
	 * <code>&nbsp;&nbsp;{"Implementor", PreferenceConstants.P_EXT_DB_IMPLEMENTOR },</code><br/>
	 * <code>&nbsp;&nbsp;{"JDBC String", PreferenceConstants.P_EXT_DB_JDBC}</code><br/>
	 * <code>}</code>
	 * @param parent the parent widget of this editor.
	 */
	public GroupedStringFieldEditor(String labelText, 
			String[][] preferencesLabelsAndNames, Composite parent) {
		
		this.preferencesLabelsAndNames = preferencesLabelsAndNames;
		setLabelText(labelText);
		this.numColumns = 2;
		createControl(parent);
	}
	
	/**
	 * create the group providing the entries.
	 * @param parent the parent of this widget.
	 * @return a composite widget holding all the graphical elements.
	 */
	public Composite getDBFieldControl(Composite parent) {
		if (null == groupedFields) {
			Font font = parent.getFont();
			
			// create a nice group box (with the nice border around it).
			Group group = new Group(parent, SWT.NONE);
			group.setFont(font);
			String text = getLabelText();
			if (null != text) {
				group.setText(text);
			} 
			groupedFields = group;
			GridLayout layout = new GridLayout();
			layout.horizontalSpacing = HORIZONTAL_GAP;
			layout.numColumns = numColumns;
			groupedFields.setLayout(layout);
		
			// create the graphical representation of the labels and the 
			// preferences.
			final int len = preferencesLabelsAndNames.length;
			preferences = new Text[len];
			preferencesLabels = new Label[len];
			for (int i=0; i<len; i++) {
				Label lbl = new Label(groupedFields, SWT.NONE);
				lbl.setFont(font);
				// add a nice : if not already provided by the label itself
				String t = preferencesLabelsAndNames[i][INDEX_LABEL];
				if (!t.endsWith(":")) {
					t += ":";
				}
				lbl.setText(t);
				preferencesLabels[i] = lbl;
				
				Text txt = new Text(groupedFields, SWT.BORDER);
				txt.setFont(font);
				// create a grid data element filling up the margin with the 
				// text field so it fills out the whole space.
				GridData gd = new GridData();
				gd.horizontalSpan = numColumns - 1;
				gd.horizontalAlignment = GridData.FILL;
				gd.grabExcessHorizontalSpace = true;
				txt.setLayoutData(gd);
				preferences[i] = txt;
			}
		} else {
			checkParent(groupedFields, parent);
		}
		return groupedFields;
	}
	
	@Override
	protected void createControl(Composite parent) {
		GridLayout layout = new GridLayout();
		layout.numColumns = getNumberOfControls();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = HORIZONTAL_GAP;
		parent.setLayout(layout);
		doFillIntoGrid(parent, layout.numColumns);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData)groupedFields.getLayoutData()).horizontalSpan = numColumns;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getDBFieldControl(parent);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		control.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		final int len = preferencesLabelsAndNames.length;
		preferencesValues = new String[len];
		for (int i=0; i<len; i++) {
			preferencesValues[i] = getPreferenceStore().getString(
					preferencesLabelsAndNames[i][INDEX_PREF_NAME]);
			preferences[i].setText(preferencesValues[i]);
		}
	}

	@Override
	protected void doLoadDefault() {
		final int len = preferencesLabelsAndNames.length;
		preferencesValues = new String[len];
		for (int i=0; i<len; i++) {
			preferencesValues[i] = getPreferenceStore().getDefaultString(
					preferencesLabelsAndNames[i][INDEX_PREF_NAME]);
			preferences[i].setText(preferencesValues[i]);
		}
	}
	
	@Override
	protected void doStore() {
		final int len = preferencesLabelsAndNames.length;
		for (int i=0; i<len; i++) {
			getPreferenceStore().setValue(
					preferencesLabelsAndNames[i][INDEX_PREF_NAME], 
					preferences[i].getText());
		}
	}

	@Override
	public int getNumberOfControls() {
		return 1;
	}
}
