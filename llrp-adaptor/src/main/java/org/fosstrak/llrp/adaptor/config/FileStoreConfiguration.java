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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.adaptor.config.type.AdaptorConfiguration;
import org.fosstrak.llrp.adaptor.config.type.ReaderConfiguration;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.adaptor.util.SortedProperties;

/**
 * The {@link FileStoreConfiguration} acts as a gateway to the configuration file. it 
 * tries to mask away the structure of the configuration file by providing 
 * prototypes for the adaptors and their readers. it provides reading and writing 
 * of configuration files.
 * @author sawielan
 *
 */
public class FileStoreConfiguration extends Configuration {
	
	public static final String KEY_STOREFILEPATH = "storeFilePath";
	public static final String KEY_LOADFILEPATH = "loadFilePath";
	
	/** the logger. */
	private static Logger log = Logger.getLogger(FileStoreConfiguration.class);
	
	/** the properties read from file. */
	private Properties props = null; 
	
	
	/** strings to parse the configuration file. */
	private static final String CFG_SEPARATOR = ".";
	
	private static final String CFG_NBR_ADAPTORS = "numberOfAdaptors";
	private static final String CFG_ADAPTOR_PREFIX = "adaptor";
	private static final String CFG_ADAPTOR_NAME = "name";
	private static final String CFG_ADAPTOR_MODE = "local";
	private static final String CFG_ADAPTOR_IP = "ip";
	
	private static final String CFG_NBR_READERS = "numberOfReaders";
	private static final String CFG_READER_PREFIX = "reader";
	private static final String CFG_READER_NAME = "name";
	private static final String CFG_READER_IP = "ip";
	private static final String CFG_READER_PORT = "port";
	private static final String CFG_READER_INITIATION = "clientInitiated";
	private static final String CFG_READER_CONNECT_IMMEDIATELY = "connectImmediately";
	
	private List<AdaptorConfiguration> adaptorConfigurations = null;
	
	/**
	 * creates a configuration loader.
	 */
	public FileStoreConfiguration(Map<String, Object> readParameters, Map<String, Object> writeParameters) {
		super(readParameters, writeParameters);
	}

	@Override
	public List<AdaptorConfiguration> getConfiguration() throws LLRPRuntimeException {
		if (readParameters == null) {
			throw new IllegalArgumentException("readParameters is null - aborting.");
		}
		return getConfiguration((String) readParameters.get(KEY_LOADFILEPATH));
	}
	
	/**
	 * reads a configuration file and delivers the content in a prototype-form.
	 * @param propertiesFile the properties file to read.
	 * @return the prototypes holding the configuration.
	 * @throws LLRPRuntimeException whenever the configuration file could not be read.
	 */
	private List<AdaptorConfiguration> getConfiguration(String propertiesFile ) throws LLRPRuntimeException {
		props = new Properties();
		// try to load the properties file
		try {
			props.load(new FileInputStream(new File(propertiesFile)));
		} catch (IOException e) {
			log.error("There has been an IO Exception when reading the configuration file " + propertiesFile, e);
			throw new LLRPRuntimeException(e.getMessage(), e);
		}	
		
		adaptorConfigurations = new LinkedList<AdaptorConfiguration>();
		
		int numAdaptors = Integer.parseInt(props.getProperty(CFG_NBR_ADAPTORS));
		// read the adaptor configuration
		log.info("loading adaptors from configuration file " + propertiesFile);
		
		// read all the adaptors.
		for (int i=0; i<numAdaptors; i++) {
			String adaptorPrefix = CFG_ADAPTOR_PREFIX + i + CFG_SEPARATOR;
			// get the adaptor name
			String adaptorName = props.getProperty(adaptorPrefix + CFG_ADAPTOR_NAME);
			boolean isLocal = Boolean.parseBoolean(props.getProperty(adaptorPrefix + CFG_ADAPTOR_MODE));
			String adaptorIP = props.getProperty(adaptorPrefix + CFG_ADAPTOR_IP, null);
			
			log.debug(String.format("read adaptor values: (name:%s, ip:%s, local:%b)", 
					adaptorName, adaptorIP, isLocal));
			
			adaptorConfigurations.add(new AdaptorConfiguration(adaptorName, adaptorIP, isLocal, adaptorPrefix));
		}
		
		getReaderPrototypes();
		log.debug("loaded configuration from file");
		return adaptorConfigurations;
	}
	
	/**
	 * reads the reader configurations from the config file.
	 */
	private void getReaderPrototypes() {
		for (AdaptorConfiguration adaptor : adaptorConfigurations) {
			if (adaptor.isLocal()) {
				List<ReaderConfiguration> readerConfigurations = new LinkedList<ReaderConfiguration>();
				
				// get the number of readers to create
				int numReaders = Integer.parseInt(props.getProperty(adaptor.getPrefix() + CFG_NBR_READERS));
				for (int j=0; j<numReaders; j++) {
					String readerPrefix = adaptor.getPrefix() + CFG_READER_PREFIX + j + CFG_SEPARATOR;
					
					String readerName = props.getProperty(readerPrefix + CFG_READER_NAME);
					String readerIp = props.getProperty(readerPrefix + CFG_READER_IP);
					int readerPort = Integer.parseInt(props.getProperty(readerPrefix + CFG_READER_PORT));
					boolean readerClientInitiated = Boolean.parseBoolean(props.getProperty(readerPrefix + CFG_READER_INITIATION));
					boolean connectImmediately = Boolean.parseBoolean(props.getProperty(readerPrefix + CFG_READER_CONNECT_IMMEDIATELY));
					
					readerConfigurations.add(new ReaderConfiguration(
							readerName, readerIp, readerPort, 
							readerClientInitiated, connectImmediately));
					
					log.debug(String.format("read reader values: (name:%s, ip:%s, port:%d, clientInitiatedConnection: %b, connectImmediately: %b)", 
							readerName, readerIp, readerPort, readerClientInitiated, connectImmediately));
					
					adaptor.setReaderConfigurations(readerConfigurations);
				}
			}
		}
	}


	@Override
	public void writeConfiguration(List<AdaptorConfiguration> configurations) throws LLRPRuntimeException {
		if (writeParameters == null) {
			throw new IllegalArgumentException("writeParameters null - aborting.");
		}
		if (configurations == null) {
			throw new IllegalArgumentException("configuration to write must not be null - aborting.");
		}
		writeConfiguration(configurations, (String) writeParameters.get(KEY_STOREFILEPATH));
	}
	
	/**
	 * writes the configuration given by a list of prototypes to the configuration file.
	 * @param configurations the prototypes to be written to the config-file.
	 * @param propertiesFile the properties file where to store.
	 * @throws LLRPRuntimeException whenever the file could not be written.
	 */
	private void writeConfiguration(List<AdaptorConfiguration> configurations, String propertiesFile) throws LLRPRuntimeException {
		
		Properties props = new SortedProperties();

		log.info("storing adaptors to configuration file " + propertiesFile);
		
		// write the number of adaptors to the properties
		log.debug(String.format("writing property/value (%s,%d)", 
				CFG_NBR_ADAPTORS,
				configurations.size()));
		props.setProperty(CFG_NBR_ADAPTORS, String.format("%s", configurations.size()));
		
		// the first adapter we write is the default adapter
		List<AdaptorConfiguration> others = new LinkedList<AdaptorConfiguration> ();
		AdaptorConfiguration defaultAdaptor = null;
		for (AdaptorConfiguration cfg : configurations) {
			if (AdaptorManagement.DEFAULT_ADAPTOR_NAME.equals(cfg.getAdaptorName())) {
				defaultAdaptor = cfg;
			} else {
				others.add(cfg);
			}
		}
		writeAdapter(defaultAdaptor, props, 0);
		int i=1;
		for (AdaptorConfiguration adaptor : others) {
			writeAdapter(adaptor, props, i);
			i++;		
		}
		
		try {
			props.store(new FileOutputStream(new File(propertiesFile)), null);
		} catch (IOException e) {
			log.error("There has been an IO Exception when writing the configuration file " + propertiesFile, e);
			throw new LLRPRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * writes the configuration of one adapter into the properties file.
	 * @param adaptor the adapter configuration to be written.
	 * @param props the properties file.
	 * @param i the index to use.
	 */
	private void writeAdapter(AdaptorConfiguration adaptor, Properties props, int i) {
		String adaptorPrefix = CFG_ADAPTOR_PREFIX + i + CFG_SEPARATOR;
		
		// write the adaptor name
		log.debug(String.format("writing property/value (%s,%s)", adaptorPrefix + CFG_ADAPTOR_NAME, adaptor.getAdaptorName()));
		props.setProperty(adaptorPrefix + CFG_ADAPTOR_NAME, adaptor.getAdaptorName());
		
		boolean isLocal = false;
		if (adaptor.isLocal()) {
			isLocal = true;
		} else {
			log.debug(String.format("writing property/value (%s,%s)",
					adaptorPrefix + CFG_ADAPTOR_IP,
					adaptor.getIp()));
			props.setProperty(adaptorPrefix + CFG_ADAPTOR_IP, adaptor.getIp());
		}
		log.debug(String.format("writing property/value (%s,%b)",
				adaptorPrefix + CFG_ADAPTOR_MODE,
				isLocal));
		props.setProperty(adaptorPrefix + CFG_ADAPTOR_MODE, String.format("%b", isLocal));
		
		// now store the reader part
		if (isLocal) {
			// store the number of readers to create
			log.debug(String.format("writing property/value (%s,%s)", adaptorPrefix + CFG_NBR_READERS, adaptor.getReaderPrototypes().size()));
			props.setProperty(adaptorPrefix + CFG_NBR_READERS, String.format("%d", adaptor.getReaderPrototypes().size()));
			int j=0;
			for (ReaderConfiguration reader : adaptor.getReaderPrototypes()) {
				
				String readerPrefix = adaptorPrefix + CFG_READER_PREFIX + j + CFG_SEPARATOR;
				j++;
				
				log.debug(String.format("writing property/value (%s,%s)",
						readerPrefix + CFG_READER_NAME,
						reader.getReaderName()));
				props.setProperty(readerPrefix + CFG_READER_NAME, reader.getReaderName());
				
				log.debug(String.format("writing property/value (%s,%s)",
						readerPrefix + CFG_READER_IP,
						reader.getReaderIp()));
				props.setProperty(readerPrefix + CFG_READER_IP, reader.getReaderIp());
				
				log.debug(String.format("writing property/value (%s,%d)",
						readerPrefix + CFG_READER_PORT,
						reader.getReaderPort()));
				props.setProperty(readerPrefix + CFG_READER_PORT, String.format("%d", reader.getReaderPort()));
				
				log.debug(String.format("writing property/value (%s,%b)",
						readerPrefix + CFG_READER_INITIATION,
						reader.isReaderClientInitiated()));
				props.setProperty(readerPrefix + CFG_READER_INITIATION, String.format("%b", reader.isReaderClientInitiated()));
				
				log.debug(String.format("writing property/value (%s,%b)",
						readerPrefix + CFG_READER_CONNECT_IMMEDIATELY,
						true));
				props.setProperty(readerPrefix + CFG_READER_CONNECT_IMMEDIATELY, String.format("%b", reader.isConnectImmediately()));
			}
		}
	}

}
