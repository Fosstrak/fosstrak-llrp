package org.fosstrak.llrp.client;

import org.llrp.ltk.types.LLRPMessage;

public interface MessageHandler {
	public void handle(String adaptorName, String readerName, LLRPMessage message);
}
