package edu.ouc.dp.reactor;

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.dp.reactor.channel.NioChannel;
import edu.ouc.dp.reactor.channel.NioServerSocketChannel;
import edu.ouc.dp.reactor.channel.NioSocketChannel;

/**
 * mainReactorPool play a role in accepting connection.
 * and dispatch to subReactorPool
 * 
 * @author wqx
 *
 */
public class Server{

	private static final Logger LOG = LoggerFactory.getLogger(Server.class);

	private InetSocketAddress localAddress ;
	
	ReactorPool mainReactorPool;

	ReactorPool subReactorPool;
	
	ChannelHandler subHandler;
	
	private CountDownLatch closeLatch = new CountDownLatch(1);
	
	public Server reactor(ReactorPool mainReactor, ReactorPool subReactor){
		this.mainReactorPool = mainReactor;
		this.subReactorPool = subReactor;
		return this;
	}
	public Server handler(ChannelHandler h){
		this.subHandler = h;
		return this;
	}
	public void bind(InetSocketAddress localAddress) throws Exception{
		this.localAddress = localAddress;
		NioChannel serverChannel = new NioServerSocketChannel();
		serverChannel.handler(new Acceptor(subReactorPool, subHandler));
		Reactor reactor = mainReactorPool.next();
		reactor.register(serverChannel, 
				SelectionKey.OP_ACCEPT, 
				serverChannel);
		reactor.start();
		serverChannel.bind(localAddress);
		if(LOG.isDebugEnabled()){
			LOG.debug("Successfully bind to localAddress:" + localAddress);
		}
	}
	public void close(){
		mainReactorPool.close();
		subReactorPool.close();
	}
	class Acceptor implements ChannelHandler{

        private final ReactorPool subReactorPool;
        private final ChannelHandler subHandler;

		public Acceptor(ReactorPool subReactorPool, ChannelHandler subHandler){
			this.subReactorPool = subReactorPool;
			this.subHandler = subHandler;
		}
		
		@Override
		public void channelRead(NioChannel channel, Object msg) {
			
			NioSocketChannel nsc = (NioSocketChannel)msg;
			nsc.handler(subHandler);
			Reactor subReactor = subReactorPool.next();
			subHandler.channelActive(nsc);
			nsc.register(subReactor, SelectionKey.OP_READ);
			subReactor.start();
			if(LOG.isDebugEnabled()){
				LOG.debug("Register socketChannel to SUbReactor");
			}
		}

		@Override
		public void channelActive(NioChannel channel) {
			//NOOP
		}
		@Override
		public void exceptionCaught(NioChannel channel, Throwable t)
				throws Exception {
			//NOOP			
		}
	}
	
	public void gracefullyClose(){
		boolean closed;
		try {
			closeLatch.await();
			if(LOG.isDebugEnabled()){
				LOG.debug("Close selector");
			}
			closed = true;
		} catch (Exception e) {
			LOG.warn("Ignoring exception during close socket, e=" + e);
		}
	}
	public InetSocketAddress getRemoteAddress() {
		return localAddress;
	}
}
