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

package org.fosstrak.llrp.adaptor.config.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.adaptor.config.FileStoreConfiguration;
import org.fosstrak.llrp.adaptor.config.type.AdaptorConfiguration;
import org.fosstrak.llrp.adaptor.config.type.ReaderConfiguration;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * small testcase asserting the file store configuration.
 * @author swieland
 *
 */
public class FileStoreConfigurationTest {
	
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	/**
	 * the read parameters must not be null.
	 * @throws LLRPRuntimeException test failure.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetConfigurationEmptyReadParameters() throws LLRPRuntimeException {
		FileStoreConfiguration config = new FileStoreConfiguration(null, null);
		config.getConfiguration();
	}
	
	@Test(expected = LLRPRuntimeException.class)
	public void testGetConfigurationInvalidConfigurationFile() throws LLRPRuntimeException {
		Map<String, Object> readParameters = new HashMap<String, Object> ();
		readParameters.put(FileStoreConfiguration.KEY_LOADFILEPATH, "jfdlskfjdlk");
		FileStoreConfiguration config = new FileStoreConfiguration(readParameters, null);
		config.getConfiguration();
	}
	
	/**
	 * the write parameters must not be null.
	 * @throws LLRPRuntimeException test failure.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWriteConfigurationEmptyWriteParameters() throws LLRPRuntimeException {
		FileStoreConfiguration config = new FileStoreConfiguration(null, null);
		config.writeConfiguration(null);
	}
	
	/**
	 * the write parameters must not be null.
	 * @throws LLRPRuntimeException test failure.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWriteConfigurationEmptyConfiguration() throws LLRPRuntimeException {
		Map<String, Object> writeParameters = new HashMap<String, Object> ();
		FileStoreConfiguration config = new FileStoreConfiguration(null, writeParameters);
		config.writeConfiguration(null);
	}
	
	@Test
	public void testReadWriteLocal() throws LLRPRuntimeException, IOException {
		AdaptorConfiguration defaultAdaptor = new AdaptorConfiguration(AdaptorManagement.DEFAULT_ADAPTOR_NAME, null, true, null);
		
		final String adaptorName = "adaptorName";
		final String prefix = "adaptorPrefix";
		final String ip = "5.5.5.5";
		AdaptorConfiguration adaptor2 = new AdaptorConfiguration(adaptorName, ip, false, prefix);
		// reset the adaptor name in order to complete the test coverage.
		adaptor2.setAdaptorName(adaptorName);

		ReaderConfiguration reader1 = new ReaderConfiguration("reader1", "1.2.3.4", 1234, true, true);
		ReaderConfiguration reader2 = new ReaderConfiguration("reader2", "2.3.4.5", 1234, false, false);

		defaultAdaptor.setReaderConfigurations(Arrays.asList(new ReaderConfiguration[] {reader1, reader2}) );
		adaptor2.setReaderConfigurations(Arrays.asList(new ReaderConfiguration[] {reader1, reader2}) );
		
		// write the nice stuff
		tempFolder.create();
		File storePath = tempFolder.newFile("testFile");
		Map<String, Object> parameters = new HashMap<String, Object> ();
		parameters.put(FileStoreConfiguration.KEY_LOADFILEPATH, storePath.getAbsolutePath());
		parameters.put(FileStoreConfiguration.KEY_STOREFILEPATH, storePath.getAbsolutePath());
		FileStoreConfiguration config = new FileStoreConfiguration(parameters, parameters);
		
		config.writeConfiguration(Arrays.asList(new AdaptorConfiguration[] {defaultAdaptor, adaptor2} ));
		
		// and read it again.
		List<AdaptorConfiguration> result = config.getConfiguration();
		Assert.assertNotNull(result);
	}
	
	@Test(expected = LLRPRuntimeException.class)
	public void testWriteToNonExistingFile() throws LLRPRuntimeException, IOException {

		AdaptorConfiguration defaultAdaptor = new AdaptorConfiguration(AdaptorManagement.DEFAULT_ADAPTOR_NAME, null, true, null);
		ReaderConfiguration reader1 = new ReaderConfiguration("reader1", "1.2.3.4", 1234, true, true);
		defaultAdaptor.setReaderConfigurations(Arrays.asList(new ReaderConfiguration[] {reader1 }) );
		
		// write the nice stuff
		tempFolder.create();
		File storePath = tempFolder.newFile("testFile");
		Map<String, Object> parameters = new HashMap<String, Object> ();
		parameters.put(FileStoreConfiguration.KEY_STOREFILEPATH, "http://" + storePath.getAbsolutePath());
		FileStoreConfiguration config = new FileStoreConfiguration(parameters, parameters);
		
		// must trigger a LLRPRuntimeException.
		config.writeConfiguration(Arrays.asList(new AdaptorConfiguration[] {defaultAdaptor} ));
	}
}
