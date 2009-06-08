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
 * This class represents an LLRP constraint stating that a certain parameter is mandatory.
 * 
 * @author Ulrich Etter, ETHZ
 *
 */
public class LLRPPresenceConstraint {
	
	private String messageOrParameterName;
	private String subparameterName;
	
	private String preconditionedEnumerationName;
	private String preconditionedEnumerationValue;
	
	/**
	 * Creates a new presence constraint.
	 * <br/><br/>
	 * <b>Example:</b> <br/>
	 * To create a constraint that specifies that the sub-parameter <code>PeriodicTriggerValue</code> of the 
	 * parameter <code>ROSpecStartTrigger</code> must be present if the enumeration
	 * <code>ROSpecStartTriggerType</code> has value <code>Periodic</code>, use the following code: <br/>
	 * <code>
	 *	new LLRPPresenceConstraint(
	 *		"ROSpecStartTrigger",
	 *		"PeriodicTriggerValue",
	 *		"ROSpecStartTriggerType",
	 *		"Periodic"),
	 * </code>
	 * 
	 * @param messageOrParameterName the message/parameter this constraint is defined for
	 * @param subparameterName the sub-parameter this constraint is defined for
	 * @param preconditionedEnumerationName the name of the enumeration on which this constraint is dependent; 
	 * use <code>null</code> if this constraint does not depend on any enumeration
	 * @param preconditionedEnumerationValue the value of the enumeration on which this constraint is dependent; 
	 * use <code>null</code> if this constraint does not depend on any enumeration
	 */
	public LLRPPresenceConstraint(
			String messageOrParameterName,
			String subparameterName,
			String preconditionedEnumerationName,
			String preconditionedEnumerationValue){
		this.messageOrParameterName = messageOrParameterName;
		this.subparameterName = subparameterName;
		this.preconditionedEnumerationName = preconditionedEnumerationName;
		this.preconditionedEnumerationValue = preconditionedEnumerationValue;
	}

	/**
	 * @return the error message associated with this constraint
	 */
	public String getErrorMessage(){
		String result = "This parameter must be present";
		if (preconditionedEnumerationName != null){
			result = result + " when " + preconditionedEnumerationName + " = " + preconditionedEnumerationValue;
		}
		result = result + ".";
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
	public String getSubparameterName() {
		return subparameterName;
	}

	/**
	 * @return the name of the enumeration on which this constraint is dependent
	 */
	public String getPreconditionedEnumerationName() {
		return preconditionedEnumerationName;
	}

	/**
	 * @return the value of the enumeration on which this constraint is dependent
	 */
	public String getPreconditionedEnumerationValue() {
		return preconditionedEnumerationValue;
	}
}
