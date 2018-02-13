package com.mmz.specs.application.core.server;

import com.mmz.specs.application.ApplicationException;

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
