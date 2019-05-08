package xyz.imxqd.luaframework;

import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Set;

import cn.vimfung.luascriptcore.LuaContext;
import cn.vimfung.luascriptcore.LuaValue;

public class LuaMethodProvider {
    private static final HashMap<String, LuaMethod> DEFINED_METHODS = new HashMap<>();
    static {
        DEFINED_METHODS.put("print", new LuaMethod("print") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {
                if (luaValues.length == 1) {
                    Log.i("lua:print", luaValues[0].toString());
                } else {
                    Log.i("lua:print", "");
                }
                return null;
            }
        });

        DEFINED_METHODS.put("version", new LuaMethod("version") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {
                return new LuaValue(BuildConfig.VERSION_CODE);
            }
        });

        DEFINED_METHODS.put("toast", new LuaMethod("toast") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {
                if (luaValues.length == 1) {
                    Toast.makeText(LuaEngine.getGlobalContext(), luaValues[0].toString(), Toast.LENGTH_SHORT).show();
                }
                return null;
            }
        });
    }

    public static void registerAll(LuaContext luaContext) {
        Set<String> names = DEFINED_METHODS.keySet();
        for (String name : names) {
            luaContext.registerMethod(name, DEFINED_METHODS.get(name));
        }
    }
}
