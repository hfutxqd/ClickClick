package xyz.imxqd.luaframework.core.value;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import cn.vimfung.luascriptcore.LuaContext;
import cn.vimfung.luascriptcore.LuaValue;
import xyz.imxqd.luaframework.LuaEngine;
import xyz.imxqd.luaframework.core.model.App;
import xyz.imxqd.luaframework.core.model.Device;

public class GlobalLuaValueProvider {
    public static void registerAll(LuaContext luaContext) {
        WindowManager wm = (WindowManager) LuaEngine.getGlobalContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            luaContext.setGlobal("device", new LuaValue(new Device(dm.widthPixels, dm.heightPixels)));

        } else {
            luaContext.setGlobal("device", new LuaValue(new Device(-1, -1)));

        }

        luaContext.setGlobal("app", new LuaValue(App.getInstance()));
    }
}
