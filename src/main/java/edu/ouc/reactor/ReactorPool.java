package edu.ouc.reactor;

import java.nio.channels.SelectableChannel;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactorPool {
	
	private static final Logger LOG = LoggerFactory.getLogger(ReactorPool.class);
	
	private Reactor[] reactors;
	
	private NioChannel handler;
	
	private AtomicInteger index = new AtomicInteger();
	
	private final int DEFAULT_THREADS = Runtime.getRuntime().availableProcessors() * 2;
	
	public ReactorPool (){
		this(0);
	}
	public ReactorPool(int nThreads){
		if(nThreads < 0){
			throw new IllegalArgumentException("nThreads must be nonnegative number");
		}
		if(nThreads == 0){
			nThreads = DEFAULT_THREADS;
		}
		reactors = new Reactor[nThreads];
		for(int i = 0; i < nThreads; i++){
			reactors[i] = new Reactor();
		}
	}
    public void register(SelectableChannel channel, int interestOps, Object attachment) {
        next().register(channel, interestOps, attachment);
    }
	public Reactor next(){
		return reactors[index.incrementAndGet() % reactors.length];
	}

	public NioChannel getHandler() {
		return handler;
	}

}
