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

import java.util.ArrayList;

/**
* Common interface that helps to run "check-processes" (processes that shall 
* ensure the integrity of the LLRP commander and its folders/files). 
* @author zhanghao
* @author sawielan
*
*/
public abstract class CheckItem {
	
	public final static int CATEGORY_ERROR = 1;
	public final static int CATEGORY_FIX = 2;
	public final static int CATEGORY_WARN = 3;
	public final static int CATEGORY_INFO = 4;
	
	private ArrayList<String> report;
	
	public CheckItem() {
		report = new ArrayList<String>();
	}
	
	/**
	 * add a specific error report.
	 * @param aItem the error report string.
	 * @param aCategory a category that describes the error.
	 */
	public void addReportItem(String aItem, int aCategory) {
		
		String prefix = "";
		
		if (aCategory == CATEGORY_ERROR) {
			prefix = "[ERROR] ";
		} else if (aCategory == CATEGORY_FIX) {
			prefix = "[FIX] ";
		} else if (aCategory == CATEGORY_WARN) {
			prefix = "[WARN] ";
		} else if (aCategory == CATEGORY_INFO) {
			prefix = "[INFO] ";
		}
		
		report.add(prefix + aItem);
	}
	
	/**
	 * adds a list of error reports to the current error list.
	 * @param aItemList new errors to add.
	 */
	public void addReportItem(ArrayList<String> aItemList) {
		report.addAll(aItemList);
	}
	
	/**
	 * clear out all the reports collected from previous health checks.
	 */
	public void clearAllReport() {
		report.clear();
	}
	
	/**
	 * @return a list of error messages (if the check went wrong) that describe the errors.
	 */
	public ArrayList<String> getReport() {
		return report;
	}
	
	/**
	 * @return true if the check went ok, false otherwise.
	 */
	public abstract boolean validate();
	
	/**
	 * invoke the repair functionality on the repair item.
	 */
	public abstract void fix();
	
	
}
