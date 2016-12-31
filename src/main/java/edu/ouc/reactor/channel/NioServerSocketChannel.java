package edu.ouc.reactor.channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioServerSocketChannel extends NioChannel{

	private static final Logger LOG = LoggerFactory.getLogger(NioServerSocketChannel.class);
	
	public NioServerSocketChannel(){
		super(newSocket());
	}
	
	public static ServerSocketChannel newSocket(){
		ServerSocketChannel socketChannel = null;
		try {
			socketChannel = ServerSocketChannel.open();
		} catch (IOException e) {
			LOG.error("Unexpected exception occur when open ServerSocketChannel");
		}
		return socketChannel;
	}
	
	@Override
	public NioChannelSink nioChannelSink() {
		return new NioServerSocketChannelSink();
	}
	
	class NioServerSocketChannelSink implements NioChannelSink{

		public void doRead() {
			try {
				ServerSocketChannel ssc = (ServerSocketChannel)sc;
				handler.channelRead(NioServerSocketChannel.this,
						new NioSocketChannel(ssc.accept()));
				if(LOG.isDebugEnabled()){
					LOG.debug("Dispatch the SocketChannel to SubReactorPool");
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		public void doSend(){
			throw new UnsupportedOperationException();
		}

		@Override
		public void sendBuffer(ByteBuffer bb) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			try {
				if(sc != null){
					sc.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}// end NioChannelSink
	
	@Override
	public void bind(InetSocketAddress remoteAddress) throws Exception {
		ServerSocketChannel ssc = (ServerSocketChannel)sc;
		ssc.bind(remoteAddress);
	}
	@Override
	public void connect(InetSocketAddress remoteAddress) throws Exception {
		throw new UnsupportedOperationException();
	}
	
}
