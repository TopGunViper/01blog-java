package edu.ouc.reactor;

import java.nio.channels.SelectableChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Reactor {
	
	private static final Logger LOG = LoggerFactory.getLogger(Reactor.class);
	
	private EventLoop[] eventLoops;
	
	private ChannelHandler handler;
	
	private AtomicInteger index = new AtomicInteger();
	
	private final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2;
	
	public Reactor (){
		this(0);
	}
	public Reactor(int nThreads){
		if(nThreads < 0){
			throw new IllegalArgumentException("nThreads must be nonnegative number");
		}
		if(nThreads == 0){
			nThreads = DEFAULT_THREADS;
		}
		eventLoops = new EventLoop[nThreads];
		for(int i = 0; i < nThreads; i++){
			eventLoops[i] = new EventLoop();
		}
	}
    public void register(SelectableChannel channel, int interestOps, Object attachment) {
        next().register(channel, interestOps, attachment);
    }
	public EventLoop next(){
		return eventLoops[index.incrementAndGet() % eventLoops.length];
	}

	public ChannelHandler getHandler() {
		return handler;
	}

}
