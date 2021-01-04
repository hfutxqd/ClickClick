package xyz.imxqd.luaframework.exception;

import android.annotation.TargetApi;
import android.os.Build;

public class LuaCoreVersionError extends RuntimeException {
    public LuaCoreVersionError() {
    }

    public LuaCoreVersionError(String message) {
        super(message);
    }

    public LuaCoreVersionError(String message, Throwable cause) {
        super(message, cause);
    }

    public LuaCoreVersionError(Throwable cause) {
        super(cause);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public LuaCoreVersionError(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
