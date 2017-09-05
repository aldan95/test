package ru.aldan95.xtest.common;

import java.io.Serializable;

public class Request implements Serializable {

    private static final long serialVersionUID = 3200384575891522932L;

    private int id;
    private String service;
    private String method;
    private boolean withParams;
    private transient Object[] params;

    public Request(int id, String service, String method, Object[] params) {
        this.id = id;
        this.service = service;
        this.method = method;
        this.params = params;
        this.withParams = params != null;
    }

    public int getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public String getMethod() {
        return method;
    }

    boolean isWithParams() {
        return withParams;
    }

    public Object[] getParams() {
        return params;
    }

    void setParams(Object[] params) {
        this.params = params;
    }
}
