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

package org.fosstrak.llrp.commander.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.*;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.commander.ResourceCenter;

/**
 * The LLRP message repository implementation based on Sun JavaDB.
 * Please make sure the derby.jar in the build path before you can
 * start the database.
 *
 * @author Haoning Zhang
 * @version 1.0
 */

public class JavaDBRepository implements Repository {

	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(JavaDBRepository.class);
	
	private Connection conn = null;
	private PreparedStatement psSelect, psSelectAll, psRemoveAll, psInsert;
	
	// Database connection string
	private final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private final String DB_PROTOCOL = "jdbc:derby:";
	private final String DB_CREATE = ";create=true";
	private final String DB_NAME = "llrpMsgDB";
    
	// SQL statements
	private final String SQL_CREATE_TABLE = "CREATE TABLE LLRP_MSG"
    	+ "(MSG_ID CHAR(32),"
    	+ "MSG_TYPE CHAR(32),"
    	+ "READER CHAR(64),"
    	+ "MSG_TIME TIME,"
    	+ "CONTENT CLOB,"
    	+ "COMMENT VARCHAR(64),"
    	+ "MARK CHAR(3))";
 	private final String SQL_DROP_TABLE = "DROP TABLE LLRP_MSG";
	private final String SQL_SELECT_MSG = "select * from LLRP_MSG where MSG_ID=?";
	private final String SQL_SELECTALL_MSG = "select * from LLRP_MSG ORDER BY MSG_TIME";
	private final String SQL_REMOVEALL_MSG = "delete from LLRP_MSG";
	private final String SQL_INSERT_MSG = "insert into LLRP_MSG values (?, ?, ?, ?, ?, ?, ?)";
	private final String SQL_UPDATE_CONTENT = "update LLRP_MSG set CONTENT = ?, MARK = 'M'";
	private final String SQL_UPDATE_COMMENT = "update LLRP_MSG set COMMENT = ?";
	
	private boolean isHealth;
    /**
     * Loads the appropriate JDBC driver for this environment/framework. For
     * example, if we are in an embedded environment, we load Derby's
     * embedded Driver, <code>org.apache.derby.jdbc.EmbeddedDriver</code>.
     */
    private boolean loadDriver() {
    	boolean isHealth = false;
    	try {
            Class.forName(DB_DRIVER).newInstance();
            log.info("Loaded the appropriate driver");
            isHealth = true;
        } catch (ClassNotFoundException cnfe) {
            log.warn("Unable to load the JDBC driver " + DB_DRIVER);
            log.warn("Please check your CLASSPATH.");
            cnfe.printStackTrace(System.err);
        } catch (InstantiationException ie) {
        	log.warn("Unable to instantiate the JDBC driver " + DB_DRIVER);
            ie.printStackTrace(System.err);
        } catch (IllegalAccessException iae) {
        	log.warn("Not allowed to access the JDBC driver " + DB_DRIVER);
            iae.printStackTrace(System.err);
        }
        
        return isHealth;
    }
    
    /**
     * Load the Derby driver.
     * And create the table in first time.
     * 
     * @Overrides
     */
	public void open() {
  	
		/* load the desired JDBC driver */
		isHealth = loadDriver();
		
		log.info("Derby EmbeddedDriver Loaded.");
			
		String connectURL = DB_PROTOCOL + DB_NAME + DB_CREATE;
		
		try {
			conn = DriverManager.getConnection(connectURL);
			log.info("Connection Established");
		} catch (Exception e) {
			isHealth = false;
			e.printStackTrace();
		}
		
		try {
			
			Statement sDropTable = conn.createStatement();
			sDropTable.execute(SQL_DROP_TABLE);
			
			log.info("Existing Table Removed.");
		} catch (Exception e) {
			log.info("Table doesn't exist. Remove failed.");
		}
		
		try {
			
			Statement sCreateTable = conn.createStatement();
			
			/* In first time, the message table will be created. If the table
			 * exists. The Exception will be triggered.
			 */
			sCreateTable.execute(SQL_CREATE_TABLE);
			sCreateTable.close();
			
			log.info("New Table Created.");
		} catch (Exception e) {
			log.info("Table exists.");
		}
	}
	
	/**
	 * Close the database connection.
	 * 
	 * @Overrides
	 */
	public void close() {
		try {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
	
	public ArrayList<LLRPMessageItem> getTopN(int aRowNum) {
		
		ArrayList<LLRPMessageItem> msgs = new ArrayList<LLRPMessageItem>();
		
		try {
			psSelectAll = conn.prepareStatement(SQL_SELECTALL_MSG);
			ResultSet results = psSelectAll.executeQuery();
			
			int row = 0;
			while ((row < aRowNum) && results.next()) {
				LLRPMessageItem msg = new LLRPMessageItem();
				
				msg.setId(results.getString(1));
				msg.setMessageType(results.getString(2));
				msg.setReader(results.getString(3));
				msg.setTime(new Date(results.getTimestamp(4).getTime()));
				
				// For message list, we don't load the message content, to saving the memory
				
				msg.setComment(results.getString(6));
				msg.setMark(results.getString(7).charAt(0));
				
				log.info("Get Message (ID=" + results.getString(1) + ") from JavaDB.");
								
				msgs.add(msg);
				row ++;
			}
			
			results.close();
			psSelectAll.close();
			
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}
		
		return msgs;
	}
	
	public LLRPMessageItem get(String aMsgSysId) {
		
		LLRPMessageItem msg = new LLRPMessageItem();
		
		try {
			psSelect = conn.prepareStatement(SQL_SELECT_MSG);
			psSelect.setString(1, aMsgSysId);
			ResultSet results = psSelect.executeQuery();
			
			if (results.next()) {
				msg.setId(results.getString(1));
				msg.setMessageType(results.getString(2));
				msg.setReader(results.getString(3));
				msg.setTime(new Date(results.getTimestamp(4).getTime()));
				msg.setContent(results.getString(5));
				msg.setComment(results.getString(6));
				msg.setMark(results.getString(7).charAt(0));
				
				log.info("Get Message (ID=" + results.getString(1) + ") from JavaDB.");
			}
			
			psSelect.close();
			results.close();
			
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}
		
		return msg;
	}
	
	public void put(LLRPMessageItem aMessage) {
		try {
			psInsert = conn.prepareStatement(SQL_INSERT_MSG);

			psInsert.setString(1, aMessage.getId());
			psInsert.setString(2, aMessage.getMessageType());
			psInsert.setString(3, aMessage.getUniqueName());
			psInsert.setTimestamp(4, new Timestamp(aMessage.getTime().getTime()));
			psInsert.setString(5, aMessage.getContent());
			psInsert.setString(6, aMessage.getComment());
			psInsert.setString(7, "" + aMessage.getMark());
			
			psInsert.executeUpdate();
			psInsert.close();
			
			ResourceCenter.getInstance().addToMessageMetadataList(aMessage);
			
			if (aMessage.getMessageType().equals("GET_READER_CONFIG_RESPONSE")) {
				log.debug("Receiving GET_READER_CONFIG_RESPONSE, set the flag...");
				ResourceCenter.getInstance().addReaderConfig(aMessage.getAdapter(), aMessage.getReader(), aMessage.getId());
			}
			
			if (aMessage.getMessageType().equals("GET_ROSPECS_RESPONSE")) {
				log.debug("Receiving GET_ROSPECS_RESPONSE, set the flag...");
				ResourceCenter.getInstance().addReaderROSpec(aMessage.getAdapter(), aMessage.getReader(), aMessage.getId());
			}
			
			log.info("Put Message (ID=" + aMessage.getId() + ") into JavaDB.");
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}
	}
	
	public void clearAll() {
		try {
			psRemoveAll = conn.prepareStatement(SQL_REMOVEALL_MSG);
			psRemoveAll.executeUpdate();
			psRemoveAll.close();
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}
	}
	
	public boolean isHealth() {
		return isHealth;
	}
}

