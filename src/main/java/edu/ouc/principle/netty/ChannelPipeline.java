package edu.ouc.principle.netty;

/**
 * ChannelPipeline
 * 
 * ChannelPipeline} p = ...;
 * p.addLast("1", new InboundHandlerA());
 * p.addLast("2", new InboundHandlerB());
 * 
 * @author wqx
 *
 */
public interface ChannelPipeline {
	
	ChannelPipeline addLast(String name, ChannelHandler handler);
	
	ChannelPipeline addFist(String name, ChannelHandler handler);
	
	ChannelPipeline fireChannelActive();
	
	ChannelPipeline fireChannelRead(Object msg);
	
	ChannelPipeline fireExceptionCaught(Throwable cause);
}
