package edu.ouc.reactor;

import java.nio.ByteBuffer;

public interface Handler {
	
	void processRequest(Processor processor, ByteBuffer msg);

}
