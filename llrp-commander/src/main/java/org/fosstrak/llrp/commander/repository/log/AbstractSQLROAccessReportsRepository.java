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

package org.fosstrak.llrp.commander.repository.log;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.client.ROAccessReportsRepository;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.commander.ResourceCenter;
import org.fosstrak.llrp.commander.repository.JavaDBRepository;
import org.llrp.ltk.generated.interfaces.AirProtocolTagData;
import org.llrp.ltk.generated.interfaces.EPCParameter;
import org.llrp.ltk.generated.messages.RO_ACCESS_REPORT;
import org.llrp.ltk.generated.parameters.AccessSpecID;
import org.llrp.ltk.generated.parameters.AntennaID;
import org.llrp.ltk.generated.parameters.C1G2_CRC;
import org.llrp.ltk.generated.parameters.C1G2_PC;
import org.llrp.ltk.generated.parameters.ChannelIndex;
import org.llrp.ltk.generated.parameters.EPCData;
import org.llrp.ltk.generated.parameters.EPC_96;
import org.llrp.ltk.generated.parameters.FirstSeenTimestampUTC;
import org.llrp.ltk.generated.parameters.FirstSeenTimestampUptime;
import org.llrp.ltk.generated.parameters.InventoryParameterSpecID;
import org.llrp.ltk.generated.parameters.LastSeenTimestampUTC;
import org.llrp.ltk.generated.parameters.LastSeenTimestampUptime;
import org.llrp.ltk.generated.parameters.PeakRSSI;
import org.llrp.ltk.generated.parameters.ROSpecID;
import org.llrp.ltk.generated.parameters.SpecIndex;
import org.llrp.ltk.generated.parameters.TagReportData;
import org.llrp.ltk.generated.parameters.TagSeenCount;
import org.llrp.ltk.types.BitArray_HEX;
import org.llrp.ltk.types.Integer96_HEX;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.UnsignedLong;
import org.llrp.ltk.types.UnsignedLong_DATETIME;

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
		= "TABLE_RO_ACCESS_REPORTS";
	
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
	
	/**
	 * creates a new handle to the RO_ACCESS_REPORTS repository. The default 
	 * constructor uses the connection from the {@link JavaDBRepository}.
	 */
	public AbstractSQLROAccessReportsRepository() {	
		conn = ResourceCenter.getInstance().
			getRepository().getDBConnection();
		
		try {
			init();
		} catch (Exception e) {
			log.error("could not connect to database or database is corrupt: " + 
					e.getMessage());
			conn = null;
		}
	}
	
	/**
	 * @return a SQL creating the necessary table.
	 */
	protected abstract String sqlCreateTable();
	
	protected abstract String sqlDropTable();
	
	/**
	 * @return a SQL allowing to insert a new log item.
	 */
	protected abstract String sqlInsert();
	
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
		
		if (recreate || ResourceCenter.getInstance().isWipeLogROAccessReportsOnStartup()) {
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
	 * @return true if everything is ok, false otherwise.
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
				throw new SQLException("missing fields");
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
		List<TagReportData> tagDataList = message.getTagReportDataList();
		int successfullyHandled = 0;
		for (TagReportData tagData : tagDataList) {
			try {
				
				String sqlInsert = sqlInsert();
				
				PreparedStatement insert = conn.prepareStatement(sqlInsert);
	
				// log time.
				insert.setDate(CINDEX_LOGTIME, 
						new Date(System.currentTimeMillis()));
				
				// adapter name.
				insert.setString(CINDEX_ADAPTER, adapterName);
				
				// reader name.
				insert.setString(CINDEX_READER, readerName);
				
				// store the EPC as EPC96 or EPCData
				EPCParameter epcParameter = tagData.getEPCParameter();
				String epc = null;
				if (epcParameter instanceof EPC_96) {
					EPC_96 epc96 = (EPC_96) epcParameter;
					Integer96_HEX hex = epc96.getEPC();
					String hx = hex.toString();
					epc = hx;
				} else if (epcParameter instanceof EPCData){
					EPCData epcData = (EPCData) epcParameter;
					BitArray_HEX hex = epcData.getEPC();
					String hx = hex.toString();
					epc = hx;
				} else {
					log.error("Unknown EPCParameter encountered - ignoring.");
				}
				if (null != epc) {
					insert.setString(CINDEX_EPC, epc);
				} else {
					insert.setNull(CINDEX_EPC, Types.VARCHAR);
				}
				
				// RO Spec ID.
				ROSpecID roSpecID = tagData.getROSpecID();
				if ((null != roSpecID) && (null != roSpecID.getROSpecID())) {
					insert.setLong(CINDEX_ROSpecID, roSpecID.getROSpecID().toLong());
				} else {
					insert.setNull(CINDEX_ROSpecID, Types.BIGINT);
				}
				
				// spec index.
				SpecIndex specIndex = tagData.getSpecIndex();
				if ((null != specIndex) && (null != specIndex.getSpecIndex())) {
					insert.setInt(CINDEX_SpecIndex, specIndex.getSpecIndex().toInteger());
				} else {
					insert.setNull(CINDEX_SpecIndex, Types.INTEGER);
				}
				
				// inventory parameter spec ID.
				InventoryParameterSpecID inventoryPrmSpecID = 
					tagData.getInventoryParameterSpecID();
				if ((null != inventoryPrmSpecID) && (null != inventoryPrmSpecID.getInventoryParameterSpecID())) {
					insert.setInt(CINDEX_InventoryParameterSpecID, 
							tagData.getInventoryParameterSpecID().
							getInventoryParameterSpecID().toInteger());	
				} else {
					insert.setNull(CINDEX_InventoryParameterSpecID, Types.INTEGER);
				}
				
				// antenna ID.
				AntennaID antennaID = tagData.getAntennaID();
				if ((null != antennaID) && (null != antennaID.getAntennaID())) {
					insert.setInt(CINDEX_AntennaID, antennaID.getAntennaID().toInteger());
				} else {
					insert.setNull(CINDEX_AntennaID, Types.INTEGER);
				}
				
				// peak RSSI.
				PeakRSSI peakRSSI = tagData.getPeakRSSI();
				if ((null != peakRSSI) && (null != peakRSSI.getPeakRSSI())) {
					insert.setShort(CINDEX_PeakRSSI, peakRSSI.getPeakRSSI().toByte());
				} else {
					insert.setNull(CINDEX_PeakRSSI, Types.SMALLINT);
				}
				
				// channel index.
				ChannelIndex channelIndex = tagData.getChannelIndex();
				if ((null != channelIndex) && (null != channelIndex.getChannelIndex())) {
					insert.setInt(CINDEX_ChannelIndex, channelIndex.getChannelIndex().toInteger());
				} else {
					insert.setNull(CINDEX_ChannelIndex, Types.INTEGER);
				}
				
				// extract the first seen UTC time stamp.
				FirstSeenTimestampUTC frstSnUTC = 
					tagData.getFirstSeenTimestampUTC();
				if ((null != frstSnUTC) && (null != frstSnUTC.getMicroseconds())) {
					insert.setTimestamp(CINDEX_FirstSeenTimestampUTC, 
							extractTimestamp(frstSnUTC.getMicroseconds()));
				} else {
					insert.setNull(CINDEX_FirstSeenTimestampUTC, Types.TIMESTAMP);
				}
				
				// extract the first seen since uptime time stamp.
				FirstSeenTimestampUptime frstSnUptime = 
					tagData.getFirstSeenTimestampUptime();
				if ((null != frstSnUptime) && (null != frstSnUptime.getMicroseconds())) {
					insert.setTimestamp(CINDEX_FirstSeenTimestampUptime, 
							extractTimestamp(frstSnUptime.getMicroseconds()));
				} else {
					insert.setNull(CINDEX_FirstSeenTimestampUptime, Types.TIMESTAMP);
				}
				
				// extract the last seen time stamp UTC.
				LastSeenTimestampUTC lstSnUTC = 
					tagData.getLastSeenTimestampUTC();
				if ((null != lstSnUTC) && (null != lstSnUTC.getMicroseconds())) {
					insert.setTimestamp(CINDEX_LastSeenTimestampUTC, 
							extractTimestamp(lstSnUTC.getMicroseconds()));
				}else {
					insert.setNull(CINDEX_LastSeenTimestampUTC, Types.TIMESTAMP);
				}
				
				// extract the last seen time stamp since uptime.
				LastSeenTimestampUptime lstSnUptime = 
					tagData.getLastSeenTimestampUptime();
				if ((null != lstSnUptime) && (null != lstSnUptime.getMicroseconds())) {
					insert.setTimestamp(CINDEX_LastSeenTimestampUptime, 
							extractTimestamp(lstSnUptime.getMicroseconds()));
				}else {
					insert.setNull(CINDEX_LastSeenTimestampUptime, Types.TIMESTAMP);
				}
				
				// extract the tag count.
				TagSeenCount tagSeenCount = tagData.getTagSeenCount();
				if ((null != tagSeenCount) && (null != tagSeenCount.getTagCount())) {
					insert.setInt(CINDEX_TagSeenCount, tagSeenCount.getTagCount().toInteger());
				} else {
					insert.setNull(CINDEX_TagSeenCount, Types.INTEGER);
				}
				
				List<AirProtocolTagData> airProtoTagData = 
					tagData.getAirProtocolTagDataList();
				
				// first set the two to null.
				insert.setNull(CINDEX_C1G2_CRC, Types.INTEGER);
				insert.setNull(CINDEX_C1G2_PC, Types.INTEGER);
				for (AirProtocolTagData aptd : airProtoTagData) {
					if (aptd instanceof C1G2_CRC) {
						C1G2_CRC c1g2_crc = (C1G2_CRC) aptd;
						if ((null != c1g2_crc) && (null != c1g2_crc.getCRC())) {
							insert.setInt(CINDEX_C1G2_CRC, c1g2_crc.getCRC().toInteger());
						}
					} else if (aptd instanceof C1G2_PC) {
						C1G2_PC c1g2_pc = (C1G2_PC) aptd;
						if ((null != c1g2_pc) && (null != c1g2_pc.getPC_Bits())) {
							insert.setInt(CINDEX_C1G2_PC, c1g2_pc.getPC_Bits().toInteger());
						}
					} else {
						log.error("Unknown AirProtocolTagData item encountered.");
					}						
				}
				
				// extract the access spec ID.
				AccessSpecID accessSpecID = tagData.getAccessSpecID();
				if ((null != accessSpecID) && 
						(null != accessSpecID.getAccessSpecID())) {
					insert.setLong(CINDEX_AccessSpecID, accessSpecID.getAccessSpecID().toLong());
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

	/**
	 * Creates a TimeStamp object from a {@link UnsignedLong_DATETIME} object.
	 * @param ulong the unsigned long TimeStamp object.
	 * @return a SQL {@link Timestamp} object.
	 */
	public Timestamp extractTimestamp(UnsignedLong ulong) {
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
