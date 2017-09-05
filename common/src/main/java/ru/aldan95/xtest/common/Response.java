package ru.aldan95.xtest.common;

import java.io.Serializable;

public class Response implements Serializable {

    private int id;
    private RemoteCallStatus status;
    private String errDesc;
    private transient Object retVal;

    public Response(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public RemoteCallStatus getStatus() {
        return status;
    }

    public void setStatus(RemoteCallStatus status) {
        this.status = status;
    }

    public Object getRetVal() {
        return retVal;
    }

    public void setRetVal(Object retVal) {
        this.retVal = retVal;
    }

    public String getErrDesc() {
        return errDesc;
    }

    public void setErrDesc(String errDesc) {
        this.errDesc = errDesc;
    }
}
