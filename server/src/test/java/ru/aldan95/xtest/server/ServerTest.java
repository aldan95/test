package ru.aldan95.xtest.server;

import org.junit.Assert;
import org.junit.Test;
import ru.aldan95.xtest.common.Request;
import ru.aldan95.xtest.server.service.TestService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;

public class ServerTest {
    @Test
    public void testStartStop() throws IOException {
        Server server = new Server(8080);
        int activeThreads = Thread.activeCount();
        server.start();
        server.stop();
        assertEquals(activeThreads, Thread.activeCount());
    }

    private static class TestServerWorker extends ServerWorker {
        TestServerWorker(Request request, Object service) {
            super(request, service, null);
        }

        public Object callMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            return super.callMethod();
        }
    }

    private Object callMethod(String methodName, Object... params) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        TestServerWorker sw = new TestServerWorker(new Request(1, "Service1", methodName, params), new TestService());
        return sw.callMethod();
    }

    private Object callMethod(String name) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        TestServerWorker sw = new TestServerWorker(new Request(1, "Service1", name, null), new TestService());
        return sw.callMethod();
    }

    @Test
    public void testMethodCall() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        try {
            callMethod("tyfgjjlkg");
            Assert.assertTrue("Should throw NoSuchMethodException", false);
        } catch (NoSuchMethodException e) {
            // ok
        }
        Assert.assertEquals(Void.TYPE, callMethod("noret"));
        Assert.assertNull(callMethod("retnull"));
        Assert.assertEquals(1L, callMethod("retlong"));
        Assert.assertEquals("Hi 1", callMethod("retvar", "Hi ", 1L));
        Assert.assertEquals("Hi there", callMethod("retvar", "Hi ", "there"));
        Assert.assertEquals("nullthere", callMethod("retvar", null, "there"));
        try {
            callMethod("retvar", "Hi ", null);
            Assert.assertTrue("Should throw IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            // ok!
        }
    }
}
