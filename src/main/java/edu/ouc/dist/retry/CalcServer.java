package edu.ouc.dist.retry;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalcServer extends Thread {
    private static final Logger LOG = LoggerFactory.getLogger(CalcServer.class);

    private static final int DEFAULT_PORT = 8088;
    private static final int DEFAULT_BACKLOG = 50;

    private ServerSocket server;
    private int port;
    private int backlog;

    private volatile boolean shutdown;

    public CalcServer() throws IOException {
        this(DEFAULT_PORT, DEFAULT_BACKLOG);
    }

    public CalcServer(int port, int backlog) throws IOException {
        server = new ServerSocket();
        server.setReceiveBufferSize(1024);
        this.port = port;
        this.backlog = backlog;
    }

    @Override
    public void run() {
        LOG.info("Start CalcServer.");
        try {
            Objects.requireNonNull(server);
            server.bind(new InetSocketAddress(port), backlog);
            while (!shutdown) {
                Socket socket = server.accept();
                // blocking handler.
                handle(socket);
            }
        } catch (IOException ioe) {
            LOG.error("An error occur during listening. port={}, e={}", port, ioe);
        } finally {
            shutdown();
        }
        LOG.info("[op:start] <===== End of CalcServer.");
    }

    private void handle(Socket socket) {
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStreams = null;
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStreams = new ObjectOutputStream(socket.getOutputStream());
            int a = inputStream.readInt();
            int b = inputStream.readInt();
            int sum = a + b;
            LOG.info("[op:handle] =====> a={}, b={}, retVal={}", a, b, sum);
            outputStreams.writeInt(sum);
            outputStreams.flush();
        } catch (IOException ioe) {
            LOG.error("An error occur during handling request.", ioe);
        } finally {
            close(inputStream, outputStreams);
        }

    }

    public synchronized void shutdown() {
        shutdown = true;
        if (server != null) {
            close(server);
            server = null;
        }
    }

    private void close(AutoCloseable... closeables) {
        if (closeables != null && closeables.length > 0) {
            for (AutoCloseable c : closeables) {
                try {
                    c.close();
                } catch (Exception ignored) {
                    // ignored
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        CalcServer server = new CalcServer();
        server.start();
    }
}
