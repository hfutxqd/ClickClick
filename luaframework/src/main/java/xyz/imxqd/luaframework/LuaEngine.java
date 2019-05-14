package xyz.imxqd.luaframework;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import cn.vimfung.luascriptcore.LuaContext;
import xyz.imxqd.luaframework.core.method.LuaMethodProvider;
import xyz.imxqd.luaframework.core.value.GlobalLuaValueProvider;
import xyz.imxqd.luaframework.exception.NotInitError;

public class LuaEngine {

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    @SuppressLint("StaticFieldLeak")
    private static AccessibilityService sAccessibilityService;

    public static void init(Context context) {
        if (sContext == null) {
            synchronized (LuaEngine.class) {
                sContext = context.getApplicationContext();
            }
        }
    }

    public static void attachToAccessibilityService(AccessibilityService service) {
        sAccessibilityService = service;
    }

    public static void detachFromAccessibilityService() {
        sAccessibilityService = null;
    }

    private static volatile ActivityInfo currentActivity = null;

    private static ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return sContext.getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private static void detectCurrentActivity(AccessibilityEvent event) {
        ComponentName componentName = new ComponentName(
                event.getPackageName().toString(),
                event.getClassName().toString()
        );
        ActivityInfo activityInfo = tryGetActivity(componentName);
        boolean isActivity = activityInfo != null;
        if (isActivity) {
            currentActivity = activityInfo;
        }

    }

    public static void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOWS_CHANGED ||
                event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            detectCurrentActivity(event);
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
        luaContext.onException(s -> {
            Log.e("lua:error", s);
        });
        GlobalLuaValueProvider.registerAll(luaContext);
        LuaMethodProvider.registerAll(luaContext);
        return luaContext;
    }
}
