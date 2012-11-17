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

	private LLRPExceptionHandlerTypeMap exceptionType = LLRPExceptionHandlerTypeMap.EXCEPTION_UNKOWN;
	
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
	 * constructor with exception message and exception type.
	 * @param message the exception message.
	 * @param e the original exception.
	 * @param exceptionType the exception type.
	 */
	public LLRPRuntimeException(String message, Exception e, LLRPExceptionHandlerTypeMap exceptionType) {
		super(message, e);
		this.exceptionType = exceptionType;
	}
	
	/**
	 * constructor from another exception.
	 * @param e the other exception.
	 */
	public LLRPRuntimeException(Exception e) {
		super(e);
	}
	
	/**
	 * constructor from another exception.
	 * @param message exception message text.
	 * @param e the other exception.
	 */
	public LLRPRuntimeException(String message, Exception e) {
		super(message, e);
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
