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
	protected String name = null;
	
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
		return this.name;
	}
}
