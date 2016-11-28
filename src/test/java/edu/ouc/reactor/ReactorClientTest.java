package edu.ouc.reactor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.base.BaseTest;

public class ReactorClientTest extends BaseTest {

	private static final Logger LOG = LoggerFactory.getLogger(ReactorClientTest.class);

	private static String HOST = "localhost";

	private static int PORT = 8888;

	@Test
	public void test(){
		LOG.info("Successfully Configurate");
	}
	private Client client;
	
	@Override
	protected void setUp() throws Exception {
		client = new Client();
		client.socket().setTcpNoDelay(true);
		client.connect(
				new InetSocketAddress(HOST,PORT));
	}
	
	@Test
	public void testBase(){
		String msg ="hello reactor";
		ByteBuffer resp = client.send(ByteBuffer.wrap(msg.getBytes()));
		byte[] retVal = new byte[resp.remaining()];
		resp.get(retVal);
		
		Assert.assertEquals("hello client", new String(retVal,0,retVal.length));
	}
	@Override
	protected void tearDown() throws Exception {
		client.close();
		LOG.debug("close client");
	}

}
