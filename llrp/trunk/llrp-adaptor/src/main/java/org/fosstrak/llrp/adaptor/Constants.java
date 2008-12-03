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

package org.fosstrak.llrp.adaptor;

/**
 * basic constants.
 * @author sawielan
 *
 */
public class Constants {
	/** the port of our rmi registry. */
	public static final int registryPort = 5556;
	
	/** the name of the llrp adaptor in the rmi registry. */
	public static final String adaptorNameInRegistry = "llrpServerAdaptor";
	
	/** the default port for a llrp reader. */
	public static final int DEFAULT_LLRP_PORT = 5084;
}
