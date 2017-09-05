package ru.aldan95.xtest.common;

import java.io.IOException;

public interface Encoder {
    byte[] encode(Request req) throws IOException;

    byte[] encodeResponse(Response res) throws IOException;

    Request decode(byte[] bytes) throws IOException, ClassNotFoundException;

    Response decodeResponse(byte[] bytes) throws IOException, ClassNotFoundException;
}
