package edu.ouc.reactor;

import java.io.IOException;

public class ServerTest {

	private static int PORT = 8888;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		Thread t = new Thread(new Reactor(PORT,1024,new MyHandler()));
		t.start();
		System.out.println("server start");
		t.join();
	}
}
