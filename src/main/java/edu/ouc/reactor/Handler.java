package edu.ouc.reactor;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Server handler
 * 
 * @author wqx
 */
public class Handler implements Runnable {

	private SocketChannel sc;

	private SelectionKey sk;

	private volatile State state;

	private static int DEFAULT_BUFSIZE = 1024;

	private ByteBuffer inputBuf = ByteBuffer.allocate(DEFAULT_BUFSIZE);

	private ByteBuffer outputBuf = ByteBuffer.allocate(DEFAULT_BUFSIZE);

	public Handler(Selector sel,SocketChannel channel) throws IOException{
		sc = channel;
		sc.configureBlocking(false);
		sk = sc.register(sel, SelectionKey.OP_READ);
		sk.attach(this);
		sel.wakeup();
	}
	@Override
	public void run() {

		switch(state){
		case READING:doRead();break; 
		case SENDING:doSend();break;
		}
	}
	private void doRead(){
		try {
			sc.read(inputBuf);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(inputComplete()){//输入完成
			process();
			state = State.SENDING;
			//注册OP_WRITE
			sk.interestOps(SelectionKey.OP_WRITE);
		}
	}

	private boolean inputComplete(){
		//1.缓存区是否已满
		//2.半包读写问题
		return true;
	}

	private void doSend(){
		try {
			while(outputBuf.hasRemaining()){
				sc.write(outputBuf);
			}
			sk.cancel();
		} catch (IOException e) {}
	}
	/**
	 * process request and get response
	 * 
	 * @param request
	 * @return
	 */
	private void process(){
		System.out.println("process request and produce response");

		inputBuf.flip();
		byte[] dest = new byte[inputBuf.remaining()];
		inputBuf.get(dest);
		String msg = new String(dest,Charset.forName("UTF-8"));

		String resp = "";

		switch(msg){
		case "request1" : resp = "response1";break;
		case "request2" : resp = "response2";break;
		case "request3" : resp = "response3";break;
		default : resp = "";
		}
		
		try{
			outputBuf.put(resp.getBytes());
		}catch(BufferOverflowException e){
			//发送缓冲区溢出，应该进行扩容操作
		}
	}

	enum State{
		READING,SENDING;
		public boolean isReadable(){
			return this == READING;
		}
		public boolean isWritable(){
			return this == SENDING;
		}
	}
}
