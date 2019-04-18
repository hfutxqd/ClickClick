package xyz.imxqd.clickclick.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.ArraySet;
import android.widget.RemoteViews;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import xyz.imxqd.clickclick.log.LogUtils;

/**
 * Created by imxqd on 2016/12/11.
 */

public class NotificationAccessUtil {

    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";
    private static final String ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS";


    public static boolean isEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean openNotificationAccess(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                context.startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            } else {
                context.startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
            }

            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @SuppressLint("PrivateApi")
    public static Intent getIntent(PendingIntent pendingIntent) {
        try {
            Method getIntent = PendingIntent.class.getDeclaredMethod("getIntent");
            return (Intent) getIntent.invoke(pendingIntent);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            LogUtils.e("error " + e);
            return null;
        }
    }

    // API 24以上可用
    @RequiresApi(Build.VERSION_CODES.N)
    public static ArraySet<PendingIntent> getAllPendingIntents(Notification n) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                ArraySet<PendingIntent> intents =
                        (ArraySet<PendingIntent>) Notification.class
                                .getField("allPendingIntents")
                                .get(n);

                return intents;
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static ArrayList<Object> getReflectionActions(RemoteViews rvs, String methodName) {
        ArrayList<Object> list = new ArrayList<>();
        if (sClassReflectionAction == null) {
            return list;
        }
        ArrayList actions = getRemoteViewsActions(rvs);
        for (Object a : actions) {
            try {
                if (sClassReflectionAction.isInstance(a)) {
                    sFieldReflectionActionMethodName.setAccessible(true);
                    String name = (String) sFieldReflectionActionMethodName.get(a);
                    if (methodName.equals(name)) {
                        list.add(a);
                    }
                }
            } catch (Exception e) {

            }
        }
        return list;
    }

    public static Object getReflectionActionValue(Object rflectionAction) {
        sFieldReflectionActionValue.setAccessible(true);
        try {
            return sFieldReflectionActionValue.get(rflectionAction);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList getReflectionActions(RemoteViews rvs, String methodName, int viewId) {
        ArrayList list = new ArrayList();
        if (sClassReflectionAction == null) {
            return list;
        }
        ArrayList actions = getRemoteViewsActions(rvs);
        for (Object a : actions) {
            try {
                if (sClassReflectionAction.isInstance(a)) {
                    sFieldReflectionActionMethodName.setAccessible(true);
                    sFieldReflectionActionViewId.setAccessible(true);
                    String name = (String) sFieldReflectionActionMethodName.get(a);
                    int id = sFieldReflectionActionViewId.getInt(a);
                    if (methodName.equals(name) && viewId == id) {
                        list.add(a);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static PendingIntent getPendingIntentByViewId(Notification n, int viewId) throws IllegalAccessException {
        PendingIntent intent = null;
        if (n.bigContentView != null) {
            intent = NotificationAccessUtil.getPendingIntentByViewId(n.bigContentView, viewId);
        }
        if (intent == null && n.contentView != null) {
            intent = NotificationAccessUtil.getPendingIntentByViewId(n.contentView, viewId);
        }
        return intent;
    }

    public static PendingIntent getPendingIntentByViewId(RemoteViews rvs,  int viewId) throws IllegalAccessException {
        if (sClassSetOnClickPendingIntent == null) {
            return null;
        }
        ArrayList list = getRemoteViewsActions(rvs);
        if (list == null) {
            return null;
        }
        for (Object a : list) {
            if (sClassSetOnClickPendingIntent.isInstance(a)) {
                sFieldReflectionActionViewId.setAccessible(true);
                sFieldSetOnClickPendingIntentPendingIntent.setAccessible(true);
                int id = sFieldReflectionActionViewId.getInt(a);
                if (id == viewId) {
                    Object i = sFieldSetOnClickPendingIntentPendingIntent.get(a);
                    if (i instanceof  PendingIntent) {
                        return (PendingIntent) i;
                    }
                }
            }
        }
        return null;
    }


    // API 19以上可用
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public static List<PendingIntent> getPendingIntents(Notification n) {
        List<PendingIntent> intents = new ArrayList<>();
        RemoteViews rvs;
        if (n.bigContentView != null) {
            rvs = n.bigContentView;
        } else if (n.contentView != null){
            rvs = n.contentView;
        } else {
            return intents;
        }
        ArrayList actions = getRemoteViewsActions(rvs);
        for (Object action : actions) {
            try {
                if (sClassSetOnClickPendingIntent.isInstance(action)) {
                    sFieldSetOnClickPendingIntentPendingIntent.setAccessible(true);
                    PendingIntent intent = null;
                    intent = (PendingIntent) sFieldSetOnClickPendingIntentPendingIntent.get(action);
                    intents.add(intent);
                }
            } catch (Throwable e) {
                continue;
            }
        }
        return intents;
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private static ArrayList getRemoteViewsActions(RemoteViews rvs) {
        try {
            sFieldRemoteViewsmActions.setAccessible(true);
            return (ArrayList) sFieldRemoteViewsmActions.get(rvs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    static {
        try {

            sMethodGetIntent = PendingIntent.class.getDeclaredMethod("getIntent");

            Field field = RemoteViews.class.getDeclaredField("mActions");
            field.setAccessible(true);
            sFieldRemoteViewsmActions = field;
            Class[] classes = RemoteViews.class.getDeclaredClasses();
            Class SetOnClickPendingIntent = null;
            for (Class c : classes) {
                if (c.getName().endsWith("$SetOnClickPendingIntent")) {
                    SetOnClickPendingIntent = c;
                }
                if (c.getName().endsWith("$ReflectionAction")) {
                    sClassReflectionAction = c;
                    sFieldReflectionActionMethodName = c.getDeclaredField("methodName");
                    sFieldReflectionActionValue = c.getDeclaredField("value");
                }
                if (c.getName().endsWith("$Action")) {
                    sFieldReflectionActionViewId = c.getDeclaredField("viewId");
                }
            }
            sClassSetOnClickPendingIntent = SetOnClickPendingIntent;
            sFieldSetOnClickPendingIntentPendingIntent = SetOnClickPendingIntent.getDeclaredField("pendingIntent");

            RemoteViews.class.getDeclaredClasses();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Class  sClassSetOnClickPendingIntent;
    private static Class  sClassReflectionAction;
    // Action
    private static Field sFieldRemoteViewsmActions;
    // String
    private static Field sFieldReflectionActionMethodName;
    // int
    private static Field sFieldReflectionActionViewId;
    // Object
    private static Field sFieldReflectionActionValue;
    // PendingIntent
    private static Field sFieldSetOnClickPendingIntentPendingIntent;

     // 通过反射调用隐藏接口，可能不支持所有设备，高版本不支持
    private static Method sMethodGetIntent;


}
