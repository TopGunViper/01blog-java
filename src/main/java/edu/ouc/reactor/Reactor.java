package edu.ouc.reactor;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.reactor.channel.NioChannel;

public class Reactor extends Thread{

	private static final Logger LOG = LoggerFactory.getLogger(Reactor.class);

	private Selector selector;

	private volatile boolean isShutdown;

	Reactor(){
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException("failed to open a new selector", e);
		}
	}

	public SelectionKey register(final NioChannel sc, final int interestOps, Object attachment){
		if(sc == null){
			throw new NullPointerException("SelectableChannel");
		}
		if(interestOps == 0){
			throw new IllegalArgumentException("interestOps must be non-zero.");
		}
		SelectionKey key;
		try {
			/**
			 *  prevent block if selection operation involving the same selector.
			 */
			getSelector().wakeup();

			key = sc.channel().register(getSelector(), interestOps, attachment);
		} catch (ClosedChannelException e) {
			throw new RuntimeException("failed to register a channel", e);
		}
		return key;
	}

	private int wakenUp = 1000;

	@Override
	public void run() {
		for(;;){
			try {
				getSelector().select(wakenUp);
				Set<SelectionKey> keys;
				synchronized(this){
					keys = getSelector().selectedKeys();
				}
				Iterator<SelectionKey> it = keys.iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					processSelectedKey(key);
					it.remove();
				}
				if(isShutdown()){
					break;
				}
			} catch (Throwable e) {
				LOG.warn("Unexpected exception in the selector loop.", e);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {	}
			}
		}
	}

	private void processSelectedKey(SelectionKey key){
		try {		
			NioChannel nioChannel = (NioChannel)key.attachment();

			if (!nioChannel.isOpen()) {
				LOG.warn("trying to do i/o on a null socket");
				return;
			}

			int readyOps = key.readyOps();
			if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
				nioChannel.sink().doRead();
			}
			if((readyOps & SelectionKey.OP_WRITE) != 0){
				nioChannel.sink().doSend();
			}
			if((readyOps & SelectionKey.OP_CONNECT) != 0){
				//remove OP_CONNECT
				key.interestOps((key.interestOps() & ~SelectionKey.OP_CONNECT));
			}
		}catch (Throwable t) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Throwable stack trace", t);
			}
			closeSocket();
		}
	}
	private void closeSocket(){
		try {
			getSelector().selectNow();

			Set<SelectionKey> keys = selector.selectedKeys();
			for (SelectionKey k: keys) {
				NioChannel ch = (NioChannel)k.attachment();
				ch.sink().close();
			}
		} catch (IOException e) {
			LOG.warn("Ignoring exception during close socket, e=" + e);
		}
	}
	public void close(){
		try {

			closeSocket();

			getSelector().close();
			if(LOG.isDebugEnabled()){
				LOG.debug("Close selector");
			}
		} catch (IOException e) {
			LOG.warn("Ignoring exception during close selector, e=" + e);
		}
	}

	public Selector getSelector() {
		return selector;
	}

	public boolean isShutdown() {
		return isShutdown;
	}

	public void setShutdown(boolean isShutdown) {
		this.isShutdown = isShutdown;
	}

}
