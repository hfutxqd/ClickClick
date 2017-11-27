package xyz.imxqd.mediacontroller.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.ArraySet;
import android.widget.RemoteViews;

import com.orhanobut.logger.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static void openNotificationAccess(Context context) {
        context.startActivity(new Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS));
    }


    // API 24以上可用
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

    // API 19以上可用
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
            PendingIntent intent = null;
            try {
                intent = (PendingIntent) sFieldActionPendingIntent.get(action);
            } catch (Throwable e) {
                continue;
            }
            intents.add(intent);
        }
        return intents;
    }

    private static ArrayList getRemoteViewsActions(RemoteViews rvs) {
        try {
            return (ArrayList) sFieldmActions.get(rvs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    static {
        try {

            sMethodGetIntent = PendingIntent.class.getMethod("getIntent");

            Field field = RemoteViews.class.getDeclaredField("mActions");
            field.setAccessible(true);
            sFieldmActions = field;
            Class[] classes = RemoteViews.class.getDeclaredClasses();
            Class SetOnClickPendingIntent = null;
            for (Class c : classes) {
                if (c.getName().contains("SetOnClickPendingIntent")) {
                    SetOnClickPendingIntent = c;
                }
            }
            sClassSetOnClickPendingIntent = SetOnClickPendingIntent;
            Field field2 = SetOnClickPendingIntent.getDeclaredField("pendingIntent");
            field2.setAccessible(true);
            sFieldActionPendingIntent = field2;
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static Class  sClassSetOnClickPendingIntent;
    private static Field sFieldmActions;

    private static Field sFieldActionPendingIntent;

    private static Method sMethodGetIntent;


}
