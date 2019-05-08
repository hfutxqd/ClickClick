package xyz.imxqd.luaframework;

import android.content.Context;
import android.util.Log;

import cn.vimfung.luascriptcore.LuaContext;

public class LuaEngine {

    private static Context sContext;

    public static void init(Context context) {
        if (sContext == null) {
            synchronized (LuaEngine.class) {
                sContext = context.getApplicationContext();
            }
        }
    }

    protected static Context getGlobalContext() {
        return sContext;
    }

    public static LuaContext createContext() {
        if (sContext == null) {
            throw new NotInitError();
        }
        LuaContext luaContext = LuaContext.create(sContext);
        luaContext.registerMethod("print", luaValues -> {
            if (luaValues.length == 1) {
                Log.i("lua:print", luaValues[0].toString());
            } else {
                Log.i("lua:print", "");
            }
            return null;
        });
        luaContext.onException(s -> {
            Log.e("lua:error", s);
        });
        return luaContext;
    }
}
