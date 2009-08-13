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

package org.fosstrak.llrp.client.repository.sql.roaccess;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.client.ROAccessReportsRepository;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.client.RepositoryFactory;
import org.fosstrak.llrp.client.repository.sql.DerbyRepository;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.UnsignedLong;

/**
 * Gives access to the database holding logged RO_ACCESS_REPORTS. There are 
 * several constants helping you to simplify your SQL code. If your database 
 * does not support the datatypes used in {@link DerbyROAccessReportsRepository} 
 * you should subclass from this class and invoke the respective class from 
 * your repository implementation {@link Repository}.
 * @author sawielan
 *
 */
public abstract class AbstractSQLROAccessReportsRepository implements ROAccessReportsRepository {
	
	// the connection to the database.
	private Connection conn = null;
	
	// log4j logger.
	private static Logger log = Logger.getLogger(AbstractSQLROAccessReportsRepository.class);
	
	// flag whether the table is up and OK.
	private boolean tableOk = false;
	
	// only report connection error at the first time.
	private boolean reportErrorFirstTime = true;
	
	/** the name of the RO_ACCESS_REPORTS table. */
	public static final String TABLE_RO_ACCESS_REPORTS 
		= "table_ro_access_reports";
	
	// NOTICE: COLUMN INDEX IN DERBY BEGINS WITH 1
	/** column index of the log time.*/ 
	public static final int CINDEX_LOGTIME = 					1;
	
	/** column index of the adapter name.*/
	public static final int CINDEX_ADAPTER = 					2;
	
	/** column index of the reader name.*/
	public static final int CINDEX_READER = 					3;
	
	/** column index of the EPC value.*/
	public static final int CINDEX_EPC = 						4;
	
	/** column index of the RO spec ID.*/
	public static final int CINDEX_ROSpecID = 					5;
	
	/** column index of the spec index.*/
	public static final int CINDEX_SpecIndex = 					6;
	
	/** column index of the inventory parameter spec ID.*/
	public static final int CINDEX_InventoryParameterSpecID = 	7;
	
	/** column index of the antenna ID.*/
	public static final int CINDEX_AntennaID = 					8;
	
	/** column index of the peak RSSI.*/
	public static final int CINDEX_PeakRSSI = 					9;
	
	/** column index of the channel index.*/
	public static final int CINDEX_ChannelIndex = 				10;
	
	/** column index of the first seen time stamp in UTC.*/
	public static final int CINDEX_FirstSeenTimestampUTC = 		11;
	
	/** column index of the first seen time stamp since uptime.*/
	public static final int CINDEX_FirstSeenTimestampUptime = 	12;
	
	/** column index of the last seen time stamp in UTC.*/
	public static final int CINDEX_LastSeenTimestampUTC = 		13;
	
	/** column index of the last seen time stamp since uptime.*/
	public static final int CINDEX_LastSeenTimestampUptime = 	14;
	
	/** column index of the tag seen count.*/
	public static final int CINDEX_TagSeenCount = 				15;
	
	/** column index of the c1g2 crc.*/
	public static final int CINDEX_C1G2_CRC = 					16;
	
	/** column index of the c1g2 pc.*/
	public static final int CINDEX_C1G2_PC = 					17;
	
	/** column index of the access spec ID.*/
	public static final int CINDEX_AccessSpecID = 				18;
	
	/** the number of columns in the table. */
	public static final int NUM_COLUMNS = 18;
	
	/** the repository "owning" this item. */
	protected Repository repository;
	
	/** whether to wipe the database at startup or not. */
	protected boolean wipe = false;
	
	/**
	 * creates a new handle to the RO_ACCESS_REPORTS repository. The default 
	 * constructor uses the connection from the {@link DerbyRepository}.
	 */
	public AbstractSQLROAccessReportsRepository() {	

	}
	
	public void setRepository(Repository repository) {
		this.repository = repository;
	}
	
	/**
	 * @return a SQL creating the necessary table.
	 */
	protected abstract String sqlCreateTable();
	
	/**
	 * @return a SQL dropping the table.
	 */
	protected abstract String sqlDropTable();
	
	/**
	 * @return a SQL allowing to insert a new log item.
	 */
	protected abstract String sqlInsert();
	
	/** flag, whether this repository is initialized or not. */
	protected boolean initialized = false;
	
	public void initialize(Repository repository) 
		throws LLRPRuntimeException {
		
		if (initialized) return; 
		
		wipe = Boolean.parseBoolean(repository.getArgs().get(
				RepositoryFactory.ARG_WIPE_RO_ACCESS_REPORTS_DB));
		this.repository = repository;
		
		conn = repository.getDBConnection();
		
		try {
			init();
		} catch (Exception e) {
			log.error("could not connect to database or database is corrupt: " + 
					e.getMessage());
			conn = null;
		}
		initialized = true;
	}
	
	/**
	 * initialize the table. (create it, if not existing yet).
	 * @throws Exception when the connection could not be established or if the 
	 * database is corrupted (and repair mode was not able to fix it).
	 */
	private void init() throws Exception {
		boolean recreate = false;
		if (!checkIfTableOk()) {
			recreate = true;
			log.error("table for RO_ACCESS_REPORT not ok, (re)create it.");
		}
		
		if (recreate || wipe) {
			dropTable();
			createTable();
		}
		
		tableOk = true;
	}

	/**
	 * drop the log table.
	 */
	protected boolean dropTable() {
		try {
			Statement drop = conn.createStatement();
			drop.execute(sqlDropTable());
			
			log.info(String.format("Removed table '%s'", 
					TABLE_RO_ACCESS_REPORTS));			
		} catch (Exception e) {
			log.error(String.format("Could not remove table '%s': %s", 
					TABLE_RO_ACCESS_REPORTS, e.getMessage()));
			return false;
		}
		return true;
	}

	/**
	 * create a new log table.
	 */
	protected boolean createTable() {
		try {
			String sqlCreate = sqlCreateTable();
			log.debug(String.format("creating table with SQL %s", sqlCreate));			
			Statement create = conn.createStatement();	
			create.execute(sqlCreate);
			create.close();
			
			log.info(String.format("Created table '%s'", 
					TABLE_RO_ACCESS_REPORTS));
		} catch (Exception e) {
			log.info(String.format("Could not create table '%s': %s",
					TABLE_RO_ACCESS_REPORTS, e.getMessage()));
			return false;
		}	
		return true;
	}
	
	/**
	 * checks whether the required tables exist or not.
	 * @return true if everything is OK, false otherwise.
	 */
	protected boolean checkIfTableOk() {		
		// we try to make a SQL query. if it fails, we assume the table to be dead...
		try {
			DatabaseMetaData dbMeta = conn.getMetaData();
			ResultSet resultSet = dbMeta.getColumns(
					null, null, TABLE_RO_ACCESS_REPORTS, null);
			int n = 0;
			while (resultSet.next()) {
				n++;
			}
			final int len = NUM_COLUMNS;
			if (n < len) {
				throw new SQLException(
						String.format("missing fields. %d instead of %d.",
							n, len));
			}
			 
		} catch (SQLException e) {
			log.error("table erroneous or missing.");
			return false;
		}
		return true;
	}
	
	/**
	 * @return true if the table is up and OK, false otherwise.
	 */
	public boolean isTableOK() {
		return tableOk;
	}

	public void handle(String adapterName, String readerName,
			LLRPMessage message) {
		
		if ((null == conn) && reportErrorFirstTime) {
			log.error("connection to the repository could not be established.");
			
			return;
		}
		
		if (message instanceof RO_ACCESS_REPORT) {
			handleROAccessReport(adapterName, 
					readerName, (RO_ACCESS_REPORT) message);
		}
	}

	/**
	 * write an RO_ACCESS_REPORT into the database.
	 * @param adapterName name of the adapter.
	 * @param readerName name of the reader.
	 * @param message the LLRP RO_ACCESS_REPORT to be logged.
	 */
	protected void handleROAccessReport(
			String adapterName, 
			String readerName,
			RO_ACCESS_REPORT message) {
		
		log.debug("logging RO_ACCESS_REPORT to database.");
		
		List<ROAccessItem> items = ROAccessItem.parse(
				message, adapterName, readerName, System.currentTimeMillis());
		int successfullyHandled = 0;
		for (ROAccessItem item : items) {
			try {
				
				String sqlInsert = sqlInsert();
				
				PreparedStatement insert = conn.prepareStatement(sqlInsert);
	
				// log time.
				insert.setTimestamp(CINDEX_LOGTIME, item.getLogTime());
				
				// adapter name.
				insert.setString(CINDEX_ADAPTER, item.getAdapterName());
				
				// reader name.
				insert.setString(CINDEX_READER, item.getReaderName());
				
				// store the EPC as EPC96 or EPCData
				if (null != item.getEpc()) {
					insert.setString(CINDEX_EPC, item.getEpc());
				} else {
					insert.setNull(CINDEX_EPC, Types.VARCHAR);
				}
				
				// RO Spec ID.
				if (null != item.getRoSpecID()) {
					insert.setLong(CINDEX_ROSpecID, item.getRoSpecID());
				} else {
					insert.setNull(CINDEX_ROSpecID, Types.BIGINT);
				}
				
				// spec index.
				if (null != item.getSpecIndex()) {
					insert.setInt(CINDEX_SpecIndex, item.getSpecIndex());
				} else {
					insert.setNull(CINDEX_SpecIndex, Types.INTEGER);
				}
				
				// inventory parameter spec ID.
				if (null != item.getInventoryPrmSpecID()) {
					insert.setInt(CINDEX_InventoryParameterSpecID, 
							item.getInventoryPrmSpecID());	
				} else {
					insert.setNull(CINDEX_InventoryParameterSpecID, Types.INTEGER);
				}
				
				// antenna ID.
				if (null != item.getAntennaID()) {
					insert.setInt(CINDEX_AntennaID, item.getAntennaID());
				} else {
					insert.setNull(CINDEX_AntennaID, Types.INTEGER);
				}
				
				// peak RSSI.
				if (null != item.getPeakRSSI()) {
					insert.setShort(CINDEX_PeakRSSI, item.getPeakRSSI());
				} else {
					insert.setNull(CINDEX_PeakRSSI, Types.SMALLINT);
				}
				
				// channel index.
				if (null != item.getChannelIndex()) {
					insert.setInt(CINDEX_ChannelIndex, item.getChannelIndex());
				} else {
					insert.setNull(CINDEX_ChannelIndex, Types.INTEGER);
				}
				
				// extract the first seen UTC time stamp.
				if (null != item.getFirstSeenUTC()) {
					insert.setTimestamp(CINDEX_FirstSeenTimestampUTC, 
							item.getFirstSeenUTC());
				} else {
					insert.setNull(CINDEX_FirstSeenTimestampUTC, Types.TIMESTAMP);
				}
				
				// extract the first seen since uptime time stamp.
				if (null != item.getFirstSeenUptime()) {
					insert.setTimestamp(CINDEX_FirstSeenTimestampUptime, 
							item.getFirstSeenUptime());
				} else {
					insert.setNull(CINDEX_FirstSeenTimestampUptime, Types.TIMESTAMP);
				}
				
				// extract the last seen time stamp UTC.
				if (null != item.getLastSeenUTC()) {
					insert.setTimestamp(CINDEX_LastSeenTimestampUTC, 
							item.getLastSeenUTC());
				}else {
					insert.setNull(CINDEX_LastSeenTimestampUTC, Types.TIMESTAMP);
				}
				
				// extract the last seen time stamp since uptime.
				if (null != item.getLastSeenUptime()) {
					insert.setTimestamp(CINDEX_LastSeenTimestampUptime, 
							item.getLastSeenUptime());
				}else {
					insert.setNull(CINDEX_LastSeenTimestampUptime, Types.TIMESTAMP);
				}
				
				// extract the tag count.
				if (null != item.getTagSeenCount()) {
					insert.setInt(CINDEX_TagSeenCount, item.getTagSeenCount());
				} else {
					insert.setNull(CINDEX_TagSeenCount, Types.INTEGER);
				}
				
				// crc
				if (null != item.getC1g2_CRC()) {
					insert.setInt(CINDEX_C1G2_CRC, item.getC1g2_CRC());
				} else {
					insert.setNull(CINDEX_C1G2_CRC, Types.INTEGER);
				}
				if (null != item.getC1g2_CRC()) {
					insert.setInt(CINDEX_C1G2_PC, item.getC1g2_PC());
				} else {
					insert.setNull(CINDEX_C1G2_PC, Types.INTEGER);
				}
				
				// extract the access spec ID.
				if (null != item.getAccessSpecID()) {
					insert.setLong(CINDEX_AccessSpecID, item.getAccessSpecID());
				} else {
					insert.setNull(CINDEX_AccessSpecID, Types.BIGINT);
				}
				
				insert.executeUpdate();
				insert.close();
				
				successfullyHandled++;
			} catch (Exception e) {
				log.debug("Could not log entry of RO_ACCESS_REPORT to the " +
						"database - ignoring the entry.");
			}
			log.debug(
					String.format("Successfully stored %s row(s) into database.",
							successfullyHandled));
		}
	}
	
	public List<ROAccessItem> getAll() throws Exception {
		List<ROAccessItem> items = new LinkedList<ROAccessItem> ();
		Statement s = repository.getDBConnection().createStatement();
		String sql = String.format("SELECT * FROM %s", TABLE_RO_ACCESS_REPORTS);
		ResultSet res = s.executeQuery(sql);
		
		while (res.next()) {
			ROAccessItem item = new ROAccessItem();
			
			item.setLogTime(res.getTimestamp(CINDEX_LOGTIME));
			item.setAdapterName(res.getString(CINDEX_ADAPTER));
			item.setReaderName(res.getString(CINDEX_READER));
			item.setEpc(res.getString(CINDEX_EPC));
			item.setRoSpecID(res.getLong(CINDEX_ROSpecID));
			item.setSpecIndex(res.getInt(CINDEX_SpecIndex));
			item.setInventoryPrmSpecID(
					res.getInt(CINDEX_InventoryParameterSpecID));	
			item.setAntennaID(res.getInt(CINDEX_AntennaID));
			item.setPeakRSSI(res.getShort(CINDEX_PeakRSSI));
			item.setChannelIndex(res.getInt(CINDEX_ChannelIndex));
			item.setFirstSeenUTC(res.getTimestamp(
					CINDEX_FirstSeenTimestampUTC));
			item.setFirstSeenUptime(
					res.getTimestamp(CINDEX_FirstSeenTimestampUptime));
			item.setLastSeenUTC(
					res.getTimestamp(CINDEX_LastSeenTimestampUTC));
			item.setLastSeenUptime(
					res.getTimestamp(CINDEX_LastSeenTimestampUptime));
			item.setTagSeenCount(res.getInt(CINDEX_TagSeenCount));
			item.setC1g2_CRC(res.getInt(CINDEX_C1G2_CRC));
			item.setC1g2_PC(res.getInt(CINDEX_C1G2_PC));
			item.setAccessSpecID(res.getLong(CINDEX_AccessSpecID));
			items.add(item);
		}
		res.close();
		return items;
	}
	
	public void clear() throws Exception {
		Statement s = repository.getDBConnection().createStatement();
		String sql = String.format("DELETE FROM %s", TABLE_RO_ACCESS_REPORTS);
		s.execute(sql);
	}

	/**
	 * Creates a TimeStamp object from a {@link UnsignedLong} object.
	 * @param ulong the unsigned long TimeStamp object.
	 * @return a SQL {@link Timestamp} object.
	 */
	public static Timestamp extractTimestamp(UnsignedLong ulong) {
		try {
//			log.debug(String.format("Extracting timestamp '%s'", ulong.toString()));
			BigInteger value = ulong.toBigInteger();
			final long tsMillis = value.divide(new BigInteger("1000")).longValue();
//			log.debug(String.format("Timestamp in Milliseconds: %d", tsMillis));
			
			final int l = value.toString().length();
			// we need the milliseconds and the microseconds to assemble the 
			// fractional seconds part.
			String fractSeconds = value.toString().substring(l-6, l);
			final int nanoseconds = Integer.parseInt(fractSeconds) * 1000;
//			log.debug(String.format(
//					"Fractional Seconds Part: %s ms, Nanoseconds: %d ns",
//					fractSeconds, nanoseconds));
			
			Timestamp ts = new Timestamp(tsMillis);
			ts.setNanos(nanoseconds);
//			log.debug(String.format("Generated Timestamp: %s", ts.toString()));
			return ts;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
