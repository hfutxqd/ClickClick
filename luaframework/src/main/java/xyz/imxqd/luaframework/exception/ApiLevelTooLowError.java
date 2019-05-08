package xyz.imxqd.luaframework.exception;

import android.annotation.TargetApi;
import android.os.Build;

public class ApiLevelTooLowError extends RuntimeException {
    public ApiLevelTooLowError() {
    }

    public ApiLevelTooLowError(String message) {
        super(message);
    }

    public ApiLevelTooLowError(String message, Throwable cause) {
        super(message, cause);
    }

    public ApiLevelTooLowError(Throwable cause) {
        super(cause);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public ApiLevelTooLowError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
