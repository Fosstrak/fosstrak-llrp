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

package org.fosstrak.llrp.adaptor.exception;

/**
 * exception class that can be used when there is a duplicate name 
 * subscription.
 * @author sawielan
 *
 */
public class LLRPDuplicateNameException extends LLRPRuntimeException {
	
	/**
	 * serial id. 
	 */
	private static final long serialVersionUID = -5236575385965206938L;
	
	/** the name that caused the exception. */
	private String name = null;
	
	/**
	 * default constructor.
	 */
	public LLRPDuplicateNameException() {
		super();
	}
	
	/**
	 * constructor with exception message.
	 * @param message the exception.
	 */
	public LLRPDuplicateNameException(String message) {
		super(message);
	}
	
	/**
	 * constructor for a detailed exception.
	 * @param name the name that caused the exception.
	 * @param message the exception.
	 */
	public LLRPDuplicateNameException(String name, String message) {
		super(message);
		this.name = name;
	}
	
	/**
	 * returns the name that caused the exception.
	 * @return the name that caused the exception.
	 */
	public String getDuplicateName() {
		return this.getName();
	}

	/**
	 * get the duplicate name.
	 * @return the duplicate name.
	 */
	public String getName() {
		return name;
	}
}
