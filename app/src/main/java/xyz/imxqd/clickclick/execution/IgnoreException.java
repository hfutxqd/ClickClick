package xyz.imxqd.clickclick.execution;

public class IgnoreException extends ClickException {
    public IgnoreException() {
    }

    public IgnoreException(String message) {
        super(message);
    }

    public IgnoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public IgnoreException(Throwable cause) {
        super(cause);
    }

    public IgnoreException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
