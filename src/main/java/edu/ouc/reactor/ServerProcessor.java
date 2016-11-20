package edu.ouc.reactor;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server handler
 * 
 * @author wqx
 */
public class ServerProcessor implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(ServerProcessor.class);

	Reactor reactor;

	private SocketChannel sc;

	private final SelectionKey sk;

	private final ByteBuffer lenBuffer = ByteBuffer.allocate(4);

	private ByteBuffer inputBuffer = lenBuffer;

	private ByteBuffer outputDirectBuffer = ByteBuffer.allocateDirect(1024 * 64);

	private LinkedBlockingQueue<ByteBuffer> outputQueue = new LinkedBlockingQueue<ByteBuffer>();


	public ServerProcessor(Reactor reactor, Selector sel,SocketChannel channel) throws IOException{
		this.reactor = reactor;
		sc = channel;
		sc.configureBlocking(false);
		sk = sc.register(sel, SelectionKey.OP_READ);
		sk.attach(this);
		sel.wakeup();
	}
	public void sendBuffer(ByteBuffer bb){
		try{
			synchronized(this.reactor){
				outputQueue.add(bb);
				enableWrite();
			}
		}catch(Exception e){
			LOG.error("Unexcepted Exception: ", e);
		}
	}
	private void enableWrite(){
		int i = sk.interestOps();
		if((i & SelectionKey.OP_WRITE) == 0){
			sk.interestOps(i | SelectionKey.OP_WRITE);
		}
	}
	private void disableWrite(){
		int i = sk.interestOps();
		if((i & SelectionKey.OP_WRITE) == 1){
			sk.interestOps(i & (~SelectionKey.OP_WRITE));			
		}
	}
	@Override
	public void run() {
		if(sc.isOpen()){
			if(sk.isReadable()){
				doRead();
			}else if(sk.isWritable()){
				doSend();
			}
		}else{
			LOG.error("try to do read/write operation on null socket");
			try {
				if(sc != null)
					sc.close();
				reactor.currentClients.decrementAndGet();
			} catch (IOException e) {}
		}
	}
	private void doRead(){
		try {
			int byteSize = sc.read(inputBuffer);
			if(byteSize < 0){
				LOG.error("Unable to read additional data");
			}
			if(LOG.isDebugEnabled()){
				LOG.debug("inputBuffer.hasRemaining():" + inputBuffer.hasRemaining());
			}
			if(!inputBuffer.hasRemaining()){
				
				if(inputBuffer == lenBuffer){
					//read length
					inputBuffer.flip();
					int len = inputBuffer.getInt();
					if(len < 0){
						throw new IllegalArgumentException("Illegal data length");
					}
					if(LOG.isDebugEnabled()){
						LOG.debug("receive data length:" + len);
					}
					//prepare to receive data
					inputBuffer = ByteBuffer.allocate(len);
				}else{
					//read data
					if(inputBuffer.hasRemaining()){
						sc.read(inputBuffer);
					}
					if(!inputBuffer.hasRemaining()){
						inputBuffer.flip();
						processRequest();
						//clear lenBuffer and waiting for next reading operation 
						lenBuffer.clear();
						inputBuffer = lenBuffer;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * process request and get response
	 * 
	 * @param request
	 * @return
	 */
	private void processRequest(){
		reactor.processRequest(this,inputBuffer);
	}
	private void doSend(){
		try{
			/**
			 * write data to channel£º
			 * step 1: write the length of data(occupy 4 byte)
			 * step 2: data content
			 */
			if(outputQueue.size() > 0){
				ByteBuffer directBuffer = outputDirectBuffer;
				directBuffer.clear();

				for(ByteBuffer buf : outputQueue){
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
				int sendSize = sc.write(directBuffer);
				
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
		} catch (CancelledKeyException e) {
            LOG.warn("Exception causing close of session 0x"
                    + " due to " + e);
            if (LOG.isDebugEnabled()) {
                LOG.debug("CancelledKeyException stack trace", e);
            }
            //close();
        } catch (IOException e) {
            LOG.warn("Exception causing close of session 0x"
                    + " due to " + e);
            if (LOG.isDebugEnabled()) {
                LOG.debug("IOException stack trace", e);
            }
            //close();
        }
	}
	
}
