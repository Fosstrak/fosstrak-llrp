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

import org.llrp.ltk.types.LLRPEnumeration;

/**
 * This class states additional LLRP constraints which are not modeled in llrp-1x0-def.xml.
 * 
 * @author Ulrich Etter, ETHZ
 *
 */
public class LLRPConstraints {
	
	/**
	 * Error message that gets shown when a mandatory parameter is not set
	 */
	public final static String MISSING_PARAMETER_ERROR_MESSAGE = "This parameter is mandatory and must be present.";
	
	/**
	 * Error message that gets shown when a field is null
	 */
	public final static String NULL_FIELD_ERROR_MESSAGE = "This field must not be empty.";
	
	/**
	 * Error message that gets shown when a list that must not be empty is empty
	 */
	public final static String EMPTY_LIST_ERROR_MESSAGE = "This list must not be empty.";

	/**
	 * An array of range constraints that are not modeled in llrpdef.xml
	 */
	public static LLRPRangeConstraint[] rangeConstraints = {
		
		new LLRPRangeConstraint(
			"START_ROSPEC", 
			"ROSpecID", 
			new Range[] {
				new Range(1, Integer.MAX_VALUE)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"STOP_ROSPEC", 
			"ROSpecID", 
			new Range[] { 
				new Range(1, Integer.MAX_VALUE)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"ROSpec",
			"ROSpecID",
			new Range[] {
				new Range(1, Integer.MAX_VALUE)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"ROSpec",
			"Priority",
			new Range[] {
				new Range(0, 7)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"GPITriggerValue",
			"GPIPortNum",
			new Range[] {
				new Range(1, Integer.MAX_VALUE)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"InventoryParameterSpec",
			"InventoryParameterSpecID",
			new Range[] {
				new Range(1, Integer.MAX_VALUE) 
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"RFSurveySpecStopTrigger",
			"DurationPeriod",
			new Range[] {
				new Range(1, Integer.MAX_VALUE) 
			},
			"StopTriggerType",
			"Duration"),
			
		new LLRPRangeConstraint(
			"RFSurveySpecStopTrigger",
			"N",
			new Range[] {
				new Range(1, Integer.MAX_VALUE) 
			},
			"StopTriggerType",
			"N_Iterations_Through_Frequency_Range"),
			
		new LLRPRangeConstraint(
			"AccessSpec",
			"AccessSpecID",
			new Range[] {
				new Range(1, Integer.MAX_VALUE)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"GPOWriteData",
			"GPOPortNumber",
			new Range[] {
				new Range(1, Integer.MAX_VALUE)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"GPIPortCurrentState",
			"GPIPortNum",
			new Range[] {
				new Range(1, Integer.MAX_VALUE)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"C1G2TagInventoryMask",
			"MB", 
			new Range[] {
				new Range(1, 3)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"C1G2RFControl",
			"Tari",
			new Range[] {
				new Range(0, 0),
				new Range(6250, 25000)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"C1G2SingulationControl",
			"Session",
			new Range[] {
				new Range(0, 3)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"C1G2TargetTag",
			"MB",
			new Range[] {
				new Range(0, 3)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"C1G2Read",
			"MB",
			new Range[] {
				new Range(0, 3)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"C1G2Write",
			"MB",
			new Range[] {
				new Range(0, 3)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"C1G2BlockErase",
			"MB",
			new Range[] {
				new Range(0, 3)
			},
			null,
			null),
			
		new LLRPRangeConstraint(
			"C1G2BlockWrite",
			"MB",
			new Range[] {
				new Range(0, 3)
			},
			null,
			null)
		
	};
	
	/**
	 * An array of presence constraints that are not modeled in llrpdef.xml
	 */
	public static LLRPPresenceConstraint[] presenceConstraints = {
		
		new LLRPPresenceConstraint(
			"ROSpecStartTrigger",
			"PeriodicTriggerValue",
			"ROSpecStartTriggerType",
			"Periodic"),
			
		new LLRPPresenceConstraint(
			"ROSpecStartTrigger",
			"GPITriggerValue",
			"ROSpecStartTriggerType",
			"GPI"),
			
		new LLRPPresenceConstraint(
			"ROSpecStopTrigger",
			"GPITriggerValue",
			"ROSpecStopTriggerType",
			"GPI_With_Timeout"),	

		new LLRPPresenceConstraint(
			"AISpecStopTrigger",
			"GPITriggerValue",
			"AISpecStopTriggerType",
			"GPI_With_Timeout"),

		new LLRPPresenceConstraint(
			"AISpecStopTrigger",
			"TagObservationTrigger",
			"AISpecStopTriggerType",
			"Tag_Observation")
			
	};
	
	/**
	 * An array of array constraints that are not modeled in llrpdef.xml
	 */
	public static LLRPArrayConstraint[] arrayConstraints = {
		
		new LLRPArrayConstraint(
			"AISpec",
			"AntennaIDs",
			new int[]{0})
			
	};
	
	/**
	 * Returns the range constraint that is specified for the given field, 
	 * or <code>null</code> if there is no such constraint. The constraint is only returned if the given
	 * field matches the constraints precondition.
	 * 
	 * @param messageOrParameter 
	 * @param fieldName 
	 * @param treeMaintainer
	 * @return a range constraint for a given field if exists, null otherwise.
	 */
	public static LLRPRangeConstraint getRangeConstraint(Object messageOrParameter, String fieldName, LLRPTreeMaintainer treeMaintainer){
		for (int i = 0; i < rangeConstraints.length; i++){
			if (treeMaintainer.getName(messageOrParameter).equals(rangeConstraints[i].getMessageOrParameterName())
					&& fieldName.equals(rangeConstraints[i].getFieldName())){
				String enumerationName = rangeConstraints[i].getPreconditionedEnumerationName();
				if (enumerationName == null){
					return rangeConstraints[i];
				}
				else {
					LLRPEnumeration enumeration = (LLRPEnumeration) treeMaintainer.getField(messageOrParameter, enumerationName);
					if (enumeration != null &&
							enumeration.toString().equals(rangeConstraints[i].getPreconditionedEnumerationValue())){
						return rangeConstraints[i];
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Returns the array constraint that is specified for the given field, or <code>null</code> if there is no such constraint.
	 * 
	 * @param messageOrParameter 
	 * @param fieldName 
	 * @param treeMaintainer
	 * @return llrp constraint if there is a constraint, null otherwise.
	 */
	public static LLRPArrayConstraint getArrayConstraint(Object messageOrParameter, String fieldName, LLRPTreeMaintainer treeMaintainer){
		for (int i = 0; i < arrayConstraints.length; i++){
			if (treeMaintainer.getName(messageOrParameter).equals(arrayConstraints[i].getMessageOrParameterName())
					&& fieldName.equals(arrayConstraints[i].getFieldName())){
				return arrayConstraints[i];
			}
		}
		return null;
	}
}
