package edu.ouc.reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

public class Handler implements Runnable {

	private SocketChannel sc;
	private SelectionKey sk;

	private static final String RESPONSE = "handle request and response from server.";

	public Handler(Selector sel,SocketChannel channel) throws IOException{
		sc = channel;
		sc.configureBlocking(false);
		sk = sc.register(sel, SelectionKey.OP_READ);
		sk.attach(this);
		sel.wakeup();
	}
	@Override
	public void run() {
		SocketChannel sc = (SocketChannel)sk.channel();
		String msg = doRead(sc);
		System.out.println("request from client :" + msg);
		doWrite(sc);
	}
	private String doRead(SocketChannel sc){

		ByteBuffer buf = ByteBuffer.allocate(1024);
		String msg = "";
		try{
			int readBytes = sc.read(buf);
			if(readBytes > 0){
				buf.flip();
				byte[] dest = new byte[buf.remaining()];
				buf.get(dest);
				msg = new String(dest,Charset.forName("UTF-8"));
			}
		}catch(IOException e){}

		return msg;
	}
	private void doWrite(SocketChannel sc){
		byte[] bytes = RESPONSE.getBytes();
		ByteBuffer buf = ByteBuffer.allocate(bytes.length);
		buf.put(bytes);
		buf.flip();
		try {
			sc.write(buf);
		} catch (IOException e) {}
	}
}
