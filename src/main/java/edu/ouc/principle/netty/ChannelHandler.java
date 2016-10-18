package edu.ouc.principle.netty;

/**
 * 
 * 
 * @author wqx
 *
 */
public interface ChannelHandler {

	void channelActive(ChannelHandler handler) throws Exception;
	
	void exceptionCaught(ChannelHandler handler, Throwable cause) throws Exception;
	
	void channelRead(ChannelHandler handler, Object msg) throws Exception;
	
	
}
