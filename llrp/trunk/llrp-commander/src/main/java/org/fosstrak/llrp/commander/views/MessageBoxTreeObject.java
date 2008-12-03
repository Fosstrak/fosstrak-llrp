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

package org.fosstrak.llrp.commander.views;

import org.eclipse.core.runtime.IAdaptable;

public class MessageBoxTreeObject implements IAdaptable {
	
	public final static String INBOX_NAME = "Inbox";
	public final static String OUTBOX_NAME = "Outbox";
	
	private String name;
	private ReaderTreeObject parentReader;
	
	public MessageBoxTreeObject(String aName) {
		name = aName;
	}
	public String getName() {
		return name;
	}
	public void setParent(ReaderTreeObject aParent) {
		parentReader = aParent;
	}
	public ReaderTreeObject getParent() {
		return parentReader;
	}
	public String toString() {
		return getName();
	}
	public Object getAdapter(Class key) {
		return null;
	}
}
