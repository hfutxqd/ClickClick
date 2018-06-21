package xyz.imxqd.clickclick.xposed;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.view.KeyEvent;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XCallback;
import xyz.imxqd.clickclick.service.Command;
import xyz.imxqd.clickclick.service.IClickCallback;
import xyz.imxqd.clickclick.service.IClickIPC;
import xyz.imxqd.clickclick.service.Result;

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
            Class<?> classInputMonitor = null;
            classInputMonitor = findClass("com.android.server.wm.InputMonitor", packageParam.classLoader);
            if (classInputMonitor != null) {
                findAndHookMethod(classInputMonitor, "interceptKeyBeforeQueueing", KeyEvent.class, int.class, handleInterceptKeyBeforeQueueing);
                Log.d(TAG, "InputMonitor hooked.");
            }
            Class<?> classMediaSessionService = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                classMediaSessionService = findClass("com.android.server.media.MediaSessionService$SessionManagerImpl", packageParam.classLoader);
            }
            if (classMediaSessionService != null) {
                findAndHookMethod(classMediaSessionService, "dispatchMediaKeyEvent", KeyEvent.class, boolean.class, handleDispatchMediaKeyEvent);
                Log.d(TAG, "MediaSessionService hooked.");
            }

        } catch (Throwable e) {
            Log.d(TAG, e.getMessage());
        } finally {
            new Thread(() -> {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bindService();
            }).start();
        }
    }

    private static PowerManager.WakeLock sKeyEventWakeLock;

    private static IClickIPC sClickIPC = null;

    private static IClickCallback sClickCallback = new IClickCallback.Stub() {

        @Override
        public Result send(Command cmd) throws RemoteException {
            Log.d(TAG, "pid : " + getCallingPid());
            Log.d(TAG, "uid : " + getCallingUid());
            switch (cmd.what) {
                case Command.WHAT_HELLO:
                    Log.d(TAG, "Hello from ClickClick");
                    return new Result(Command.WHAT_HELLO, null);
                case Command.WHAT_WAKELOCK:
                    Log.d(TAG, "Acquire wakelock from ClickClick");
                    if (sKeyEventWakeLock != null && !sKeyEventWakeLock.isHeld()) {
                        sKeyEventWakeLock.acquire();
                        return new Result(Command.WHAT_RESULT_OK, null);
                    } else {
                        return new Result(Command.WHAT_RESULT_ERROR, null);
                    }

                case Command.WHAT_WAKELOCK_RELEASE:
                    Log.d(TAG, "Release wakelock from ClickClick");
                    if (sKeyEventWakeLock != null && sKeyEventWakeLock.isHeld()) {
                        sKeyEventWakeLock.release();
                        return new Result(Command.WHAT_RESULT_OK, null);
                    } else {
                        return new Result(Command.WHAT_RESULT_ERROR, null);
                    }

            }
            return null;
        }
    };

    private static boolean isConnecting = false;

    private static void bindService() {
        try {
            Application app = AndroidAppHelper.currentApplication();
            if (app == null) {
                return;
            }
            if (sClickIPC != null) {
                return;
            }
            if (isConnecting) {
                return;
            }
            PowerManager pm = (PowerManager)app.getSystemService(Context.POWER_SERVICE);
            if (sKeyEventWakeLock != null && sKeyEventWakeLock.isHeld()) {
                sKeyEventWakeLock.release();
            }
            sKeyEventWakeLock = pm.newWakeLock(1, "handleKeyEvent");
            Intent intent = new Intent("xyz.imxqd.clickclick.xposed.ipc");
            intent.setPackage("xyz.imxqd.clickclick.xposed");
            app.bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d(TAG, "onServiceConnected");
                    isConnecting = false;
                    sClickIPC = IClickIPC.Stub.asInterface(service);
                    try {
                        sClickIPC.hello();
                        sClickIPC.registerCallback(sClickCallback);
                    } catch (RemoteException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG, "onServiceDisconnected");
                    sClickIPC = null;
                    isConnecting = false;
                }
            }, Context.BIND_AUTO_CREATE);
            isConnecting = true;
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
            boolean inject = false;
            try {
                if (sClickIPC != null) {
                    inject = sClickIPC.onKeyEvent(event);
                }
            } catch (Exception e) {
                sClickIPC = null;
                bindService();
                inject = false;
            }
            if (inject) {
                param.setResult(0);
            }

        }
    };


    private static XC_MethodHook handleDispatchMediaKeyEvent = new XC_MethodHook(XCallback.PRIORITY_HIGHEST) {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            bindService();
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            final KeyEvent event = (KeyEvent) param.args[0];
            boolean inject = false;
            try {
                if (sClickIPC != null) {
                    inject = sClickIPC.onKeyEvent(event);
                }
            } catch (Exception e) {
                sClickIPC = null;
                bindService();
                inject = false;
            }
            if (inject) {
                param.setResult(0);
            }

        }
    };

}
