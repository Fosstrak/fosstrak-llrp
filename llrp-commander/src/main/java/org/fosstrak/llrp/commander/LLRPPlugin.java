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

package org.fosstrak.llrp.commander;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.fosstrak.llrp.commander.check.CheckEclipseProject;
import org.fosstrak.llrp.commander.check.CheckRepository;
import org.fosstrak.llrp.commander.check.HealthCheck;
import org.fosstrak.llrp.commander.preferences.PreferenceConstants;
import org.fosstrak.llrp.commander.util.Utility;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Haoning Zhang
 * @version 1.0
 */

public class LLRPPlugin extends AbstractUIPlugin {

	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(LLRPPlugin.class);
	
	/**
	 * The Eclipse Plug-in ID
	 */
	public static final String PLUGIN_ID = "llrp_commander";

	// The shared instance
	private static LLRPPlugin plugin;
	
	/**
	 * The constructor
	 */
	public LLRPPlugin() {
		
		String prop = Utility.findWithFullPath("/log4j.properties");
    	// initialize the log4j facilities.
    	PropertyConfigurator.configure(prop);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		IPreferenceStore store = LLRPPlugin.getDefault().getPreferenceStore();
		String storedProjectName = store.getString(PreferenceConstants.P_PROJECT);
		
		if ((storedProjectName != null) || (!storedProjectName.equals(""))) {
			ResourceCenter.getInstance().setEclipseProjectName(storedProjectName);
		}
		
		/*
		if ((readerDefFilename != null) || (!readerDefFilename.equals(""))) {
			ResourceCenter.getInstance().setReaderDefinitionFilename(readerDefFilename);
			AdaptorManagement.loadFromFile(readerDefFilename);
		}*/
		
		/* swieland deactivate the window and auto fix...
		HealthCheckDialog dlg = new HealthCheckDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		if (!dlg.getHealthCheck().validate()) {
			dlg.open();
		*/
				
		HealthCheck healthCheck = new HealthCheck();
		healthCheck.registerCheckItem(new CheckEclipseProject());
		healthCheck.registerCheckItem(new CheckRepository());
		ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		if (!healthCheck.validate()) {
			for (String report : healthCheck.getReport()) {
				log.debug(report);
			}
			healthCheck.fix();
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			for (String report : healthCheck.getReport()) {
				log.debug(report);
			}
		}
		
		// now all the path should be OK and it is safe to start the management.
		ResourceCenter.getInstance().initializeAdaptorMgmt();
		
		// and now initialize the logging facility
		ResourceCenter.getInstance().initializeROAccessReportsLogging();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		log.info("Stopping the Plug-In, and disposing resources...");
		
		ResourceCenter.getInstance().getMessageBoxRefresh().stop();
		plugin = null;
		super.stop(context);
		
		//String defFilename = ResourceCenter.getInstance().getReaderDefinitionFilename();
		//log.info("Store reader configuration into " + defFilename + "...");
		
		//AdaptorManagement.storeToFile(defFilename);
		ResourceCenter.getInstance().close();
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LLRPPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public void getResourceFile(String aRelativePath) {
		URL bundleRoot = getBundle().getEntry("/");
		
		try {
			URL fileURL = FileLocator.toFileURL(bundleRoot);
			java.io.File file = new java.io.File(fileURL.toURI());
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (URISyntaxException urie) {
			urie.printStackTrace();
		} 

		//System.out.println("Bundle location:" + file.getAbsolutePath());
	}
}
