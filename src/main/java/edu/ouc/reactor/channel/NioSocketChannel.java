package edu.ouc.reactor.channel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NioSocketChannel extends NioChannel{

	private static final Logger LOG = LoggerFactory.getLogger(NioSocketChannel.class);

	public NioSocketChannel() throws IOException{
		super( newSocket());
	}
	public NioSocketChannel(SocketChannel sc) throws IOException{
		super(sc);
	}
	public static SocketChannel newSocket(){
		SocketChannel socketChannel = null;
		try {
			socketChannel = SocketChannel.open();
		} catch (IOException e) {
		}
		return socketChannel;
	}

	@Override
	public NioChannelSink nioChannelSink() {
		return new NioSocketChannelSink();
	}

	class NioSocketChannelSink implements NioChannelSink{
		
		private static final int MAX_LEN = 1024;
		
		ByteBuffer lenBuffer = ByteBuffer.allocate(4);

		ByteBuffer inputBuffer = lenBuffer;

		ByteBuffer outputDirectBuffer = ByteBuffer.allocateDirect(1024 * 64);

		LinkedBlockingQueue<ByteBuffer> outputQueue = new LinkedBlockingQueue<ByteBuffer>();

		public void close(){
			//clear buffer
			outputDirectBuffer = null;

			try {
				if(sc != null){
					sc.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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
						lenBuffer.flip();
						int len = lenBuffer.getInt();
						if(len < 0 || len > MAX_LEN){
							throw new IllegalArgumentException("Illegal data length, len:" + len);
						}
						//prepare for receiving data
						inputBuffer = ByteBuffer.allocate(len);
						inputBuffer.clear();
					}else{
						//read data
						if(inputBuffer.hasRemaining()){
							socketChannel.read(inputBuffer);
						}
						if(!inputBuffer.hasRemaining()){
							inputBuffer.flip();
							
							fireChannelRead(inputBuffer);
							
							//clear lenBuffer and waiting for next reading operation 
							lenBuffer.clear();
							inputBuffer = lenBuffer;
						}
					}
				}
			} catch (Throwable t) {
				if(LOG.isDebugEnabled()){
					LOG.debug("Exception :" + t);
				}
				fireExceptionCaught(t);
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
			} catch (Throwable t) {
				fireExceptionCaught(t);
			}
		}
		private ByteBuffer wrapWithHead(ByteBuffer bb){
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
		public void sendBuffer(ByteBuffer bb){
			try{
				synchronized(this){
					//wrap ByteBuffer with length header
					ByteBuffer wrapped = wrapWithHead(bb);

					outputQueue.add(wrapped);

					enableWrite();
				}
			}catch(Exception e){
				LOG.error("Unexcepted Exception: ", e);
			}
		}
	}// end NioChannelSink

	@Override
	public void bind(InetSocketAddress remoteAddress) throws Exception {
		throw new UnsupportedOperationException();
	}
	@Override
	public void connect(InetSocketAddress remoteAddress) throws Exception {
		SocketChannel socketChannel = (SocketChannel)sc;
		socketChannel.connect(remoteAddress);
	}
}
