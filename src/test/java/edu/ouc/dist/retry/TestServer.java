package edu.ouc.dist.retry;

import java.io.IOException;

public class TestServer {
    public static void main(String[] args) {
        CalcServer server = null;
        try {
            server = new CalcServer();
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
