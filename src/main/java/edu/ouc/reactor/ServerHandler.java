package edu.ouc.reactor;

import java.nio.ByteBuffer;

public interface ServerHandler {
	
	void processRequest(ServerProcessor processor, ByteBuffer msg);

}
