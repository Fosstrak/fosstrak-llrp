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

package org.fosstrak.llrp.commander.editors;

/**
 * Single entry in Binary Viewer.
 * 
 * @author Haoning Zhang
 * @version 1.0
 */
public class BinarySingleValue {
	
	private String key, value, description;
	private BinaryMessage parent;
	
	/**
	 * Constructor, initialize with one BinaryMessage node
	 * as parent.
	 * 
	 * @param aParent Parent node of this entry
	 */
	public BinarySingleValue(BinaryMessage aParent) {
		parent = aParent;
	}
	
	/**
	 * Get the Parent node as one <code>BinaryMessage</code> instance.
	 * 
	 * @return Parent node
	 */
	public BinaryMessage getParent() {
		return parent;
	}
	
	/**
	 * Get the name of the Key
	 * 
	 * @return Name of the Key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Set the key
	 * 
	 * @param aKey Name of the Key
	 */
	public void setKey(String aKey) {
		key = aKey;
	}

	/**
	 * Get the value of the entry
	 * 
	 * @return Value of the entry
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Set the value of the entry
	 * 
	 * @param aValue Value of the entry
	 */
	public void setValue(String aValue) {
		value = aValue;
	}
	
	/**
	 * Get the description of the entry
	 * 
	 * @return Description of the entry
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * Set the description of the entry
	 * 
	 * @param aDescription Description of the entry
	 */
	public void setDescription(String aDescription) {
		description = aDescription;
	}
}
