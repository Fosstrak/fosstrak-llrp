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

import java.lang.reflect.Constructor;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.config.Configuration;
import org.fosstrak.llrp.adaptor.config.DefaultConfiguration;
import org.fosstrak.llrp.adaptor.config.type.AdaptorConfiguration;
import org.fosstrak.llrp.adaptor.config.type.ReaderConfiguration;
import org.fosstrak.llrp.adaptor.exception.LLRPDuplicateNameException;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.adaptor.queue.QueueEntry;
import org.fosstrak.llrp.client.LLRPExceptionHandler;
import org.fosstrak.llrp.client.LLRPExceptionHandlerTypeMap;
import org.fosstrak.llrp.client.MessageHandler;
import org.llrp.ltk.types.LLRPMessage;

/**
 * The AdaptorManagement handles your adaptors, enqueues LLRPMessages, handles 
 * errors from the reader site and notifies you about incoming LLRPMessages.<br/>
 * <br/>
 * There are some common pitfalls when using the AdaptorManagement:
 * <ul>
 * <li>you must specify the repository where the messages shall be logged to (see example)</li>
 * <li>you must register an exception handler (see example)</li>
 * <li>you must shutdown the AdaptorManagement through the provided 
 * shutdown method. Otherwise the reader connections don't get shutdown properly (see example)</li>
 * </ul>
 * <br/>
 * Below there is some sample-code, how you can use the AdaptorManagement:
 * <p>
 * <code>// create a message handler</code><br/>
 * <code>MessageHandler msgHandler = new MessageHandler();</code><br/>
 * <br/>
 * <code>// create an exception handler</code><br/>
 * <code>ExceptionHandler handler = new ExceptionHandler();</code><br/>
 * <br/>
 * <code>// run the initializer method</code><br/>
 * <code>String readConfig = Utility.findWithFullPath("/readerDefaultConfig.properties");</code><br/>
 * <code>String writeConfig = readConfig;</code><br/>
 * <code>Map<String, Object> config = new HashMap<String, Object> ();</code><br/>
 * <code>config.put(FileStoreConfiguration.KEY_LOADFILEPATH, readConfig);</code><br/>
 * <code>config.put(FileStoreConfiguration.KEY_STOREFILEPATH, writeConfig);
 * <code>String configurationClass = FileStoreConfiguration.class.getCanonicalName();
 * <code>boolean commitChanges = true;</code><br/>
 * <code>AdaptorManagement.getInstance().initialize(config, config, configurationClass, commitChanges, msgHandler, handler);
 * <br/>
 * <code>// now the management should be initialized and ready to be used</code><br/>
 * <br/>
 * <code>// create an adaptor</code><br/>
 * <code>String adaptorName = "myAdaptor";</code><br/>
 * <code>AdaptorManagement.getInstance().define(adaptorName, "localhost");</code><br/>
 * <br/>
 * <code>// create a reader</code><br/>
 * <code>String readerName = "myReader";</code><br/>
 * <code>Adaptor adaptor = AdaptorManagement.getAdaptor(adaptorName);</code><br/>
 * <code>adaptor.define(readerName, "192.168.1.23", 5084, true, true);</code><br/>
 * <br/>
 * <code>//Enqueue some LLRPMessage on the adaptor</code><br/>
 * <code>AdaptorManagement.enqueueLLRPMessage(adaptorName, readerName, message);</code><br/>
 * <br/>
 * <code>// when you shutdown your application call the shutdown method</code><br/>
 * <code>AdaptorManagement.getInstance().shutdown();</code><br/>
 * </p>
 * @author swieland
 *
 */
public class AdaptorManagement {
	
	/** the name for the default local adaptor. */
	public static final String DEFAULT_ADAPTOR_NAME = "DEFAULT";
	
	/** the logger. */
	private static Logger log = Logger.getLogger(AdaptorManagement.class);

	/** the exception handler. */
	private LLRPExceptionHandler exceptionHandler = null;
	
	/** 
	 * if storeConfig is set and commitChanges is true then all 
	 * the changes to the AdaptorManagement are committed to storeConfig.
	 */
	private boolean commitChanges = true;
	
	/** 
	 * flags whether the AdaptorManagement has been initialized or not. 
	 * you cannot initialize it twice!.
	 */
	private boolean initialized = false;
	
	
	/** internal state keeper. if set to true, the first local adaptor gets exported by rmi. */
	private boolean export = false;
	
	/** if there is a severe error in the adaptorManagement this is set to true */
	private static boolean error = false;
	
	/** the exception that discribes the error condition. */
	private static LLRPRuntimeException errorException = null;
	
	/** the error code for the exception handler. */
	private static LLRPExceptionHandlerTypeMap errorType = null;
	
	/**
	 * by default use a no store and no load configuration.
	 */
	private Configuration configLoader = new DefaultConfiguration(null, null);
	
	// we need to distinguish between local and remote adaptors as 
	// for local adaptors we want to be able to store the configuration
	// at all the time.
	
	/** all the worker threads running an adaptor held by the management (local and remote adaptors). */
	private Map<String, AdaptorWorker> workers = new ConcurrentHashMap<String, AdaptorWorker> ();
	
	/** all the worker threads running an adaptor held by the management (local adaptors). */
	private Map<String, AdaptorWorker> localWorkers = new ConcurrentHashMap<String, AdaptorWorker> ();
	
	/** all the worker threads running an adaptor held by the management (remote adaptors). */
	private Map<String, AdaptorWorker> remoteWorkers = new ConcurrentHashMap<String, AdaptorWorker> ();
	
	/** a list of handlers that like to receive all the LLRP messages. */
	private List<MessageHandler> fullHandlers = Collections.synchronizedList(new LinkedList<MessageHandler> ());
	
	/** these handlers would like to receive only certain LLRP Messages. */
	private Map<Class<?>, List<MessageHandler> > partialHandlers = new ConcurrentHashMap<Class<?>, List<MessageHandler> > ();
	
	/**
	 * initializes the AdaptorManagement. Make sure this method is only invoked <strong>once</strong>.
	 * @param readParameters the parameters for the configuration loader as a key value map. see {@link Configuration} for details.
	 * @param writeParameters  the parameters for the configuration writer as a key value map. see {@link Configuration} for details.
	 * @param configurationClass the implementation of the configuration loader/writer to use. see {@link DefaultConfiguration} for an example.
	 * @param commitChanges if storeConfig is set and commitChanges is true then all the changes to the AdaptorManagement are committed to configuration writer.
	 * @param exceptionHandler the exception handler from the GUI (or whatsoever).
	 * @param handler a handler to dispatch the LLRP messages (can be set to null).
	 * @throws LLRPRuntimeException whenever the AdaptorManagement could not be loaded.
	 * @return returns <ul><li>true if initialization has been performed</li><li>false if initialization has already been performed and therefore the process was aborted</li></ul>.
	 */
	public boolean initialize(Map<String, Object> readParameters, Map<String, Object> writeParameters, String configurationClass, boolean commitChanges, LLRPExceptionHandler exceptionHandler, MessageHandler handler) throws LLRPRuntimeException {
		return initialize(readParameters, writeParameters, configurationClass, commitChanges, exceptionHandler, handler, false);
	}
	
	/**
	 * initializes the AdaptorManagement.<strong>DO NOT USE THIS METHOD as long as you know what you are doing (this method instructs with export=true to export the first local adaptor as a server adaptor.</strong> 
	 * @param readParameters the parameters for the configuration loader as a key value map. see {@link Configuration} for details.
	 * @param writeParameters  the parameters for the configuration writer as a key value map. see {@link Configuration} for details.
	 * @param configurationClass the implementation of the configuration loader/writer to use. see {@link DefaultConfiguration} for an example.
	 * @param commitChanges if storeConfig is set and commitChanges is true then all the changes to the AdaptorManagement are committed to configuration writer.
	 * @param exceptionHandler the exception handler from the GUI (or whatsoever).
	 * @param handler a handler to dispatch the LLRP messages (can be set to null).
	 * @param export if the first local adaptor is to be exported by RMI or not.
	 * @throws LLRPRuntimeException whenever the AdaptorManagement could not be loaded.
	 * @return returns <ul><li>true if initialization has been performed</li><li>false if initialization has already been performed and therefore the process was aborted</li></ul>.
	 */
	public boolean initialize(Map<String, Object> readParameters, Map<String, Object> writeParameters, String configurationClass, boolean commitChanges, LLRPExceptionHandler exceptionHandler, MessageHandler handler, boolean export) throws LLRPRuntimeException {
		if (initialized) {
			log.error("You cannot initialize the AdaptorManagement twice!\nuse the getters/setters to perform the requested changes!\nwe will abort now!!!");
			return false;
		}
		
		this.export = export;
		
		configLoader = initializeConfigurationStrategy(readParameters, writeParameters, configurationClass);

		this.commitChanges = commitChanges;
		this.exceptionHandler = exceptionHandler;
		
		if (null != handler) {
			registerFullHandler(handler);
		}
		
		load();
		initialized = true;
		return true;
	}
	
	/**
	 * load the configuration strategy via reflection from the given implementation class.
	 * @param readParameters the read parameters used by the strategy.
	 * @param writeParameters the write parameters used by the strategy.
	 * @param configurationClass the fully qualified name of the class implementing the chosen configuration strategy.
	 * @return an instance of the configuration strategy.
	 * @throws LLRPRuntimeException when the strategy could not be loaded.
	 */
	private Configuration initializeConfigurationStrategy(Map<String, Object> readParameters, Map<String, Object> writeParameters, String configurationClass) throws LLRPRuntimeException {
		try {
			Class<?> clzz = Class.forName(configurationClass);
			Constructor<?> ctor = clzz.getConstructor(Map.class, Map.class);
			Object instance = ctor.newInstance(readParameters, writeParameters);
			return (Configuration) instance;
		} catch (Exception ex) {
			log.error("could not initialize the configuration strategy", ex);
			throw new LLRPRuntimeException(ex);
		}
	}

	/**
	 * flags whether the AdaptorManagement has already been initialized.
	 * @return whether the AdaptorManagement has already been initialized.
	 */
	public boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * resets the management to initial state. 
	 * @throws LLRPRuntimeException if an error occurs during reset.
	 */
	public synchronized void reset() throws LLRPRuntimeException {
		if (!initialized) {
			throw new LLRPRuntimeException("AdaptorManagement is not initialized");
		}
		clearWorkers();
		
		// drop all the handlers
		synchronized (fullHandlers) {
			fullHandlers.clear();
		}
		
		synchronized (partialHandlers) {
			partialHandlers.clear();
		}
		
		load();
		log.debug("finished reset");
	}
	
	/**
	 * loads the whole AdaptorManagement.
	 * @throws LLRPRuntimeException when the configuration could not be loaded from file.
	 */
	private void load() throws LLRPRuntimeException {
		try {
			loadConfiguration();
		} catch (LLRPRuntimeException e) {
			log.error("Could not load the configuration file", e);
			setStatus(true, e, LLRPExceptionHandlerTypeMap.EXCEPTION_ADAPTOR_MANAGEMENT_NOT_INITIALIZED);
			throw e;
		}
	}
	
	/**
	 * commits the adaptor managements internal configuration state as a snapshot to the configuration.
	 */
	public void commit() {
		if (isCommitChanges()) {
			try {
				storeToConfiguration();
			} catch (LLRPRuntimeException e) {
				log.error("could not commit the changes to the configuration file", e);
				setStatus(true, e, LLRPExceptionHandlerTypeMap.EXCEPTION_ADAPTOR_MANAGEMENT_NOT_INITIALIZED);
			}
		}
	}
	
	/**
	 * check whether the AdaptorManagement is OK or not. 
	 * if not, an exception is thrown and reported to the exception handler.
	 */
	public void checkStatus() throws LLRPRuntimeException {
		if (error) {
			postAndThenThrowException(errorException, errorType, "", "");
		}
	}
	
	/**
	 * sets the status of the adaptorManagement.
	 */
	private void setStatus(boolean error, LLRPRuntimeException errorException, LLRPExceptionHandlerTypeMap errorType) {
		AdaptorManagement.error = error;
		AdaptorManagement.errorException = errorException;
		AdaptorManagement.errorType = errorType;
	}
	
	/**
	 * the client leaves the adaptor management. the management makes the 
	 * cleanup.
	 */
	public synchronized void shutdown() {
		log.debug("shutting AdaptorManagement down.");
		
		// first disconnect the local readers.
		disconnectReaders();
		
		// remove the remote readers.
		synchronized (AdaptorManagement.class) {
			for (AdaptorWorker worker : workers.values()) {
				// stop all the workers.
				worker.tearDown();
			}
			// deregister the asynchronous callbacks
			for (AdaptorWorker worker : remoteWorkers.values()) {
				// stop all the workers.
				try {
					worker.getAdaptor().deregisterFromAsynchronous(worker.getCallback());
				} catch (RemoteException e) {
					log.error("an error occured when deregistering from remote adaptor: " + e.getMessage(), e);
				}
			}
		} // synchronized adaptorManagement
	}
	
	/**
	 * remove all the adaptors before loading the new adaptors from configuration file.
	 * @throws LLRPRuntimeException
	 */
	private synchronized void clearWorkers() throws LLRPRuntimeException {
		// remove the workers.
		for (AdaptorWorker worker : workers.values()) {
			try {
				if (worker.getAdaptorIpAddress() == null) {
					// if it is a local adaptor undefine the readers 
					worker.getAdaptor().undefineAll();
				}
				undefine(worker.getAdaptor().getAdaptorName());
			} catch (RemoteException e) {
				log.error("Got exception during cleanup.", e);
			}
		}		
	}
	
	/**
	 * disconnectReaders shuts down all local readers. 
	 */
	public void disconnectReaders() {
		for (AdaptorWorker worker : localWorkers.values()) {
			try {
				worker.getAdaptor().disconnectAll();
			} catch (RemoteException e) {
				log.error("Got remote exception on local adaptor during disconnect.", e);
			} catch (LLRPRuntimeException e) {
				log.error("Got runtime exception during disconnect", e);
			}
		}
	}
	
	/**
	 * tells whether an adaptorName already exists.
	 * @param adaptorName the name of the adaptor to check.
	 * @throws LLRPRuntimeException whever something goes wrong ...
	 * @return true if adaptor exists else false.
	 */
	public boolean containsAdaptor(String adaptorName) throws LLRPRuntimeException {
		checkStatus();
		return workers.containsKey(adaptorName);
	}
	
	/**
	 * checks, whether a given adapter is a local adapter or not.
	 * @param adapterName the name of the adapter to check.
	 * @return true if the adapter is local, false otherwise.
	 * @throws LLRPRuntimeException whenever something goes wrong...
	 */
	public boolean isLocalAdapter(String adapterName) throws LLRPRuntimeException {
		checkStatus();
		return localWorkers.containsKey(adapterName);
	}
	
	private Adaptor getRemoteAdapter(String adaptorName, String address) throws LLRPRuntimeException, RemoteException {
		try {
			// try to get the instance from the remote 
			// adaptor.
			Registry registry = LocateRegistry.getRegistry(address, Constants.registryPort);
			return (Adaptor) registry.lookup(Constants.adaptorNameInRegistry);
		} catch (NotBoundException ex) {
			throw new LLRPRuntimeException("Could not get a handle on the remote adaptor", ex);
		}
	}
	
	/**
	 * adds a new adaptor to the adaptor list.
	 * @param adaptorName the name of the new adaptor.
	 * @param address if you are using a client adaptor you have to provide the address of the server stub.
	 * @throws LLRPRuntimeException when either name already exists or when there occurs an error in adaptor creation.
	 * @throws RemoteException when there is an error during transmition.
	 * @throws NotBoundException when there is no registry available.
	 */
	public synchronized String define(String adaptorName, String address) throws LLRPRuntimeException, RemoteException {
		checkStatus();
		
		boolean createLocalAdapter = address == null;
		
		Adaptor adaptor = null;
		if (createLocalAdapter && isExportLocalAdapterToRMI()) {
			// determine the special hopefully unique server adaptor name
			// change the name of the adaptor to the ip of the current machine.
			final String hostNamePrefix = "server adaptor - ";
			String hostAddress = String.format("unknown ip %d", System.currentTimeMillis());
			try {
				java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
				hostAddress = localMachine.getHostAddress();
			}
			catch (java.net.UnknownHostException uhe) {
				log.error("hmmm, what happened? " + "This should not occur here :-).", uhe);
			}
			adaptorName = hostNamePrefix + hostAddress;
			
		} else if (!createLocalAdapter) {
			adaptor = getRemoteAdapter(adaptorName, address);
			// server adaptor always keeps its name. therefore we rename the adaptor
			log.debug(String.format("adaptor is remote. therefore renaming %s to %s.", adaptorName, adaptor.getAdaptorName()));					
			adaptorName = adaptor.getAdaptorName();
		}
		
		// tests whether there exists already a adaptor of this name
		containsAndThrowExceptionIfExists(adaptorName);
		
		AdaptorCallback cb = null;
		AdaptorWorker worker = null;

		cb = new AdaptorCallback(!createLocalAdapter);
		if (createLocalAdapter) {
			// local case
			adaptor = new AdaptorImpl(adaptorName);
			((AdaptorImpl)adaptor).setAdaptorManagement(this);
			worker = new AdaptorWorker(cb, adaptor);
			worker.setAdaptorIpAddress(null);
			// insertion must take place atomically on both workers and localWorkers
			synchronized (workers) {
				synchronized (localWorkers) {
					containsAndThrowExceptionIfExists(adaptorName);
					localWorkers.put(adaptorName, worker);
					workers.put(adaptorName, worker);
					log.debug("created a new local adaptor '" + adaptorName + "'.");
				}
			}
		} else {
			// remote case
			worker = new AdaptorWorker(cb, adaptor);
			// store the ip address of the remote adaptor.
			worker.setAdaptorIpAddress(address);

			// insertion must take place atomically on both workers and remoteWorkers
			synchronized (workers) {
				synchronized (remoteWorkers) {
					containsAndThrowExceptionIfExists(adaptorName);
					remoteWorkers.put(adaptorName, worker);
					log.debug("created a new client adaptor '" + adaptorName + "' with url '" + address + "'.");
					workers.put(adaptorName, worker);
				}
			}
		}
		registerCallbackOnAdapter(adaptor, cb);
		
		// register the thread.
		new Thread(worker).start();
		
		// if the user requests an export of the adaptor we do this...
		// the adaptor HAS to be local!
		if (createLocalAdapter && isExportLocalAdapterToRMI()) {
			exportLocalAdapterToRMI(adaptor);
		}
			
		commit();
		
		return adaptorName;
	}
	
	private void containsAndThrowExceptionIfExists(String adaptorName) throws LLRPRuntimeException {
		// tests whether there exists already a adaptor of this name
		if (containsAdaptor(adaptorName)) {
			log.error("Adaptor '" + adaptorName + "' already exists!");
			LLRPDuplicateNameException e = new LLRPDuplicateNameException(adaptorName, "Adaptor '" + adaptorName + "' already exists!");						
			postAndThenThrowException(e, LLRPExceptionHandlerTypeMap.EXCEPTION_ADAPTOR_ALREADY_EXISTS, adaptorName, "");
		}		
	}

	private void registerCallbackOnAdapter(Adaptor adaptor, AdaptorCallback cb) {
		try {
			adaptor.registerForAsynchronous(cb);
		} catch (RemoteException e) {
			log.error("Could not register the adapter for asynchronous messages.", e);
		}
	}

	private boolean isExportLocalAdapterToRMI() {
		return export;
	}
	
	private void exportLocalAdapterToRMI(Adaptor adaptor) throws RemoteException {
		// create the new registry
		log.debug("create a registry for the export of the local adaptor.");
		LocateRegistry.createRegistry(Constants.registryPort);
		Registry registry = LocateRegistry.getRegistry(Constants.registryPort);
		
		log.debug("bind the adaptor to the registry");
		try {
			registry.bind(Constants.adaptorNameInRegistry, adaptor);
		} catch (AlreadyBoundException e) {
			// this exception should NEVER occur as we destroy the 
			// registry when we register the new adaptor.
			log.error("THERE WAS A SEVERE ERROR THAT SHOULD NEVER OCCUR!!!", e);
		}		
	}

	/**
	 * removes an adaptor from the adaptor list.
	 * @param adaptorName the name of the adaptor to remove.
	 * @throws LLRPRuntimeException when either the name does not exist or when an internal runtime error occurs.
	 */
	public synchronized void undefine(String adaptorName) throws LLRPRuntimeException {
		checkStatus();
		
		if (!containsAdaptor(adaptorName)) {
			log.error("Adaptor '" + adaptorName + "' does not exist!");
			LLRPRuntimeException e = new LLRPRuntimeException("Adaptor '" + adaptorName + "' does not exist!");
			postAndThenThrowException(e, LLRPExceptionHandlerTypeMap.EXCEPTION_ADAPTER_NOT_EXIST, adaptorName, "");
		}
		
		// remove the adaptor
		localWorkers.remove(adaptorName);
		remoteWorkers.remove(adaptorName);
		AdaptorWorker worker = workers.remove(adaptorName);
		try {
			worker.getAdaptor().deregisterFromAsynchronous(worker.getCallback());
			// stop the worker.
			worker.tearDown();
			
		} catch (RemoteException e) {
			log.error("caught remote exception.", e);
		}
		commit();
	}
	
	/**
	 * returns a list of all the available adaptor names.
	 * @return a list of all the available adaptor names.
	 */
	public List<String> getAdaptorNames() throws LLRPRuntimeException {
		checkStatus();
		
		List<String> adaptorNames = new ArrayList<String> (workers.size());
		
		// make a deep copy (no leakage)
		for (AdaptorWorker worker : workers.values()) {
			try {
				adaptorNames.add(worker.getAdaptor().getAdaptorName());
				worker.cleanConnFailure();
			} catch (RemoteException e) {
				worker.reportConnFailure();
				log.error("could not connect to remote adaptor: " + e.getMessage());
			}
		}
		checkWorkers();

		return adaptorNames;
	}
	
	/**
	 * returns an adaptor to a given adaptorName.
	 * @param adaptorName the name of the requested adaptor.
	 * @return an adaptor to a given adaptorName.
	 * @throws LLRPRuntimeException when the adaptor does not exist.
	 */
	public Adaptor getAdaptor(String adaptorName) throws LLRPRuntimeException {
		checkStatus();
		
		if (!containsAdaptor(adaptorName)) {			
			log.error("Adaptor '" + adaptorName + "' does not exist!");
			LLRPRuntimeException e = new LLRPRuntimeException("Adaptor '" + adaptorName + "' does not exist!");
			postAndThenThrowException(e, LLRPExceptionHandlerTypeMap.EXCEPTION_ADAPTER_NOT_EXIST, adaptorName, "");
		}
		
		return workers.get(adaptorName).getAdaptor();
	}
	
	/**
	 * helper to access the default local adaptor more convenient.
	 * @return the default local adaptor.
	 * @throws LLRPRuntimeException this should never occur!
	 */
	// FIXME: check if we really need to cast explicitly...
	public AdaptorImpl getDefaultAdaptor() throws LLRPRuntimeException {
		checkStatus();
						
		if (!workers.containsKey(DEFAULT_ADAPTOR_NAME)) {
			// create the default adaptor
			try {
				define(DEFAULT_ADAPTOR_NAME, null);
			} catch (Exception e) {
				// these two exceptions only occur in remote adaptors. 
				// therefore we can safely ignore them
				log.error("hmmm, what happened? This should not occur here :-).", e);
			}
		}
		return (AdaptorImpl) getAdaptor(DEFAULT_ADAPTOR_NAME);
	}
	
	/**
	 * you can check whether an adaptor is ready to accept messages.
	 * @param adaptorName the name of the adaptor to check.
	 * @return true when ok, else false.
	 */
	public boolean isReady(String adaptorName) throws LLRPRuntimeException {
		checkStatus();
		
		if (!containsAdaptor(adaptorName)) {			
			log.error("Adaptor '" + adaptorName + "' does not exist!");
			LLRPRuntimeException e = new LLRPRuntimeException("Adaptor '" + adaptorName + "' does not exist!");
			
			postAndThenThrowException(e, LLRPExceptionHandlerTypeMap.EXCEPTION_ADAPTER_NOT_EXIST, adaptorName, "");
		}
		
		return workers.get(adaptorName).isReady();
	}
	
	// --------------------------- message and error handling ---------------------------
	/**
	 * enqueue an LLRPMessage to be sent to a llrp reader. the adaptor will
	 * process the message when ready.
	 * @param adaptorName the name of the adaptor holding the llrp reader.
	 * @param readerName the name of the llrp reader.
	 * @param message the LLRPMessage.
	 * @throws LLRPRuntimeException when the queue of the adaptor is full.
	 */
	public void enqueueLLRPMessage(String adaptorName, String readerName, LLRPMessage message) throws LLRPRuntimeException {
		checkStatus();
		
		AdaptorWorker theWorker = workers.get(adaptorName);
		if (null == theWorker) {
			postAndThenThrowException(new LLRPRuntimeException("Adaptor does not exist"), LLRPExceptionHandlerTypeMap.EXCEPTION_ADAPTER_NOT_EXIST, adaptorName, readerName);
		}
		
		if (!theWorker.isReady()) {
			LLRPRuntimeException e = new LLRPRuntimeException("Queue is full");
			postAndThenThrowException(e, LLRPExceptionHandlerTypeMap.EXCEPTION_READER_LOST, "AdaptorManagement", readerName);
		}
		log.debug("enqueueLLRPMessage(" + adaptorName + ", " + readerName + ")");
		theWorker.enqueue(new QueueEntry(message, readerName, adaptorName));
	}
	
	/**
	 * register a handler that will receive all the incoming messages.
	 * @param handler the handler.
	 */
	public void registerFullHandler(MessageHandler handler) {
		// fullHandlers is a synchronized collection, thus no explicit synchronization required.
		fullHandlers.add(handler);
	}
	
	/**
	 * remove a handler from the full handler list.
	 * @param handler the handler to be removed.
	 */
	public void deregisterFullHandler(MessageHandler handler) {
		// fullHandlers is a synchronized collection, thus no explicit synchronization required.
		fullHandlers.remove(handler);
	}
	
	/**
	 * tests whether a given handler is already registered or not.
	 * @param handler the handler to check for.
	 * @return true if the handler is present, false otherwise.
	 */
	public boolean hasFullHandler(MessageHandler handler) {
		return fullHandlers.contains(handler);
	}
	
	/**
	 * register a handler that will receive only a restricted set of messages.
	 * @param handler the handler.
	 * @param clzz the type of messages that the handler likes to receive (example KEEPALIVE.class).
	 */
	public void registerPartialHandler(MessageHandler handler, Class<?> clzz) {
		synchronized (partialHandlers) {
			List<MessageHandler> handlers = partialHandlers.get(clzz);			
			if (null == handlers) {
				List<MessageHandler> temp = Collections.synchronizedList(new LinkedList<MessageHandler> ());
				handlers = temp;
				partialHandlers.put(clzz, handlers);
			}
			handlers.add(handler);
		}
	}
	
	/**
	 * remove a handler from the handlers list.
	 * @param handler the handler to remove.
	 * @param clzz the class where the handler is registered.
	 */
	public void deregisterPartialHandler(MessageHandler handler, Class<?> clzz) {
		// partialHandlers is synchronized thus retrieval is safe.
		List<MessageHandler> handlers = partialHandlers.get(clzz);
		if (null != handlers) {
			// handlers is a synchronized collection, thus we can safely work on it.
			handlers.remove(handler);
		}
	}
	
	/**
	 * checks whether a given handler is registered at a given selector class.
	 * @param handler the handler to check.
	 * @param clzz the class where to search for the handler.
	 * @return true if the handler is present, false otherwise.
	 */
	public boolean hasPartialHandler(MessageHandler handler, Class<?> clzz) {
		// partialHandlers and its content are synchronized, thus reading on it is safe without explicit synchronization.
		List<MessageHandler> handlers = partialHandlers.get(clzz);
		if (null != handlers) {
			return handlers.contains(handler);
		}
		return false;
	}
	
	/**
	 * dispatches an LLRP message to all the registered full handlers. All the 
	 * handlers that have interest into the class of the message will be 
	 * informed as well.
	 * @param adaptorName the name of the adapter that received the message.
	 * @param readerName the reader that received the message. 
	 * @param message the LLRP message itself.
	 */
	public void dispatchHandlers(String adaptorName, String readerName, 
			LLRPMessage message) {
		
		// handle full handlers...
		for (MessageHandler handler : fullHandlers) {
			handler.handle(adaptorName, readerName, message);
		}
		
		// handle partial handlers
		List<MessageHandler> handlers = partialHandlers.get(message.getClass());
		if (null != handlers) {
			for (MessageHandler handler : handlers) {
				handler.handle(adaptorName, readerName, message);
			}
		}
	}

	/**
	 * posts an exception the the exception handler and then throws the given exception.
	 * @param exceptionType the type of the exception. see {@link LLRPExceptionHandler} for more details.
	 * @param adapterName the name of the adaptor that caused the exception.
	 * @param readerName the name of the reader that caused the exception.
	 * @param e the exception itself.
	 * @throws LLRPRuntimeException the given exception is thrown always.
	 */
	public void postAndThenThrowException(LLRPRuntimeException e, LLRPExceptionHandlerTypeMap exceptionType, String adapterName, String readerName) throws LLRPRuntimeException{
		postException(e, exceptionType, adapterName, readerName);
		throw e;
	}
	
	/**
	 * posts an exception the the exception handler.
	 * @param exceptionType the type of the exception. see {@link LLRPExceptionHandler} for more details.
	 * @param adapterName the name of the adaptor that caused the exception.
	 * @param readerName the name of the reader that caused the exception.
	 * @param e the exception itself.
	 */
	public void postException(LLRPRuntimeException e, LLRPExceptionHandlerTypeMap exceptionType, String adapterName, String readerName) {		
		if (exceptionHandler == null) {
			log.error("ExceptionHandler not set!!!", e);
			return;
		}

		log.debug(String.format("Received error call on callback from '%s'.\nException:\n%s", readerName, e.getMessage(), e));		
		exceptionHandler.postExceptionToGUI(exceptionType, e, adapterName, readerName);
	}
	
	
	// ------------------------------- singleton handling -------------------------------
	
	/** private constructor for singleton. */
	private AdaptorManagement() {}
	
	/** the instance of the singleton. */
	private static AdaptorManagement instance = new AdaptorManagement();
	
	/**
	 * returns the singleton of the AdaptorManagement.
	 * @return the singleton of the AdaptorManagement.
	 */
	public static AdaptorManagement getInstance() {
		return instance;
	}

	
	
	// ------------------------------- default config -------------------------------
	private synchronized void createDefaultConfiguration() throws LLRPRuntimeException {
		// no config -> no changes to commit
		setCommitChanges(false);
		
		// clear the workers 
		clearWorkers();
		
		// create a default adaptor
		try {
			define(DEFAULT_ADAPTOR_NAME, null);
		} catch (RemoteException e) {
			log.error("could not define the default adaptor.", e);
		}
	}
	// ------------------------------- load and store -------------------------------
	
	/**
	 * loads the adaptorManagement configuration from file (holds the adaptors and the readers for the local adaptor).
	 * all the adaptors defined currently get removed!!! the action is atomic, this means that depending on your 
	 * setting, the client might get blocked for a short moment!
	 * @throws LLRPRuntimeException whenever there is an exception during restoring.
	 */
	public synchronized void loadConfiguration() throws LLRPRuntimeException {

		// store the commit mode.
		boolean commitMode = isCommitChanges();
		setCommitChanges(false);
		
		boolean isExported = false;
		synchronized (AdaptorManagement.class) {
			// clear out all available adaptors
			clearWorkers();
			
			List<AdaptorConfiguration> configurations = null;
			try {
				configurations = configLoader.getConfiguration();
			} catch (LLRPRuntimeException e) {
				log.info("could not read the config -> create a default configuration");
				
				createDefaultConfiguration();
				return;
			}
			
			for (AdaptorConfiguration adaptorConfiguration : configurations) {
				
				String adaptorName = adaptorConfiguration.getAdaptorName();
				String adaptorIP = adaptorConfiguration.getIp();
								
				if (adaptorConfiguration.isLocal()) {
					log.debug("Load local Adaptor");
					adaptorIP = null;
				} else {
					log.debug(String.format("Load Remote Adaptor: '%s' on '%s'",
							adaptorName, 
							adaptorConfiguration.getIp()));
				}
				
				boolean adaptorCreated = false;
				try {
					if ((export) && (isExported)) {
						// only export the first adaptor
						isExported = true;
						define(adaptorName, adaptorIP);
					}
					adaptorName = define(adaptorName, adaptorIP);

					adaptorCreated = true;
					log.debug(String.format("adaptor '%s' successfully created", adaptorName));
				} catch (Exception e) {
					log.error(String.format("could not create adaptor '%s': %s", adaptorName,
							e.getMessage()));
				}
				
				// only create the readers when the adaptor has been created successfully
				// and if the adaptor is remote, we just retrieve the readers. 
				if ((adaptorCreated) && (adaptorConfiguration.isLocal())) {
					// get a handle of the adaptor and register all the readers.
					Adaptor adaptor = getAdaptor(adaptorName);
					
					if (adaptorConfiguration.getReaderPrototypes() != null) {
						for (ReaderConfiguration readerConfiguration : adaptorConfiguration.getReaderPrototypes()) {
							
							String readerName = readerConfiguration.getReaderName();
							String readerIp = readerConfiguration.getReaderIp();
							int readerPort = readerConfiguration.getReaderPort();
							boolean readerClientInitiated = readerConfiguration.isReaderClientInitiated();
							boolean connectImmediately = readerConfiguration.isConnectImmediately();
							
							log.debug(String.format("Load llrp reader: '%s' on '%s:%d', clientInitiatedConnection: %b, connectImmediately: %b", 
									readerName, readerIp, readerPort, readerClientInitiated, connectImmediately));
							
							// create the reader
							try {
								// try to establish the connection immediately
								adaptor.define(readerName, readerIp, readerPort, readerClientInitiated, connectImmediately);
								log.debug(String.format("reader '%s' successfully created", readerName));
							} catch (RemoteException e) {
								log.error(String.format("could not create reader '%s'", readerName), e);
							}
						}
					}
				}
			}
		} // synchronized adaptorManagement
		
		// restore the commit mode.
		setCommitChanges(commitMode);
	}
	
	/**
	 * stores the configuration of the adaptor management to file. the remote adaptors get stored and for 
	 * the local adaptor all readers get stored as well.
	 * @throws LLRPRuntimeException whenever there occurs an error during storage.
	 */
	public synchronized void storeToConfiguration() throws LLRPRuntimeException {		
		synchronized (AdaptorManagement.class) {
			List<AdaptorConfiguration> configurations = new LinkedList<AdaptorConfiguration>();
			
			for (String adaptorName : workers.keySet()) {
				String ip = workers.get(adaptorName).getAdaptorIpAddress();
				boolean isLocal = false;
				if (ip == null) {
					isLocal = true;
				}
				configurations.add(new AdaptorConfiguration(adaptorName, ip, isLocal, null));
			}
			
			for (AdaptorConfiguration configuration : configurations) {
				if (configuration.isLocal()) {
 					List<ReaderConfiguration> readerConfigurations = new LinkedList<ReaderConfiguration> ();
 					configuration.setReaderConfigurations(readerConfigurations);
 					// get a handle on the adaptor
 					Adaptor adaptor = getAdaptor(configuration.getAdaptorName());
 					try {
						for (String readerName : adaptor.getReaderNames()) {
							Reader reader = adaptor.getReader(readerName);
							boolean connectImmediately = false;	// somehow this causes bugs with MINA, if we start the reader at startup.
							boolean clientInit = reader.isClientInitiated();
							String ip = reader.getReaderAddress();
							int port = reader.getPort();
							
							readerConfigurations.add(new ReaderConfiguration(readerName, ip, port, clientInit, connectImmediately));
						}
					} catch (RemoteException e) {
						// local configuration therefore we can ignore the remote exception.
						log.error("caugth remote exception on local configuration - humm, something smells here ...", e);
					}
				}
			}
			
			try {
				configLoader.writeConfiguration(configurations);
			} catch (LLRPRuntimeException e) {
				postException(e, LLRPExceptionHandlerTypeMap.EXCEPTION_ADAPTOR_MANAGEMENT_CONFIG_NOT_STORABLE, "", "");
			}
		} // synchronized adaptorManagement
	}
	
	// ------------------------------- getter and setter -------------------------------
	/**
	 * returns the exception handler.
	 * @return the exception handler.
	 */
	public LLRPExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	/**
	 * sets the exception handler.
	 * @param exceptionHandler the exception handler.
	 */
	public void setExceptionHandler(LLRPExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
	}

	/**
	 * flags whether all changes to the AdaptorManagement get reflected to the 
	 * configuration file.
	 * @return true if yes, false otherwise.
	 */
	public boolean isCommitChanges() {
		return commitChanges;
	}

	/**
	 * sets whether all changes to the AdaptorManagement get reflected to the 
	 * configuration file.
	 * @param commitChanges 
	 * <ul>
	 * <li>true then the changes get stored back to the configuration immediately</li>
	 * <li>false the changes are not stored back</li>
	 * </ul>
	 */
	public void setCommitChanges(boolean commitChanges) {
		this.commitChanges = commitChanges;
	}
	
	private synchronized void checkWorkers() {
		LinkedList<AdaptorWorker> error = new LinkedList<AdaptorWorker> ();
		synchronized (workers) {
			synchronized (localWorkers) {
				synchronized (remoteWorkers) {
					for (AdaptorWorker worker : workers.values()) {
						if (!worker.ok()) {
							error.add(worker);
						}
					}
					
					// remove the erroneous
					for (AdaptorWorker worker : error) {
						// remove from all the workers.
						workers.remove(worker);
						remoteWorkers.remove(worker);
						localWorkers.remove(worker);
					}
				}
			}
		}
		commit();
	}
}
