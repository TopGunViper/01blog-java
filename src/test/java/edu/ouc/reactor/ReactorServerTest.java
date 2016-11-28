package edu.ouc.reactor;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.base.BaseTest;

public class ReactorServerTest extends BaseTest {

	private static final Logger LOG = LoggerFactory.getLogger(ReactorServerTest.class);

	private static int PORT = 8888;

	@Test
	public void test(){
		LOG.info("Successfully Configurate");
	}
	Thread t;
	
	@Override
	protected void setUp() throws Exception {
		t = new Thread(new Reactor(PORT,1024,new MyHandler()));
		t.start();
		LOG.debug("server started");
		t.join();
	}
	@Override
	protected void tearDown() throws Exception {
	}

}
