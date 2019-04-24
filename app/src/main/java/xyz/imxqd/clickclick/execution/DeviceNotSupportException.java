package xyz.imxqd.clickclick.execution;

public class DeviceNotSupportException extends ClickException {
    public DeviceNotSupportException() {
    }

    public DeviceNotSupportException(String message) {
        super(message);
    }

    public DeviceNotSupportException(String message, Throwable cause) {
        super(message, cause);
    }

    public DeviceNotSupportException(Throwable cause) {
        super(cause);
    }

    public DeviceNotSupportException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
