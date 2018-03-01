package com.mmz.specs.application.core.server;

public class ServerStartException extends ServerException {
    public ServerStartException() {
        super();
    }

    public ServerStartException(String message) {
        super(message);
    }

    public ServerStartException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServerStartException(Throwable cause) {
        super(cause);
    }

    protected ServerStartException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
