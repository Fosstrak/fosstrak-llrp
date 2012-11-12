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

package org.fosstrak.llrp.adaptor.config;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.config.type.AdaptorConfiguration;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;

/**
 * default configuration not storing nor loading any configuration.
 * @author sbw
 *
 */
public class DefaultConfiguration extends Configuration {
	
	/** the logger. */
	private static Logger log = Logger.getLogger(DefaultConfiguration.class);

	public DefaultConfiguration(Map<String, Object> readParameters,	Map<String, Object> writeParameters) {
		super(readParameters, writeParameters);
	}

	@Override
	public List<AdaptorConfiguration> getConfiguration() throws LLRPRuntimeException {
		log.debug("default configuration strategy not loading the configuration.");
		return Collections.emptyList();
	}

	@Override
	public void writeConfiguration(List<AdaptorConfiguration> configurations) throws LLRPRuntimeException {
		log.debug("default configuration strategy not storing the configuration.");
	}

}
