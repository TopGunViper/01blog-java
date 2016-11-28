package edu.ouc.reactor;

import java.nio.ByteBuffer;

public class MyHandler implements Handler {
	
	@Override
	public void processRequest(Processor processor, ByteBuffer msg) {
		byte[] con = new byte[msg.remaining()];
		msg.get(con);
		
		String str = new String(con,0,con.length);
		
		String resp = "";
		switch(str){
		case "request1":resp = "response1";break;
		case "request2":resp = "response2";break;
		case "request3":resp = "response3";break;
		default :resp = "hello client";
		}
		
		ByteBuffer buf = ByteBuffer.allocate(resp.getBytes().length);
		buf.put(resp.getBytes());
		
		processor.sendBuffer(buf);
	}

}
