package edu.ouc.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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

	private volatile boolean isActive;

	public Client(){
		try {
			sc = SocketChannel.open();
			sc.configureBlocking(true);
		} catch (IOException e) {}
	}

	public void connect(InetSocketAddress address){
		this.remoteAddress = address;
		try {
			socket().connect(remoteAddress);
			this.isActive = true;
		} catch (IOException e) {
			LOG.error("Can not connecting to remoteAddress:" + this.remoteAddress);
			this.isActive = false;
			throw new RuntimeException("Can not connecting to remoteAddress:" + this.remoteAddress);
		}
		LOG.info("Successfully connecting to remoteAddress:" + this.remoteAddress);
	}
	public Socket socket(){
		return sc.socket();
	}

	public synchronized void close(){
		if(!isActive) return;
		isActive = false;
		try {
			if(sc != null){
				sc.close();
			}
		} catch (Exception e) {
			LOG.error("Unexpected Exception occur during close socket, e=" + e);
		}
	}
	/**
	 * send msg synchronous
	 * 
	 * @param msg
	 * @return
	 * @throws IOException 
	 */
	public ByteBuffer syncSend(final ByteBuffer msg){

		if(msg == null){
			throw new IllegalArgumentException("msg is null");
		}
		if(!isActive){
			throw new RuntimeException("Connection is invalid");
		}
		try{
			ByteBuffer lenBuffer = ByteBuffer.allocate(4);
			int capacity = msg.remaining();
			lenBuffer.putInt(capacity);
			lenBuffer.flip();

			ByteBuffer bb = ByteBuffer.allocate(capacity + 4);
			bb.put(lenBuffer);
			bb.put(msg);
			bb.flip();//prepare for write
			while(bb.hasRemaining()){
				sc.write(bb);//blocking model
			}
		}catch(Exception e){
			LOG.error("Unexpected Exception during writing msg, e=" + e);
		}

		return readResponse();
	}
	private ByteBuffer readResponse(){
		ByteBuffer resp = null;;
		try {
			int len = readLength();
			if(len == 0){
				close();
				return null;
			}
			resp = ByteBuffer.allocate(len);
			sc.read(resp);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (ByteBuffer) resp.flip();
	}
	private int readLength() throws IOException{
		int length = 0;
		try {
			ByteBuffer lenBuffer = ByteBuffer.allocateDirect(4);
			sc.read(lenBuffer);//blocked

			lenBuffer.flip();
			length = lenBuffer.getInt();
		}catch(Exception e){
			length = -1;
		}finally{
			if(length <= 0 || length > 1024){
				length = 0;
			}
		}
		return length;
	}
}
