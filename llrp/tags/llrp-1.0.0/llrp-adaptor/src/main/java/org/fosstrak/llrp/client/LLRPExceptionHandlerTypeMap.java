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

package org.fosstrak.llrp.client;

public enum LLRPExceptionHandlerTypeMap {

	EXCEPTION_ADAPTOR_MANAGEMENT_NOT_INITIALIZED,
	EXCEPTION_ADAPTOR_MANAGEMENT_CONFIG_NOT_STORABLE,
	EXCEPTION_READER_NOT_EXIST,
	EXCEPTION_ADAPTER_NOT_EXIST,
	EXCEPTION_ADAPTOR_ALREADY_EXISTS,
	EXCEPTION_READER_LOST,
	EXCEPTION_ADAPTER_LOST,
	EXCEPTION_MSG_SENDING_ERROR,
	EXCEPTION_NO_READER_CONFIG_MSG,
	EXCEPTION_NO_ROSPEC_MSG,
	EXCEPTION_MSG_SYNTAX_ERROR,
	EXCEPTION_UNKOWN;
}
