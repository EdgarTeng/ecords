package com.tenchael.cords.netty;

/**
 * Server exception
 */
public class CordsException extends Exception {
    public CordsException() {
        super();
    }

    public CordsException(Throwable cause) {
        super(cause);
    }

    public CordsException(String message) {
        super(message);
    }

    public CordsException(String message, Throwable cause) {
        super(message, cause);
    }

    protected CordsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
