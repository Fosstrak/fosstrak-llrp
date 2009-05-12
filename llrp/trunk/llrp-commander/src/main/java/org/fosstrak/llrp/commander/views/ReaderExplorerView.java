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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.llrp.ltk.generated.messages.DELETE_ACCESSSPEC;
import org.llrp.ltk.generated.messages.DELETE_ROSPEC;
import org.llrp.ltk.generated.messages.DISABLE_ACCESSSPEC;
import org.llrp.ltk.generated.messages.DISABLE_ROSPEC;
import org.llrp.ltk.generated.messages.ENABLE_ACCESSSPEC;
import org.llrp.ltk.generated.messages.ENABLE_ROSPEC;
import org.llrp.ltk.generated.messages.START_ROSPEC;
import org.llrp.ltk.generated.messages.STOP_ROSPEC;
import org.llrp.ltk.types.LLRPMessage;
import org.llrp.ltk.types.UnsignedInteger;

import org.fosstrak.llrp.adaptor.Adaptor;
import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.fosstrak.llrp.adaptor.exception.LLRPRuntimeException;
import org.fosstrak.llrp.commander.*;
import org.fosstrak.llrp.commander.dialogs.*;
import org.fosstrak.llrp.commander.util.LLRPConstraints;
import org.fosstrak.llrp.commander.util.LLRPFactory;
import org.fosstrak.llrp.commander.util.LLRPRangeConstraint;
import org.apache.log4j.Logger;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * The Reader Management View. It embeds one <code>TreeViewer</code>, and illustrates 
 * the Adapter/Reader relationship in tree-like hierarchy.
 * 
 * Only two partitions are defined, XML tag and XML comment.
 * 
 * @author Haoning Zhang
 * @author Ulrich Etter
 * @author sawielan
 * @version 1.0
 */
public class ReaderExplorerView extends ViewPart {

	/**
	 * Log4j instance.
	 */
	private static Logger log = Logger.getLogger(ReaderExplorerView.class);
	
	/**
	 * JFace Tree control for Reader hierarchy.
	 */
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private ReaderTreeObject currentSelectedReader;
	private boolean isFocusOnReaderTreeObject;
	
	private ExceptionHandler exceptionHandler;
	
	/**
	 * Add Reader Action
	 */
	private Action actionAddReader;
	
	/**
	 * Remove Reader Action.
	 */
	private Action actionRemoveReader;
	
	/**
	 * Add Adapter Action
	 */
	private Action actionAddAdapter;
	
	/**
	 * Remove Adaptor Action
	 */
	private Action actionRemoveAdaptor;
	
	/**
	 * Action triggered when user click the Connect menu item.
	 */
	private Action actionConnect;
	private Action actionDisconnect;
	private Action actionRefresh;
	
	private Action actionGetReaderCapabilities;
	private Action actionGetReaderConfig;
	
	private Action actionGetRospecs;
	private Action actionEnableRospec;
	private Action actionDisableRospec;
	private Action actionStartRospec;
	private Action actionStopRospec;
	private Action actionDeleteRospec;

	private Action actionGetAccessspecs;
	private Action actionEnableAccessspec;
	private Action actionDisableAccessspec;
	private Action actionDeleteAccessspec;
	
	private Action actionReaderSettings;
	/**
	 * Default Constructor.
	 */
	public ReaderExplorerView() {
		isFocusOnReaderTreeObject = false;
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ReaderExplorerViewContentProvider());
		viewer.setLabelProvider(new ReaderExplorerViewLabelProvider());
		viewer.setInput(getViewSite());
		viewer.addSelectionChangedListener(new ReaderChangeListenser());
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();

		//Publish the Reader selection to MessageboxView, then the MessageboxView can refresh the window 
		getSite().setSelectionProvider(viewer);
		
		exceptionHandler = new ExceptionHandler(viewer.getControl().getShell());
		ResourceCenter.getInstance().setExceptionHandler(exceptionHandler);
		
		viewer.expandAll();
		ResourceCenter.getInstance().setReaderExplorerView(this);
	}
	
	class ReaderChangeListenser implements ISelectionChangedListener {
		public void selectionChanged(SelectionChangedEvent event) {
			
			// if the selection is empty then quit
			if (event.getSelection().isEmpty()) {
				return;
			}

			if (event.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				
				Object selectedObj = selection.getFirstElement();
				if (selectedObj instanceof ReaderTreeObject) {
					ReaderTreeObject reader = (ReaderTreeObject) selectedObj;
					currentSelectedReader = reader;
					isFocusOnReaderTreeObject = true;
				} else if (selectedObj instanceof MessageBoxTreeObject) {
					MessageBoxTreeObject msgBox = (MessageBoxTreeObject) selectedObj;
					currentSelectedReader = msgBox.getParent();
					isFocusOnReaderTreeObject = false;
				}
			}
		}
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ReaderExplorerView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionAddReader);
		manager.add(new Separator());
		manager.add(actionRemoveReader);
	}

	/**
	 * Construct the context menu.
	 * @param manager IMenuManager instance
	 */
	private void fillContextMenu(IMenuManager manager) {
		if (null != currentSelectedReader) {
			if (isFocusOnReaderTreeObject && currentSelectedReader.isReader()) {
				if (currentSelectedReader.isConnected()) {
					actionConnect.setEnabled(false);
					actionDisconnect.setEnabled(true);
					actionRemoveReader.setEnabled(false);
					
					actionGetReaderCapabilities.setEnabled(true);
					actionGetReaderConfig.setEnabled(true);
					
					actionGetRospecs.setEnabled(true);
					actionEnableRospec.setEnabled(true);
					actionDisableRospec.setEnabled(true);
					actionStartRospec.setEnabled(true);
					actionStopRospec.setEnabled(true);
					actionDeleteRospec.setEnabled(true);
					
					actionGetAccessspecs.setEnabled(true);
					actionEnableAccessspec.setEnabled(true);
					actionDisableAccessspec.setEnabled(true);
					actionDeleteAccessspec.setEnabled(true);
					
				} else {
					actionConnect.setEnabled(true);
					actionDisconnect.setEnabled(false);
					actionRemoveReader.setEnabled(true);
					
					actionGetReaderCapabilities.setEnabled(false);
					actionGetReaderConfig.setEnabled(false);
					
					actionGetRospecs.setEnabled(false);
					actionEnableRospec.setEnabled(false);
					actionDisableRospec.setEnabled(false);
					actionStartRospec.setEnabled(false);
					actionStopRospec.setEnabled(false);
					actionDeleteRospec.setEnabled(false);
					
					actionGetAccessspecs.setEnabled(false);
					actionEnableAccessspec.setEnabled(false);
					actionDisableAccessspec.setEnabled(false);
					actionDeleteAccessspec.setEnabled(false);
				
				}
				

				actionReaderSettings.setEnabled(true);

				manager.add(actionConnect);
				manager.add(actionDisconnect);
				manager.add(actionRemoveReader);
				
				MenuManager sendMessageMenuManager = new MenuManager("Send Message", "sendMessage");
				
				sendMessageMenuManager.add(actionGetReaderCapabilities);
				sendMessageMenuManager.add(actionGetReaderConfig);
				sendMessageMenuManager.add(new Separator());
				sendMessageMenuManager.add(actionGetRospecs);
				sendMessageMenuManager.add(actionEnableRospec);
				sendMessageMenuManager.add(actionDisableRospec);
				sendMessageMenuManager.add(actionStartRospec);
				sendMessageMenuManager.add(actionStopRospec);
				sendMessageMenuManager.add(actionDeleteRospec);
				sendMessageMenuManager.add(new Separator());
				sendMessageMenuManager.add(actionGetAccessspecs);
				sendMessageMenuManager.add(actionEnableAccessspec);
				sendMessageMenuManager.add(actionDisableAccessspec);
				sendMessageMenuManager.add(actionDeleteAccessspec);
				
				manager.add(sendMessageMenuManager);

				sendMessageMenuManager.add(new Separator());
				manager.add(actionReaderSettings);
			} else if (isFocusOnReaderTreeObject){
				// get the name of the currently selected adaptor item
				String adaptorName = currentSelectedReader.getName();
				try {
					// by default do not show the remove adaptor item
					actionRemoveAdaptor.setEnabled(false);
					actionAddAdapter.setEnabled(false);
					actionAddReader.setEnabled(false);
					
					// check whether the requested adaptor is registered in the mgmt
					if (AdaptorManagement.getInstance().containsAdaptor(adaptorName)) {
						Adaptor adaptor = (Adaptor) AdaptorManagement.getInstance().
							getAdaptor(adaptorName);
						
						// default adaptor cannot be deleted therefore showing the 
						// delete action only when the selected adaptor is not the 
						// default adaptor.
						if (!adaptor.getAdaptorName().equalsIgnoreCase(
							AdaptorManagement.DEFAULT_ADAPTOR_NAME)) {
							
							actionRemoveAdaptor.setEnabled(true);
						} else {
							actionAddReader.setEnabled(true);
						}
					} else {
						actionAddAdapter.setEnabled(true);
					}
				} catch (Exception e) {
					log.error("caught exception when adding undefine adaptor menu");
					e.printStackTrace();
				}
				
				
				manager.add(actionRemoveAdaptor);
				manager.add(actionAddAdapter);
				manager.add(actionAddReader);
			}
		}
		//drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		//manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionAddAdapter);
		manager.add(actionAddReader);
		manager.add(actionRefresh);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		
		actionAddAdapter = new Action() {
			public void run() {
				
				// Open the Dialog
				AddFCDialog dlg = new AddFCDialog(viewer.getControl().getShell());
				if (dlg.open() == Window.CANCEL) {
					return;
				}
				
				String message = null;
				try {
					// Define the Adapter in Adapter Management module.
					AdaptorManagement.getInstance().define(dlg.getName(), dlg.getIP());
				} catch (LLRPRuntimeException llrpe) {
					log.info(llrpe.getMessage());
					message = llrpe.getMessage();
				} catch (NotBoundException nbe) {
					log.info(nbe.getMessage());
					message = nbe.getMessage();
				} catch (RemoteException re) {
					log.info(re.getMessage());
					message = re.getMessage();
				}
				if (null != message) {
					MessageDialog.openWarning(viewer.getControl().getShell(), "Could not create Adapter", message);
				}
				
				refresh();
			}
		};
		
		actionAddAdapter.setText("Add Adapter Instance");
		actionAddAdapter.setToolTipText("Manually Add Adapter Instance");
		actionAddAdapter.setImageDescriptor(ResourceCenter.getInstance().getImageDescriptor("adapter.gif"));
		
		
		actionAddReader = new Action() {
			public void run() {
				
				// Open the Dialog
				AddReaderDialog dlg = new AddReaderDialog(viewer.getControl().getShell());
				if (dlg.open() == Window.CANCEL) {
					return;
				}
				
				String message = null;
				try {
					// Define the Adapter in Adapter Management module.
					Adaptor localAdapter = AdaptorManagement.getInstance().getDefaultAdaptor();
					localAdapter.define(dlg.getName(), dlg.getIP(), dlg.getPort(), true, true);
				} catch (LLRPRuntimeException llrpe) {
					log.info(llrpe.getMessage());
					message = llrpe.getMessage();
				} catch (RemoteException re) {
					log.info(re.getMessage());
					message = re.getMessage();
				}
				if (null != message) {
					MessageDialog.openWarning(viewer.getControl().getShell(), "Could not create Reader", message);
				}
				
				log.debug("Refreshing the Reader Tree...");
				viewer.refresh(true);
				viewer.expandAll();
			}
		};
		
		actionAddReader.setText("Add RFID Reader");
		actionAddReader.setToolTipText("Manually Add Local RFID Reader");
		actionAddReader.setImageDescriptor(ResourceCenter.getInstance().getImageDescriptor("reader.gif"));
				
		
		actionRemoveReader = new Action() {
			public void run() {
				if ((null != currentSelectedReader) && (currentSelectedReader.isReader())) {
				
					try {
						Adaptor localAdapter = AdaptorManagement.getInstance().getDefaultAdaptor();
						localAdapter.undefine(currentSelectedReader.getName());
					} catch (LLRPRuntimeException llrpe) {
						llrpe.printStackTrace();
					} catch (RemoteException re) {
						re.printStackTrace();
					}
					
					log.debug("Refreshing the Reader Tree...");
					viewer.refresh(true);
					viewer.expandAll();
				}
			}
		};
		actionRemoveReader.setText("Remove RFID Reader");
		actionRemoveReader.setToolTipText("Manually Remove RFID Reader");
		actionRemoveReader.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		
		actionRefresh = new Action() {
			public void run() {
				log.debug("Refreshing the Reader Tree...");
				viewer.refresh(true);
				viewer.expandAll();
			}
		};
		actionRefresh.setText("Refresh");
		actionRefresh.setToolTipText("Refresh");
		actionRefresh.setImageDescriptor(ResourceCenter.getInstance().getImageDescriptor("refresh.gif"));
		
		/**
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showMessage("Double-click detected on "+obj.toString());
			}
		};
		*/
		
		actionConnect = new Action() {
			public void run() {
				if ((null != currentSelectedReader) && (currentSelectedReader.isReader())) {
					ReaderTreeObject adapterNode = currentSelectedReader.getParent();
					
					try {
						Adaptor adaptor = AdaptorManagement.getInstance().getAdaptor(adapterNode.getName());
						adaptor.getReader(currentSelectedReader.getName()).connect(true);
					} catch (LLRPRuntimeException llrpe) {
						llrpe.printStackTrace();
					} catch (RemoteException re) {
						re.printStackTrace();
					}
				}
				
				viewer.refresh(true);
				viewer.expandAll();
			}
		};
		actionConnect.setText("Connect");
		actionConnect.setToolTipText("Connect to RFID Reader");
		actionConnect.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
		
		actionDisconnect = new Action() {
			public void run() {
				log.debug("Disconnecting...");
				if ((null != currentSelectedReader) && (currentSelectedReader.isReader())) {
					
					log.debug("Disconnecting " + currentSelectedReader.getName());
					
					ReaderTreeObject adapterNode = currentSelectedReader.getParent();
					
					try {
						Adaptor adaptor = AdaptorManagement.getInstance().getAdaptor(adapterNode.getName());
						log.debug("Geting adapter " + adaptor.getAdaptorName());
						adaptor.getReader(currentSelectedReader.getName()).disconnect();
						log.debug("Disconnecting from Adapter " + currentSelectedReader.getName());
					} catch (LLRPRuntimeException llrpe) {
						llrpe.printStackTrace();
					} catch (RemoteException re) {
						re.printStackTrace();
					}
				}
				viewer.refresh(true);
				viewer.expandAll();
			}
		};
		actionDisconnect.setText("Disconnect");
		actionDisconnect.setToolTipText("Disconnect from local adapter");
		actionDisconnect.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
		
		actionReaderSettings = new Action() {
			public void run() {
				if ((null != currentSelectedReader) && (currentSelectedReader.isReader())) {
					
					String adapter = currentSelectedReader.getParent().getName();
					String reader = currentSelectedReader.getName();
					new ReaderSettingsDialog(viewer.getControl().getShell(), adapter, reader).open();
				}
			}
		};
		actionReaderSettings.setText("Settings");
		actionReaderSettings.setToolTipText("Settings for the reader");
		
		actionGetReaderCapabilities = createSendMessageAction("GET_READER_CAPABILITIES");
		actionGetReaderConfig = createSendMessageAction("GET_READER_CONFIG");
		
		actionGetRospecs = createSendMessageAction("GET_ROSPECS");
		actionEnableRospec = createSendMessageAction("ENABLE_ROSPEC");
		actionDisableRospec = createSendMessageAction("DISABLE_ROSPEC");
		actionStartRospec = createSendMessageAction("START_ROSPEC");
		actionStopRospec = createSendMessageAction("STOP_ROSPEC");
		actionDeleteRospec = createSendMessageAction("DELETE_ROSPEC");
		
		actionGetAccessspecs = createSendMessageAction("GET_ACCESSSPECS");
		actionEnableAccessspec = createSendMessageAction("ENABLE_ACCESSSPEC");
		actionDisableAccessspec = createSendMessageAction("DISABLE_ACCESSSPEC");
		actionDeleteAccessspec = createSendMessageAction("DELETE_ACCESSSPEC");
					
		
		// action to remove a remote instance of an adaptor
		actionRemoveAdaptor = new Action() {
			public void run() {
				if ((currentSelectedReader != null) && (!currentSelectedReader.isReader())) {
					// get the name of the currently selected adaptor item
					String adaptorName = currentSelectedReader.getName();
					try {
						
						// only call delete method if adaptor is present in the mgmt
						if (AdaptorManagement.getInstance().containsAdaptor(adaptorName)) {
							Adaptor adaptor = (Adaptor) AdaptorManagement.getInstance().
								getAdaptor(adaptorName);
							
							// default adaptor cannot be deleted.
							if (!adaptor.getAdaptorName().equalsIgnoreCase(
								AdaptorManagement.DEFAULT_ADAPTOR_NAME)) {
								
								// delete the adaptor
								AdaptorManagement.getInstance().undefine(adaptorName);
							}
						}
						
						viewer.refresh(true);
						viewer.expandAll();
					} catch (Exception e) {
						log.error("caught exception when undefining adaptor instance");
						e.printStackTrace();
					}
					
					
				}
			}
		};
		actionRemoveAdaptor.setText("Delete Adaptor");
		actionRemoveAdaptor.setToolTipText("Delete a remote adaptor");
		actionRemoveAdaptor.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
	}

	private Action createSendMessageAction(final String messageName) {
		Action result = new Action() {
			public void run() {
				if ((null != currentSelectedReader) && (currentSelectedReader.isReader())) {
					LLRPMessage message;
					if (messageName.equals("DELETE_ROSPEC")){
						InputDialog dlg = new InputDialog(viewer.getControl().getShell(), messageName, "ROSpecID:", "1", new IDValidator("ROSpecID", "DELETE_ROSPEC"));
						if (dlg.open() == Window.OK) {
							try {
								message = (DELETE_ROSPEC) LLRPFactory.createLLRPMessage("DELETE_ROSPEC");	
								UnsignedInteger rOSpecID = new UnsignedInteger(dlg.getValue());
								((DELETE_ROSPEC) message).setROSpecID(rOSpecID);
							}
							catch (Exception e){
								e.printStackTrace();
								return;
							}
						}
						else{
							return;
						}
					}
					else if (messageName.equals("START_ROSPEC")){
						InputDialog dlg = new InputDialog(viewer.getControl().getShell(), messageName, "ROSpecID:", "1", new IDValidator("ROSpecID", "START_ROSPEC"));
						if (dlg.open() == Window.OK) {
							try {
								message = (START_ROSPEC) LLRPFactory.createLLRPMessage("START_ROSPEC");	
								UnsignedInteger rOSpecID = new UnsignedInteger(dlg.getValue());
								((START_ROSPEC) message).setROSpecID(rOSpecID);
							}
							catch (Exception e){
								e.printStackTrace();
								return;
							}
						}
						else{
							return;
						}
					}
					else if (messageName.equals("STOP_ROSPEC")){
						InputDialog dlg = new InputDialog(viewer.getControl().getShell(), messageName, "ROSpecID:", "1", new IDValidator("ROSpecID", "STOP_ROSPEC"));
						if (dlg.open() == Window.OK) {
							try {
								message = (STOP_ROSPEC) LLRPFactory.createLLRPMessage("STOP_ROSPEC");	
								UnsignedInteger rOSpecID = new UnsignedInteger(dlg.getValue());
								((STOP_ROSPEC) message).setROSpecID(rOSpecID);
							}
							catch (Exception e){
								e.printStackTrace();
								return;
							}
						}
						else{
							return;
						}
					}
					else if (messageName.equals("ENABLE_ROSPEC")){
						InputDialog dlg = new InputDialog(viewer.getControl().getShell(), messageName, "ROSpecID:", "1", new IDValidator("ROSpecID", "ENABLE_ROSPEC"));
						if (dlg.open() == Window.OK) {
							try {
								message = (ENABLE_ROSPEC) LLRPFactory.createLLRPMessage("ENABLE_ROSPEC");	
								UnsignedInteger rOSpecID = new UnsignedInteger(dlg.getValue());
								((ENABLE_ROSPEC) message).setROSpecID(rOSpecID);
							}
							catch (Exception e){
								e.printStackTrace();
								return;
							}
						}
						else{
							return;
						}
					}
					else if (messageName.equals("DISABLE_ROSPEC")){
						InputDialog dlg = new InputDialog(viewer.getControl().getShell(), messageName, "ROSpecID:", "1", new IDValidator("ROSpecID", "DISABLE_ROSPEC"));
						if (dlg.open() == Window.OK) {
							try {
								message = (DISABLE_ROSPEC) LLRPFactory.createLLRPMessage("DISABLE_ROSPEC");	
								UnsignedInteger rOSpecID = new UnsignedInteger(dlg.getValue());
								((DISABLE_ROSPEC) message).setROSpecID(rOSpecID);
							}
							catch (Exception e){
								e.printStackTrace();
								return;
							}
						}
						else{
							return;
						}
					}
					else if (messageName.equals("DELETE_ACCESSSPEC")){
						InputDialog dlg = new InputDialog(viewer.getControl().getShell(), messageName, "AccessSpecID:", "1", new IDValidator("AccessSpecID", "DELETE_ACCESSSPEC"));
						if (dlg.open() == Window.OK) {
							try {
								message = (DELETE_ACCESSSPEC) LLRPFactory.createLLRPMessage("DELETE_ACCESSSPEC");	
								UnsignedInteger accessSpecID = new UnsignedInteger(dlg.getValue());
								((DELETE_ACCESSSPEC) message).setAccessSpecID(accessSpecID);
							}
							catch (Exception e){
								e.printStackTrace();
								return;
							}
						}
						else{
							return;
						}
					}
					else if (messageName.equals("ENABLE_ACCESSSPEC")){
						InputDialog dlg = new InputDialog(viewer.getControl().getShell(), messageName, "AccessSpecID:", "1", new IDValidator("AccessSpecID", "ENABLE_ACCESSSPEC"));
						if (dlg.open() == Window.OK) {
							try {
								message = (ENABLE_ACCESSSPEC) LLRPFactory.createLLRPMessage("ENABLE_ACCESSSPEC");	
								UnsignedInteger accessSpecID = new UnsignedInteger(dlg.getValue());
								((ENABLE_ACCESSSPEC) message).setAccessSpecID(accessSpecID);
							}
							catch (Exception e){
								e.printStackTrace();
								return;
							}
						}
						else{
							return;
						}
					}
					else if (messageName.equals("DISABLE_ACCESSSPEC")){
						InputDialog dlg = new InputDialog(viewer.getControl().getShell(), messageName, "AccessSpecID:", "1", new IDValidator("AccessSpecID", "DISABLE_ACCESSSPEC"));
						if (dlg.open() == Window.OK) {
							try {
								message = (DISABLE_ACCESSSPEC) LLRPFactory.createLLRPMessage("DISABLE_ACCESSSPEC");	
								UnsignedInteger accessSpecID = new UnsignedInteger(dlg.getValue());
								((DISABLE_ACCESSSPEC) message).setAccessSpecID(accessSpecID);
							}
							catch (Exception e){
								e.printStackTrace();
								return;
							}
						}
						else{
							return;
						}
					}
					else{
						message = LLRPFactory.createLLRPMessage(messageName);					
					}
					ReaderTreeObject adapterNode = currentSelectedReader.getParent();					
					ResourceCenter.getInstance().sendMessage(adapterNode.getName(),currentSelectedReader.getName(), message, "");				
				}				
			}
		};
		if (messageName.equals("ENABLE_ROSPEC") || 
				messageName.equals("DISABLE_ROSPEC") ||
				messageName.equals("START_ROSPEC") ||
				messageName.equals("STOP_ROSPEC") ||
				messageName.equals("DELETE_ROSPEC") ||
				messageName.equals("ENABLE_ACCESSSPEC") ||
				messageName.equals("DISABLE_ACCESSSPEC") ||
				messageName.equals("DELETE_ACCESSSPEC")){
			result.setText(messageName + "...");
		}
		else{
			result.setText(messageName);
		}
		result.setToolTipText("Send a " + messageName + " message to this reader");
		result.setImageDescriptor(ResourceCenter.getInstance().getImageDescriptor("Message.gif"));
		return result;
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Reader Explorer",
			message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	/**
	 * refreshes the viewer.
	 */
	public void refresh() {
		viewer.refresh(true);
		viewer.expandAll();
	}	

	/**
	 * When a user wants to send a START_ROSPEC message via the context menu of the ReaderExplorerView,
	 * a dialog asks him for a ROSpecID. This class validates the user input.
	 *
	 * @author Ulrich Etter, ETHZ
	 *
	 */
	class IDValidator implements IInputValidator {
		
		private String iDName;
		private String iDParentName;

		/**
		 * Creates a new IDValidator.
		 * <br/>
		 * <br/>
		 * Example:
		 * <br/>
		 * <br/>
		 * If this
		 * validator should validate the ROSpecID field of the START_ROSPEC message, you should call
		 * <code>new ROSpecIDValidator("ROSpecID", "START_ROSPEC")</code>.
		 * 
		 * @param iDName the name of the ID to validate
		 * @param iDParentName he name of the parent of the ID field
		 */
		public IDValidator(String iDName, String iDParentName){
			this.iDName = iDName;
			this.iDParentName = iDParentName;
		}

		public String isValid(String newText) {
			try {
				UnsignedInteger value = new UnsignedInteger(newText);
				
				// find matching constraint and check it
				LLRPRangeConstraint[] rangeConstraints = LLRPConstraints.rangeConstraints;
				for (int i = 0; i < rangeConstraints.length; i++){
					if (rangeConstraints[i].getMessageOrParameterName().equals(iDParentName)){
						if (rangeConstraints[i].getFieldName().equals(iDName)){
							// check constraint
							if (!rangeConstraints[i].isSatisfied(value.intValue())){
								return rangeConstraints[i].getErrorMessage();
							}
							break;
						}
					}
				}
				return null;
			} catch (Exception e) {
				return "";
			}
		}
		
	}
}