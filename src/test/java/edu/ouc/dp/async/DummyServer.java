package edu.ouc.dp.async;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import com.alibaba.fastjson.JSON;

public class DummyServer {

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(8888);
		System.out.println("");

		while(!Thread.interrupted()){

			Socket socket = ss.accept();
			System.out.println("Server started...");

			OutputStream oos = socket.getOutputStream();
			InputStream ois = socket.getInputStream();
			try{
				byte[] buf = new byte[1024];
				int recvSize = ois.read(buf);
				String text = new String(buf,0,recvSize);
				Packet request = (Packet)JSON.parseObject(text, Packet.class);

				Packet response = new Packet();				
				if("exist NodeInfo".equals(request.getRequest())){
					response.setErrorCode(110);
					response.setResponse("node is exist.");
					response.setCtx(request.getCtx());
				}
				oos.write(JSON.toJSONString(response).getBytes());
				oos.flush();
			}finally{
				if(oos != null){
					oos.close();
				}
				if(ois != null){
					ois.close();
				}
			}
		}
	}

}
