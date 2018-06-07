package xyz.imxqd.clickclick.xposed;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.KeyEvent;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XCallback;
import xyz.imxqd.clickclick.service.IClickIPC;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

public class KeyEventMod {
    private static final String TAG = "ClickClick_KeyEventMod";

    public final static int FLAG_WAKE = 0x00000001;
    public final static int FLAG_VIRTUAL = 0x00000002;

    public final static int FLAG_INJECTED = 0x01000000;
    public final static int FLAG_TRUSTED = 0x02000000;
    public final static int FLAG_FILTERED = 0x04000000;
    public final static int FLAG_DISABLE_KEY_REPEAT = 0x08000000;

    public final static int FLAG_INTERACTIVE = 0x20000000;
    public final static int FLAG_PASS_TO_USER = 0x40000000;

    protected static Field mFieldKeyFlags;

    static {
        try {
            mFieldKeyFlags = KeyEvent.class.getDeclaredField("mFlags");
            mFieldKeyFlags.setAccessible(true);

        } catch (Throwable e) {
        }
    }

    public static void init(XC_LoadPackage.LoadPackageParam packageParam) {
        String packageName = packageParam.packageName;
        if (!packageName.equals("android")) {
            return;
        }
        Log.d(TAG, "android loaded.");
        try {
            Class<?> classPhoneWindowManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                classPhoneWindowManager = findClass("com.android.server.policy.PhoneWindowManager", packageParam.classLoader);
            } else {
                classPhoneWindowManager = findClass("com.android.internal.policy.impl.PhoneWindowManager", packageParam.classLoader);
            }
            if (classPhoneWindowManager == null) {
                return;
            }
            findAndHookMethod(classPhoneWindowManager, "interceptKeyBeforeQueueing", KeyEvent.class, int.class,
                    handleInterceptKeyBeforeQueueing);
        } catch (Throwable e) {
            Log.d(TAG, e.getMessage());
        }
        bindService();
    }

    private static IClickIPC sClickIPC = null;
    private static void bindService() {
        try {
            Application app = AndroidAppHelper.currentApplication();
            if (app == null) {
                return;
            }
            if (sClickIPC != null) {
                return;
            }
            Intent intent = new Intent("xyz.imxqd.clickclick.xposed.ipc");
            intent.setPackage("xyz.imxqd.clickclick.xposed");
            app.bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d(TAG, "onServiceConnected");
                    try {
                        sClickIPC = IClickIPC.Stub.asInterface(service);
                        sClickIPC.hello("xposed hello");
                    } catch (RemoteException e) {
                        Log.d(TAG, e.getMessage());
                        sClickIPC = null;
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG, "onServiceDisconnected");
                    sClickIPC = null;
                }
            }, Context.BIND_AUTO_CREATE);
        } catch (Throwable e) {
            Log.d(TAG, e.getMessage());
        }

    }

    private static XC_MethodHook handleInterceptKeyBeforeQueueing = new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            bindService();
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            final KeyEvent event = (KeyEvent) param.args[0];
            Log.d(TAG, "afterHookedMethod -----> " + event);
            boolean inject = false;
            if (sClickIPC != null) {
                inject = sClickIPC.onKeyEvent(event);
            }
            if (inject) {
                param.setResult(0);
            }

        }
    };

}
