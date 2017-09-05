package ru.aldan95.xtest.server;

import org.slf4j.Logger;
import ru.aldan95.xtest.common.Encoder;
import ru.aldan95.xtest.common.Request;
import ru.aldan95.xtest.common.Response;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import static org.slf4j.LoggerFactory.getLogger;

public class IOWorker implements Runnable {

    private static final Logger logger = getLogger(IOWorker.class);

    private final ExecutorService serverExecutor;
    private final Socket socket;
    private final Encoder encoder;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private final ServiceResolver serviceResolver;

    IOWorker(Socket socket, Encoder encoder, ExecutorService serverExecutor, ServiceResolver serviceResolver) throws IOException {
        this.socket = socket;
        this.encoder = encoder;
        this.serverExecutor = serverExecutor;
        this.serviceResolver = serviceResolver;
        try {
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    @Override
    public void run() {
        try {
            readLoop();
        } catch (EOFException eof) {
            logger.info("Client has closed connection");
        } catch (IOException ioe) {
            logger.error("Error processing input", ioe);
        } catch (ClassNotFoundException e) {
            logger.error("Error decoding request", e);
        } finally {
            close();
        }
    }

    private void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Error closing socket", e);
        }
    }

    private void readLoop() throws IOException, ClassNotFoundException {

        while (!Thread.currentThread().isInterrupted()) {
            int dataLength = inputStream.readInt();
            byte[] buf = new byte[dataLength];
            inputStream.readFully(buf);
            Request request = encoder.decode(buf);
            logger.info("Received id={}:{}.{} with params {}", request.getId(), request.getService(), request.getMethod(), request.getParams());
            Object service = serviceResolver.resolve(request.getService());
            serverExecutor.execute(new ServerWorker(request, service, this::sendResponse));
        }
    }

    private void sendResponse(Response response) {
        try {
            byte[] resBytes = encoder.encodeResponse(response);
            synchronized (outputStream) {
                outputStream.writeInt(resBytes.length);
                outputStream.write(resBytes);
                outputStream.flush();
            }
        } catch (IOException e) {
            logger.error("Error sending response", e);
        }
    }

}
