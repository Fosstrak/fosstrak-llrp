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

package org.fosstrak.llrp.client.repository.sql.roaccess.test;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.fosstrak.llrp.client.repository.sql.roaccess.AbstractSQLROAccessReportsRepository;
import org.fosstrak.llrp.client.repository.sql.roaccess.ROAccessItem;
import org.junit.Test;
import org.llrp.ltk.generated.interfaces.AirProtocolTagData;
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
import org.llrp.ltk.types.SignedByte;
import org.llrp.ltk.types.UnsignedInteger;
import org.llrp.ltk.types.UnsignedLong_DATETIME;
import org.llrp.ltk.types.UnsignedShort;

/**
 * test the ROAccessItem.
 * @author swieland
 *
 */
public class ROAccessItemTest {
	
	private final static Long RO_SPEC_ID = 1000L;
	private final static Integer RO_SPEC_INDEX = 1001;
	private final static Integer RO_SPEC_INV_INDEX = 1002;
	private final static Integer ANTENNA_ID = 1003;
	private final static Integer PEAK_RSSI = 1004;
	private final static Integer CHANNEL_INDEX = 1005;
	private final static Long SEEN_TS = 100006L;
	private final static Integer TAG_COUNT = 1007;
	private final static Long ACCESS_SPEC_ID = 1008L;
	private final static Integer C1G2PC = 1009;
	private final static Integer C1G2CRC = 1010;

	@Test
	public void testParse() {
		final long now = System.currentTimeMillis();
		final String adapterName = "adapterName";
		final String readerName = "readerName";
		
		final ROSpecID roSpecId = EasyMock.createMock(ROSpecID.class);
		EasyMock.expect(roSpecId.getROSpecID()).andReturn(new UnsignedInteger(RO_SPEC_ID)).atLeastOnce();
		final SpecIndex specIndex = EasyMock.createMock(SpecIndex.class);
		EasyMock.expect(specIndex.getSpecIndex()).andReturn(new UnsignedShort(RO_SPEC_INDEX)).atLeastOnce();
		final InventoryParameterSpecID invParmSpecId = EasyMock.createMock(InventoryParameterSpecID.class);
		EasyMock.expect(invParmSpecId.getInventoryParameterSpecID()).andReturn(new UnsignedShort(RO_SPEC_INV_INDEX)).atLeastOnce();
		final AntennaID antennaId = EasyMock.createMock(AntennaID.class);
		EasyMock.expect(antennaId.getAntennaID()).andReturn(new UnsignedShort(ANTENNA_ID)).atLeastOnce();
		final PeakRSSI peakRSSI = EasyMock.createMock(PeakRSSI.class);
		EasyMock.expect(peakRSSI.getPeakRSSI()).andReturn(new SignedByte(PEAK_RSSI)).atLeastOnce();
		final ChannelIndex channelIndex = EasyMock.createMock(ChannelIndex.class);
		EasyMock.expect(channelIndex.getChannelIndex()).andReturn(new UnsignedShort(CHANNEL_INDEX)).atLeastOnce();
		final FirstSeenTimestampUTC firstSeenTSUTC = EasyMock.createMock(FirstSeenTimestampUTC.class);
		EasyMock.expect(firstSeenTSUTC.getMicroseconds()).andReturn(new UnsignedLong_DATETIME(SEEN_TS)).atLeastOnce();
		final FirstSeenTimestampUptime firstSeenTSUptime = EasyMock.createMock(FirstSeenTimestampUptime.class);
		EasyMock.expect(firstSeenTSUptime.getMicroseconds()).andReturn(new UnsignedLong_DATETIME(SEEN_TS)).atLeastOnce();
		final LastSeenTimestampUTC lastSeenTSUTC = EasyMock.createMock(LastSeenTimestampUTC.class);
		EasyMock.expect(lastSeenTSUTC.getMicroseconds()).andReturn(new UnsignedLong_DATETIME(SEEN_TS)).atLeastOnce();
		final LastSeenTimestampUptime lastSeenTSUptime = EasyMock.createMock(LastSeenTimestampUptime.class);
		EasyMock.expect(lastSeenTSUptime.getMicroseconds()).andReturn(new UnsignedLong_DATETIME(SEEN_TS)).atLeastOnce();
		final TagSeenCount tagSeenCount = EasyMock.createMock(TagSeenCount.class);
		EasyMock.expect(tagSeenCount.getTagCount()).andReturn(new UnsignedShort(TAG_COUNT)).atLeastOnce();
		final AccessSpecID accessSpecId = EasyMock.createMock(AccessSpecID.class);
		EasyMock.expect(accessSpecId.getAccessSpecID()).andReturn(new UnsignedInteger(ACCESS_SPEC_ID)).atLeastOnce();
		
		final String epc = "3069c336d2797f9802345bd3";
		
		EPC_96 epc96 = EasyMock.createMock(EPC_96.class);
		EasyMock.expect(epc96.getEPC()).andReturn(new Integer96_HEX(epc));
		
		EPCData epcData = EasyMock.createMock(EPCData.class);
		EasyMock.expect(epcData.getEPC()).andReturn(new BitArray_HEX(epc));
		
		TagReportData tData = EasyMock.createMock(TagReportData.class);
		EasyMock.expect(tData.getEPCParameter()).andReturn(epc96);
		EasyMock.expect(tData.getROSpecID()).andReturn(roSpecId);
		EasyMock.expect(tData.getSpecIndex()).andReturn(specIndex);
		EasyMock.expect(tData.getInventoryParameterSpecID()).andReturn(invParmSpecId);
		EasyMock.expect(tData.getAntennaID()).andReturn(antennaId);
		EasyMock.expect(tData.getPeakRSSI()).andReturn(peakRSSI);
		EasyMock.expect(tData.getChannelIndex()).andReturn(channelIndex);
		EasyMock.expect(tData.getFirstSeenTimestampUTC()).andReturn(firstSeenTSUTC);
		EasyMock.expect(tData.getFirstSeenTimestampUptime()).andReturn(firstSeenTSUptime);
		EasyMock.expect(tData.getLastSeenTimestampUTC()).andReturn(lastSeenTSUTC);
		EasyMock.expect(tData.getLastSeenTimestampUptime()).andReturn(lastSeenTSUptime);
		EasyMock.expect(tData.getTagSeenCount()).andReturn(tagSeenCount);
		
		C1G2_PC g2_pc = EasyMock.createMock(C1G2_PC.class);
		EasyMock.expect(g2_pc.getPC_Bits()).andReturn(new UnsignedShort(C1G2PC)).atLeastOnce();
		C1G2_CRC g2_crc = EasyMock.createMock(C1G2_CRC.class);
		EasyMock.expect(g2_crc.getCRC()).andReturn(new UnsignedShort(C1G2CRC)).atLeastOnce();
		List<AirProtocolTagData> airProtocolTagData = new LinkedList<AirProtocolTagData>();
		airProtocolTagData.add(g2_pc);
		airProtocolTagData.add(g2_crc);
		airProtocolTagData.add(null);
		
		EasyMock.expect(tData.getAirProtocolTagDataList()).andReturn(airProtocolTagData);
		EasyMock.expect(tData.getAccessSpecID()).andReturn(accessSpecId);
		

		TagReportData tData2 = EasyMock.createMock(TagReportData.class);
		EasyMock.expect(tData2.getEPCParameter()).andReturn(epcData);
		EasyMock.expect(tData2.getROSpecID()).andReturn(roSpecId);
		EasyMock.expect(tData2.getSpecIndex()).andReturn(specIndex);
		EasyMock.expect(tData2.getInventoryParameterSpecID()).andReturn(invParmSpecId);
		EasyMock.expect(tData2.getAntennaID()).andReturn(antennaId);
		EasyMock.expect(tData2.getPeakRSSI()).andReturn(peakRSSI);
		EasyMock.expect(tData2.getChannelIndex()).andReturn(channelIndex);
		EasyMock.expect(tData2.getFirstSeenTimestampUTC()).andReturn(firstSeenTSUTC);
		EasyMock.expect(tData2.getFirstSeenTimestampUptime()).andReturn(firstSeenTSUptime);
		EasyMock.expect(tData2.getLastSeenTimestampUTC()).andReturn(lastSeenTSUTC);
		EasyMock.expect(tData2.getLastSeenTimestampUptime()).andReturn(lastSeenTSUptime);
		EasyMock.expect(tData2.getTagSeenCount()).andReturn(tagSeenCount);
		EasyMock.expect(tData2.getAirProtocolTagDataList()).andReturn(airProtocolTagData);
		EasyMock.expect(tData2.getAccessSpecID()).andReturn(accessSpecId);
		
		TagReportData tData3 = EasyMock.createMock(TagReportData.class);
		EasyMock.expect(tData3.getEPCParameter()).andReturn(null);
		EasyMock.expect(tData3.getROSpecID()).andReturn(roSpecId);
		EasyMock.expect(tData3.getSpecIndex()).andReturn(specIndex);
		EasyMock.expect(tData3.getInventoryParameterSpecID()).andReturn(invParmSpecId);
		EasyMock.expect(tData3.getAntennaID()).andReturn(antennaId);
		EasyMock.expect(tData3.getPeakRSSI()).andReturn(peakRSSI);
		EasyMock.expect(tData3.getChannelIndex()).andReturn(channelIndex);
		EasyMock.expect(tData3.getFirstSeenTimestampUTC()).andReturn(firstSeenTSUTC);
		EasyMock.expect(tData3.getFirstSeenTimestampUptime()).andReturn(firstSeenTSUptime);
		EasyMock.expect(tData3.getLastSeenTimestampUTC()).andReturn(lastSeenTSUTC);
		EasyMock.expect(tData3.getLastSeenTimestampUptime()).andReturn(lastSeenTSUptime);
		EasyMock.expect(tData3.getTagSeenCount()).andReturn(tagSeenCount);
		EasyMock.expect(tData3.getAirProtocolTagDataList()).andReturn(airProtocolTagData);
		EasyMock.expect(tData3.getAccessSpecID()).andReturn(accessSpecId);
		
		final List<TagReportData> tagReportData = new LinkedList<TagReportData> ();
		tagReportData.add(tData);
		tagReportData.add(tData2);
		tagReportData.add(tData3);
		
		RO_ACCESS_REPORT report = EasyMock.createMock(RO_ACCESS_REPORT.class);		
		EasyMock.expect(report.getTagReportDataList()).andReturn(tagReportData);

		EasyMock.replay(g2_pc);
		EasyMock.replay(g2_crc);
		EasyMock.replay(accessSpecId);
		EasyMock.replay(tagSeenCount);
		EasyMock.replay(firstSeenTSUTC);
		EasyMock.replay(lastSeenTSUTC);
		EasyMock.replay(firstSeenTSUptime);
		EasyMock.replay(lastSeenTSUptime);
		EasyMock.replay(channelIndex);
		EasyMock.replay(peakRSSI);
		EasyMock.replay(antennaId);
		EasyMock.replay(invParmSpecId);
		EasyMock.replay(specIndex);
		EasyMock.replay(roSpecId);
		EasyMock.replay(epc96);
		EasyMock.replay(epcData);
		EasyMock.replay(tData3);
		EasyMock.replay(tData2);
		EasyMock.replay(tData);
		EasyMock.replay(report);
		
		List<ROAccessItem> parsedItems = ROAccessItem.parse(report, adapterName, readerName, now);
		
		Assert.assertSame(3, parsedItems.size());
		
		ROAccessItem item = parsedItems.get(0);
		Assert.assertEquals(readerName,  item.getAsString(AbstractSQLROAccessReportsRepository.CINDEX_READER));
		Assert.assertEquals(adapterName, item.getAsString(AbstractSQLROAccessReportsRepository.CINDEX_ADAPTER));
		Assert.assertEquals(0, ((Timestamp) item.get(AbstractSQLROAccessReportsRepository.CINDEX_LOGTIME)).compareTo(new Timestamp(now)));
		Assert.assertEquals(epc, ((String) item.get(AbstractSQLROAccessReportsRepository.CINDEX_EPC)));
		Assert.assertEquals(RO_SPEC_ID , ((Long) item.get(AbstractSQLROAccessReportsRepository.CINDEX_ROSpecID)));
		Assert.assertEquals(RO_SPEC_INDEX , ((Integer) item.get(AbstractSQLROAccessReportsRepository.CINDEX_SpecIndex)));
		Assert.assertEquals(RO_SPEC_INV_INDEX , ((Integer) item.get(AbstractSQLROAccessReportsRepository.CINDEX_InventoryParameterSpecID)));
		Assert.assertEquals(ANTENNA_ID , ((Integer) item.get(AbstractSQLROAccessReportsRepository.CINDEX_AntennaID)));
		Assert.assertEquals(new Short(new SignedByte(PEAK_RSSI).toByte()), ((Short) item.get(AbstractSQLROAccessReportsRepository.CINDEX_PeakRSSI)));
		Assert.assertEquals(CHANNEL_INDEX , ((Integer) item.get(AbstractSQLROAccessReportsRepository.CINDEX_ChannelIndex)));
		Assert.assertNotNull(item.get(AbstractSQLROAccessReportsRepository.CINDEX_FirstSeenTimestampUTC));
		Assert.assertNotNull(item.get(AbstractSQLROAccessReportsRepository.CINDEX_FirstSeenTimestampUptime));
		Assert.assertNotNull(item.get(AbstractSQLROAccessReportsRepository.CINDEX_LastSeenTimestampUTC));
		Assert.assertNotNull(item.get(AbstractSQLROAccessReportsRepository.CINDEX_LastSeenTimestampUptime));
		Assert.assertEquals(TAG_COUNT, ((Integer) item.get(AbstractSQLROAccessReportsRepository.CINDEX_TagSeenCount)));
		Assert.assertEquals(ACCESS_SPEC_ID, ((Long) item.get(AbstractSQLROAccessReportsRepository.CINDEX_AccessSpecID)));
		Assert.assertEquals(C1G2PC, ((Integer) item.get(AbstractSQLROAccessReportsRepository.CINDEX_C1G2_PC)));
		Assert.assertEquals(C1G2CRC, ((Integer) item.get(AbstractSQLROAccessReportsRepository.CINDEX_C1G2_CRC)));
		Assert.assertNull(item.get(-1));
		Assert.assertNull(item.getAsString(-1));

		ROAccessItem item2 = parsedItems.get(1);
		Assert.assertEquals(epc, item2.getAsString(AbstractSQLROAccessReportsRepository.CINDEX_EPC));
		
		ROAccessItem item3 = parsedItems.get(2);
		Assert.assertNull(epc, item3.getAsString(AbstractSQLROAccessReportsRepository.CINDEX_EPC));
		

		EasyMock.verify(g2_pc);
		EasyMock.verify(g2_crc);
		EasyMock.verify(accessSpecId);
		EasyMock.verify(tagSeenCount);
		EasyMock.verify(firstSeenTSUTC);
		EasyMock.verify(lastSeenTSUTC);
		EasyMock.verify(firstSeenTSUptime);
		EasyMock.verify(lastSeenTSUptime);
		EasyMock.verify(channelIndex);
		EasyMock.verify(peakRSSI);
		EasyMock.verify(antennaId);
		EasyMock.verify(invParmSpecId);
		EasyMock.verify(specIndex);
		EasyMock.verify(roSpecId);
		EasyMock.verify(tData3);
		EasyMock.verify(tData2);
		EasyMock.verify(tData);
		EasyMock.verify(report);
		EasyMock.verify(epc96);
		EasyMock.verify(epcData);
	}
}
