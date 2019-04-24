package xyz.imxqd.clickclick.execution;

public class APILevelException extends ClickException {
    public APILevelException() {
    }

    public APILevelException(String message) {
        super(message);
    }

    public APILevelException(String message, Throwable cause) {
        super(message, cause);
    }

    public APILevelException(Throwable cause) {
        super(cause);
    }

    public APILevelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
