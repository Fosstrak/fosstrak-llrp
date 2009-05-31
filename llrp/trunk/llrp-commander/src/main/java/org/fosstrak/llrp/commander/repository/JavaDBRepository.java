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
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.views.ReaderExplorerView;
import org.fosstrak.llrp.commander.views.ReaderExplorerViewContentProvider;

/**
 * The LLRP message repository implementation based on Sun JavaDB.
 * Please make sure the derby.jar in the build path before you can
 * start the database.
 *
 * @author Haoning Zhang
 * @author sawielan
 * @version 1.0
 */

public class JavaDBRepository implements Repository {

	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(JavaDBRepository.class);
	
	private Connection conn = null;
	private PreparedStatement psSelect, psRemoveAll, psInsert;
	
	// Database connection string
	private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DB_PROTOCOL = "jdbc:derby:";
	private static final String DB_CREATE = ";create=true";
	private static final String DB_NAME = "llrpMsgDB";
    
	// selectors within the database fields.
	private static final int SELECTOR_ID = 1;
	private static final int SELECTOR_MESSAGE_TYPE = 2;
	private static final int SELECTOR_READER = 3;
	private static final int SELECTOR_ADAPTOR = 4;
	private static final int SELECTOR_TIMESTAMP = 5;
	private static final int SELECTOR_STATUS = 6;
	private static final int SELECTOR_COMMENT = 7;
	private static final int SELECTOR_MARK = 8;	
	private static final int SELECTOR_CONTENT = 9;
	
	/** the number of table columns. */
	private static final int NUM_TABLE_COLUMNS = 8;
	
	// SQL statements
	private static final String SQL_CREATE_TABLE = "CREATE TABLE LLRP_MSG "
    	+ "(MSG_ID CHAR(32),"
    	+ "MSG_TYPE CHAR(32),"
    	+ "READER CHAR(64),"
    	+ "ADAPTER CHAR(64),"
    	+ "MSG_TIME TIME,"
    	+ "STATUS CHAR(64),"
    	+ "COMMENT VARCHAR(64),"
    	+ "MARK CHAR(3),"
		+ "CONTENT CLOB)";
	
 	private static final String SQL_DROP_TABLE = "DROP TABLE LLRP_MSG";
 	
 	/** select a specific message. */
	private static final String SQL_SELECT_MSG = 
		"select * from LLRP_MSG where MSG_ID=?";
	
	/** remove all the messages. */
	private static final String SQL_REMOVEALL_MSG = "delete from LLRP_MSG";
	
	/** remove all the messages that belong to a given adapter. */
	private static final String SQL_REMOVE_ADAPTER_MSG = 
		"delete from LLRP_MSG where ADAPTER=?";
	
	/** remove all the messages that belong to a given reader. */
	private static final String SQL_REMOVE_READER_MSG = 
		"delete from LLRP_MSG where ADAPTER=? and READER=?";
	
	/** insert a new item into the database. */
	private static final String SQL_INSERT_MSG = 
		"insert into LLRP_MSG values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	/** select all the messages with the content. */
	private static final String SQL_SELECT_ALL_CONTENT = 
		"select * from LLRP_MSG " +
		"order by MSG_TIME DESC";

	/** select all the messages no content. */
	private static final String SQL_SELECT_ALL = 
		"select MSG_ID,MSG_TYPE,READER,ADAPTER,MSG_TIME,STATUS,COMMENT,MARK " +
		"from LLRP_MSG " +
		"order by MSG_TIME DESC";
	
	// ------ adaptor given ------
	/** retrieve by adaptor with content. */
	private static final String SQL_SELECT_BY_ADAPTOR_CONTENT = 
		"select * from LLRP_MSG where ADAPTER=? " +
		"order by MSG_TIME DESC";
	
	/** retrieve by adaptor name no content. */
	private static final String SQL_SELECT_BY_ADAPTOR = 
		"select MSG_ID,MSG_TYPE,READER,ADAPTER,MSG_TIME,STATUS,COMMENT,MARK " +
		"from LLRP_MSG where ADAPTER=? " +
		"order by MSG_TIME DESC";
	
	// ------ reader and adaptor given ------
	/** 
	 * retrieve by adaptor and reader name with content. */
	private final static String SQL_SELECT_BY_ADAPTOR_AND_READER_CONTENT = 
		"select * from LLRP_MSG where ADAPTER=? and READER=? " +
		"order by MSG_TIME DESC";
	
	/** retrieve by adaptor and reader name no content. */
	private static final String SQL_SELECT_BY_ADAPTOR_AND_READER = 
		"select MSG_ID,MSG_TYPE,READER,ADAPTER,MSG_TIME,STATUS,COMMENT,MARK " +
		"from LLRP_MSG where ADAPTER=? and READER=? " +
		"order by MSG_TIME DESC";

	
	
	private boolean isHealth;
	
	/** the location of the repository. */
	private final String repoLocation;
	
	/**
	 * construct a new java db repository.
	 * @param repoLocation the location where to create/load the repository.
	 */
	public JavaDBRepository(String repoLocation) {
		if (repoLocation == null) {
			repoLocation = DB_NAME;
		}
		this.repoLocation = repoLocation + DB_NAME;
	}
	
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
     * And create the table in first time.\
     */
	public void open() {
  	
		/* load the desired JDBC driver */
		isHealth = loadDriver();
		
		log.info("Derby EmbeddedDriver Loaded.");
			
		String connectURL = DB_PROTOCOL + repoLocation + DB_CREATE;
		
		try {
			conn = DriverManager.getConnection(connectURL);
			log.info("Connection Established");
		} catch (Exception e) {
			isHealth = false;
			e.printStackTrace();
		}
		
		// wipe table if erroneous or if user requests it by preferences.
		if (!existsTable() || ResourceCenter.getInstance().wipeRepositoryOnStartup()) {
			dropTable();
			createTable();
		}
	}
	
	/**
	 * checks whether the required tables exist or not.
	 * @return true if everything is ok, false otherwise.
	 */
	private boolean existsTable() {
		// we try to make a SQL query. if it fails, we assume the table to be dead...
		try {
			DatabaseMetaData dbMeta = conn.getMetaData();
			ResultSet resultSet = dbMeta.getColumns(null, null, "LLRP_MSG", null);
			int n = 0;
			while (resultSet.next()) {
				n++;
			}
			if (n<NUM_TABLE_COLUMNS) {
				throw new SQLException("missing fields");
			}
			 
		} catch (SQLException e) {
			log.error("table erroneous or missing. therefore recreate it.");
			return false;
		}
		return true;
	}
	
	/** 
	 * drops the table. 
	 */
	private void dropTable() {
		try {
			
			Statement sDropTable = conn.createStatement();
			sDropTable.execute(SQL_DROP_TABLE);
			
			log.info("Existing Table Removed.");
			
		} catch (Exception e) {
			log.info("Table doesn't exist. Remove failed." + e.getMessage());
		}
	}
	
	/**
	 * generates the necessary tables.
	 */
	private void createTable() {
		try {
			
			Statement sCreateTable = conn.createStatement();
			
			// In first time, the message table will be created. If the table
			// exists. The Exception will be triggered.
			//
			sCreateTable.execute(SQL_CREATE_TABLE);
			sCreateTable.close();
			
			log.info("New Table Created.");
		} catch (Exception e) {
			log.info("Table exists. " + e.getMessage());
		}
	}
	
	/**
	 * Close the database connection.
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
	
	/**
	 * returns all the messages from the specified adaptor and the reader 
	 * limited by num. if you set num to RETRIEVE_ALL all messages get returned.
	 * if you set readerName to null, all the messages of all the readers with 
	 * adaptor adaptorName will be returned.
	 * @param adaptorName the name of the adaptor.
	 * @param readerName the name of the reader.
	 * @param num how many messages to retrieve.
	 * @param content if true retrieve the message content, false no content.
	 * @return a list of messages.
	 */
	public ArrayList<LLRPMessageItem> get(
			String adaptorName, String readerName, int num, boolean content) {
		
		ArrayList<LLRPMessageItem> msgs = new ArrayList<LLRPMessageItem> ();
		
		try {
			PreparedStatement st = null;
			ResultSet results = null;
			String sql = null;
			if ((null == adaptorName) || 
					(ReaderExplorerViewContentProvider.ROOT_NAME.equals(
							adaptorName))) {	
				sql = SQL_SELECT_ALL;
				if (content) {
					sql = SQL_SELECT_ALL_CONTENT;
				}
				st = conn.prepareStatement(sql);
			} else if (readerName != null) {
				sql = SQL_SELECT_BY_ADAPTOR_AND_READER;
				if (content) {
					sql = SQL_SELECT_BY_ADAPTOR_AND_READER_CONTENT;
				}
				st = conn.prepareStatement(sql);
				st.setString(1, adaptorName.trim());
				st.setString(2, readerName.trim());
			} else {
				sql = SQL_SELECT_BY_ADAPTOR;
				if (content) {
					sql = SQL_SELECT_BY_ADAPTOR_CONTENT;
				}
				
				st = conn.prepareStatement(sql);
				st.setString(1, adaptorName.trim());
			}
			// bound the number of items to retrieve
			if (num != Repository.RETRIEVE_ALL) {
				st.setMaxRows(num);
			}
			results = st.executeQuery();
			while (results.next()) {
				LLRPMessageItem item = new LLRPMessageItem();
				item.setAdapter(results.getString(SELECTOR_ADAPTOR));
				item.setComment(results.getString(SELECTOR_COMMENT));
				item.setId(results.getString(SELECTOR_ID));
				item.setMark(results.getInt(SELECTOR_MARK));
				item.setMessageType(results.getString(SELECTOR_MESSAGE_TYPE));
				item.setReader(results.getString(SELECTOR_READER));
				item.setStatusCode(results.getString(SELECTOR_STATUS));
				item.setTime(
						new Date(results.getTimestamp(
								SELECTOR_TIMESTAMP).getTime()));
				if (content) {
					item.setContent(results.getString(SELECTOR_CONTENT));
				}
				msgs.add(item);
			}
			
		} catch (Exception e) {
			log.error(
					String.format(
							"could not retrieve from database: %s\n", 
							e.getMessage())
					);
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
				msg.setId(results.getString(SELECTOR_ID));
				msg.setMessageType(results.getString(SELECTOR_MESSAGE_TYPE));
				msg.setReader(results.getString(SELECTOR_READER));
				msg.setAdapter(results.getString(SELECTOR_ADAPTOR));
				msg.setTime(new Date(
						results.getTimestamp(SELECTOR_TIMESTAMP).getTime()));
				msg.setContent(results.getString(SELECTOR_CONTENT));
				msg.setComment(results.getString(SELECTOR_COMMENT));
				msg.setMark(results.getInt(SELECTOR_MARK));
				msg.setStatusCode(results.getString(SELECTOR_STATUS));
				
				log.debug("Get Message (ID=" + results.getString(1) + ") from JavaDB.");
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

			psInsert.setString(SELECTOR_ID, aMessage.getId());
			psInsert.setString(SELECTOR_MESSAGE_TYPE, aMessage.getMessageType());
			psInsert.setString(SELECTOR_READER, aMessage.getReader());
			String adaptor = aMessage.getAdapter();
			if (adaptor == null) {
				adaptor = AdaptorManagement.DEFAULT_ADAPTOR_NAME;
			}
			psInsert.setString(SELECTOR_ADAPTOR, adaptor);
			psInsert.setTimestamp(
					SELECTOR_TIMESTAMP, 
					new Timestamp(aMessage.getTime().getTime()));
			psInsert.setString(SELECTOR_CONTENT, aMessage.getContent());
			psInsert.setString(SELECTOR_COMMENT, aMessage.getComment());
			psInsert.setString(SELECTOR_MARK, "" + aMessage.getMark());
			psInsert.setString(SELECTOR_STATUS, aMessage.getStatusCode());
			
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
			
			log.debug("Put Message (ID=" + aMessage.getId() + ") into JavaDB.");
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

	public int count(String adaptor, String reader) {
				
		int rowcount = 0;
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT COUNT(*) FROM LLRP_MSG";
			if (null == adaptor) {
				// all ok
			} else if (null == reader) {
				// restrict to adaptor
				query = String.format("%s WHERE ADAPTER='%s'", query, adaptor);
			} else {
				query = String.format("%s WHERE ADAPTER='%s' AND READER='%s'", 
						query, adaptor, reader);
			}
			ResultSet resultSet = stmt.executeQuery(query);
	    
	        // Get the number of rows from the result set
	        resultSet.next();
	        rowcount = resultSet.getInt(1);
	        stmt.close();
	        resultSet.close();
		} catch (SQLException e) {
			log.error("Could not retrieve the number of messages: " + 
					e.getMessage());
		}
		
		return rowcount;
	}

	public void clearAdapter(String adapter) {
		try {
			PreparedStatement psRemove = conn.prepareStatement(SQL_REMOVE_ADAPTER_MSG);
			psRemove.setString(1, adapter);
			psRemove.executeUpdate();
			psRemove.close();
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}		
	}

	public void clearReader(String adapter, String reader) {
		try {
			PreparedStatement psRemove = conn.prepareStatement(SQL_REMOVE_READER_MSG);
			psRemove.setString(1, adapter);
			psRemove.setString(2, reader);
			psRemove.executeUpdate();
			psRemove.close();
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}	
	}
}

