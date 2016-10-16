package edu.ouc.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * Reactor
 * 
 * @author wqx
 *
 */
public class Reactor implements Runnable {
	
	private Selector selector;
	private ServerSocketChannel ssc;
	/**
	 * 启动阶段
	 * @param port
	 * @throws IOException
	 */
	public Reactor(int port) throws IOException{
		selector = Selector.open();
		ssc = ServerSocketChannel.open();
		ssc.socket().bind(new InetSocketAddress(port));
		ssc.configureBlocking(false);
		SelectionKey sk = ssc.register(selector, SelectionKey.OP_ACCEPT);
		sk.attach(new Acceptor());
	}
	/**
	 * 轮询阶段
	 */
	@Override
	public void run() {
		System.out.println("Reactor started!!!");
		try {
			while(!Thread.interrupted()){
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					dispatch(key);
					it.remove();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
					new Handler(selector,sc);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
