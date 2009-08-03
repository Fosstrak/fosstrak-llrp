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

package org.fosstrak.llrp.client.repository.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.client.ROAccessReportsRepository;
import org.fosstrak.llrp.client.repository.sql.roaccess.DerbyROAccessReportsRepository;

/**
 * The LLRP message repository implementation based on Sun JavaDB.
 * Please make sure the derby.jar in the build path before you can
 * start the database.
 *
 * @author Haoning Zhang
 * @author sawielan
 * @version 1.0
 */

public class DerbyRepository extends AbstractSQLRepository {

	// Log4j instance.
	private static Logger log = Logger.getLogger(DerbyRepository.class);
	
	// a handle to the RO_ACCESS_REPORTS logging table.
	protected DerbyROAccessReportsRepository repoROAccessReports = null;
	
	// Database connection string
	
	private static final String DB_PROTOCOL = "jdbc:derby:";
	private static final String DB_CREATE = ";create=true";
	
	/** the name of the property for the repository location in the args map. */
	public static final String ARG_REPO_LOCATION = "argRepoLocation";
    
	/** the location of the repository. */
	private String repoLocation;
	
	/**
	 * construct a new java DB repository.
	 */
	public DerbyRepository() {
	}
	
	@Override
	public void initialize(Map<String, String> args) 
		throws LLRPRuntimeException {
		
		super.initialize(args);
		
		String argRepoLoc = null;
		if ((null == args) || (null == args.get(ARG_REPO_LOCATION))) {
			argRepoLoc = DB_NAME;
		} else {
			argRepoLoc = args.get(ARG_REPO_LOCATION);
		}
		repoLocation = argRepoLoc + DB_NAME;
	}

	@Override
	protected Connection openConnection() throws Exception {
		 return DriverManager.getConnection(DB_PROTOCOL + repoLocation + DB_CREATE);
	}

	public ROAccessReportsRepository getROAccessRepository() {
		if (null == repoROAccessReports) {
			log.debug("No RepoROAccessReports handle yet - Create a new one.");
			repoROAccessReports = new DerbyROAccessReportsRepository();
			try {
				repoROAccessReports.initialize(this);
			} catch (LLRPRuntimeException e) {
				log.error(String.format(
						"Could not initialize the RO_ACCESS_REPORTS repo: '%s'",
						e.getMessage()));
			}			
		}
		return repoROAccessReports;
	}
}

