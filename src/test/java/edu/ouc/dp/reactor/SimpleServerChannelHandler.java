package edu.ouc.dp.reactor;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ouc.dp.reactor.channel.NioChannel;

public class SimpleServerChannelHandler implements ChannelHandler {
	
	private static Logger LOG = LoggerFactory.getLogger(SimpleServerChannelHandler.class);
	
	public volatile int receiveSize;
	
	public volatile Throwable t;
	
	@Override
	public void channelActive(NioChannel channel) {
		if(LOG.isDebugEnabled()){
			LOG.debug("ChannelActive");
		}
	}

	@Override
	public void channelRead(NioChannel channel, Object msg) throws Exception {
		
		ByteBuffer bb = (ByteBuffer)msg;

		byte[] con = new byte[bb.remaining()];
		bb.get(con);

		String str = new String(con,0,con.length);

		String resp = "";
		switch(str){
		case "request1":resp = "response1";break;
		case "request2":resp = "response2";break;
		case "request3":resp = "response3";break;
		default :resp = "Hello Calculator";
		}

		ByteBuffer buf = ByteBuffer.allocate(resp.getBytes().length);
		buf.put(resp.getBytes());
		
		receiveSize++;
		
		channel.sendBuffer(buf);
	}

	@Override
	public void exceptionCaught(NioChannel channel, Throwable t)
			throws Exception {
		this.t = t;
		channel.close();
	}

}
