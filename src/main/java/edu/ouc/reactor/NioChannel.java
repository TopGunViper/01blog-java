package edu.ouc.reactor;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioChannel {

	private static final Logger LOG = LoggerFactory.getLogger(NioChannel.class);

	private Reactor reactor;

	private SelectableChannel sc;

	private SelectionKey key;

	private NioChannelSink sink;

	private volatile ChannelHandler handler;

	private final ByteBuffer lenBuffer = ByteBuffer.allocate(4);

	private ByteBuffer inputBuffer = lenBuffer;

	private ByteBuffer outputDirectBuffer = ByteBuffer.allocateDirect(1024 * 64);

	private LinkedBlockingQueue<ByteBuffer> outputQueue = new LinkedBlockingQueue<ByteBuffer>();

	public NioChannel(SelectionKey key){
		this.key = key;
		this.sc = key.channel();
		try {
			sc.configureBlocking(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		sink = new NioChannelSink();
	}

	public ChannelHandler handler(){
		return handler;
	}
	public NioChannelSink nioChannelSink(){
		return sink;
	}

	public void sendBuffer(ByteBuffer bb){
		try{
			synchronized(this){
				if(LOG.isDebugEnabled()){
					LOG.debug("add sendable bytebuffer into outputQueue");
				}
				//wrap ByteBuffer with length header
				ByteBuffer wrapped = wrap(bb);

				outputQueue.add(wrapped);

				enableWrite();
			}
		}catch(Exception e){
			LOG.error("Unexcepted Exception: ", e);
		}
	}

	private ByteBuffer wrap(ByteBuffer bb){
		bb.flip();
		lenBuffer.clear();
		int len = bb.remaining();
		lenBuffer.putInt(len);
		ByteBuffer resp = ByteBuffer.allocate(len+4);
		lenBuffer.flip();

		resp.put(lenBuffer);
		resp.put(bb);
		return resp;
	}
	private void enableWrite(){
		int i = key.interestOps();
		if((i & SelectionKey.OP_WRITE) == 0){
			key.interestOps(i | SelectionKey.OP_WRITE);
		}
	}
	private void disableWrite(){
		int i = key.interestOps();
		if((i & SelectionKey.OP_WRITE) == 1){
			key.interestOps(i & (~SelectionKey.OP_WRITE));			
		}
	}

	class NioChannelSink{

		public void doRead() {

			SocketChannel socketChannel = (SocketChannel)sc;

			int byteSize;
			try {
				byteSize = socketChannel.read(inputBuffer);

				if(byteSize < 0){
					LOG.error("Unable to read additional data");
					throw new RuntimeException("Unable to read additional data");
				}
				if(!inputBuffer.hasRemaining()){

					if(inputBuffer == lenBuffer){
						//read length
						inputBuffer.flip();
						int len = inputBuffer.getInt();
						if(len < 0){
							throw new IllegalArgumentException("Illegal data length");
						}
						//prepare for receiving data
						inputBuffer = ByteBuffer.allocate(len);
					}else{
						//read data
						if(inputBuffer.hasRemaining()){
							socketChannel.read(inputBuffer);
						}
						if(!inputBuffer.hasRemaining()){
							inputBuffer.flip();
							//processAndHandOff(inputBuffer);
							handler;
							//clear lenBuffer and waiting for next reading operation 
							lenBuffer.clear();
							inputBuffer = lenBuffer;
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void doSend(){
			/**
			 * write data to channel£º
			 * step 1: write the length of data(occupy 4 byte)
			 * step 2: data content
			 */
			try {
				if(outputQueue.size() > 0){
					ByteBuffer directBuffer = outputDirectBuffer;
					directBuffer.clear();
					for(ByteBuffer buf : outputQueue){
						buf.flip();

						if(buf.remaining() > directBuffer.remaining()){
							//prevent BufferOverflowException
							buf = (ByteBuffer) buf.slice().limit(directBuffer.remaining());
						}
						//transfers the bytes remaining in buf into  directBuffer
						int p = buf.position();
						directBuffer.put(buf);
						//reset position
						buf.position(p);

						if(!directBuffer.hasRemaining()){
							break;
						}
					}
					directBuffer.flip();
					int sendSize = ((SocketChannel)sc).write(directBuffer);

					while(!outputQueue.isEmpty()){
						ByteBuffer buf = outputQueue.peek();
						int left = buf.remaining() - sendSize;
						if(left > 0){
							buf.position(buf.position() + sendSize);
							break;
						}
						sendSize -= buf.remaining();
						outputQueue.remove();
					}
				}

				synchronized(reactor){
					if(outputQueue.size() == 0){
						//disable write
						disableWrite();
					}else{
						//enable write
						enableWrite();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}// end NioChannelSink
}
