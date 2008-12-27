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

package org.fosstrak.llrp.commander.check;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.fosstrak.llrp.commander.LLRPPlugin;
import org.fosstrak.llrp.commander.ResourceCenter;

public class CheckEclipseProject extends CheckItem {

	private static Logger log = Logger.getLogger(CheckEclipseProject.class);
	
	public boolean validate() {
		
		this.clearAllReport();
		
		String projectName = ResourceCenter.getInstance().getEclipseProjectName();
		IProject project = ResourceCenter.getInstance().getEclipseProject();
		
		if (null == project) {
			addReportItem("Eclipse Project '" + projectName + "' doesn't exist.", this.CATEGORY_ERROR);
			addReportItem("If you are using this tool for the first time, please click the 'Fix it!' button to " +
					"initialize the project folder.", this.CATEGORY_INFO);
			return false;
		}
		
		try {
			// open if necessary
			if (project.exists() && !project.isOpen()) {
				project.open(null);
			}

			IFolder msgFolder = project
					.getFolder(ResourceCenter.REPO_SUBFOLDER);
			IFolder draftFolder = project
					.getFolder(ResourceCenter.DRAFT_SUBFOLDER);

			if (!msgFolder.exists()) {
				addReportItem("Subfolder '" + ResourceCenter.REPO_SUBFOLDER
						+ "' doesn't exist.", CATEGORY_ERROR);
			}

			if (!draftFolder.exists()) {
				addReportItem("Subfolder '" + ResourceCenter.DRAFT_SUBFOLDER
						+ "' doesn't exist.", CATEGORY_ERROR);
			}
			
			if (!msgFolder.exists() || !draftFolder.exists()) {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return true;
	}
	
	public void fix() {
		
		this.clearAllReport();
		
		String projectName = ResourceCenter.getInstance().getEclipseProjectName();
		
		IProgressMonitor progressMonitor = new NullProgressMonitor();

		IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = myWorkspaceRoot.getProject(projectName);
	
		try {
			if (!project.exists()) {
				project.create(progressMonitor);
				project.open(progressMonitor);
				
				addReportItem("Project '" + projectName	+ "' created.", CATEGORY_FIX);
			}
			
			if (project.exists() && !project.isOpen()) {
				project.open(null);
			}
			
			//Try to add subfolder for repository messages
			IFolder msgFolder = project.getFolder(ResourceCenter.REPO_SUBFOLDER);
			if (!msgFolder.exists()) {
				msgFolder.create(true, false, progressMonitor);
				
				addReportItem("Subfolder '" + ResourceCenter.REPO_SUBFOLDER	+ "' created.", CATEGORY_FIX);
			}
			
			
			IFolder draftFolder = project.getFolder(ResourceCenter.DRAFT_SUBFOLDER);
			if (!draftFolder.exists()) {
				draftFolder.create(true, true, progressMonitor);
				
				addReportItem("Subfolder '" + ResourceCenter.DRAFT_SUBFOLDER + "' created.", CATEGORY_FIX);
				
				URL bundleRoot = LLRPPlugin.getDefault().getBundle().getEntry("/sampleXML");
				
				try {
					URL fileURL = FileLocator.toFileURL(bundleRoot);
					File folderSource = new File(fileURL.getPath());
					
					FilenameFilter filter = new FilenameFilter() {
				        public boolean accept(File dir, String name) {
				            return name.endsWith(".llrp");
				        }
				    };
				    
				    String[] sampleFileNames = folderSource.list(filter);
				    
				    for (int i = 0; i < sampleFileNames.length; i ++) {
				    	String urlFile = fileURL.getPath() + "/" + sampleFileNames[i];
				    	File sampleFile = new File(urlFile);
				    	
				    	IFile file = project.getFile(ResourceCenter.DRAFT_SUBFOLDER + "/" + sampleFileNames[i]);
				    	
				    	file.create(new FileInputStream(sampleFile), true, progressMonitor);
				    }
				    
				} catch (IOException ioe) {
					ioe.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
			}
			
//			IFolder sampleFolder = project.getFolder(ResourceCenter.SAMPLE_SUBFOLDER);
//			if (!sampleFolder.exists()) {
//				sampleFolder.create(true, true, progressMonitor);
//				
//				addReportItem("Subfolder '" + ResourceCenter.SAMPLE_SUBFOLDER + "' created.", CATEGORY_FIX);
//				
//			    URL bundleRoot = LLRPPlugin.getDefault().getBundle().getEntry("/sampleXML");
//				
//				try {
//					URL fileURL = FileLocator.toFileURL(bundleRoot);
//					File folderSource = new File(fileURL.getPath());
//					
//					FilenameFilter filter = new FilenameFilter() {
//				        public boolean accept(File dir, String name) {
//				            return name.endsWith(".llrp");
//				        }
//				    };
//				    
//				    String[] sampleFileNames = folderSource.list(filter);
//				    
//				    for (int i = 0; i < sampleFileNames.length; i ++) {
//				    	String urlFile = fileURL.getPath() + "/" + sampleFileNames[i];
//				    	File sampleFile = new File(urlFile);
//				    	
//				    	//String urlTargetFile = sampleFolder.getLocationURI() + "/" + sampleFileNames[i];
//				    	//File sampleTargetFile = new File(urlTargetFile);
//				    	
//				    	IFile file = project.getFile(ResourceCenter.SAMPLE_SUBFOLDER + "/" + sampleFileNames[i]);
//				    	
//				    	file.create(new FileInputStream(sampleFile), true, progressMonitor);
//				    	
//				    	//copyFile(sampleFile, sampleTargetFile);
//				    }
//				    
//				} catch (IOException ioe) {
//					ioe.printStackTrace();
//				} catch (Exception e) {
//					e.printStackTrace();
//				} 
//			}

		} catch (CoreException coe) {
			coe.printStackTrace();
		}
	}
	
}
