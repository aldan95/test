package ru.aldan95.xtest.common;

import java.io.*;

public class SerializeEncoder implements Encoder {

    @Override
    public byte[] encode(Request req) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(req);
        if (req.isWithParams() && req.getParams() != null) {
            oos.writeObject(req.getParams());
        }
        oos.close();
        return baos.toByteArray();
    }

    @Override
    public byte[] encodeResponse(Response res) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(res);
        if (res.getStatus() == RemoteCallStatus.Success) {
            oos.writeObject(res.getRetVal() != null? res.getRetVal() : Void.TYPE);
        }
        oos.close();
        return baos.toByteArray();
    }

    @Override
    public Request decode(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Request req = (Request)ois.readObject();
        if (req.isWithParams()) {
            req.setParams((Object[])ois.readObject());
        }
        return req;
    }

    @Override
    public Response decodeResponse(byte[] bytes) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
        Response res = (Response) ois.readObject();
        if (res.getStatus() == RemoteCallStatus.Success) {
            res.setRetVal(ois.readObject());
        }
        return res;
    }
}
