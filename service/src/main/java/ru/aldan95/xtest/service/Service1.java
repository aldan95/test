package ru.aldan95.xtest.service;

import org.slf4j.Logger;

import java.util.Date;

import static org.slf4j.LoggerFactory.getLogger;

public class Service1 {
    private static final Logger logger = getLogger(Service1.class);
    public void sleep(Long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            logger.error("sleep", e);
        }
    }
    public Date getCurrentDate() {
        return new Date();
    }
}
