package edu.ouc.dp.reactor;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.dp.base.BaseTest;

public class ReactorTest extends BaseTest{
	private static final Logger LOG = LoggerFactory.getLogger(ReactorTest.class);

	private static String HOST = "localhost";

	private static int PORT = 8888;

	private static Client client;
	private static Server server;

	static SimpleServerChannelHandler h;

	@BeforeClass
	public static void setUp() throws Exception {
		startServer();
		startClient();
	}
	private static void startServer() throws Exception{
		server = new Server();
		ReactorPool mainReactor = new ReactorPool();
		ReactorPool subReactor = new ReactorPool();

		h = new SimpleServerChannelHandler();
		server.reactor(mainReactor, subReactor)
		.handler(h)
		.bind(new InetSocketAddress(HOST,PORT));
	}
	private static void startClient() throws SocketException{
		client = new Client();
		client.socket().setTcpNoDelay(true);
		client.connect(
				new InetSocketAddress(HOST,PORT));
	}
	@Test
	public void test() {
		LOG.info("Sucessful configuration");
	}

	@Test
	public void testBaseFunction(){
		LOG.debug("testBaseFunction()");

		String msg ="Hello Reactor";
		ByteBuffer resp = client.syncSend(ByteBuffer.wrap(msg.getBytes()));
		byte[] res = new byte[resp.remaining()];
		resp.get(res);

		Assert.assertEquals("Hello Calculator", new String(res,0,res.length));
	}

	@Test
	public void testMultiSend(){

		int sendSize = 1024;

		for(int i = 0; i < sendSize; i++){
			ByteBuffer bb = ByteBuffer.wrap("Hello Reactor".getBytes());
			ByteBuffer resp = client.syncSend(bb);
			byte[] res = new byte[resp.remaining()];
			resp.get(res);

			Assert.assertEquals("Hello Calculator", new String(res,0,res.length));
		}
		Assert.assertEquals(sendSize, h.receiveSize);
	}
	@Test
	public void testTooLongReceivedByteSizeEexception(){
		LOG.debug("testTooLongReceivedByteSizeEexception()");

		int threshold = 1024;
		byte[] dest = new byte[threshold + 1];
		Random r = new Random();
		r.nextBytes(dest);
		client.syncSend(ByteBuffer.wrap(dest));
		
		Assert.assertEquals(IllegalArgumentException.class, h.t.getClass());
		
		Assert.assertEquals("Illegal data length, len:" + (threshold+1), h.t.getMessage());
	}
	@AfterClass
	public static void tearDown() throws Exception {
		server.close();
		client.close();
	}
}
