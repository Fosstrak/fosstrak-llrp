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

package org.fosstrak.llrp.commander;

import org.fosstrak.llrp.client.LLRPExceptionHandler;
import org.fosstrak.llrp.client.LLRPExceptionHandlerTypeMap;
import org.fosstrak.llrp.commander.views.ReaderExplorerView;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public class ExceptionHandler implements LLRPExceptionHandler {
	
	
	
	private final static String DIALOG_CAPTION = "LLRP Client Warning";
	
	private Shell shell;
	
	public ExceptionHandler(Shell aShell) {
		shell = aShell;
	}
	
	public void postExceptionToGUI(LLRPExceptionHandlerTypeMap exceptionType, LLRPRuntimeException e, String adaptorName, String readerName) {
		

		final LLRPExceptionHandlerTypeMap aExceptionType = exceptionType;
		final String aAdapter = adaptorName;
		final String aReader = readerName;
		final LLRPRuntimeException ex = e;
		
		// we need a special thread access to post a method to a swt widget concurrently.
		shell.getDisplay().asyncExec(new Runnable () {

			public void run() {
				switch (aExceptionType) {
					case EXCEPTION_ADAPTOR_MANAGEMENT_NOT_INITIALIZED: {
						processMessage("AdaptorManagement is not initialized." + '\n' + 
								ex.getMessage());
						break;
					}
					case EXCEPTION_ADAPTOR_MANAGEMENT_CONFIG_NOT_STORABLE: {
						processMessage("Could not store the configuration." + '\n' + 
								ex.getMessage());
						break;
					}
					case EXCEPTION_ADAPTOR_ALREADY_EXISTS: {
						processMessage("Adaptor already exists: " +  aAdapter);
						break;
					}
					case EXCEPTION_READER_NOT_EXIST: {
						processReaderNotExist(aAdapter, aReader);
						break;
					}
					case EXCEPTION_ADAPTER_NOT_EXIST: {
						processAdapterNotExist(aAdapter);
						break;
					}
					case EXCEPTION_READER_LOST: {
						processReaderLost(aAdapter, aReader);
						break;
					}
					case EXCEPTION_ADAPTER_LOST: {
						processAdapterLost(aAdapter);
						break;
					}
					case EXCEPTION_MSG_SENDING_ERROR: {
						processSendingError(aAdapter, aReader);
						break;
					}
					case EXCEPTION_NO_READER_CONFIG_MSG: {
						processNoReaderConfigError(aAdapter, aReader);
						break;
					}
					case EXCEPTION_NO_ROSPEC_MSG: {
						processNoROSpecError(aAdapter, aReader);
						break;
					}
					case EXCEPTION_MSG_SYNTAX_ERROR: {
						processMsgSyntaxError();
						break;
					}
					default: {
						processGeneralException(aAdapter, aReader);
					}
				}
				ResourceCenter.getInstance().
					getReaderExplorerView().refresh();
			}

		});

	}
	
	private void processReaderNotExist(String aAdapter, String aReader) {
		String aText = "Reader " + aReader + " @ " + aAdapter + " is not found!";
		MessageDialog.openWarning(shell, DIALOG_CAPTION, aText);
	}
	
	private void processAdapterNotExist(String aAdapter) {
		String aText = "Adapter " + aAdapter + " is not found!";
		MessageDialog.openWarning(shell, DIALOG_CAPTION, aText);
	}
	
	private void processReaderLost(String aAdapter, String aReader) {
		String aText = "Reader " + aReader + " @ " + aAdapter + " is lost!";
		MessageDialog.openWarning(shell, DIALOG_CAPTION, aText);
	}
	
	private void processAdapterLost(String aAdapter) {
		String aText = "Adapter " + aAdapter + " is lost!";
		MessageDialog.openWarning(shell, DIALOG_CAPTION, aText);
	}
	
	private void processSendingError(String aAdapter, String aReader) {
		String aText = "Sending Message to " + aReader + " @ " + aAdapter + " failed!";
		MessageDialog.openWarning(shell, DIALOG_CAPTION, aText);
	}
	
	private void processNoReaderConfigError(String aAdapter, String aReader) {
		String aText = "No existing GET_READER_CONFIG_RESPONSE message \nfrom " + aReader + " @ " + aAdapter + ".\nPlease send GET_READER_CONFIG first.";
		MessageDialog.openWarning(shell, DIALOG_CAPTION, aText);
	}
	
	private void processNoROSpecError(String aAdapter, String aReader) {
		String aText = "No existing GET_ROSPECS_RESPONSE message \nfrom " + aReader + " @ " + aAdapter + ".\nPlease send GET_ROSPECS first.";
		MessageDialog.openWarning(shell, DIALOG_CAPTION, aText);
	}
	
	private void processGeneralException(String aAdapter, String aReader) {
		String aText = "Unkown Error!";
		MessageDialog.openWarning(shell, DIALOG_CAPTION, aText);
	}
	
	private void processMsgSyntaxError() {
		String aText = "XML Syntax Error!";
		MessageDialog.openWarning(shell, DIALOG_CAPTION, aText);
	}
	
	private void processMessage(String message) {
		MessageDialog.openWarning(shell, DIALOG_CAPTION, message);
	}
}
