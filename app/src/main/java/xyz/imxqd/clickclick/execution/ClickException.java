package xyz.imxqd.clickclick.execution;

public class ClickException extends RuntimeException {
    public ClickException() {
    }

    public ClickException(String message) {
        super(message);
    }

    public ClickException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClickException(Throwable cause) {
        super(cause);
    }

    public ClickException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
