package edu.ouc.dist.retry;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalcClientProxy implements InvocationHandler {
    private static final Logger LOG = LoggerFactory.getLogger(CalcClientProxy.class);

    private static final int TIMEOUT = 10 * 1000;
    private static final String HOST = "localhost";
    private static final int PORT = 8088;

    private CalcClientProxy(){}

    public static Calculator getProxy() {
        return (Calculator) Proxy.newProxyInstance(CalcClientProxy.class.getClassLoader(),
                new Class<?>[] {Calculator.class}, new CalcClientProxy());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return calcInternal((Integer) args[0], (Integer) args[1]);
    }

    private int calcInternal(int a, int b) throws Exception {
        LOG.info("[op:calcInternal] =====> a={}, b={}", a, b);
        int retVal;
        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        try {
            Socket cli = new Socket();
            cli.connect(new InetSocketAddress(HOST, PORT), TIMEOUT);
            LOG.info("[op:calcInternal] =====> Connect to Server. host={}, port={}", HOST, PORT);
            outputStream = new ObjectOutputStream(cli.getOutputStream());
            outputStream.writeInt(a);
            outputStream.writeInt(b);
            outputStream.flush();
            inputStream = new ObjectInputStream(cli.getInputStream());
            retVal = inputStream.readInt();
        } catch (Exception e) {
            LOG.error("Error occur. ", e);
            throw e;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
        return retVal;
    }

}
