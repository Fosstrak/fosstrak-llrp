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

package org.fosstrak.llrp.adaptor.config;

import java.util.List;

/**
 * a prototype class holding all information to create a adaptor.
 * @author sawielan
 *
 */
public class AdaptorConfiguration {
	/** the name of the adaptor. */
	private String adaptorName = null;
	
	/** the ip of the adaptor if remote. */
	private String ip = null;
	
	/** flags whether remote or local. */
	private boolean isLocal = true;
	
	/** the configuration of all the readers of this adaptor. */
	private List<ReaderConfiguration> readerConfigurations = null;
	
	/** the prefix (needed in the configurations. only for parsing.)*/
	private String prefix = null;
	
	/**
	 * constructor for a configuration prototype.
	 * @param adaptorName the name of the adaptor.
	 * @param ip the ip of the adaptor if remote.
	 * @param isLocal flags whether remote or local.
	 * @param prefix the prefix (needed in the configurations. only for parsing.) unless you are not 
	 * writing your own configuration loader it is safe to pass null here.
	 */
	public AdaptorConfiguration(String adaptorName, String ip, boolean isLocal, String prefix) {
		super();
		this.adaptorName = adaptorName;
		this.ip = ip;
		this.isLocal = isLocal;
		this.prefix = prefix;
	}

	/**
	 * returns the name of the adaptor.
	 * @return the name of the adaptor.
	 */
	public String getAdaptorName() {
		return adaptorName;
	}

	/**
	 * sets the name of the adaptor.
	 * @param adaptorName the name of the adaptor.
	 */
	public void setAdaptorName(String adaptorName) {
		this.adaptorName = adaptorName;
	}

	/**
	 * returns the ip of the adaptor if remote.
	 * @return the ip of the adaptor if remote.
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * flags whether remote or local.
	 * @return whether remote or local.
	 */
	public boolean isLocal() {
		return isLocal;
	}
	
	/**
	 * returns the prefix.
	 * @return the prefix.
	 */
	public String getPrefix() {
		return prefix;
	}

	/**
	 * returns the configuration for all the readers of this adaptor.
	 * @return the configuration for all the readers of this adaptor.
	 */
	public List<ReaderConfiguration> getReaderPrototypes() {
		return readerConfigurations;
	}

	/**
	 * sets the configuration for all the readers of this adaptor.
	 * @param readerConfigurations the configuration for all the readers of this adaptor.
	 */
	public void setReaderConfigurations(List<ReaderConfiguration> readerConfigurations) {
		this.readerConfigurations = readerConfigurations;
	}

}
