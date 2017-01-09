package edu.ouc.async;

import java.net.SocketException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.base.BaseTest;

public class CallBackTest extends BaseTest{
	private static final Logger LOG = LoggerFactory.getLogger(CallBackTest.class);

	private static String HOST = "localhost";

	private static int PORT = 8888;

	private static Client client;

	@BeforeClass
	public static void setUp() throws Exception {
		startServer();
		startClient();
	}
	private static void startServer() throws Exception{
	}
	private static void startClient() throws SocketException{
	}
	@Test
	public void test() {
		LOG.info("Sucessful configuration");
	}

	@Test
	public void testCreateNode() throws InterruptedException{

		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicInteger count = new AtomicInteger(0);
		
		Client client = new Client("localhost",8888);
		long begin = System.currentTimeMillis();
		
		client.asyncCreate("exist NodeInfo", new CallBack(){
			@Override
			public void process(int rc, Object response, Object ctx) {
				try {
					TimeUnit.SECONDS.sleep(3);
				} catch (InterruptedException e) {}
				
				count.incrementAndGet();
				latch.countDown();
				
				Assert.assertEquals("I'm context", ctx);
			}
		}, "I'm context");
		//asyncCreate return immediately
		Assert.assertTrue((System.currentTimeMillis() - begin) < 1000);
		latch.await();
		
		Assert.assertEquals(1, count.intValue());
	}

	@AfterClass
	public static void tearDown() throws Exception {
		//client.close();
	}
}
