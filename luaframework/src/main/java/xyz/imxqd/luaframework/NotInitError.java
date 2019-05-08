package xyz.imxqd.luaframework;

public class NotInitError extends RuntimeException {
    public NotInitError() {
    }

    public NotInitError(String message) {
        super(message);
    }

    public NotInitError(String message, Throwable cause) {
        super(message, cause);
    }

    public NotInitError(Throwable cause) {
        super(cause);
    }

    public NotInitError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
