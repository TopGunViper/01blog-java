package edu.ouc.principle.netty;

public class ZkClient {
	
	private volatile State state = State.UN_CONNECTED;
	
	
	enum State{
		//正在连接
		CONNECTING,
		//已连接
		CONNECTED,
		//已连接
		UN_CONNECTED,
		//已关闭
		CLOSE;
		
		public boolean isConnected(){
			return this == CONNECTED;
		}
		public boolean isAlive(){
			return this != CLOSE;
		}
	}
}
