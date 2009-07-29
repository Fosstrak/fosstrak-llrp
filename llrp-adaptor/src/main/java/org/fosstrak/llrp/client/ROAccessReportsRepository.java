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

package org.fosstrak.llrp.client;

import org.fosstrak.llrp.adaptor.AdaptorManagement;

/**
 * Common interface for all the implementations providing access to the 
 * RO_ACCESS_REPORTS data-base. The actual implementation of the interface 
 * is chosen at runtime via the strategy pattern from the respective Context 
 * (in this case the implementation of the {@link Repository} interface). The 
 * interface extends the {@link MessageHandler} interface, in order to be able 
 * to receive LLRP RO_ACCESS_REPORTS messages. 
 * <h3>NOTICE:</h3> The registration at the {@link AdaptorManagement} is done 
 * automatically for the implementing class. So do this ONLY, if you know 
 * exactly what you are planing to do (otherwise messages might get logged 
 * twice!!!).
 * @author sawielan
 *
 */
public interface ROAccessReportsRepository extends MessageHandler {
}
