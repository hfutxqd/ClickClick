package xyz.imxqd.luaframework.exception;

import android.annotation.TargetApi;
import android.os.Build;

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

    @TargetApi(Build.VERSION_CODES.N)
    public NotInitError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
