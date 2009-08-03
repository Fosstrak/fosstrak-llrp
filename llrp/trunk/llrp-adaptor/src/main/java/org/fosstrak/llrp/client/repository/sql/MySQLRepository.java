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

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.client.ROAccessReportsRepository;
import org.fosstrak.llrp.client.repository.sql.roaccess.DerbyROAccessReportsRepository;

/**
 * The {@link MySQLRepository} provides the basis for a MySQL repository 
 * back-end. The SQL-server is accessed via the MySQL-JDBC connector.<br/>
 * <h3>NOTICE:</h3>
 * We share most SQL statements with the Derby implementation of the SQL 
 * repository. However, the statement to create the LLRP messages table, and 
 * the way how the connection gets established, differ.<br/>
 * The user credentials as well as the JDBC connector URL are both obtained 
 * from the eclipse preference store (The user can configure the settings 
 * in the Preferences page in the tab LLRP-Commander).
 * @author sawielan
 *
 */
public class MySQLRepository extends AbstractSQLRepository {

	/** the MySQL JDBC driver. */
	protected final String DBDRIVER = "com.mysql.jdbc.Driver";

	// a handle to the RO_ACCESS_REPORTS logging table. we can reuse the derby 
	// database, as there are no conflicts with the data-types.
	protected DerbyROAccessReportsRepository repoROAccessReports = null;
	
	/** default JDBC Connector URL. */
	public static final String JDBC_STR = 
		String.format("jdbc:mysql://localhost:3306/%s", DB_NAME);
	
	// log4j instance.
	private static Logger log = Logger.getLogger(MySQLRepository.class);
	
	@Override
	protected String getDBDriver() {
		return DBDRIVER;
	}

	@Override
	protected Connection openConnection() throws Exception {
		log.debug(String.format("Opening MySQL connection with:\n" +
				"\tusername: %s\n " +
				"\tJDBC connector URL: %s\n", username, connectURL));
		
		return DriverManager.getConnection(connectURL, username, password);
	}
	
	@Override
	protected String sqlCreateTable() {
		return "CREATE TABLE " + TABLE_LLRP_REPOSITORY + " "
	    	+ "(MSG_ID CHAR(32),"
	    	+ "MSG_TYPE CHAR(32),"
	    	+ "READER CHAR(64),"
	    	+ "ADAPTER CHAR(64),"
	    	+ "MSG_TIME TIMESTAMP,"
	    	+ "STATUS CHAR(64),"
	    	+ "COMMENT VARCHAR(64),"
	    	+ "MARK CHAR(3),"
			+ "CONTENT MEDIUMTEXT)";
	}

	public ROAccessReportsRepository getROAccessRepository() {
		// for the RO_ACCESS_REPORTS repository, we can use the derby one as 
		// the SQL set used, works for both MySQL and Derby (only in the case
		// RO_ACCESS_REPORTS repository!).
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
