package ru.aldan95.xtest.client;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import ru.aldan95.xtest.common.*;

import org.slf4j.Logger;
import org.slf4j.MDC;
import static org.slf4j.LoggerFactory.getLogger;

public class Client implements AutoCloseable {

    private static final Logger logger = getLogger(Client.class);

    private final Socket socket;
    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;
    private final AtomicInteger idGen = new AtomicInteger(1);
    private final Encoder encoder = new SerializeEncoder();
    private final ConcurrentSkipListMap<Integer, RequestInProgress> requestsInProgress = new ConcurrentSkipListMap<>();
    private final Thread readLoop;

    public Client(String host, int port) throws IOException {
        socket = new Socket(host, port);
        outputStream = new DataOutputStream(socket.getOutputStream());
        inputStream = new DataInputStream(socket.getInputStream());
        readLoop = new Thread(this::readLoop, "readLoop:" + host + ":" + port);
        readLoop.setDaemon(true);
        readLoop.start();
    }

    public Object remoteCall(String serviceName, String methodName, Object[] params) throws ExecutionException, InterruptedException {
        Request request = new Request(idGen.getAndIncrement(), serviceName, methodName, params);
        MDC.put("req", "id=" + request.getId() + ":" + request.getService() + "." + request.getMethod());
        logger.info("Call with params {}", params);
        try {
            Future<Object> reqCompletion = send(request);
            Object result = reqCompletion.get();
            logger.info("Call result: {}", result);
            return reqCompletion.get();
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error calling method", e);
            throw e;
        } finally {
            MDC.clear();
        }
    }

    private Future<Object> send(Request req) {
        CompletableFuture<Object> sendFuture = new CompletableFuture<>();
        requestsInProgress.put(req.getId(), new RequestInProgress(req, sendFuture));
        try {
            byte[] reqBytes = encoder.encode(req);
            synchronized (outputStream) {
                outputStream.writeInt(reqBytes.length);
                outputStream.write(reqBytes);
                outputStream.flush();
            }
        } catch (IOException e) {
            sendFuture.completeExceptionally(e);
            logger.error("Error sending request", e);
        }
        return sendFuture;
    }

    @Override
    public void close() throws Exception {
        readLoop.interrupt();
        outputStream.close();
        inputStream.close();
        socket.close();
        EOFException eof = new EOFException("Client stopped by close() call");
        requestsInProgress.forEach((id, rip) -> rip.future.completeExceptionally(eof));
        requestsInProgress.clear();
    }

    private void readLoop() {
        while (true) {
            try {
                int dataLength = inputStream.readInt();
                byte[] buf = new byte[dataLength];
                inputStream.readFully(buf);
                Response response = encoder.decodeResponse(buf);
                RequestInProgress inProgress = requestsInProgress.remove(response.getId());
                if (inProgress != null) {
                    if (response.getStatus() == RemoteCallStatus.Success) {
                        inProgress.future.complete(response.getRetVal());
                    } else {
                        inProgress.future.completeExceptionally(new RemoteCallException(response.getStatus(), response.getErrDesc()));
                    }
                } else {
                    logger.error("Request with id={} not found in requestsInProgress while processing response", response.getId());
                }
            } catch (IOException e) {
                logger.error("Error reading response, closing client", e);
                requestsInProgress.forEach((id, rip) -> rip.future.completeExceptionally(e));
                requestsInProgress.clear();
                try {
                    close();
                } catch (Exception e1) {
                    logger.error("Error closing client", e1);
                }
                return;
            } catch (ClassNotFoundException e) {
                logger.error("Error deserialize response", e);
            }
        }
    }

    private static class RequestInProgress {
        Request request;
        long sendTime;
        CompletableFuture<Object> future;

        RequestInProgress(Request request, CompletableFuture<Object> future) {
            this.request = request;
            this.sendTime = System.currentTimeMillis();
            this.future = future;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 2) {
            System.err.println("Usage: client host port");
            return;
        }
        final int threadCount = 10;
        Client client = new Client(args[0], Integer.parseInt(args[1]));
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
    }
}
