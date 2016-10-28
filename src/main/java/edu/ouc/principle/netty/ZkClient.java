package edu.ouc.principle.netty;

public class ZkClient {
	
	private volatile State state = State.CONNECTED;
	
	
	enum State{
		//正在连接
		CONNECTING,
		//已连接
		CONNECTED,
		//已关闭
		CLOSE;
		
		public boolean isConnected(){
			return this == CONNECTED;
		}
		public boolean isClosed(){
			return this == CLOSE;
		}
	}
}
