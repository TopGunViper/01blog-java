package edu.ouc.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reactor
 * 
 * @author wqx
 *
 */
public class Reactor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Reactor.class);
	
	private Selector selector;
	
	private ServerSocketChannel ssc;

	private Handler DEFAULT_HANDLER = new Handler(){
		@Override
		public void processRequest(Processor processor, ByteBuffer msg) {
			//NOOP
		}
	};
	private Handler handler = DEFAULT_HANDLER;
	
	
	/**
	 * 启动阶段
	 * @param port
	 * @throws IOException
	 */
	public Reactor(int port, int maxClients, Handler serverHandler) throws IOException{
		selector = Selector.open();
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(port));
		
		this.handler = serverHandler;
		SelectionKey sk = ssc.register(selector, SelectionKey.OP_ACCEPT);
		sk.attach(new Acceptor());
	}
	/**
	 * 轮询阶段
	 */
	@Override
	public void run() {
		while(!ssc.socket().isClosed()){
			try {
				selector.select(1000);
				Set<SelectionKey> keys;
				synchronized(this){
					keys = selector.selectedKeys();
				}
				Iterator<SelectionKey> it = keys.iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					dispatch(key);
					it.remove();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        close();
	}
	public void dispatch(SelectionKey key){
		Runnable r = (Runnable)key.attachment();
		if(r != null)
			r.run();
	}
	/**
	 * 用于接受TCP连接的Acceptor
	 * 
	 */
	class Acceptor implements Runnable{

		@Override
		public void run() {
			SocketChannel sc;
			try {
				sc = ssc.accept();
				if(sc != null){
					new Processor(Reactor.this,selector,sc);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close(){
		try {
			selector.close();
			if(LOG.isDebugEnabled()){
				LOG.debug("Close selector");
			}
		} catch (IOException e) {
			LOG.warn("Ignoring exception during close selector, e=" + e);
		}
	}
	public void processRequest(Processor processor, ByteBuffer msg){
		if(handler != DEFAULT_HANDLER){
			handler.processRequest(processor, msg);
		}
	}
}
