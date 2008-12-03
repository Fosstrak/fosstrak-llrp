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
 * This class represents an LLRP range constraint. It specifies that a certain numeric field value
 * must lie within some ranges.
 * 
 * @author Ulrich Etter, ETHZ
 *
 */
public class LLRPRangeConstraint {
	
	private String messageOrParameterName;
	private String fieldName;
	private Range[] ranges;
	
	private String preconditionedEnumerationName;
	private String preconditionedEnumerationValue;
		
	/**
	 * Creates a new range constraint. 
	 * <br/><br/>
	 * <b>Example:</b> <br/>
	 * To create a constraint that specifies that the field <code>DurationPeriod</code> of the 
	 * parameter <code>RFSurveySpecStopTrigger</code> must be greater than 0 if the enumeration
	 * <code>StopTriggerType</code> has value <code>Duration</code>, use the following code: <br/>
	 * <code>
	 * 	new LLRPRangeConstraint(
	 *		"RFSurveySpecStopTrigger",
	 *		"DurationPeriod",
	 *		new Range[] {
	 *			new Range(1, Integer.MAX_VALUE) 
	 *		},
	 *		"StopTriggerType",
	 *		"Duration");
	 * </code>
	 * 
	 * @param messageOrParameterName the name of the message/parameter this constraint is defined for
	 * @param fieldName the name of the field this constraint is defined for
	 * @param ranges an array of ranges; for a value to satisfy a constraint it must lie in
	 * one of this ranges
	 * @param preconditionedEnumerationName the name of the enumeration on which this constraint 
	 * is dependent; use <code>null</code> if this constraint does not depend on any enumeration
	 * @param preconditionedEnumerationValue the value of the enumeration on which this constraint 
	 * is dependent; use <code>null</code> if this constraint does not depend on any enumeration
	 */
	public LLRPRangeConstraint(
			String messageOrParameterName,
			String fieldName,
			Range[] ranges,
			String preconditionedEnumerationName,
			String preconditionedEnumerationValue){
		this.messageOrParameterName = messageOrParameterName;
		this.fieldName = fieldName;
		this.ranges = ranges;
		this.preconditionedEnumerationName = preconditionedEnumerationName;
		this.preconditionedEnumerationValue = preconditionedEnumerationValue;
	}
	
	/**
	 * Checks whether this constraint is satisfied by the given field value.
	 * 
	 * @param fieldValue the field value to validate
	 * @return <code>true</code> if the constraint is satisfied, and <code>false</code> otherwise
	 */
	public boolean isSatisfied(int fieldValue){
		boolean result = false;
		for (int i = 0; i < ranges.length; i++){
			result = result || (ranges[i].getLowerBound() <= fieldValue &&  fieldValue <= ranges[i].getUpperBound());
		}
		return result;
	}
	
	/**
	 * @return the error message associated with this constraint
	 */
	public String getErrorMessage(){
		String result = fieldName + " must be ";
		for (int i = 0; i < ranges.length; i++){
			if (ranges[i].getLowerBound() == Integer.MIN_VALUE){
				result = result + "smaller than " + (ranges[i].getUpperBound() + 1);
			}
			else if (ranges[i].getUpperBound() == Integer.MAX_VALUE){
				result = result + "greater than " + (ranges[i].getLowerBound() - 1);
			}
			else if (ranges[i].getLowerBound() == ranges[i].getUpperBound()){
				result = result + ranges[i].getLowerBound();
			}
			else {
				result = result + "between " + ranges[i].getLowerBound() + " and " + ranges[i].getUpperBound();
			}
			
			if (i < (ranges.length - 1)){
				result = result + " or ";
			}
		}
		if (preconditionedEnumerationName != null){
			result = result + " when " + preconditionedEnumerationName + " = " + preconditionedEnumerationValue;
		}
		result = result + ".";
		return result;
	}
	
	public String getMessageOrParameterName() {
		return messageOrParameterName;
	}
	
	public String getFieldName() {
		return fieldName;
	}

	public String getPreconditionedEnumerationName() {
		return preconditionedEnumerationName;
	}

	public String getPreconditionedEnumerationValue() {
		return preconditionedEnumerationValue;
	}
	
	/**
	 * @return a default value for the field this constraint is specified for
	 */
	public int getDefaultValue() {
		return ranges[0].getLowerBound();
	}
}
