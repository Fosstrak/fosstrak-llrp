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

import java.util.List;
import java.util.Map;

import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;

/**
 * the adaptor management is able to use different configuration strategies in order 
 * to store and/or retrieve a configuration. this interface exports the 
 * required methods. 
 * @author swieland
 *
 */
public abstract class Configuration {
	
	protected final Map<String, Object> readParameters;
	protected final Map<String, Object> writeParameters;
	
	public Configuration(Map<String, Object> readParameters, Map<String, Object> writeParameters) {
		this.readParameters = readParameters;
		this.writeParameters = writeParameters;
	}
	
	/**
	 * load a configuration.
	 * @return the configuration loaded by this configuration loader.
	 * @throws LLRPRuntimeException upon issues while loading the configuration.
	 */
	public abstract List<AdaptorConfiguration> getConfiguration() throws LLRPRuntimeException;
	
	/**
	 * persist a given configuration.
	 * @param configurations the configuration to be persisted.
	 * @throws LLRPRuntimeException upon issues while writing the configuration.
	 */
	public abstract void writeConfiguration(List<AdaptorConfiguration> configurations) throws LLRPRuntimeException;
}
