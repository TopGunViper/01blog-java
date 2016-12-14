package edu.ouc.reactor;


public interface ChannelHandler {
	
	void channelActive(NioChannel channel);
	
	void channelRead(NioChannel channel, Object msg) throws Exception;
	
	void exceptionCaught(NioChannel channel, Object exception) throws Exception;
}
