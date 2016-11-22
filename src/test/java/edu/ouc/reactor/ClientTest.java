package edu.ouc.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class ClientTest {

	private static String HOST = "localhost";
	private static int PORT = 8888;

	public static void main(String[] args) throws IOException {
		
		Client client = new Client();
		client.socket().setTcpNoDelay(true);
		
		client.connect(
				new InetSocketAddress(HOST,PORT));
		
		ByteBuffer msg;
		for(int i = 1; i <= 3; i++){
			msg = ByteBuffer.wrap(("request" + i).getBytes());
			System.out.println("send-" + "request" + i);
			
			ByteBuffer resp = client.send(msg);
			byte[] retVal = new byte[resp.remaining()];
			resp.get(retVal);

			System.out.println("receive-" + new String(retVal,0,retVal.length));
			
		}
	}
}
