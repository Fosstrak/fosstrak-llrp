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

package org.fosstrak.llrp.commander.util;

/**
 * This class represents a numeric range.
 * 
 * @author Ulrich Etter, ETHZ
 *
 */
public class Range {
	
	private int lowerBound;
	private int upperBound;
	
	/**
	 * Creates a new range.
	 * 
	 * @param lowerBound the lower bound of the range; use <code>Integer.MIN_VALUE</code> 
	 * if this range shall not have a lower bound
	 * @param upperBound the upper bound of the range; use <code>Integer.MAX_VALUE</code> 
	 * if this range shall not have an upper bound
	 */
	public Range(int lowerBound, int upperBound){
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}

	/**
	 * @return the lower bound of this range
	 */
	public int getLowerBound() {
		return lowerBound;
	}

	/**
	 * @return the upper bound of this range
	 */
	public int getUpperBound() {
		return upperBound;
	}
}
