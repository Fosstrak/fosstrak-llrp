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

package org.fosstrak.llrp.adaptor;

/**
 * basic constants.
 * @author sawielan
 *
 */
public final class Constants {
		
	/** the port of our rmi registry. */
	public static final int REGISTRY_PORT = 5556;
	
	/** the name of the llrp adaptor in the rmi registry. */
	public static final String ADAPTOR_NAME_IN_REGISTRY = "llrpServerAdaptor";
	
	/** the default port for a llrp reader. */
	public static final int DEFAULT_LLRP_PORT = 5084;

	private Constants() {		
	}
}
