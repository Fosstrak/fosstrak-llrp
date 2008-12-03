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
