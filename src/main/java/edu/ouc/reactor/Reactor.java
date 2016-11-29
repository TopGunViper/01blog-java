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
import java.util.concurrent.atomic.AtomicInteger;

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
	
	//just for Acceptor
	private Selector selector;
	
	private EventLoop[] eventLoops;
	
	private AtomicInteger index = new AtomicInteger();
	
	private ServerSocketChannel ssc;

	/**
	 * 启动阶段
	 * @param port
	 * @throws IOException
	 */
	public Reactor(int port) throws IOException{
		
		selector = Selector.open();
		ssc = ServerSocketChannel.open();
		ssc.configureBlocking(false);
		ssc.socket().bind(new InetSocketAddress(port));
		int nThreads = Runtime.getRuntime().availableProcessors();
		eventLoops = new EventLoop[nThreads];
		for(int i = 0; i < nThreads; i++){
			eventLoops[i] = new EventLoop();			
		}
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

	private EventLoop nextEventLoop(){
		return eventLoops[index.incrementAndGet() % eventLoops.length];
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
					new Processor(nextEventLoop(),sc);
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

}
