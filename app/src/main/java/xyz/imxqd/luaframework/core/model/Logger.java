package xyz.imxqd.luaframework.core.model;

import android.util.Log;

import cn.vimfung.luascriptcore.LuaExportType;

public class Logger implements LuaExportType {
    private static final String TAG = "lua.log";

    private static Logger _sLogger;

    public static Logger get() {
        if (_sLogger == null) {
            synchronized (Logger.class) {
                _sLogger = new Logger();
            }
        }
        return _sLogger;
    }

    public void i(String str) {
        Log.i(TAG, str);
    }

    public void d(String str) {
        Log.d(TAG, str);
    }

    public void v(String str) {
        Log.v(TAG, str);
    }

    public void w(String str) {
        Log.w(TAG, str);
    }

    public void e(String str) {
        Log.e(TAG, str);
    }

    public void wtf(String str) {
        Log.wtf(TAG, str);
    }
}
