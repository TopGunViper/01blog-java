package edu.ouc.reactor;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reactor extends Thread{

	private static final Logger LOG = LoggerFactory.getLogger(Reactor.class);

	private Selector selector;

	Reactor(){
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException("failed to open a new selector", e);
		}
	}

	public SelectionKey register(final SelectableChannel sc, final int interestOps, Object attachment){
		if(sc == null){
			throw new NullPointerException("SelectableChannel");
		}
		if(interestOps == 0){
			throw new IllegalArgumentException("interestOps must be non-zero.");
		}
		SelectionKey key;
		try {
			key = sc.register(getSelector(), interestOps, attachment);
		} catch (ClosedChannelException e) {
			throw new RuntimeException("failed to register a channel", e);
		}
		return key;
	}

	public void close(){
		try {
			getSelector().close();
			if(LOG.isDebugEnabled()){
				LOG.debug("Close selector");
			}
		} catch (IOException e) {
			LOG.warn("Ignoring exception during close selector, e=" + e);
		}
	}

	private int wakenUp;

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
			} catch (Throwable e) {
				LOG.warn("Unexpected exception in the selector loop.", e);

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {	}
			}
		}
	}
	private void processSelectedKey(SelectionKey key){
		if(! key.isValid())
			return;
		
		NioChannel nioChannel = (NioChannel)key.attachment();
		
		int readyOps = key.readyOps();
		
        if ((readyOps & (SelectionKey.OP_READ | SelectionKey.OP_ACCEPT)) != 0 || readyOps == 0) {
        	nioChannel.nioChannelSink().doRead();
        }
		if((readyOps & SelectionKey.OP_WRITE) != 0){
			nioChannel.nioChannelSink().doSend();
		}
		if((readyOps & SelectionKey.OP_CONNECT) != 0){
			//remove OP_CONNECT
            key.interestOps((key.interestOps() & ~SelectionKey.OP_CONNECT));
		}

	}

	public Selector getSelector() {
		return selector;
	}
}
