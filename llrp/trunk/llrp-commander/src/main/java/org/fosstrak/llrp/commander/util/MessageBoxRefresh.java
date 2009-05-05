package org.fosstrak.llrp.commander.util;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.fosstrak.llrp.client.LLRPMessageItem;
import org.fosstrak.llrp.client.Repository;
import org.fosstrak.llrp.commander.ResourceCenter;
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

	/** the default interval to refresh the messagebox view. */
	public static final long DEFAULT_REFRESH_INTERVAL_MS = 1500;
	
	/** the interval to refresh the messagebox view. */ 
	public long refreshTime = DEFAULT_REFRESH_INTERVAL_MS;
	
	/** the log4j logger. */
	private static Logger log = Logger.getLogger(MessageBoxRefresh.class);
	
	/** flag whether we need a refresh in the message box. */
	private boolean dirty = false;
	
	/** flags whether to execute the refresher or not... */
	private boolean doRun = true;
	
	/** flag that is true when the refresher has stopped. */
	private boolean stopped = false;
	
	/** the message box to refresh. */
	private MessageboxView messageboxView = null;
	
	/** whether to refresh or not by default. */
	public static final boolean DEFAULT_REFRESH_BEHAVIOR = true;
	
	/** whether to refresh or not. */
	private boolean doRefresh = DEFAULT_REFRESH_BEHAVIOR;
	
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
	 * turn on/off the refresh behavior.
	 * @param refresh if set to true refresh the messagebox, otherwise not.
	 */
	public void setRefresh(boolean refresh) {
		doRefresh = refresh;
	}
	
	/**
	 * sets the messagebox to refresh.
	 * @param messageboxView the message box to be refreshed.
	 */
	public void setMessageBox(MessageboxView messageboxView) {
		this.messageboxView = messageboxView;
	}
	
	/**
	 * stops the refresher from executing. 
	 */
	public void stop() {
		this.doRun = false;
	}
	
	/**
	 * if the refresher has stopped true is returned.
	 * @return true if stopped, false otherwise.
	 */
	public boolean hasStopped() {
		return stopped;
	}
	
	/**
	 * set the refresh time to use. if never set, the default refresh
	 * time is used.
	 * @param refreshTime the new refresh time.
	 */
	public void setRefreshTime(long refreshTime) {
		if (refreshTime > 0) {
			this.refreshTime = refreshTime;
		}
	}
	
	/**
	 * @return the refresh time.
	 */
	public long getRefreshTime() {
		return refreshTime;
	}
	
	/**
	 * execute the refresher thread.
	 */
	public void run() {
		try {
			while (doRun) {
				if ((true == dirty) && (true == doRefresh)) {
					dirty = false;
					
					// SWT threads do not allow other threads to access 
					// the SWT widgets. to circumvent this issue one has 
					// to run the call through a asyncExec/syncExec API 
					// on the corresponding display
					if (messageboxView != null) {
						// execute synchronous
						messageboxView.getDisplay().syncExec(
							new Runnable() {
								public void run() {
									messageboxView.updateViewer(false);
									log.debug("update message box.");
								}
							}
						);

					} else {
						log.debug("no message box set...");
					}
				}

				// wait for the next refresh...
				Thread.sleep(refreshTime);
			}
		} catch (InterruptedException e) {
			log.info("received interrupt, stop refreshing messagebox.");
		} catch (Exception e) {
			log.error("some unknown error occured:\n" + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}
		
		stopped = true;
	}
}
