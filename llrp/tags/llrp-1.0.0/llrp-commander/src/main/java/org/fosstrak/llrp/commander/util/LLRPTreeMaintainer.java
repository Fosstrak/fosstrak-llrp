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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import org.llrp.ltk.types.*;
import org.llrp.ltkGenerator.generated.FieldDefinition;

/**
 * This class constitutes the interface to llrp message objects. 
 * All modifications to llrp message objects are performed through this class.
 * 
 * Users of this class can register to be notified when changes to the llrp message occur.
 * 
 * This class has a reference to the <code>LLRPMessage</code> object and is not implemented 
 * as a static class, because the implementation of the <code>getParent(...)</code> method
 * requires to know the root of the object tree.
 * 
 * @author Ulrich Etter, ETHZ
 *
 */
public class LLRPTreeMaintainer extends Observable {
	
	private final String EMPTY_STRING = "";
	
	private LLRPMessage root;

	public LLRPTreeMaintainer(LLRPMessage root){
		this.root = root;
	}
	
	/**
	 * Sets the llrp message this <code>LLRPTreeMaintainer</code> shall maintain.
	 * 
	 * @param root the llrp message <code>LLRPTreeMaintainer</code> shall maintain
	 */
	public void setRoot(LLRPMessage root){
		this.root = root;
	}

	/**
	 * Returns the llrp message associated with this <code>LLRPTreeMaintainer</code>.
	 * 
	 * @return the llrp message associated with this <code>LLRPTreeMaintainer</code>.
	 */
	public LLRPMessage getRoot() {
		return root;
	}
    
    /**
     * Sets the given parameter as child of the given message/parameter. 
     * The message/parameter will know the parameter under the given name.
     * 
     * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
     * @param childName the name of the child
     * @param child the parameter that should be set as a child
     */
    public void setChild(Object messageOrParameter, String childName, LLRPParameter child){
		String methodName = "set" + childName;	
		Object[] methodArguments = {child};
		
		for (Method method : messageOrParameter.getClass().getMethods()){
			if (method.getName().equals(methodName)){
				try {
					method.invoke(messageOrParameter, methodArguments);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		setChanged();
		notifyObservers();
    }
    
    /**
     * Adds the given parameter to the given parameter list.
     * 
     * @param list the parameter list to which the child shall be added
     * @param child the parameter to add to the parameter list
     */
    public void addChild(List<LLRPParameter> list, LLRPParameter child){
    	list.add(child);
    	setChanged();
		notifyObservers();
    }
    
    /**
     * Removes the given parameter from the given parameter list.
     * 
     * @param list the parameter list from which the child shall be removed
     * @param child the 
     */
    public void removeChild(List<LLRPParameter> list, LLRPParameter child){
		for (LLRPParameter parameter : list){
			if (parameter == child){
				list.remove(parameter);
				break;
			}
		}
		setChanged();
		notifyObservers();
    }
    
    /**
     * Returns the child with the given name of the given message/parameter.
     * 
     * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
     * @param childName the name of the child.
     * @return the child to the given name, null if not existing.
     */
    public Object getChild(Object messageOrParameter, String childName){
    	Object child = null;
    	String methodName = "get" + childName;
		if (LLRP.canOccurMultipleTimes(getDefinition(messageOrParameter), childName)){
			methodName = methodName + "List";
		}
		try {
			child = messageOrParameter.getClass().getMethod(methodName, new Class[0]).invoke(messageOrParameter, new Object[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	return child;
    }
	
	/**
	 * Returns all children of the given tree element that are not null.
	 * 
	 * @param treeElement either a <code>LLRPMessage</code> or a <code>LLRPParameter</code> 
	 * 		or a <code>List&lt;LLRPParameter&gt;</code>
	 * @return a list of children to a given tree element.
	 */
	public List<Object> getNonNullChildren(Object treeElement){
		List<Object> children = new LinkedList<Object>();
		if (treeElement instanceof LLRPMessage || treeElement instanceof LLRPParameter){
			Object messageOrParameterDefinition = getDefinition(treeElement);
			List<String> childrenNames = LLRP.getParameterAndChoiceNames(messageOrParameterDefinition);
			for (String childName : childrenNames){
				Object o = getChild(treeElement, childName);
				if (o != null){
					children.add(o);
				}
			}
		}
		else if (treeElement instanceof List){
			children = (List<Object>) treeElement;
		}
		return children;
	}
	
	/**
	 * Returns the parent of the given tree element. 
	 * If the given element is the root of the tree,
	 * <code>Null</code> is returned.
	 * 
	 * This implementation searches the whole message object tree
	 * for the given tree element (starting at the root). This is
	 * done, because LTKJava does not provide references from children
	 * to their parents (i.e. from sub-parameters to parameters).
	 * 
	 * @param treeElement either a <code>LLRPMessage</code> or a <code>LLRPParameter</code> 
	 * 		or a <code>List&lt;LLRPParameter&gt;</code>
	 * @return the parent of a given element in the tree, null if it is the root.
	 */
	public Object getParent(Object treeElement){
		return findParent(root, treeElement);
	}
	
	private Object findParent(Object ancestor, Object treeElement){
		Object parent = null;
		if (treeElement instanceof LLRPMessage){
			// message doesn't have a parent
			parent = null;
		}
		else{
			List<Object> children = getNonNullChildren(ancestor);			
			if (children.size() == 0){
				parent = null;
			}
			else {
				for (Object o : children){
						if (o == treeElement){
							parent = ancestor;
							break;
						}
						else{
							parent = findParent(o, treeElement);
							if (parent != null){
								break;
							}
						}
				}
			}
		}
		return parent;
	}
	
	/**
	 * Returns the field with the given name of the given message/parameter
	 * 
	 * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
	 * @param fieldName
	 * @return the field with the given name of the given message/parameter.
	 */
	public LLRPType getField(Object messageOrParameter, String fieldName) {
		String methodName = "get" + fieldName;
		LLRPType field = null;
		try {
			field = (LLRPType) messageOrParameter.getClass().getMethod(methodName, new Class[0]).invoke(messageOrParameter, new Object[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return field;
	}
	
	/**
	 * Sets the field with the given name of the given message/parameter to the given value.
	 * 
	 * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
	 * @param fieldName
	 * @param fieldValue
	 */
	public void setField(Object messageOrParameter, String fieldName, LLRPType fieldValue){
		String methodName = "set" + fieldName;
		Object[] methodArguments = {fieldValue};
		
		for (Method method : messageOrParameter.getClass().getMethods()){
			if (method.getName().equals(methodName)){
				try {
					method.invoke(messageOrParameter, methodArguments);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
		}
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Returns all fields of the given message/parameter.
	 * 
	 * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
	 * @return a list of llrp fields to a given parameter or message.
	 */
	public List<LLRPType> getFields(Object messageOrParameter){
		LinkedList<LLRPType> fields = new LinkedList<LLRPType>();
    	List<FieldDefinition> fieldDefinitions = LLRP.getFieldDefinitions(getDefinition(messageOrParameter));
    	for (int i = 0; i < fieldDefinitions.size(); i++){
			FieldDefinition fieldDefinition = fieldDefinitions.get(i);
			fields.add(getField(messageOrParameter, fieldDefinition.getName()));
		}
		return fields;
    }

	
	/**
	 * Returns <code>true</code> if the given tree element is valid (including all its descendants), and <code>false</code> otherwise.
	 * 
	 * @param treeElement either a <code>LLRPMessage</code> or a <code>LLRPParameter</code> 
	 * 		or a <code>List&lt;LLRPParameter&gt;</code>
	 * @return true if the given tree element is valid, false otherwise.
	 */
	public boolean isValid(Object treeElement){		
		boolean result = isNonRecursivelyValid(treeElement);
		List<Object> children = getNonNullChildren(treeElement);
		for (Object o : children){
			result = result && isValid(o);
		}
		return result;
	}
	
	/**
	 * Returns <code>true</code> if the given tree element is valid (ignoring the validity of its descendants), 
	 * and <code>false</code> otherwise.
	 * 
	 * @param treeElement either a <code>LLRPMessage</code> or a <code>LLRPParameter</code> 
	 * 		or a <code>List&lt;LLRPParameter&gt;</code>
	 * @return true if the given tree element is valid, false otherwise.
	 */
	public boolean isNonRecursivelyValid(Object treeElement){
		if (treeElement instanceof LLRPMessage || treeElement instanceof LLRPParameter){
			return areFieldsValid(treeElement) && areMandatoryParametersPresent(treeElement) && areListsNonRecursivelyValid(treeElement);
		}
		else if (treeElement instanceof List){
			return validateEmptiness((List<LLRPParameter>) treeElement).equals(EMPTY_STRING);
		}
		return false;
	}
	
	/**
	 * Returns all message/parameter descendants of the given tree element which are (non-recursively) invalid 
	 * (including the tree element itself).
	 * 
	 * @param treeElement either a <code>LLRPMessage</code> or a <code>LLRPParameter</code> 
	 * 		or a <code>List&lt;LLRPParameter&gt;</code>
	 * @return a list of invalid <code>LLRPMessage</code>s and <code>LLRPParameter</code>s
	 */
	public List<Object> getNonRecursivelyInvalidMessageOrParameterDescendants(Object treeElement){
		List<Object> result = new LinkedList<Object>();
		if (!isNonRecursivelyValid(treeElement) && !(treeElement instanceof List)){
			result.add(treeElement);
		}
		for (Object child : getNonNullChildren(treeElement)){
			result.addAll(getNonRecursivelyInvalidMessageOrParameterDescendants(child));
		}
		return result;
	}
	
	/**
	 * Checks whether the field with the given name of the given message/parameter is valid.
	 * 
	 * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
	 * @param fieldName
	 * @return an error message if the field is not valid, and an empty string otherwise
	 */
	public String validateField(Object messageOrParameter, String fieldName){
		LLRPType field = getField(messageOrParameter, fieldName);
		if (field == null){
			return LLRPConstraints.NULL_FIELD_ERROR_MESSAGE;
		}
		else{		
			int fieldValue = 0;
			if (field instanceof LLRPNumberType){
				fieldValue = ((LLRPNumberType) field).intValue();
			}
			else if (field instanceof TwoBitField){
				fieldValue = ((TwoBitField) field).intValue();
			}
			else {
				return EMPTY_STRING;
			}
			LLRPRangeConstraint[] constraints = LLRPConstraints.rangeConstraints;
			for (int i = 0; i < constraints.length; i++){
				if (getName(messageOrParameter).equals(constraints[i].getMessageOrParameterName())
						&& fieldName.equals(constraints[i].getFieldName())){
					String enumerationName = constraints[i].getPreconditionedEnumerationName();
					if (enumerationName != null){
						LLRPEnumeration enumeration = (LLRPEnumeration) getField(messageOrParameter, enumerationName);
						if (enumeration != null &&
								enumeration.toString().equals(constraints[i].getPreconditionedEnumerationValue())){
							if (! constraints[i].isSatisfied(fieldValue)){
								return constraints[i].getErrorMessage();
							}
						}
					}
					else {
						if (! constraints[i].isSatisfied(fieldValue)){
							return constraints[i].getErrorMessage();
						}
					}
				}
			}
			return EMPTY_STRING;
		}
	}
	
	/**
	 * Checks whether the child is present when it has to be present.
	 * 
	 * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
	 * @param childName
	 * @return an error message if the child is not present illegally, and an empty string otherwise
	 */
	public String validateChildPresence(Object messageOrParameter, String childName){
		if (messageOrParameter instanceof LLRPMessage || messageOrParameter instanceof LLRPParameter){
			Object messageOrParameterDefinition = getDefinition(messageOrParameter);
			if (getChild(messageOrParameter, childName) == null){
				if (LLRP.mustOccurAtLeastOnce(messageOrParameterDefinition, childName)){
						return LLRPConstraints.MISSING_PARAMETER_ERROR_MESSAGE;
				}
				else {
					
					LLRPPresenceConstraint[] constraints = LLRPConstraints.presenceConstraints;
					for (int i = 0; i < constraints.length; i++){
						if (getName(messageOrParameter).equals(constraints[i].getMessageOrParameterName())
								&& childName.equals(constraints[i].getSubparameterName())){
							String enumerationName = constraints[i].getPreconditionedEnumerationName();
							if (enumerationName != null){
								LLRPEnumeration enumeration = (LLRPEnumeration) getField(messageOrParameter, enumerationName);
								if (enumeration != null &&  
										enumeration.toString().equals(constraints[i].getPreconditionedEnumerationValue())){
									return constraints[i].getErrorMessage();
								}
							}
						}
					}
				
				}
			}
		}
		return EMPTY_STRING;
	}
	
	/**
	 * Checks whether the list is non-empty when it has to be non-empty.
	 * 
	 * @param list
	 * @return an error message if the list is empty illegally, and an empty string otherwise
	 */
	public String validateEmptiness(List<LLRPParameter> list){
		String errorMessage = EMPTY_STRING;
		Object parent = getParent(list);
		Object messageOrParameterDefinition = getDefinition(parent);
		if (LLRP.mustOccurAtLeastOnce(messageOrParameterDefinition, getName(list)) && list.isEmpty()){
			errorMessage = LLRPConstraints.EMPTY_LIST_ERROR_MESSAGE;
		}
		return errorMessage;
	}
	
	/**
	 * Returns <code>true</code> if all fields of the given message/parameter are valid, and <code>false</code> otherwise.
	 * 
	 * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
	 * @return
	 */
	private boolean areFieldsValid(Object messageOrParameter) {
		boolean result = true;
		List<FieldDefinition> fieldDefinitions = LLRP.getFieldDefinitions(getDefinition(messageOrParameter));
		for (FieldDefinition fd : fieldDefinitions){
			if (!validateField(messageOrParameter, fd.getName()).equals(EMPTY_STRING)){
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * Returns <code>true</code> if all mandatory parameters of the given message/parameter are present, and <code>false</code> otherwise.
	 * 
	 * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
	 * @return
	 */
	private boolean areMandatoryParametersPresent(Object messageOrParameter){
		boolean result = true;
		if (messageOrParameter instanceof LLRPMessage || messageOrParameter instanceof LLRPParameter){
			Object messageOrParameterDefinition = getDefinition(messageOrParameter);
			List<String> childrenNames = LLRP.getParameterAndChoiceNames(messageOrParameterDefinition);
			for (String childName : childrenNames){
				if (!validateChildPresence(messageOrParameter, childName).equals(EMPTY_STRING)){
					result = false;
					break;
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns <code>true</code> if all lists of the given message/parameter are (non-recursively) valid, 
	 * and <code>false</code> otherwise.
	 * 
	 * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
	 * @return
	 */
	private boolean areListsNonRecursivelyValid(Object messageOrParameter){
		boolean result = true;
		Object messageOrParameterDefinition = getDefinition(messageOrParameter);
		List<String> childrenNames = LLRP.getParameterAndChoiceNames(messageOrParameterDefinition);
		for (String childName : childrenNames){
			if (LLRP.canOccurMultipleTimes(messageOrParameterDefinition, childName)){
				if (!validateEmptiness((List<LLRPParameter>) getChild(messageOrParameter, childName)).equals(EMPTY_STRING)){
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the name of the given tree element.
	 * 
	 * @param treeElement either a <code>LLRPMessage</code> or a <code>LLRPParameter</code> 
	 * 		or a <code>List&lt;LLRPParameter&gt;</code>
	 * @return the name of a given tree element.
	 */
	public String getName(Object treeElement) {
		String name = EMPTY_STRING;
		if (treeElement instanceof LLRPMessage){
			name = ((LLRPMessage) treeElement).getName();
		}
		else if (treeElement instanceof LLRPParameter){
			name = ((LLRPParameter) treeElement).getName();
		}
		else if (treeElement instanceof List){		
			Object parent = getParent(treeElement);
			Object messageOrParameterDefinition = getDefinition(parent);
			List<String> childrenNames = LLRP.getParameterAndChoiceNames(messageOrParameterDefinition);
			for (String childName : childrenNames){
				Object child = getChild(parent, childName);
				if (child == treeElement){
					name = childName;
					break;
				}
			}
		}
		return name;
	}
	
	/**
	 * Returns the definition of the given message or parameter.
	 * 
	 * @param messageOrParameter either a <code>LLRPMessage</code> or a <code>LLRPParameter</code>
	 * @return either a <code>MessageDefinition</code> or a <code>ParameterDefinition</code>
	 */
	public Object getDefinition(Object messageOrParameter) {
		Object messageOrParameterDefinition = null;
		if (messageOrParameter instanceof LLRPMessage){
			messageOrParameterDefinition = LLRP.getMessageDefinition(getName(messageOrParameter));
		}
		else if (messageOrParameter instanceof LLRPParameter){
			messageOrParameterDefinition = LLRP.getParameterDefinition(getName(messageOrParameter));
		}
		return messageOrParameterDefinition;
	}
}
