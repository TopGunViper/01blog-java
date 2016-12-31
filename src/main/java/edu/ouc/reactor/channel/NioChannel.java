package edu.ouc.reactor.channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.reactor.ChannelHandler;
import edu.ouc.reactor.Reactor;

public abstract class NioChannel {

	private static final Logger LOG = LoggerFactory.getLogger(NioChannel.class);

	protected Reactor reactor;

	protected SelectableChannel sc;

	protected SelectionKey selectionKey;

	private NioChannelSink sink;

	protected volatile ChannelHandler handler;

	public NioChannel(SelectableChannel sc){
		this.sc = sc;
		try {
			sc.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sink = nioChannelSink();
	}
	
	public void register(Reactor reactor, int interestOps){
		this.reactor = reactor;
		try {
			selectionKey = sc.register(reactor().getSelector(), interestOps, this);
		} catch (ClosedChannelException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void bind(InetSocketAddress remoteAddress) throws Exception;
	
	public abstract void connect(InetSocketAddress remoteAddress) throws Exception;
	
	public void handler(ChannelHandler h){
		handler = h;
	}
	public SelectableChannel channel(){
		return sc;
	}
	public Reactor reactor(){
		return reactor;
	}

	public NioChannelSink sink(){
		return sink;
	}
	public void sendBuffer(ByteBuffer bb){
		sink().sendBuffer(bb);
	}

	protected final void enableWrite(){
		int i = selectionKey.interestOps();
		if((i & SelectionKey.OP_WRITE) == 0){
			selectionKey.interestOps(i | SelectionKey.OP_WRITE);
		}
	}
	protected final void disableWrite(){
		int i = selectionKey.interestOps();
		if((i & SelectionKey.OP_WRITE) == 1){
			selectionKey.interestOps(i & (~SelectionKey.OP_WRITE));			
		}
	}

	protected void fireChannelRead(ByteBuffer bb){
		try {
			handler.channelRead(this, bb);
		} catch (Exception e) {
			fireExceptionCaught(e);
		}
	}
	protected void fireExceptionCaught(Throwable t){
		try {
			handler.exceptionCaught(this, t);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean isOpen(){
		return sc.isOpen();
	}
	public void close(){
		sink().close();
	}
	public abstract NioChannelSink nioChannelSink();

	public interface NioChannelSink{

		void doRead();

		void doSend();

		void sendBuffer(ByteBuffer bb);
		
		void close();
	}
}
