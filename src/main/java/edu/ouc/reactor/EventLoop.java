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

public class EventLoop extends Thread{
	
	private static final Logger LOG = LoggerFactory.getLogger(EventLoop.class);
	
	private Selector selector;
	
    EventLoop(){
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
				processSelectedKeys(keys);
			} catch (Throwable e) {
				LOG.warn("Unexpected exception in the selector loop.", e);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {	}
			}
		}
	}
	private void processSelectedKeys(Set<SelectionKey> keys){
		if(keys.isEmpty()){
			return;
		}
		Iterator<SelectionKey> it = keys.iterator();
		while(it.hasNext()){
			SelectionKey key = it.next();
			dispatch(key);
			it.remove();
		}
	}
	
	public void dispatch(SelectionKey key){
		ChannelHandler handler = (ChannelHandler)key.attachment();
		if(handler != null)
			handler.handle(key);
	}

	public Selector getSelector() {
		return selector;
	}
}
