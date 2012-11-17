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

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.config.FileStoreConfiguration;
import org.fosstrak.llrp.adaptor.exception.LLRPDuplicateNameException;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.client.MessageHandler;
import org.junit.Before;
import org.junit.Test;
import org.llrp.ltk.types.LLRPMessage;

/**
 * this class runs some basic tests on the AdaptorManagement. the tests 
 * cover the AdaptorManagement itself and some basic tests for the 
 * Adaptor. (tests on the adaptor and reader will be covered by a separate test case).
 * @author sawielan
 *
 */
public class AdaptorManagementTest {
	
	public static final String READ_CONFIG = "src/test/config/readerDefaultConfig.properties";
	public static final String WRITE_CONFIG = "src/test/config/readerDefaultConfig.properties";
	

	/** the logger. */
	private static Logger log = Logger.getLogger(AdaptorManagementTest.class);
	
	@Before
	public void setUp() throws Exception {
		// prepare the logger
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.DEBUG);
	}
	
	/**
	 * resets the management. 
	 * @throws Exception whenever there is an error.
	 */
	private void resetManagement() throws Exception {
		AdaptorManagement.getInstance().reset();
	}
	
	/**
	 * initializes the management
	 * @throws Exception whenever there is an error.
	 */
	private void initializeManagement() throws Exception {
		if (!AdaptorManagement.getInstance().isInitialized()) {
			Map<String, Object> config = new HashMap<String, Object> ();
			config.put(FileStoreConfiguration.KEY_LOADFILEPATH, READ_CONFIG);
			config.put(FileStoreConfiguration.KEY_STOREFILEPATH, WRITE_CONFIG);
			String configurationClass = FileStoreConfiguration.class.getCanonicalName();
			
			AdaptorManagement.getInstance().initialize(config, config, configurationClass, false, null, null);
		}
	}
	
	/**
	 * this method tests the initial setup is right. in detail:
	 * <ul>
	 * <li>test whether the default adaptor gets created upon reset.</li>
	 * <li>test that the shortcut for getDefaultAdaptor and getAdaptor(name) yields the same adaptor.</li>
	 * <li>test that the adaptor is empty (contains no readers).</li>
	 * <li>test to create the default adaptor twice (should trigger an exception).</li>
	 * <li>test that getAdaptorNames delivers the adaptor names correctly.</li>
	 * <li>test that the repository gets removed.</li>
	 * <li>test that the repository can be added and retrieved correctly.</li>
	 * </ul>
	 * @throws Exception
	 */
	@Test
	public void testInitialSetup() throws Exception {
		initializeManagement();
		
		AdaptorManagement management = AdaptorManagement.getInstance();
		
		// we do not want the changes to be written back to the configuration file.
		management.setCommitChanges(false);
		
		String defaultAdaptorName = AdaptorManagement.DEFAULT_ADAPTOR_NAME;

		// reset the adaptor management to just the default adaptor (local adaptor).
		resetManagement();
			
		// test whether the default adaptor gets created upon reset. 
		Assert.assertEquals(true, management.containsAdaptor(defaultAdaptorName));
		
		// test that the shortcut for getDefaultAdaptor and getAdaptor(name) yields the same adaptor.
		Adaptor getAdaptor = management.getAdaptor(defaultAdaptorName);
		Adaptor getDefaultAdaptor = management.getDefaultAdaptor();
		Assert.assertEquals(getAdaptor.getAdaptorName(), getDefaultAdaptor.getAdaptorName());
		
		
		
		// test that the adaptor is empty (contains no readers).
		Adaptor adaptor = management.getAdaptor(defaultAdaptorName);
		Assert.assertNotNull(adaptor.getReaderNames());
		Assert.assertEquals(0, adaptor.getReaderNames().size());
		
		
		
		// test to create the default adaptor twice (should trigger an exception).
		try {
			management.define(defaultAdaptorName, null);
			
			throw new Exception("Could define an adaptor with the same name twice!");
		} catch (LLRPDuplicateNameException e) {
			// everything is fine!
		}
		
		
		
		// test that getAdaptorNames delivers the adaptor names correctly.
		Assert.assertNotNull(management.getAdaptorNames());
		Assert.assertEquals(1, management.getAdaptorNames().size());
		Assert.assertEquals(defaultAdaptorName, management.getAdaptorNames().get(0));
			
		
		
		// test that the handler can be added and retrieved correctly.
		MessageHandler handler = new MessageHandler() {

			public void handle(String adaptorName, String readerName,
					LLRPMessage message) {
				// do nothing
			}
			
		};
		
		management.registerFullHandler(handler);
		management.registerPartialHandler(handler, String.class);
		Assert.assertTrue(management.hasFullHandler(handler));
		Assert.assertTrue(management.hasPartialHandler(handler, String.class));
		
		management.deregisterFullHandler(handler);
		management.deregisterPartialHandler(handler, String.class);
		Assert.assertFalse(management.hasFullHandler(handler));
		Assert.assertFalse(management.hasPartialHandler(handler, String.class));
	}
	
	/**
	 * this test creates a remote adaptor through the AdaptorManagement.
	 * @throws Exception
	 */
	@Test
	public void testCreateRemoteAdaptor() throws Exception {
		initializeManagement();
		
		AdaptorManagement management = AdaptorManagement.getInstance();
		
		// we do not want the changes to be written back to the configuration file.
		management.setCommitChanges(false);
		
		resetManagement();
	
		String adaptorName = Constants.ADAPTOR_NAME_IN_REGISTRY;
		
		/**
		 * helper class to create a remote server adaptor running on a thread.
		 * this thread will hold the adaptor accessible through rmi.
		 * @author sawielan
		 *
		 */
		class MyRunner implements Runnable {

			public MyRunner(Adaptor adaptor) {
				try {
					try {
						LocateRegistry.createRegistry(Constants.REGISTRY_PORT);
					} catch (Exception e) {
						// the registry already exists...
					}
					
					Registry r = LocateRegistry.getRegistry(Constants.REGISTRY_PORT);
					r.bind(Constants.ADAPTOR_NAME_IN_REGISTRY, adaptor);					
				} catch (RemoteException e) {
					log.error("remote exception", e);
				} catch (AlreadyBoundException e) {
					log.error("already bound", e);
				}				
			}
			
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						break;
					}
				}
				// deregister the adaptor.
				Registry r;
				try {
					r = LocateRegistry.getRegistry(Constants.REGISTRY_PORT);
					r.unbind(Constants.ADAPTOR_NAME_IN_REGISTRY);
				} catch (RemoteException e) {
					log.error("remote exception", e);
				} catch (NotBoundException e) {
					log.error("not bound exception", e);
				}				
			}
			
		}
		// create an adaptor an register it through rmi
		AdaptorImpl serverAdaptor = new AdaptorImpl(adaptorName);
		Thread thread = new Thread(new MyRunner(serverAdaptor));
		thread.start();
		
		
		// ----------------------------  test part ----------------------------
		try {
			management.getAdaptor(adaptorName);
		} catch (LLRPRuntimeException e) {
			// this is good!
		}
		
		// save the name of the adaptor that might get returned by adaptorManagement
		adaptorName = management.define(adaptorName, null);
		// retrieve the adaptor
		Adaptor adaptor = management.getAdaptor(adaptorName);
		
		Assert.assertNotNull(adaptor.getAdaptorName());
		Assert.assertEquals(adaptorName, adaptor.getAdaptorName());
		
		// stop the thread.
		thread.interrupt();
	}
}
