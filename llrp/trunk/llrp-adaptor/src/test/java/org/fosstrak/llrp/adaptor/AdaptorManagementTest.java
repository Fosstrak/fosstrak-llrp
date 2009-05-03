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

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.Adaptor;
import org.fosstrak.llrp.adaptor.AdaptorImpl;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.adaptor.Constants;
import org.fosstrak.llrp.adaptor.exception.LLRPDuplicateNameException;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.Repository;

import junit.framework.TestCase;

/**
 * this class runs some basic tests on the AdaptorManagement. the tests 
 * cover the AdaptorManagement itself and some basic tests for the 
 * Adaptor. (tests on the adaptor and reader will be covered by a separate test case).
 * @author sawielan
 *
 */
public class AdaptorManagementTest extends TestCase {
	
	public static final String READ_CONFIG = "src/test/config/readerDefaultConfig.properties";
	public static final String WRITE_CONFIG = "src/test/config/readerDefaultConfig.properties";
	
	protected void setUp() throws Exception {
		super.setUp();
		
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
			AdaptorManagement.getInstance().initialize(
					READ_CONFIG, WRITE_CONFIG, false, null, null);
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
	public void testInitialSetup() throws Exception {
		initializeManagement();
		
		AdaptorManagement management = AdaptorManagement.getInstance();
		
		// we do not want the changes to be written back to the configuration file.
		management.setCommitChanges(false);
		
		String defaultAdaptorName = AdaptorManagement.DEFAULT_ADAPTOR_NAME;

		// reset the adaptor management to just the default adaptor (local adaptor).
		resetManagement();
			
		// test whether the default adaptor gets created upon reset. 
		assertEquals(true, management.containsAdaptor(defaultAdaptorName));
		
		// test that the shortcut for getDefaultAdaptor and getAdaptor(name) yields the same adaptor.
		Adaptor getAdaptor = management.getAdaptor(defaultAdaptorName);
		Adaptor getDefaultAdaptor = management.getDefaultAdaptor();
		assertEquals(getAdaptor.getAdaptorName(), getDefaultAdaptor.getAdaptorName());
		
		
		
		// test that the adaptor is empty (contains no readers).
		Adaptor adaptor = management.getAdaptor(defaultAdaptorName);
		assertNotNull(adaptor.getReaderNames());
		assertEquals(0, adaptor.getReaderNames().size());
		
		
		
		// test to create the default adaptor twice (should trigger an exception).
		try {
			management.define(defaultAdaptorName, null);
			
			throw new Exception("Could define an adaptor with the same name twice!");
		} catch (LLRPDuplicateNameException e) {
			// everything is fine!
		}
		
		
		
		// test that getAdaptorNames delivers the adaptor names correctly.
		assertNotNull(management.getAdaptorNames());
		assertEquals(1, management.getAdaptorNames().size());
		assertEquals(defaultAdaptorName, management.getAdaptorNames().get(0));

		
		
		// test that the repository gets removed.
		assertNull(management.getRepository());
				
		
		
		// test that the repository can be added and retrieved correctly.
		class TestNotify implements Repository {

			public void clearAll() {
				// TODO Auto-generated method stub
				
			}

			public void close() {
				// TODO Auto-generated method stub
				
			}

			public LLRPMessageItem get(String arg0) {
				// TODO Auto-generated method stub
				return null;
			}

			public void open() {
				// TODO Auto-generated method stub
				
			}

			public void put(LLRPMessageItem arg0) {
				// TODO Auto-generated method stub
				
			}

			public ArrayList<LLRPMessageItem> get(String adaptorName,
					String readerName, int num, boolean content) {
				// TODO Auto-generated method stub
				return null;
			}

			public int count(String adaptor, String reader) {
				// TODO Auto-generated method stub
				return 0;
			}

			
		}
		TestNotify testNotify = new TestNotify();
		management.setRepository(testNotify);
		assertNotNull(management.getRepository());
		assertEquals(testNotify, management.getRepository());
	}
	
	/**
	 * this test creates a remote adaptor through the AdaptorManagement.
	 * @throws Exception
	 */
	public void testCreateRemoteAdaptor() throws Exception {
		initializeManagement();
		
		AdaptorManagement management = AdaptorManagement.getInstance();
		
		// we do not want the changes to be written back to the configuration file.
		management.setCommitChanges(false);
		
		resetManagement();
	
		String adaptorName = Constants.adaptorNameInRegistry;
		
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
						LocateRegistry.createRegistry(Constants.registryPort);
					} catch (Exception e) {
						// the registry already exists...
					}
					
					Registry r = LocateRegistry.getRegistry(Constants.registryPort);
					r.bind(Constants.adaptorNameInRegistry, adaptor);					
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (AlreadyBoundException e) {
					e.printStackTrace();
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
					r = LocateRegistry.getRegistry(Constants.registryPort);
					r.unbind(Constants.adaptorNameInRegistry);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (NotBoundException e) {
					e.printStackTrace();
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
		
		assertNotNull(adaptor.getAdaptorName());
		assertEquals(adaptorName, adaptor.getAdaptorName());
		
		// stop the thread.
		thread.interrupt();
	}
}
