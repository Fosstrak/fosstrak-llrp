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

/**
 * Constant definitions for plug-in preferences
 *
 * @author Haoning Zhang
 * @author sawielan
 * @version 1.0
 */
public class PreferenceConstants {

	/** the name of the eclipse project. */
	public static final String P_PROJECT = "eclipseProject";

	/** the name of the choice preference property. */
	public static final String P_READER_FROM = "choicePreference";

	/** the name of the string preference property. */
	public static final String P_STRING = "stringPreference";
	
	/** the name of the default editor property. */
	public static final String P_DEFAULT_EDITOR = "defaultEditor";
	
	/** the name of the graphical editor property. */
	public static final String P_DEFAULT_EDITOR_GRAPHICAL = "graphical_editor";
	
	/** the name of the XML editor property. */
	public static final String P_DEFAULT_EDITOR_XML = "xml_editor";
	
	/** the name of the binary viewer property. */
	public static final String P_DEFAULT_EDITOR_BINARY = "binary_viewer";
	
	/** name of the preference field for the configuration of the DB-wipe. */
	public static final String P_WIPE_DB_ON_STARTUP = "wipeDBOnStartup";
	
}
