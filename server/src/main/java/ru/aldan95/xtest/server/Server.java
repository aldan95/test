package ru.aldan95.xtest.server;

import org.slf4j.Logger;
import ru.aldan95.xtest.common.Encoder;
import ru.aldan95.xtest.common.SerializeEncoder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.*;

import static org.slf4j.LoggerFactory.getLogger;

public class Server {
    private static final Logger logger = getLogger(Server.class);
    private int port;
    private int numWorkers = 4;
    private ServerSocket serverSocket;
    private ExecutorService ioWorkers;
    private ExecutorService serverWorkers;
    private Encoder encoder = new SerializeEncoder();
    private Thread serverAcceptor;
    private ServiceResolver serviceResolver;

    public Server(int port) {
        this.port = port;
    }

    private Server(int port, int numWorkers) {
        this.port = port;
        this.numWorkers = numWorkers;
    }

    public void start() throws IOException {
        serviceResolver = new PropertiesServiceResolver("server.properties");
        logger.info("Starting server on port {} with {} server workers...", port, numWorkers);
        serverSocket = new ServerSocket(port);
        ioWorkers = Executors.newCachedThreadPool(new ServerThreadFactory("io"));
        serverWorkers = new ThreadPoolExecutor(numWorkers, numWorkers,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ServerThreadFactory("exec")
        );
        serverAcceptor = new Thread(this::acceptLoop, "acceptLoop");
        serverAcceptor.start();
        logger.info("Server started.");
    }


    public void stop() {
        logger.info("Stopping server...");
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.error("Error closing server socket", e);
        }
        if (serverAcceptor != null) {
            serverAcceptor.interrupt();
            try {
                serverAcceptor.join(1000);
            } catch (InterruptedException e) {
                logger.warn("Can't stop acceptor thread in 1000 ms");
            }
        }
        shutdownWorkers();
        logger.info("Server stopped.");
    }

    private void acceptLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket clientSocket = serverSocket.accept();
                ioWorkers.submit(new IOWorker(clientSocket, encoder, serverWorkers, serviceResolver));
            } catch (SocketException e) {
                if ("Socket is closed".equalsIgnoreCase(e.getMessage()) || "Socket closed".equalsIgnoreCase(e.getMessage())) {
                    logger.info("Server is being shut down...");
                } else {
                    logger.error("Error accepting connection. Shutting down server...", e);
                }
            } catch (IOException e) {
                logger.error("Error accepting connection. Shutting down server...", e);
                break;
            }
        }
        shutdownWorkers();
    }

    private void shutdownWorkers() {
        serverWorkers.shutdownNow();
        ioWorkers.shutdownNow();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: java " + Server.class.getName() + " port [numWorkers(default:4)]");
            return;
        }
        int maxWorkers = 4;
        if (args.length > 1) {
            maxWorkers = Integer.parseInt(args[1]);
        }
        Server server = new Server(Integer.parseInt(args[0]), maxWorkers);
        server.start();
    }
}
