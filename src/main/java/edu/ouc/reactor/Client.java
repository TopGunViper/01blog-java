package edu.ouc.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * client
 * 
 * @author wqx
 *
 */
public class Client extends Thread{

	private final static Logger LOG = LoggerFactory.getLogger(Client.class);

	private InetSocketAddress remoteAddress;

	private SocketChannel sc;

	private Selector selector;

	Thread thread;

	CountDownLatch connectedLatch = new CountDownLatch(1);


	public Client connect(InetSocketAddress address) throws IOException{
		this.remoteAddress = address;
		thread = new Thread(this,"client");
		selector = Selector.open();
		sc = SocketChannel.open();
		sc.connect(remoteAddress);
		start();
		return this;
	}

	public void sync(){
		try {
			connectedLatch.await();
		} catch (InterruptedException e) {
			LOG.error("InterruptedException e=" + e);
		}
	}
	public Socket socket(){
		return sc.socket();
	}
	@Override
	public void run() {
		try{
			while(!Thread.interrupted()){
				int keys = selector.select(100);
				Set<SelectionKey> selectedKeys;
				synchronized(this){
					selectedKeys = selector.selectedKeys();
				}
				for(SelectionKey key : selectedKeys){
					if((key.readyOps() & SelectionKey.OP_CONNECT) != 0){
						onConnected();
					}else if((key.readyOps() & (SelectionKey.OP_READ | SelectionKey.OP_WRITE)) != 0){
						doReadAndWrite(key);
					}
				}
				selectedKeys.clear();
			}
		}catch(Exception e){
			LOG.error("Unexcepted Exception e=" + e);
		}
	}
	private void onConnected(){
		connectedLatch.countDown();
		if(LOG.isDebugEnabled()){
			LOG.debug("client connected to the server[remoteAddress:" + this.remoteAddress + "]");
		}
	}
	public void start(){
		thread.start();
	}

	private final ByteBuffer lenBuffer = ByteBuffer.allocateDirect(4);

	private ByteBuffer incomingBuffer = lenBuffer;

	private ByteBuffer outputBuffer;

	private void doReadAndWrite(SelectionKey key) throws IOException{
		SocketChannel sc = (SocketChannel)key.channel();
		if(key.isReadable()){
			sc.read(incomingBuffer);
			if(incomingBuffer.remaining() == 0){
				incomingBuffer.flip();
				if(incomingBuffer == lenBuffer){// read length
					int len = incomingBuffer.getInt();
					incomingBuffer = ByteBuffer.allocate(len);
				}else{
					//read data
					processResponse(incomingBuffer);
					lenBuffer.clear();
					incomingBuffer = lenBuffer;
				}
			}
		}else if(key.isWritable()){
			sc.write(outputBuffer);
			while(outputBuffer.hasRemaining()){
				sc.write(outputBuffer);
			}
			outputBuffer = null;//help GC
			disableWrite(key);
		}
	}
	private void enableWrite(SelectionKey sk){
		int i = sk.interestOps();
		if((i & SelectionKey.OP_WRITE) == 0){
			sk.interestOps(i | SelectionKey.OP_WRITE);
		}
	}
	private void disableWrite(SelectionKey sk){
		int i = sk.interestOps();
		if((i & SelectionKey.OP_WRITE) == 1){
			sk.interestOps(i & (~SelectionKey.OP_WRITE));			
		}
	}

	private void processResponse(ByteBuffer bb){

	}
	/**
	 * send msg synchronous
	 * 
	 * @param msg
	 * @return
	 */
	public ByteBuffer syncSend(final ByteBuffer msg){
		if(msg == null){
			throw new IllegalArgumentException("msg is null");
		}

		//switch to write model and init outputBuffer
		msg.flip();
		int capacity = msg.remaining() + 4;
		outputBuffer = ByteBuffer.allocateDirect(capacity);
		
		outputBuffer.putInt(capacity-4);
		outputBuffer.put(msg);
		
		
		return null;
	}
	
	/**
	 * send msg asynchronous
	 * 
	 * @param msg
	 * @param listener
	 * @return
	 */
	public Future<ByteBuffer> asyncSend(final ByteBuffer msg, final CallbackListener listener){
		if(msg == null){
			throw new IllegalArgumentException("msg is null");
		}
		Future<ByteBuffer> retVal = new FutureTask<>(new Callable<ByteBuffer>(){
			@Override
			public ByteBuffer call() throws Exception {
				return syncSend(msg);
			}
		});
		if(listener != null){
			try {
				listener.process(retVal.get());
			} catch (InterruptedException e) {
				LOG.error("InterruptedException :" + e);
			} catch (ExecutionException e) {
				LOG.error("ExecutionException :" + e);
			}
		}
		return retVal;
	}

	public interface CallbackListener{
		void process(ByteBuffer response);
	}
}
