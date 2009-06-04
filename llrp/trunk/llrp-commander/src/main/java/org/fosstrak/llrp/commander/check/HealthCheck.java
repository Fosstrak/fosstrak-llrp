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

package org.fosstrak.llrp.commander.check;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This HealthCheck maintain a chain of <code>CheckItem</code>.
 * when the PlugIn start up, it is triggered by validate each <code>CheckItem</code>
 * in the chain. If any of them failed validation, the specific fix function
 * will be called.
 *
 * @author Haoning Zhang
 * @version 1.0
 */
public class HealthCheck extends CheckItem {

	private ArrayList<CheckItem> checkList;
	
	/**
	 * Default Constructor.
	 */
	public HealthCheck() {
		checkList = new ArrayList<CheckItem>();
	}
	
	/**
	 * Register new check items
	 * @param aItem New Check Item
	 */
	public void registerCheckItem(CheckItem aItem) {
		checkList.add(aItem);
	}
	
	/**
	 * Validate each check item in the chain.
	 */
	public boolean validate() {
		
		boolean isHealth = true;
		this.clearAllReport();
		
		Iterator<CheckItem> i = checkList.iterator();
		while (i.hasNext()) {
			CheckItem item = i.next();
			isHealth = isHealth && item.validate();
			this.addReportItem(item.getReport());
		}
		
		return isHealth;
	}
	
	/**
	 * Execute <code>fix</code> function of each check item in the chain
	 */
	public  void fix() {
		
		this.clearAllReport();
		
		Iterator<CheckItem> i = checkList.iterator();
		while (i.hasNext()) {
			CheckItem item = i.next();
			if (!item.validate()) {
				item.fix();
				this.addReportItem(item.getReport());
			}
		}
	}
}
