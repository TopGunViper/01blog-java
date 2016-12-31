package edu.ouc.reactor;

import edu.ouc.reactor.channel.NioChannel;


public interface ChannelHandler {
	
	void channelActive(NioChannel channel);
	
	void channelRead(NioChannel channel, Object msg) throws Exception;
	
	void exceptionCaught(NioChannel channel, Throwable t) throws Exception;
}
