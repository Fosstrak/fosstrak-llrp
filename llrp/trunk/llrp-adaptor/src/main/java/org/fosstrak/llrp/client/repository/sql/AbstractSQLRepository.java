package org.fosstrak.llrp.client.repository.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.client.Constants;
import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.client.RepositoryFactory;

/**
 * The {@link AbstractSQLRepository} represents a common super class for all 
 * SQL based Repositories. The class implements the different methods like 
 * <code>put(LLRPMessageItem)</code> or <code>get(String)</code>  in an 
 * abstract manner, using the strategy pattern. We explain the idea below:<br/>
 * Two examples:<br/>
 * <ol>
 * <li>Create the table to store the LLRP messages:<br/>
 * <code>Statement sCreateTable = conn.createStatement();</code><br/>
 * <code>sCreateTable.execute(sqlCreateTable());</code><br/>
 * <code>sCreateTable.close();</code><br/>
 * As you can see, the {@link AbstractSQLRepository} runs on a prepared 
 * statement (in this case a normal SQL statement would do as well), but this 
 * statement is not hard-coded. The create SQL is obtained via the method 
 * <code>sqlCreateTable()</code>. Depending on the implementing data base, 
 * different SQL statements for the create instruction can be used. Example: a 
 * derby implementation {@link DerbyRepository} uses a different create SQL 
 * than a {@link MySQLRepository} (as the data types differ).
 * </li>
 * <li>Clear all the messages received by an adapter:<br/>
 * <code>PreparedStatement psRemove = conn.prepareStatement(</code><br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<code>sqlRemoveAllAdapterMessages());</code><br/>
 * <code>psRemove.setString(1, adapter);</code><br/>
 * <code>psRemove.executeUpdate();</code><br/>
 * <code>psRemove.close();</code><br/>
 * Here the {@link AbstractSQLRepository} executes a prepared statement. The 
 * name of the adapter to be cleared, is injected from the code in the 
 * {@link AbstractSQLRepository}. Again, the actual query is delivered from the 
 * implementing strategy (example: {@link MySQLRepository}).
 * </li>
 * </ol>
 * <h3>NOTICE:</h3>
 * By default, the {@link AbstractSQLRepository} uses the SQL commands tailored 
 * to the Derby database (as this is the default internal database). So, if 
 * you do not override certain SQL "queries-getter", please be aware that this 
 * might cause trouble with a database differing from Derby.
 * @author sawielan
 *
 */
public abstract class AbstractSQLRepository implements Repository {
	
	/** column index of the ID.*/
	public static final int SELECTOR_ID = 1;
	
	/** column index of the message type. */
	public static final int SELECTOR_MESSAGE_TYPE = 2;
	
	/** column index of the reader name. */
	public static final int SELECTOR_READER = 3;
	
	/** column index of the adapter name. */
	public static final int SELECTOR_ADAPTOR = 4;
	
	/** column index of the time-stamp column. */
	public static final int SELECTOR_TIMESTAMP = 5;
	
	/** column index of the status flag. */
	public static final int SELECTOR_STATUS = 6;
	
	/** column index of the comment field. */
	public static final int SELECTOR_COMMENT = 7;
	
	/** column index of the mark. */
	public static final int SELECTOR_MARK = 8;	
	
	/** column index of the comment column. */
	public static final int SELECTOR_CONTENT = 9;
	
	/** the name of the database in the database server. */
	public static final String DB_NAME = "llrpMsgDB";
	
	/** the name of the LLRP message repository table. */
	public static final String TABLE_LLRP_REPOSITORY = "LLRP_MSG";
	
	// the log4j logger.
	private static Logger log = Logger.getLogger(AbstractSQLRepository.class);
	
	/** whether the repository is healthy or not. */
	protected boolean isHealth;
	
	/** the number of table columns. */
	protected static final int NUM_TABLE_COLUMNS = 8;
	
	// ------------------------- JDBC Stuff ------------------------------
	/** the JDBC connection. */
	protected Connection conn = null;
	
	/** the database driver to use. NOTICE: the default is derby!. */
	public static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	
	/** the database user name. */
	protected String username = "llrp";
	
	/** the database password. */
	protected String password = "llrp";
	
	/** the connection URL. */
	protected String connectURL;
	
	/** whether to wipe the database at startup or not. */
	protected boolean wipe = false;
	
	/** whether to wipe the RO_ACCESS_REPORTS database at startup or not. */
	protected boolean wipeROAccess = false;
	
	/** map with additional arguments to be passed to the initializer. */
	protected Map<String, String> args = null;
	
	// ------------------------- SQL STATEMENTS -------------------------------
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!.
	 * @return a SQL command that creates the table.
	 */
	protected String sqlCreateTable(){
		return "CREATE TABLE " + TABLE_LLRP_REPOSITORY + " "
    	+ "(MSG_ID CHAR(32),"
    	+ "MSG_TYPE CHAR(32),"
    	+ "READER CHAR(64),"
    	+ "ADAPTER CHAR(64),"
    	+ "MSG_TIME TIMESTAMP,"
    	+ "STATUS CHAR(64),"
    	+ "COMMENT VARCHAR(64),"
    	+ "MARK CHAR(3),"
		+ "CONTENT CLOB)";
	}
	
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that erases all the LLRP messages.
	 */
	protected String sqlRemoveAllMessages() {
		return "delete from " + TABLE_LLRP_REPOSITORY;
	}
	
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that drops the LLRP message table.
	 */
	protected String sqlDropTable() {
		return "DROP TABLE " + TABLE_LLRP_REPOSITORY;
	}
	
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that removes all the messages that belong to a given adapter.
	 */
	protected String sqlRemoveAllAdapterMessages() {
		return "delete from " + TABLE_LLRP_REPOSITORY + " where ADAPTER=?";
	}
	
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that removes all the messages that belong to a given reader.
	 */
	protected String sqlRemoveAllReaderMessages() {
		return "delete from " + TABLE_LLRP_REPOSITORY + " where ADAPTER=? and READER=?";
	}
	
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that inserts a new item into the database.
	 */
	protected String sqlInsertMessage() {
		return "insert into " + TABLE_LLRP_REPOSITORY + " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	}
	
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that selects an item by its ID.
	 */
	protected String sqlSelectMessageByID() {
		return "select * from " + TABLE_LLRP_REPOSITORY + " where MSG_ID=?";
	}
	
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that selects all the messages with the content.
	 */
	protected String sqlSelectMessagesWithContent() { 
		return "select * from " + TABLE_LLRP_REPOSITORY + " " +
			"order by MSG_TIME DESC";
	}

	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that selects all the messages without the content.
	 */
	protected String sqlSelectMessagesWithoutContent() {
		return "select MSG_ID,MSG_TYPE,READER,ADAPTER,MSG_TIME,STATUS,COMMENT,MARK " +
			"from " + TABLE_LLRP_REPOSITORY + " " +
			"order by MSG_TIME DESC";
	}
	
	// ------ adaptor given ------
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that selects all the messages to a given adapter 
	 * with the content.
	 */
	protected String sqlSelectByAdapterWithContent() { 
		return "select * from " + TABLE_LLRP_REPOSITORY + " where ADAPTER=? " +
			"order by MSG_TIME DESC";
	}
	
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that selects all the messages to a given adapter 
	 * without the content.
	 */
	protected String sqlSelectByAdapterWithoutContent() { 
		return "select MSG_ID,MSG_TYPE,READER,ADAPTER,MSG_TIME,STATUS,COMMENT,MARK " +
			"from " + TABLE_LLRP_REPOSITORY + " where ADAPTER=? " +
			"order by MSG_TIME DESC";
	}
	
	// ------ reader and adaptor given ------
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that selects all the messages to a given adapter 
	 * and a given reader with the content.
	 */
	protected String sqlSelectByAdapterAndReaderWithContent() {
		return "select * from " + TABLE_LLRP_REPOSITORY + " where ADAPTER=? and READER=? " +
			"order by MSG_TIME DESC";
	}
	
	/**
	 * <strong>NOTICE:</strong> this SQL command corresponds to derby SQL!. So 
	 * override, if your database uses different SQL instructions.
	 * @return a SQL command that selects all the messages to a given adapter 
	 * and a given reader without the content.
	 */
	protected String sqlSelectByAdapterAndReaderWithoutContent() { 
		return "select MSG_ID,MSG_TYPE,READER,ADAPTER,MSG_TIME,STATUS,COMMENT,MARK " +
			"from " + TABLE_LLRP_REPOSITORY + " where ADAPTER=? and READER=? " +
			"order by MSG_TIME DESC";
	}

	
    /**
     * Loads the appropriate JDBC driver for this environment/framework. 
     * @return true if the loading went fine, false otherwise.
     */
	protected boolean loadDriver() {
    	boolean isHealth = false;
    	final String driver = getDBDriver();
    	try {
            Class.forName(driver).newInstance();
            log.info(String.format("Loaded the appropriate driver: %s",
            		driver));
            isHealth = true;
        } catch (ClassNotFoundException cnfe) {
            log.warn("Unable to load the JDBC driver " + driver);
            log.warn("Please check your CLASSPATH.");
            cnfe.printStackTrace(System.err);
        } catch (InstantiationException ie) {
        	log.warn("Unable to instantiate the JDBC driver " + driver);
            ie.printStackTrace(System.err);
        } catch (IllegalAccessException iae) {
        	log.warn("Not allowed to access the JDBC driver " + driver);
            iae.printStackTrace(System.err);
        }
        return isHealth;
    }
	
	/**
	 * Returns the class name of the JDBC driver to be used. <strong>NOTICE
	 * </strong>: you should override this method if you use a database other 
	 * than derby.
	 * @return a class name of the JDBC driver to be used.
	 */
	protected String getDBDriver() {
		return DB_DRIVER;
	}
	
	/**
	 * Opens the JDBC connection to the database.
	 * @return a handle to the Connection item.
	 * @throws Exception whenever the connection could not be established.
	 */
	protected abstract Connection openConnection() throws Exception;
	
	public void initialize(Map<String, String> args) 
		throws LLRPRuntimeException {

		this.args = args;
		
		username = args.get(RepositoryFactory.ARG_USERNAME);
		password = args.get(RepositoryFactory.ARG_PASSWRD);
		connectURL = args.get(RepositoryFactory.ARG_JDBC_STRING);
		try {
			wipe = Boolean.parseBoolean(args.get(RepositoryFactory.ARG_WIPE_DB));
			wipeROAccess = Boolean.parseBoolean(
				args.get(RepositoryFactory.ARG_WIPE_RO_ACCESS_REPORTS_DB));
		} catch (NumberFormatException e) {
			wipe = false;
			wipeROAccess = false;
			log.error("wrong boolean value in args table for wipe-db|wipe-ro" + 
					" - using defaults (false).");
		}
		
		// check if values are set correctly.
		if (null == username) {
			throw new LLRPRuntimeException("username missing in args table.");
		}
		if (null == password) {
			throw new LLRPRuntimeException("password missing in args table.");
		}
		if (null == connectURL) {
			throw new LLRPRuntimeException("connectURL missing in args table.");
		}

		//load the desired JDBC driver
		isHealth = loadDriver();
		log.debug("database driver loaded.");
		
		try {
			conn = openConnection();
		} catch (Exception e) {
			isHealth = false;
			throw new LLRPRuntimeException(e);
		}
		log.info("Connection Established");
	
		// wipe table if erroneous or if user requests it by preferences.
		if (!existsTable() || wipe) {
			dropTable();
			createTable();
		}
	}
	
	public Map<String, String> getArgs() {
		return args;
	}
	
	/**
	 * checks whether the required tables exist or not.
	 * @return true if everything is ok, false otherwise.
	 */
	protected boolean existsTable() {
		// we try to make a SQL query. if it fails, we assume the table to be dead...
		try {
			DatabaseMetaData dbMeta = conn.getMetaData();
			ResultSet resultSet = dbMeta.getColumns(
					null, null, TABLE_LLRP_REPOSITORY, null);
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
	protected void dropTable() {
		try {
			
			Statement sDropTable = conn.createStatement();
			sDropTable.execute(sqlDropTable());
			
			log.info("Existing Table Removed.");
			
		} catch (Exception e) {
			log.info("Table doesn't exist. Remove failed." + e.getMessage());
		}
	}
	
	/**
	 * generates the necessary tables.
	 */
	protected void createTable() {
		try {
			
			Statement sCreateTable = conn.createStatement();
			
			// In first time, the message table will be created. If the table
			// exists. The Exception will be triggered.
			//
			sCreateTable.execute(sqlCreateTable());
			sCreateTable.close();
			
			log.info("New Table Created.");
		} catch (Exception e) {
			log.info("Table exists. " + e.getMessage());
		}
	}
	
	/**
	 * store an LLRP message into the repository.
	 * @param aMessage the message to be stored.
	 */
	public void put(LLRPMessageItem aMessage) {
		try {
			PreparedStatement psInsert = conn.prepareStatement(sqlInsertMessage());

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
					aMessage.getTime());
			psInsert.setString(SELECTOR_CONTENT, aMessage.getContent());
			psInsert.setString(SELECTOR_COMMENT, aMessage.getComment());
			psInsert.setString(SELECTOR_MARK, "" + aMessage.getMark());
			psInsert.setString(SELECTOR_STATUS, aMessage.getStatusCode());
			
			psInsert.executeUpdate();
			psInsert.close();
		
			log.debug("Put Message (ID=" + aMessage.getId() + ") into database.");
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}
	}
	
	/**
	 * remove all the messages from the repository.
	 */
	public void clearAll() {
		try {
			PreparedStatement psRemoveAll = 
				conn.prepareStatement(sqlRemoveAllMessages());
			psRemoveAll.executeUpdate();
			psRemoveAll.close();
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}
	}
	
	/**
	 * @return true if the repository is ok, false otherwise.
	 */
	public boolean isHealth() {
		return isHealth;
	}

	/**
	 * the method computes the number of messages stored in the repository 
	 * depending on the input parameters:
	 * <ol>
	 * 	<li>(adaptor == null) then compute all messages in the repository.</li>
	 *  <li>(adaptor != null) && (reader == null) then compute all the messages 
	 *  for the adapter ignoring the name of the reader.</li>
	 *  <li>(adaptor != null) && (reader != null) then compute all the messages 
	 *  for the adapter where the reader name is equal to reader.</li> 
	 * </ol>
	 * @param adaptor the name of the adapter.
	 * @param reader the name of the reader.
	 * @return the number of messages stored in the repository.
	 */
	public int count(String adaptor, String reader) {
				
		int rowcount = 0;
		try {
			Statement stmt = conn.createStatement();
			String query = "SELECT COUNT(*) FROM LLRP_MSG";
			if (null == adaptor) {
				// all OK
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

	/**
	 * clear the repository from entries to a given adapter.
	 * @param adapter the name of the adapter to clean out.
	 */
	public void clearAdapter(String adapter) {
		try {
			PreparedStatement psRemove = conn.prepareStatement(
					sqlRemoveAllAdapterMessages());
			psRemove.setString(1, adapter);
			psRemove.executeUpdate();
			psRemove.close();
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}		
	}

	/**
	 * clear the repository from entries to a given adapter and a given reader.
	 * @param adapter the name of the adapter.
	 * @param reader the name of the reader.
	 */
	public void clearReader(String adapter, String reader) {
		try {
			PreparedStatement psRemove = conn.prepareStatement(
					sqlRemoveAllReaderMessages());
			psRemove.setString(1, adapter);
			psRemove.setString(2, reader);
			psRemove.executeUpdate();
			psRemove.close();
		} catch (SQLException sqle) {
            sqle.printStackTrace();
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
					(Constants.ROOT_NAME.equals(
							adaptorName))) {	
				sql = sqlSelectMessagesWithoutContent();
				if (content) {
					sql = sqlSelectMessagesWithContent();
				}
				st = conn.prepareStatement(sql);
			} else if (readerName != null) {
				sql = sqlSelectByAdapterAndReaderWithoutContent();
				if (content) {
					sql = sqlSelectByAdapterAndReaderWithContent();
				}
				st = conn.prepareStatement(sql);
				st.setString(1, adaptorName.trim());
				st.setString(2, readerName.trim());
			} else {
				sql = sqlSelectByAdapterWithoutContent();
				if (content) {
					sql = sqlSelectByAdapterWithContent();
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
				item.setTime(results.getTimestamp(SELECTOR_TIMESTAMP));
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
	
	/**
	 * @param aMsgSysId the message id of the item to be retrieved.
	 * @return the LLRP message to the given message id.
	 */
	public LLRPMessageItem get(String aMsgSysId) {
		
		LLRPMessageItem msg = new LLRPMessageItem();
		
		try {
			PreparedStatement psSelect = conn.prepareStatement(sqlSelectMessageByID());
			psSelect.setString(1, aMsgSysId);
			ResultSet results = psSelect.executeQuery();
			
			if (results.next()) {
				msg.setId(results.getString(SELECTOR_ID));
				msg.setMessageType(results.getString(SELECTOR_MESSAGE_TYPE));
				msg.setReader(results.getString(SELECTOR_READER));
				msg.setAdapter(results.getString(SELECTOR_ADAPTOR));
				msg.setTime(results.getTimestamp(SELECTOR_TIMESTAMP));
				msg.setContent(results.getString(SELECTOR_CONTENT));
				msg.setComment(results.getString(SELECTOR_COMMENT));
				msg.setMark(results.getInt(SELECTOR_MARK));
				msg.setStatusCode(results.getString(SELECTOR_STATUS));
				
				log.debug("Get Message (ID=" + results.getString(1) + ") from database.");
			}
			
			psSelect.close();
			results.close();
			
		} catch (SQLException sqle) {
            sqle.printStackTrace();
		}
		
		return msg;
	}
	
	/**
	 * @return a handle to the database connection. users of the repository are 
	 * allowed to use the database for their own purposes.
	 */
	public Connection getDBConnection() {
		return conn;
	}
}
