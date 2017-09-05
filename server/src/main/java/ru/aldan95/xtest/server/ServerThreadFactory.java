package ru.aldan95.xtest.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerThreadFactory implements ThreadFactory {
    private final String poolName;
    private final ThreadFactory defaultThreadFactory;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    ServerThreadFactory(String poolName) {
        this.poolName = poolName;
        this.defaultThreadFactory = Executors.defaultThreadFactory();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = defaultThreadFactory.newThread(r);
        t.setName(poolName + "-" + threadNumber.getAndIncrement());
        return t;
    }
}
