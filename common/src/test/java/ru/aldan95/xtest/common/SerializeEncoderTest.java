package ru.aldan95.xtest.common;

import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SerializeEncoderTest {

    private Encoder enc = new SerializeEncoder();

    private void assertRequestsEquals(Request r1, Request r2) {
        assertEquals("id", r1.getId(), r2.getId());
        assertEquals("service", r1.getService(), r2.getService());
        assertEquals("method", r1.getMethod(), r2.getMethod());
        assertEquals("isWithParams", r1.isWithParams(), r2.isWithParams());
        assertTrue("params", Objects.deepEquals(r1.getParams(), r2.getParams()));
    }

    @Test
    public void testFullRequest() throws IOException, ClassNotFoundException {
        Object[] params = new Object[1];
        params[0] = new Date();
        Request req = new Request(1, "service", "method", params);
        assertRequestsEquals(req, enc.decode(enc.encode(req)));
    }

    @Test
    public void testEmptyParams() throws IOException, ClassNotFoundException {
        Object[] params = new Object[1];
        Request req = new Request(1, "service", "method", params);
        assertRequestsEquals(req, enc.decode(enc.encode(req)));
    }

    @Test
    public void testNullParams() throws IOException, ClassNotFoundException {
        Request req = new Request(1, "service", "method", null);
        assertRequestsEquals(req, enc.decode(enc.encode(req)));
    }
}
