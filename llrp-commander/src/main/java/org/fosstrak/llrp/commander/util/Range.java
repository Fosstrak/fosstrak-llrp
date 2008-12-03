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
