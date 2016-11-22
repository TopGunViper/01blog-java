package edu.ouc.reactor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * client
 * blocking i/o 
 * 
 * @author wqx
 *
 */
public class Client{

	private final static Logger LOG = LoggerFactory.getLogger(Client.class);

	private InetSocketAddress remoteAddress;

	private SocketChannel sc;

	private CallbackListener listener;

	private static  Serialization serialization;

	static{
		 serialization = new DefaultJDKSerialization();
	}
	
	private final ByteBuffer lenBuffer = ByteBuffer.allocateDirect(4);
	
	private final AtomicLong pendingData = new AtomicLong();

	private volatile boolean isActive;

	public Client connect(InetSocketAddress address) throws IOException{
		this.remoteAddress = address;
		this.isActive = true;
		sc = SocketChannel.open();
		socket().connect(remoteAddress);
		if(listener != null){
			listener.onConnected();
		}
		return this;
	}
	public Client hook(CallbackListener listener){
		this.listener = listener;
		return this;
	}
	public Client serialization(Serialization serialization){
		this.serialization = serialization;
		return this;
	}
	public Socket socket(){
		return sc.socket();
	}

	public void closeGracefully(){
		if(!isActive) return;

		if(pendingData.get() == 0){
			try {
				if(sc != null){
					isActive = false;
					sc.close();
				}
			} catch (Exception e) {
				LOG.error("Unexpected Exception occur during close socket, e=" + e);
			}
		}else{
			//
		}
	}
	/**
	 * send msg synchronous
	 * 
	 * @param msg
	 * @return
	 * @throws IOException 
	 */
	public ByteBuffer syncSend(final Object msg){

		if(msg == null){
			throw new IllegalArgumentException("msg is null");
		}
		try{
			byte[] b = serialization.serialize(msg);
			int capacity = b.length;
			lenBuffer.putInt(capacity);
			lenBuffer.flip();
			
			ByteBuffer bb = ByteBuffer.allocate(capacity + 4);
			bb.put(lenBuffer);
			bb.put(b);
			
			bb.flip();//prepare for write
			sc.write(bb);
			while(bb.hasRemaining()){
				sc.write(bb);
			}
			
		}catch(Exception e){
			LOG.error("Unexpected Exception , e=" + e);
		}finally{
			lenBuffer.clear();
		}

		return null;
	}

	/**
	 * send msg asynchronous
	 * 
	 * @param msg
	 * @param listener
	 * @return
	 */
	public Future<ByteBuffer> asyncSend(final ByteBuffer msg){
		if(msg == null){
			throw new IllegalArgumentException("msg is null");
		}
		Future<ByteBuffer> retVal = new FutureTask<>(new Callable<ByteBuffer>(){
			@Override
			public ByteBuffer call() throws Exception {
				pendingData.incrementAndGet();
				return syncSend(msg);
			}
		});
		if(listener != null){
			try {
				ByteBuffer bb = retVal.get();
				/**
				 * CancellationException
				 * ExecutionException
				 * InterruptedException 
				 */
				pendingData.decrementAndGet();
				//
			} catch (InterruptedException e) {
				LOG.error("InterruptedException :" + e);
			} catch (ExecutionException e) {
				LOG.error("ExecutionException :" + e);
			}
		}
		return retVal;
	}

	static class DefaultJDKSerialization implements Serialization{

		@Override
		public byte[] serialize(Object src) throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(src);
			oos.flush();
			oos.close();
			return baos.toByteArray();
		}

		@Override
		public Object deserialize(byte[] src, Class<?> cls) throws IOException, ClassNotFoundException {
			Object object=null;
			ByteArrayInputStream sais=new ByteArrayInputStream(src);
			ObjectInputStream ois = new ObjectInputStream(sais);
			object=ois.readObject();
			return object;
		}
	}
}
