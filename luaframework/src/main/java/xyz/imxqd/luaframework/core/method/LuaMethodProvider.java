package xyz.imxqd.luaframework.core.method;

import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import cn.vimfung.luascriptcore.LuaContext;
import cn.vimfung.luascriptcore.LuaValue;
import xyz.imxqd.luaframework.BuildConfig;
import xyz.imxqd.luaframework.LuaEngine;
import xyz.imxqd.luaframework.exception.ApiLevelTooLowError;
import xyz.imxqd.luaframework.exception.LuaCoreVersionError;

public class LuaMethodProvider {
    private static final HashMap<String, LuaMethod> DEFINED_METHODS = new HashMap<>();
    private static final Random sRandom = new Random();

    static {

        DEFINED_METHODS.put("requiresApi", new LuaMethod("requiresApi") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {
                if (luaValues.length == 1) {
                    int apiLevel = (int) luaValues[0].toInteger();
                    if (Build.VERSION.SDK_INT < apiLevel) {
                        throw new ApiLevelTooLowError("This lua requires api level >= " + apiLevel);
                    }
                }
                return new LuaValue(true);
            }
        });

        DEFINED_METHODS.put("requiresLuaCoreVersion", new LuaMethod("requiresLuaCoreVersion") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {
                if (luaValues.length == 1) {
                    int coreVersion = (int) luaValues[0].toInteger();
                    if (BuildConfig.VERSION_CODE < coreVersion) {
                        throw new LuaCoreVersionError("This lua requires lua core version >= " + coreVersion);
                    }
                }
                return new LuaValue(true);
            }
        });

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


        DEFINED_METHODS.put("toast", new LuaMethod("toast") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {
                if (luaValues.length == 1) {
                    Toast.makeText(LuaEngine.getGlobalContext(), luaValues[0].toString(), Toast.LENGTH_SHORT).show();
                }
                return null;
            }
        });

        DEFINED_METHODS.put("sleep", new LuaMethod("sleep") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {
                if (luaValues.length == 1) {
                    try {
                        long time = luaValues[0].toInteger();
                        Thread.sleep(time);
                    } catch (Throwable ignore) { }
                }
                return null;
            }
        });

        DEFINED_METHODS.put("waitForActivity", new LuaMethod("waitForActivity") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {

                return null;
            }
        });

        DEFINED_METHODS.put("waitForPackage", new LuaMethod("waitForPackage") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {

                return null;
            }
        });

        DEFINED_METHODS.put("random", new LuaMethod("random") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {
                if (luaValues.length == 2) {
                    long start = luaValues[0].toInteger();
                    long end = luaValues[1].toInteger();
                    return new LuaValue(start + sRandom.nextInt((int)(end - start)));
                }
                return null;
            }
        });



        DEFINED_METHODS.put("exit", new LuaMethod("exit") {
            @Override
            public LuaValue onExecute(LuaValue[] luaValues) {
                this.luaContext.raiseException("exit");
                return null;
            }
        });
    }

    public static void registerAll(LuaContext luaContext) {
        Set<String> names = DEFINED_METHODS.keySet();
        for (String name : names) {
            LuaMethod luaMethod = DEFINED_METHODS.get(name);
            if (luaMethod != null) {
                luaMethod.setLuaContext(luaContext);
                luaContext.registerMethod(name, luaMethod);
            }

        }
    }
}
