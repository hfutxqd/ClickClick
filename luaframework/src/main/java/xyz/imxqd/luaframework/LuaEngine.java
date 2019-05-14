package xyz.imxqd.luaframework;

import android.annotation.SuppressLint;
import android.content.Context;

import cn.vimfung.luascriptcore.LuaContext;
import xyz.imxqd.luaframework.core.method.LuaMethodProvider;
import xyz.imxqd.luaframework.core.value.GlobalLuaValueProvider;
import xyz.imxqd.luaframework.exception.NotInitError;

public class LuaEngine {

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    public static void init(Context context) {
        if (sContext == null) {
            synchronized (LuaEngine.class) {
                sContext = context.getApplicationContext();
            }
        }
    }

    public static Context getGlobalContext() {
        return sContext;
    }

    public static LuaContext createContext() {
        if (sContext == null) {
            throw new NotInitError();
        }
        LuaContext luaContext = LuaContext.create(sContext);
        GlobalLuaValueProvider.registerAll(luaContext);
        LuaMethodProvider.registerAll(luaContext);
        return luaContext;
    }
}
