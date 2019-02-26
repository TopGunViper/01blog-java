package edu.ouc.dp.async.future;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.ouc.dp.base.BaseTest;

public class FutureTest extends BaseTest{

	private static ExecutorService exec;
	private static int nThreads = Runtime.getRuntime().availableProcessors() * 2;
	
	@BeforeClass
	public static void setUp() throws Exception {
		exec  = Executors.newFixedThreadPool(nThreads);
	}
	
	@Test
	public void testFuture() throws InterruptedException, ExecutionException{
		
		FutureTask<String> f = new FutureTask<String>(new Callable<String>(){
			@Override
			public String call() throws Exception {
				String result = "Hello World"; 
				
				TimeUnit.SECONDS.sleep(2);
				
				return result;
			}
		});
		
		exec.submit(f);
		
		Assert.assertTrue(!f.isDone());
		Assert.assertEquals("Hello World", f.get());
	}
	
}
