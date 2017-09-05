package ru.aldan95.xtest.client;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import static org.junit.Assert.assertNotNull;

import org.slf4j.Logger;
import ru.aldan95.xtest.server.Server;

import static org.slf4j.LoggerFactory.getLogger;

public class ClientTest {

    private Logger logger = getLogger(ClientTest.class);

    @Test
    public void testGetCurrentDate() throws IOException, ExecutionException, InterruptedException {
        Server server = new Server(8080);
        server.start();
        Client client = new Client("localhost", 8080);
        Object obj = client.remoteCall("service1", "getCurrentDate", null);
        assertNotNull(obj);
        Assert.assertEquals(Date.class, obj.getClass());
        server.stop();
    }

    @Test
    public void testMultythread() throws IOException, InterruptedException {
        final int threadCount = 10;
        Server server = new Server(8080);
        server.start();
        Client client = new Client("localhost", 8080);
        Thread[] threads = new Thread[threadCount];
        for(int i=0;i<threadCount;i++) {
            threads[i] = new Thread(() -> {
                try {
                    for (int j=0; j<5; j++) {
                        client.remoteCall("service1", "sleep", new Object[]{1000L});
                        logger.info("Current Date is:" + client.remoteCall("service1", "getCurrentDate", new Object[]{}));
                    }
                } catch (Exception e) {
                    //logger.error("Exception!", e);
                }
            });
            threads[i].start();
        }
        for(int i=0;i<threadCount;i++) {
            threads[i].join();
        }
        server.stop();
    }
}
