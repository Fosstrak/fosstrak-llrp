package org.fosstrak.llrp.commander.util;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.commander.views.MessageboxView;

/**
 * helper thread that periodically refreshes the 
 * message box view if a new message has arrived. 
 * (refreshing whenever a message arrives kills 
 * eclipse ui.).
 * @author sawielan
 *
 */
public class MessageBoxRefresh implements Runnable {

	/** the interval to refresh the messagebox view. */
	public static final long REFRESH_INTERVAL_MS = 1000;
	
	/** the log4j logger. */
	private static Logger log = Logger.getLogger(MessageBoxRefresh.class);
	
	/** flag whether we need a refresh in the message box. */
	private boolean dirty = false;
	
	/** the message box to refresh. */
	private MessageboxView messageboxView = null;
	
	/**
	 * constructor for the message box refresher thread. 
	 * @param messageboxView the message box to be refreshed.
	 */
	public MessageBoxRefresh(MessageboxView messageboxView) {
		this.messageboxView = messageboxView;
	}
	
	/** 
	 * sets the messagebox to dirty, meaning a new message arrived 
	 * and we need to refresh the message box.
	 */
	public void setDirty() {
		dirty = true;
	}
	
	/**
	 * sets the messagebox to refresh.
	 * @param messageboxView the message box to be refreshed.
	 */
	public void setMessageBox(MessageboxView messageboxView) {
		this.messageboxView = messageboxView;
	}
	
	/**
	 * execute the refresher thread.
	 */
	public void run() {
		try {
			while (true) {
				if (dirty = true) {
					dirty = false;
					
					// SWT threads do not allow other threads to access 
					// the SWT widgets. to circumvent this issue one has 
					// to run the call through a asyncExec/syncExec API 
					// on the corresponding display
					if (messageboxView != null) {
						messageboxView.getDisplay().asyncExec(
							new Runnable() {
								public void run() {
									messageboxView.updateViewer();		
								}
							}
						);
					} else {
						log.debug("no message box set...");
					}
				}

				// wait for the next refresh...
				Thread.sleep(REFRESH_INTERVAL_MS);
			}
		} catch (InterruptedException e) {
			log.info("received interrupt, stop refreshing messagebox.");
		} catch (Exception e) {
			log.error("some unknown error occured:\n" + e.getMessage());
		}
	}

}
