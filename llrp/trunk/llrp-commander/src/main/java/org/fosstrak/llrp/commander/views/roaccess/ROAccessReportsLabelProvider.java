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

package org.fosstrak.llrp.commander.views.roaccess;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.fosstrak.llrp.client.repository.sql.roaccess.AbstractSQLROAccessReportsRepository;
import org.fosstrak.llrp.client.repository.sql.roaccess.ROAccessItem;

/**
 * Provides the labels for the table in {@link ROAccessReportsView}.
 * @author sawielan
 *
 */
public class ROAccessReportsLabelProvider extends LabelProvider implements
		ITableLabelProvider {

	public Image getColumnImage(Object arg0, int index) {
		return null;
	}

	public String getColumnText(Object arg0, int index) {
		
		if (arg0 instanceof ROAccessItem) {
			ROAccessItem item = (ROAccessItem) arg0;
			index++;	// increase by one to fit to derby index.

			try {
				switch (index) {
				case AbstractSQLROAccessReportsRepository.CINDEX_LOGTIME:
					return item.getLogTime().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_ADAPTER:
					return item.getAdapterName();
				case AbstractSQLROAccessReportsRepository.CINDEX_READER:
					return item.getReaderName();
				case AbstractSQLROAccessReportsRepository.CINDEX_EPC:
					return item.getEpc();
				case AbstractSQLROAccessReportsRepository.CINDEX_ROSpecID:
					return item.getRoSpecID().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_SpecIndex:
					return item.getSpecIndex().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_InventoryParameterSpecID:
					return item.getInventoryPrmSpecID().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_AntennaID:
					return item.getAntennaID().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_PeakRSSI:
					return item.getPeakRSSI().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_ChannelIndex:
					return item.getChannelIndex().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_FirstSeenTimestampUTC:
					return item.getFirstSeenUTC().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_FirstSeenTimestampUptime:
					return item.getFirstSeenUptime().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_LastSeenTimestampUTC:
					return item.getLastSeenUTC().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_LastSeenTimestampUptime:
					return item.getLastSeenUptime().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_TagSeenCount:
					return item.getTagSeenCount().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_C1G2_CRC:
					return item.getC1g2_CRC().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_C1G2_PC:
					return item.getC1g2_PC().toString();
				case AbstractSQLROAccessReportsRepository.CINDEX_AccessSpecID:
					return item.getAccessSpecID().toString();
				}
			} catch (Exception e) {
				// values might be null..
				return "";
			}
		}
		return "";
	}

}
