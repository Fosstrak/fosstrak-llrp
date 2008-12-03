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

package org.fosstrak.llrp.commander.util;

import java.io.IOException;
import java.net.URL;

import org.fosstrak.llrp.adaptor.AdaptorManagement;

public class Utility {

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
			System.out.println("found resource: " + value);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return value;
	}

}
