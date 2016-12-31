package edu.ouc.reactor;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactorPool {

	private static final Logger LOG = LoggerFactory.getLogger(ReactorPool.class);

	private Reactor[] reactors;

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
			boolean succeed = false;
			try{
				reactors[i] = new Reactor();
				succeed = true;
			}catch(Exception e){
				throw new IllegalStateException("failed to create a Reactor", e);
			}finally{
				if (!succeed) {
					for (int j = 0; j < i; j ++) {
						reactors[j].close();
					}
				}
			}
		}
	}

	public Reactor next(){
		return reactors[index.incrementAndGet() % reactors.length];
	}

	public void close(){
		for(int i = 0; i < reactors.length; i++){
			reactors[i].setShutdown(true);
			reactors[i].close();
		}
	}
}
