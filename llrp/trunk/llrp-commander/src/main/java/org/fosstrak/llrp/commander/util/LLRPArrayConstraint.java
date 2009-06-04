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

package org.fosstrak.llrp.commander.util;

/**
 * This class represents an LLRP constraint stating that a certain array field must not be empty.
 * 
 * @author Ulrich Etter, ETHZ
 *
 */
public class LLRPArrayConstraint {
	
	private String messageOrParameterName;
	private String fieldName;
	private int[] defaultValue;
	
	/**
	 * Creates a new array constraint.
	 * <br/><br/>
	 * <b>Example:</b> <br/>
	 * To create a constraint that specifies that the array field <code>AntennaIDs</code> of the 
	 * parameter <code>AISpec</code> must not be empty and has 0 as default value, use the following code: <br/>
	 * <code>
	 *	new LLRPArrayConstraint(
	 *		"AISpec",
	 *		"AntennaIDs",
	 *		new int[]{0}),
	 * </code>
	 * 
	 * @param messageOrParameterName the message/parameter this constraint is defined for
	 * @param fieldName the name of the field this constraint is defined for
	 * @param defaultValue a default value for the field this constraint is defined for
	 */
	public LLRPArrayConstraint(
			String messageOrParameterName,
			String fieldName,
			int[] defaultValue){
		this.messageOrParameterName = messageOrParameterName;
		this.fieldName = fieldName;
		this.defaultValue = defaultValue;
	}

	/**
	 * @return the error message associated with this constraint
	 */
	public String getErrorMessage(){
		String result = "This array must not be empty.";
		return result;
	}
	
	/**
	 * @return the message/parameter this constraint is defined for
	 */
	public String getMessageOrParameterName() {
		return messageOrParameterName;
	}
	
	/**
	 * @return the sub-parameter this constraint is defined for
	 */
	public String getFieldName() {
		return fieldName;
	}
	
	/**
	 * @return the default value for the field this constraint is defined for
	 */
	public int[] getDefaultValue(){
		return defaultValue;
	}

}
