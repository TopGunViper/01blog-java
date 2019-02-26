package edu.ouc.dp.async;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.alibaba.fastjson.JSON;

public class Client {

	private final BlockingQueue<Packet> outgoingQueue = new LinkedBlockingQueue<Packet>(); 

	private Worker worker;

	public Client(String host, int port){
		worker = new Worker(host,port);
		worker.start();
	}
	/**
	 * 
	 * @param nodeInfo:模拟节点信息
	 * @param cb：回调函数
	 * @param ctx：上下文信息context
	 */
	public void asyncCreate(String nodeInfo,CallBack cb, Object ctx){
		Packet packet = new Packet();

		packet.setRequest(nodeInfo);
		packet.setCb(cb);
		packet.setCtx(ctx);

		outgoingQueue.offer(packet);
	}

	class Worker extends Thread {

		private String host;

		private int port;

		public Worker(String host, int port){
			this.host = host;
			this.port = port;
			setDaemon(true);
		}
		public void run(){
			while(Thread.interrupted()){

				Packet packet = null;
				try {
					packet = outgoingQueue.take();
				} catch (InterruptedException e) {}

				Object resp = sendPacket(packet);

				Packet p = (Packet)resp;

				packet.getCb().process(p.getErrorCode(), p.getResponse(), p.getCtx());
			}

		}
		public Object sendPacket(Packet packet){
			Object resp = null;
			try{
				Socket socket = new Socket(host,port);

				OutputStream oos = socket.getOutputStream();
				InputStream ois = socket.getInputStream();
				try{
					oos.write(JSON.toJSONString(packet).getBytes());
					oos.flush();

					byte[] buf = new byte[1024];
					int recvSize = ois.read(buf);
					String text = new String(buf,0,recvSize);
					resp = JSON.parseObject(text, Packet.class);
				}finally{
					if(oos != null){
						oos.close();
					}
					if(ois != null){
						ois.close();
					}
					socket.close();
				}
			}catch(Exception e){}

			return resp;
		}
	}
}
