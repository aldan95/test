package ru.aldan95.xtest.server.service;

import org.slf4j.Logger;

import java.util.Date;
import static org.slf4j.LoggerFactory.getLogger;

public class TestService {
    private static final Logger logger = getLogger(TestService.class);
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
    public Date adjust(Date d, Long l) {
        return new Date(d.getTime() + l);
    }
    public void noret() {

    }
    public Long retnull() {
        return null;
    }
    public Long retlong() {
        return 1L;
    }
    public String retvar(String s, Long l) {
        return s + String.valueOf(l);
    }
    public String retvar(String s, String s2) {
        return s + s2;
    }
}
