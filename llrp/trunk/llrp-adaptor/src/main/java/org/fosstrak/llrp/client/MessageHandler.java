package org.fosstrak.llrp.client;

import org.fosstrak.llrp.adaptor.AdaptorManagement;
import org.llrp.ltk.types.LLRPMessage;

/**
 * All processes that want to receive messages from the {@link AdaptorManagement} 
 * need to implement this interface. Upon arrival of a new LLRP message, the 
 * {@link AdaptorManagement} invokes the handle method on the registered 
 * handlers.
 * @author sawielan
 *
 */
public interface MessageHandler {
	
	/**
	 * This method is invoked from the adapter management whenever a new LLRP 
	 * message arrives on an attached reader.
	 * @param adaptorName the name of the adapter where the reader belongs to. 
	 * @param readerName the name of the receiving reader.
	 * @param message the LLRP message.
	 */
	public void handle(String adaptorName, String readerName, LLRPMessage message);
}
