package edu.ouc.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MainReactor play a role in accepting connection.
 * and dispatch to SubReactor
 * 
 * @author wqx
 *
 */
public class Server{

	private static final Logger LOG = LoggerFactory.getLogger(Server.class);
	
	Reactor mainReactor;
	
	Reactor subReactor;
	
	private ServerSocketChannel ssc;
	
	/**
	 * @throws IOException
	 */
	public Server(){
		
		try {
			initChannel();
		} catch (IOException e) {
			LOG.error("Unexpected Exception during init Channel, e=" + e);
		}
	}
	private void initChannel() throws IOException{
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.register(mainReactor.next().getSelector()
						, SelectionKey.OP_ACCEPT, new Acceptor(subReactor));
	}
	public void bind(InetSocketAddress remoteAddress){
		try {
			ssc.socket().bind(remoteAddress);
		} catch (IOException e) {
			LOG.error("Unexpected Exception during bind remoteAddress:" + remoteAddress);
		}
	}
	
	class Acceptor implements ChannelHandler{
		
		Reactor subReactor;
		
		public Acceptor(Reactor subReactor){
			this.subReactor = subReactor;
		}
		@Override
		public void handle(Object msg) {
			/**
			 * In this place, we can do some authenticating or authorizing work
			 * If permitted,  deliver to subReactor
			 */
			SelectionKey key = (SelectionKey)msg;
			SocketChannel sc = (SocketChannel) key.channel();
			try {
				EventLoop loop = subReactor.next();
				sc.register(loop.getSelector(),
						SelectionKey.OP_READ,
						new DefaultChannelHandler(loop, key));
			} catch (ClosedChannelException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close(){
		try {
			ssc.close();
			if(LOG.isDebugEnabled()){
				LOG.debug("Close selector");
			}
		} catch (IOException e) {
			LOG.warn("Ignoring exception during close selector, e=" + e);
		}
	}
}
