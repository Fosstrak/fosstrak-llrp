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

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.repository.JavaDBRepository;

/**
* Helper that checks the derby database folder. If the folder is missing or 
* is corrupt, the helper reports the issue.
* @author zhanghao
* @author sawielan
*
*/
public class CheckRepository extends CheckItem {
	
	/** log4j logger. */
	private static Logger log = Logger.getLogger(CheckRepository.class);
	
	public boolean validate() {
		
		this.clearAllReport();
		
		IProject project = ResourceCenter.getInstance().getEclipseProject();
				
		try {
			IFolder dbFolder = project
					.getFolder(ResourceCenter.DB_SUBFOLDER);

			if (!dbFolder.exists()) {
				addReportItem("Subfolder '" + ResourceCenter.DB_SUBFOLDER
						+ "' doesn't exist.", CATEGORY_ERROR);

				return false;
			}
			
		} catch (Exception e) {
			return false;
		}
		
		JavaDBRepository repo = (JavaDBRepository) ResourceCenter.getInstance().getRepository();
		if (!repo.isHealth()) {
			addReportItem("JavaDB Repository doesn't exist or is corrupted.", CATEGORY_ERROR);
			return false;
		}
		return true;
	}
	
	public void fix() {
		this.clearAllReport();
		
		IProject project = ResourceCenter.getInstance().getEclipseProject();
		// refresh the workspace...
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		
		// check if the configuration folder exists.
		IFolder dbFolder = project.getFolder(
				ResourceCenter.DB_SUBFOLDER);
		if (!dbFolder.exists()) {
			try {
				log.info("create new db folder...");
				dbFolder.create(true, true, null);
				log.info("created db folder.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

