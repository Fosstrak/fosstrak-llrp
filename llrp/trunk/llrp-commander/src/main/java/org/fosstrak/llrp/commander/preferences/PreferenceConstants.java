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
	
	/** name of the preference field for the log RO_ACCESS_REPORTS. */
	public static final String P_LOG_RO_ACCESS_REPORTS = "logRO_ACCESS_REPORTS";
	
	/** name of the preference field for the configuration field of the wipe DB.*/
	public static final String P_WIPE_RO_ACCESS_REPORTS_ON_STARTUP 
		= "wipeROACCESSREPORTSOnStartup";
}
