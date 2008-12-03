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
