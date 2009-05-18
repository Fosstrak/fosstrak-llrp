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

package org.fosstrak.llrp.client;

import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;

/**
 * All exception handlers registered at the AdaptorManagement 
 * need to implement this interface in order to be delivered 
 * with exceptions triggered on the reader level.
 * @author sawielan
 *
 */
public interface LLRPExceptionHandler {
	
	/**
	 * This method will be called asynchronously whenever 
	 * an Exception is triggered.
	 * @param aExceptionType a type-map describing the exception.
	 * @param e the exception itself.
	 * @param aAdapter the adapter that triggered the exception.
	 * @param aReader the reader that triggered the exception.
	 */
	public void postExceptionToGUI(LLRPExceptionHandlerTypeMap aExceptionType, LLRPRuntimeException e, String aAdapter, String aReader);
}
