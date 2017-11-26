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

import java.lang.reflect.Field;
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

    public static List<Notification.Action> getAllActions(Notification n) {
        try {
            if (n.actions != null) {
                return Arrays.asList(n.actions);
            }
            Field field = RemoteViews.class
                    .getDeclaredField("mActions");
            field.setAccessible(true);
            ArrayList<Object> actions =
                    (ArrayList<Object>) field.get(n.contentView);

            //todo 解析RemoteViews里的Action
            actions.size();
            return null;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

}
