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

package org.fosstrak.llrp.commander.util;

import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.AdaptorManagement;

/**
 * helper class that holds utility routines.
 * @author sawielan
 *
 */
public class Utility {

	/** the logger. */
	private static Logger log = Logger.getLogger(Utility.class);
	
	/**
	 * searches the full system path for this resource.
	 * @param fileName the name of the resource.
	 * @return an absolute path to the resource.
	 */
	public static String findWithFullPath(String fileName) {
		URL url;
		String value = null;
		try {
			URL url1 = AdaptorManagement.class.getResource(fileName);
			if (url1 == null) return null;
			url = org.eclipse.core.runtime.FileLocator.resolve(url1);
			if (url != null) {
				value = url.getFile();	
			}
			log.debug("found resource: " + value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}

}
