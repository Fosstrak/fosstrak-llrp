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

import org.fosstrak.llrp.client.LLRPExceptionHandlerTypeMap;

/**
 * Exception class. This class acts as the top level exception for 
 * the llrp exception messages.
 * @author sawielan
 *
 */
public class LLRPRuntimeException extends Exception {
	
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = -174158847556608407L;

	protected LLRPExceptionHandlerTypeMap exceptionType = LLRPExceptionHandlerTypeMap.EXCEPTION_UNKOWN;
	
	/**
	 * default constructor.
	 */
	public LLRPRuntimeException() {
		super();
	}
	
	/**
	 * constructor with exception message.
	 * @param message the exception message.
	 */
	public LLRPRuntimeException(String message) {
		super(message);
	}
	
	/**
	 * constructor with exception message and exception type.
	 * @param message the exception message.
	 * @param exceptionType the exception type.
	 */
	public LLRPRuntimeException(String message, LLRPExceptionHandlerTypeMap exceptionType) {
		super(message);
		this.exceptionType = exceptionType;
	}
	
	/**
	 * sets the exception type.
	 * @param exceptionType the exception type.
	 */
	public void setExceptionType(LLRPExceptionHandlerTypeMap exceptionType) {
		this.exceptionType = exceptionType;
	}
	
	/**
	 * gets the exception type.
	 * @return the exception type.
	 */
	public LLRPExceptionHandlerTypeMap getExceptionType() {
		return exceptionType;
	}
}
