package edu.ouc.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.reactor.Server.Acceptor;

public class NioServerSocketChannel extends NioChannel{

	private static final Logger LOG = LoggerFactory.getLogger(NioServerSocketChannel.class);
	
	public NioServerSocketChannel(){
		super(newSocket(), SelectionKey.OP_ACCEPT);
	}
	
	public static ServerSocketChannel newSocket(){
		ServerSocketChannel socketChannel = null;
		try {
			socketChannel = ServerSocketChannel.open();
		} catch (IOException e) {
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
						new NioSocketChannel(
								ssc.accept(),
								SelectionKey.OP_READ));
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
	}// end NioChannelSink
	
	@Override
	protected void bind(InetSocketAddress remoteAddress) throws Exception {
		// TODO Auto-generated method stub
		ServerSocketChannel ssc = (ServerSocketChannel)sc;
		ssc.bind(remoteAddress);
	}
	@Override
	protected void connect(InetSocketAddress remoteAddress) throws Exception {
		throw new UnsupportedOperationException();
	}
	
}
