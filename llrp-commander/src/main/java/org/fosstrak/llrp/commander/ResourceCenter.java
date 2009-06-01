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

package org.fosstrak.llrp.commander;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.client.LLRPExceptionHandlerTypeMap;
import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.MessageHandler;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.commander.preferences.PreferenceConstants;
import org.fosstrak.llrp.commander.repository.JavaDBRepository;
import org.fosstrak.llrp.commander.repository.MessageModel;
import org.fosstrak.llrp.commander.util.LLRP;
import org.fosstrak.llrp.commander.util.MessageBoxRefresh;
import org.fosstrak.llrp.commander.util.Utility;
import org.fosstrak.llrp.commander.views.MessageboxView;
import org.fosstrak.llrp.commander.views.ReaderExplorerView;
import org.jdom.Document;
import org.llrp.ltk.exceptions.InvalidLLRPMessageException;
import org.llrp.ltk.generated.LLRPMessageFactory;
import org.llrp.ltk.generated.parameters.LLRPStatus;
import org.llrp.ltk.types.LLRPMessage;

/**
 * This single access point for lower level resources, like Reader and Messages, from
 * the GUI side. The class apply the <strong>Singleton</strong> pattern.
 *
 * @author Haoning Zhang
 * @author sawielan
 * @version 1.0
 */
public class ResourceCenter {

	/**
	 * Maximal message retrieval number
	 */
	public final static int GET_MAX_MESSAGES = 25;
	
	/**
	 * Default Eclipse Project for storing editable messages
	 */
	public final static String DEFAULT_ECLIPSE_PROJECT = "LLRP_CMDR";
	
	/**
	 * Default reader configuration file name
	 */
	public final static String DEFAULT_READER_DEF_FILENAME = "readers.xml";
	
	/**
	 * Pre-built folder, for opened incoming messages
	 */
	public final static String REPO_SUBFOLDER = "Temporary";
	
	/**
	 * Pre-built folder, for editable outgoing messages
	 */
	public final static String DRAFT_SUBFOLDER = "Draft";
	
	/**
	 * Pre-built folder, for messages template (samples)
	 */
	public final static String SAMPLE_SUBFOLDER = "Sample";
	
	/** folder storing the configuration files. */
	public final static String CONFIG_SUBFOLDER = "cfg";
	
	/** the name of the configuration file for the reader configuration. */
	public final static String RDR_CFG_FILE = "rdrCfg.properties";
	
	public final static String DB_SUBFOLDER = "db"; 
	
	private static ResourceCenter instance;
	
	private JavaDBRepository repo;
	
	private static Logger log = Logger.getLogger(ResourceCenter.class);
	
	private MessageModel messageModel;
	
	private String eclipseProjectName;
	
	private String readerDefinitionFilename;
	
	private ExceptionHandler exceptionHandler;
	
	private HashMap<String, String> readerConfigMap;
	
	private HashMap<String, String> readerROSpecMap;
	
	private ReaderExplorerView readerExplorerView;
	
	/**
	 * Only store meta data, without XML content, to save the memory
	 */
	private ArrayList<LLRPMessageItem> messageList;
	
	/** the worker thread that refreshes the message box periodically. */
	private MessageBoxRefresh messageBoxRefresh = null;
	
	/** flags whether the adaptor management has been initialized or not. */
	private boolean initialized = false;
	
	/** use an image cache in order not to recreate images over and over again.*/
	private Map<String, Image> imageCache = new HashMap<String, Image> ();
	
    /**
     * Private Constructor, internally called.
     */
	private ResourceCenter() {

		// load class LLRP
		LLRP.getLlrpDefintion();
			
		setEclipseProjectName(DEFAULT_ECLIPSE_PROJECT);
		setReaderDefinitionFilename(DEFAULT_READER_DEF_FILENAME);
		
		messageList = new ArrayList<LLRPMessageItem>();
		
		messageModel = new MessageModel();
		
		readerConfigMap = new HashMap<String, String>();
		readerROSpecMap = new HashMap<String, String>();
	}
	
	/**
	 * helper to initialize the adaptor management at the right moment.
	 */
	public void initializeAdaptorMgmt() {
		if (initialized) {
			log.info("adaptor management already initialized");
			return;
		}
		
		IProject project = getEclipseProject();
		// refresh the workspace...
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		
		// check if the configuration folder exists.
		IFolder configFolder = project.getFolder(
				ResourceCenter.CONFIG_SUBFOLDER);
		if (!configFolder.exists()) {
			try {
				log.info("create new config folder...");
				configFolder.create(true, true, null);
				log.info("created config folder.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// check if the reader configuration exists.
		IFile cfg = configFolder.getFile(
				ResourceCenter.RDR_CFG_FILE
				);
		
		if (cfg.exists()) {
			log.info("found configuration file - good.");
		} else {
			log.info("reader configuration file missing. create new...");
			String defaultCFG = Utility.findWithFullPath(
					"/readerDefaultConfig.properties");
			
			try {
				// copy the file
				InputStream in = new FileInputStream(new File(defaultCFG));					
				cfg.create(in, false, null);
				in.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// create our message handler
		MessageHandler handler = new MessageHandler() {

			public void handle(String adapter, String reader, LLRPMessage msg) {
				LLRPMessageItem item = new LLRPMessageItem();
				item.setAdapter(adapter);
				item.setReader(reader);
				
				String msgName = msg.getName();
				item.setMessageType(msgName);
				
				// if the message contains a "LLRPStatus" parameter, set the status code (otherwise use empty string)
				String statusCode = "";
				try {
					Method getLLRPStatusMethod = msg.getClass().getMethod("getLLRPStatus", new Class[0]);
					LLRPStatus status = (LLRPStatus) getLLRPStatusMethod.invoke(msg, new Object[0]);
					statusCode = status.getStatusCode().toString();
				} catch (Exception e) {
					// do nothing
				} 
				item.setStatusCode(statusCode);
				
				// store the xml string to the repository
				try {
					item.setContent(msg.toXMLString());
				} catch (InvalidLLRPMessageException e) {
					e.printStackTrace();
				}
				
				getRepository().put(item);
			}
		};
		
		AdaptorManagement.getInstance().registerFullHandler(handler);
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		String readConfig = myWorkspaceRoot.getLocation().toString() + 
				cfg.getFullPath().toString();
		
		String storeConfig = readConfig;
		boolean commitChanges = true;
		try {
			AdaptorManagement.getInstance().initialize(
					readConfig, storeConfig, commitChanges, null, null);
		} catch (LLRPRuntimeException e) {
			e.printStackTrace();
		}
		
		initialized = true;
	}	
	
	/**
	 * Get the Message model
	 * 
	 * @return Message Model
	 */
	public MessageModel getMessageModel() {
		return messageModel;
	}
	
    /**
     * Return the only instance of this class, call the Constructor in its first call.
     *
     */
	public static ResourceCenter getInstance() {
		if (null == instance) {
			instance = new ResourceCenter();
		}
		return instance;
	}
	
	/**
	 * Add LLRP Message Item to Repository and Content Provider.
	 * 
	 * @param aNewMessage Incoming LLRP Message
	 */
	public void addMessage(LLRPMessageItem aNewMessage) {
		if (null == aNewMessage) {
			return;
		}
		
		//Add the Repository
		repo.put(aNewMessage);
	}
	
	/**
	 * Get the message meta data list.
	 * 
	 * @return Message meta data list 
	 */
	public ArrayList<LLRPMessageItem> getMessageMetadataList() {
		return messageList;
	}
	
	/**
	 * Add new message meta data item into list
	 * @param aNewMessage New message meta data item
	 */
	public void addToMessageMetadataList(LLRPMessageItem aNewMessage) {
		//Remove XML Content to save the memory, then put into the 1st place of the list
		aNewMessage.setContent("");
		messageList.add(aNewMessage);
		
		// flag the refresher to refresh the messagebox 
		if (messageBoxRefresh != null) {
			messageBoxRefresh.setDirty();
		}
	}
	
	/**
	 * Clear all data in meta data list
	 */
	public void clearMessageMetadataList() {
		messageList.clear();
	}
	
	/**
	 * Get LLRP XML content by Message ID
	 * @param aMsgId Message ID
	 * @return LLRP XML Content
	 */
	public String getMessageContent(String aMsgId) {
		
		if (null == aMsgId) {
			return null;
		}
		
		LLRPMessageItem msg = repo.get(aMsgId);
		
		return msg.getContent().equals("") ? null : msg.getContent();
	}
	
	/**
	 * Get the Repository interface.
	 * 
	 * @return Repository interface
	 */
	public Repository getRepository() {
		if (repo == null) {
			log.debug("open/create new repository");
			IProject project = getEclipseProject();
			// refresh the workspace...
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e1) {
				e1.printStackTrace();
			}
			
			IWorkspaceRoot myWorkspaceRoot = 
				ResourcesPlugin.getWorkspace().getRoot();
			IFolder dbFolder = project.getFolder(
					ResourceCenter.DB_SUBFOLDER);
			
			String dbLocation = myWorkspaceRoot.getLocation().toString() + 
					dbFolder.getFullPath().toString() + "/";
			
			log.info("using db location: " + dbLocation);
			repo = new JavaDBRepository(dbLocation);
			repo.open();
		}
		return repo;
	}
	
	/**
	 * @return wipe the repository on startup.
	 */
	public boolean wipeRepositoryOnStartup() {
		IPreferenceStore store = LLRPPlugin.getDefault().getPreferenceStore();
		return store.getBoolean(PreferenceConstants.P_WIPE_DB_ON_STARTUP);
	}
	
	/**
	 * Get Eclipse Project Name
	 * @return Eclipse Project Name
	 */
	public String getEclipseProjectName() {
		return eclipseProjectName;
	}
	
	/**
	 * Set Eclipse Project Name
	 * @param aName Eclipse Project Name
	 */
	public void setEclipseProjectName(String aName) {
		eclipseProjectName = aName;
	}
	
	/**
	 * Get Eclipse <code>IProject</code> instance
	 * @return Eclipse IProject instance
	 */
	public IProject getEclipseProject() {
		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject myWebProject = myWorkspaceRoot.getProject(getEclipseProjectName());
		
		if (!myWebProject.exists()) {
			log.info("Project " + getEclipseProjectName() + " doesn't exists!");
			return null;
		}
		
		try {
			if (myWebProject.exists() && !myWebProject.isOpen()) {
				myWebProject.open(null);
			}
		} catch (CoreException ce) {
			log.debug("could not open project " + ce.getMessage());
		}
		
		return myWebProject;
	}
	
	/**
	 * Get current editing file name
	 * @return Current editing file name
	 */
	public String getCurrentFileName() {
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		String fileName = page.getActiveEditor().getEditorInput().getName();
		
		return fileName;
	}
	
	/**
	 * Get current editing XML content
	 * @return Current editing XML content
	 */
	public String getCurrentFile() {
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		IFileEditorInput input = (IFileEditorInput) page.getActiveEditor().getEditorInput();
		
		StringBuffer aXMLContent = new StringBuffer();
		
		try {
			InputStream is = input.getFile().getContents();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(is));

			String line;
			while ((line = reader.readLine()) != null) {
				aXMLContent.append(line);
			}

			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return aXMLContent.toString();
	}
	
	/**
	 * Helper function. Generate <code>LLRPMessage</code> instance
	 * by XML content.
	 * If the exchange by LTKJava hold errors, return null.
	 * 
	 * @param aXMLFileContent XML file content
	 * @return LLRPMessage instance.
	 */
	public LLRPMessage generateLLRPMessage(String aXMLFileContent) {
		
		LLRPMessage message = null;
		
		log.info("Start generating LLRPMessage...");
		
		try {
			Document doc = new org.jdom.input.SAXBuilder()
					.build(new StringReader(aXMLFileContent));
			
			message = LLRPMessageFactory.createLLRPMessage(doc);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		log.info("LLRPMessage successfully generated.");
		
		return message;
	}
	
	/**
	 * Send LLRP Message.
	 * 
	 * @param aAdapterName Adapter Logical Name
	 * @param aReaderName Reader Logical Name
	 * @param aMessage LLRPMessage instance
	 * @param aComment User Input Comments
	 */
	public void sendMessage(String aAdapterName, String aReaderName, LLRPMessage aMessage, String aComment) {
		try {
			String msgName = aMessage.getName();
			
			LLRPMessageItem  item = new LLRPMessageItem();
			item.setMark(LLRPMessageItem.MARK_OUTGOING);
			item.setAdapter(aAdapterName);
			item.setReader(aReaderName);
			item.setContent(aMessage.toXMLString());
			item.setMessageType(msgName);
			item.setComment(aComment);
			
			this.addMessage(item);
			
			AdaptorManagement.getInstance().enqueueLLRPMessage(aAdapterName, aReaderName, aMessage);
						
		} catch (LLRPRuntimeException e) {
			e.printStackTrace();
		} catch (InvalidLLRPMessageException ive) {
			ive.printStackTrace();
		}
	}
	
	/**
	 * Disconnect all readers.
	 */
	public void disconnectAllReaders() {
		log.info("Disconnecting all readers...");
		AdaptorManagement.getInstance().disconnectReaders();
		
		AdaptorManagement.getInstance().shutdown();
	}

	/**
	 * Get Reader definition filename
	 * @return Reader definition filename
	 */
	public String getReaderDefinitionFilename() {
		return readerDefinitionFilename;
	}

	/**
	 * Set Reader definition filename
	 * @param aReaderDefinitionFilename Reader definition filename
	 */
	public void setReaderDefinitionFilename(String aReaderDefinitionFilename) {
		readerDefinitionFilename = aReaderDefinitionFilename;
	}
		
	/**
	 * Get <code>Image</code> from icon folder
	 * @param aFilename Image filename
	 * @return Image instance
	 */
	public Image getImage(String aFilename) {
		synchronized (imageCache) {
			if (imageCache.containsKey(aFilename)) {
				return imageCache.get(aFilename);
			}
			log.debug("Generate Image:" + "icons/" + aFilename);
			Image img = LLRPPlugin.getImageDescriptor("icons/" + aFilename).createImage();
			imageCache.put(aFilename, img);
			return img;
		}
	}
	
	/**
	 * Get <code>ImageDescriptor</code> from icon folder
	 * @param aFilename Image filename
	 * @return ImageDescriptor instance
	 */
	public ImageDescriptor getImageDescriptor(String aFilename) {
		log.debug("Generate ImageDescriptor:" + "icons/" + aFilename);
		return LLRPPlugin.getImageDescriptor("icons/" + aFilename);
	}
	
	public void setExceptionHandler(ExceptionHandler aHandler) {
		exceptionHandler = aHandler;
		AdaptorManagement.getInstance().setExceptionHandler(aHandler);
	}
	
	public void postExceptionToGUI(LLRPExceptionHandlerTypeMap aExceptionType, String aAdapter, String aReader) {
		if (null == exceptionHandler) {
			return;
		}
		
		exceptionHandler.postExceptionToGUI(aExceptionType, null, aAdapter, aReader);
		
	}
	
	/**
	 * set the message box view in the message box refresh thread. If the 
	 * refresh thread is not started yet, a new instance is generated and the 
	 * thread is started, otherwise the new message box view is registered on 
	 * the running thread.
	 * @param aMessagebox the message box to be set.
	 */
	public void setMessageboxView(MessageboxView aMessagebox) {
		if (messageBoxRefresh == null) {
			messageBoxRefresh = new MessageBoxRefresh(aMessagebox);
			new Thread(messageBoxRefresh).start();
		} else {
			messageBoxRefresh.setMessageBox(aMessagebox);
		}
	}

	/**
	 * @param readerExplorerView the readerExplorerView to set
	 */
	public void setReaderExplorerView(ReaderExplorerView readerExplorerView) {
		this.readerExplorerView = readerExplorerView;
	}


	/**
	 * @return the readerExplorerView
	 */
	public ReaderExplorerView getReaderExplorerView() {
		return readerExplorerView;
	}
	
	public void addReaderConfig(String aAdapterName, String aReaderName, String aMessageID) {
		readerConfigMap.put(aAdapterName + aReaderName, aMessageID);
	}
	
	public void removeReaderConfig(String aAdapterName, String aReaderName) {
		readerConfigMap.remove(aAdapterName + aReaderName);
	}
	
	public String getReaderConfigMsgId(String aAdapterName, String aReaderName) {
		String result = readerConfigMap.get(aAdapterName + aReaderName);
		return result;
	}
	
	public void addReaderROSpec(String aAdapterName, String aReaderName, String aMessageID) {
		readerROSpecMap.put(aAdapterName + aReaderName, aMessageID);
	}
	
	public void removeReaderROSpec(String aAdapterName, String aReaderName) {
		readerROSpecMap.remove(aAdapterName + aReaderName);
	}
	
	public String getReaderROSpecMsgId(String aAdapterName, String aReaderName) {
		String result = readerROSpecMap.get(aAdapterName + aReaderName);
		return result;
	}
	
	public boolean existReaderConfig(String aAdapterName, String aReaderName) {
		String result = getReaderConfigMsgId(aAdapterName, aReaderName);
		return (null == result) ? false : true;
	}
	
	public boolean existReaderROSpec(String aAdapterName, String aReaderName) {
		String result = getReaderROSpecMsgId(aAdapterName, aReaderName);
		return (null == result) ? false : true;
	}
	
	public void writeMessageToFile(String aMsgId) {
		
		if (null == aMsgId) {
			log.warn("Message is null!");
			return;
		}
		
		String content = getMessageContent(aMsgId);
		
		try {
	      
			IProject project = getEclipseProject();

			// open if necessary
			if (project.exists() && !project.isOpen())
				project.open(null);

			IFolder repoFolder = project.getFolder(ResourceCenter.REPO_SUBFOLDER);
			if (repoFolder.exists()) {
				
				IFile msgFile = repoFolder.getFile(aMsgId + ".llrp");
				
				if (!msgFile.exists()) {
					InputStream is = 
						new ByteArrayInputStream(content.getBytes());
					msgFile.create(is, false, null);
				}
			
				// Open new file in editor
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
				
				IDE.openEditor(page, msgFile, "org.fosstrak.llrp.commander.editors.LLRPEditor", true);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * tear down the resource center.
	 */
	public void close() {
		log.info("Closing Database...");
		ResourceCenter.getInstance().getRepository().close();
		log.info("Undefine all readers...");
		ResourceCenter.getInstance().disconnectAllReaders();
		log.info("stopping message box refresher...");
		messageBoxRefresh.stop();
	}

	/**
	 * @return the messageBoxRefresh
	 */
	public MessageBoxRefresh getMessageBoxRefresh() {
		return messageBoxRefresh;
	}
}
