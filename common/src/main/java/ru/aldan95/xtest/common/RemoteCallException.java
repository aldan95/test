package ru.aldan95.xtest.common;

public class RemoteCallException extends Exception {

    private RemoteCallStatus status;

    public RemoteCallException(RemoteCallStatus status, String message) {
        super(message);
        this.status = status;
    }

    public RemoteCallStatus getStatus() {
        return status;
    }
}
