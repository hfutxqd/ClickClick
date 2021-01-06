package xyz.imxqd.clickclick.utils;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;

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

    private static final String TAG = "NotificationAccess";

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
                    sFieldReflectionAction_methodName.setAccessible(true);
                    String name = (String) sFieldReflectionAction_methodName.get(a);
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
        sFieldReflectionAction_value.setAccessible(true);
        try {
            return sFieldReflectionAction_value.get(rflectionAction);
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
                    sFieldReflectionAction_methodName.setAccessible(true);
                    sFieldAction_viewId.setAccessible(true);
                    String name = (String) sFieldReflectionAction_methodName.get(a);
                    int id = sFieldAction_viewId.getInt(a);
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
        if (intent == null && n.tickerView != null) {
            intent = NotificationAccessUtil.getPendingIntentByViewId(n.tickerView, viewId);
        }
        if (intent == null && Build.VERSION.SDK_INT >= 21 && n.headsUpContentView != null) {
            intent = NotificationAccessUtil.getPendingIntentByViewId(n.headsUpContentView, viewId);
        }
        if (intent == null) {
            Log.e(TAG, "can not found intent by id, use content intent instead.");
            intent = n.contentIntent;
        }
        return intent;
    }

    public static PendingIntent getPendingIntentByViewId(RemoteViews rvs,  int viewId) throws IllegalAccessException {
        ArrayList list = getRemoteViewsActions(rvs);
        if (list == null) {
            return null;
        }
        for (Object a : list) {
            if (sClassSetOnClickPendingIntent != null && sClassSetOnClickPendingIntent.isInstance(a)) {
                sFieldAction_viewId.setAccessible(true);
                sFieldSetOnClickPendingIntent_pendingIntent.setAccessible(true);
                int id = sFieldAction_viewId.getInt(a);
                if (id == viewId) {
                    Object i = sFieldSetOnClickPendingIntent_pendingIntent.get(a);
                    if (i instanceof  PendingIntent) {
                        return (PendingIntent) i;
                    }
                }
            } else if (sClassSetOnClickResponse != null && sClassSetOnClickResponse.isInstance(a)) {
                sFieldAction_viewId.setAccessible(true);
                sFieldSetOnClickResponse_mResponse.setAccessible(true);
                sFieldRemoteResponse_mPendingIntent.setAccessible(true);

                int id = sFieldAction_viewId.getInt(a);
                Object mResponse = sFieldSetOnClickResponse_mResponse.get(a);
                if (id == viewId) {
                    Object i = sFieldRemoteResponse_mPendingIntent.get(mResponse);
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
                    sFieldSetOnClickPendingIntent_pendingIntent.setAccessible(true);
                    PendingIntent intent = null;
                    intent = (PendingIntent) sFieldSetOnClickPendingIntent_pendingIntent.get(action);
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
            sFieldRemoteViews_mActions.setAccessible(true);
            return (ArrayList) sFieldRemoteViews_mActions.get(rvs);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return new ArrayList();
    }

    static {
        try {

//            Field[] fields = RemoteViews.class.getDeclaredFields();
//            for (Field field : fields) {
//                if(!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
//                    System.out.println(field.toString());
//                }
//            }
            sFieldRemoteViews_mActions = RemoteViews.class.getDeclaredField("mActions");
            sFieldRemoteViews_mApplication = RemoteViews.class.getDeclaredField("mApplication");
            if (Build.VERSION.SDK_INT >= 21) {
                Class<?> classIntArray = Class.forName("android.util.IntArray");
                sFieldIntArray_mValues = classIntArray.getDeclaredField("mValues");
                sClassIntArray = classIntArray;
            }
            if (Build.VERSION.SDK_INT <= 28) {
                Class<?> classSetOnClickPendingIntent = Class.forName(RemoteViews.class.getName() + "$SetOnClickPendingIntent");
                sFieldSetOnClickPendingIntent_pendingIntent = classSetOnClickPendingIntent.getDeclaredField("pendingIntent");
                sClassSetOnClickPendingIntent = classSetOnClickPendingIntent;

            } else {
                Class<?> classSetOnClickResponse = Class.forName(RemoteViews.class.getName() + "$SetOnClickResponse");
                sFieldSetOnClickResponse_mResponse = classSetOnClickResponse.getDeclaredField("mResponse");
                sClassSetOnClickResponse = classSetOnClickResponse;
                Class<?> classRemoteResponse = Class.forName(RemoteViews.class.getName() + "$RemoteResponse");
                sFieldRemoteResponse_mPendingIntent = classRemoteResponse.getDeclaredField("mPendingIntent");
                sFieldRemoteResponse_mViewIds = classRemoteResponse.getDeclaredField("mViewIds");
                sClassRemoteResponse = classRemoteResponse;
            }
            Class<?> classReflectionAction = Class.forName(RemoteViews.class.getName() + "$ReflectionAction");
            sFieldReflectionAction_methodName = classReflectionAction.getDeclaredField("methodName");
            sFieldReflectionAction_value = classReflectionAction.getDeclaredField("value");
            sClassReflectionAction = classReflectionAction;

            Class<?> classAction = Class.forName(RemoteViews.class.getName() + "$Action");
            sFieldAction_viewId = classAction.getDeclaredField("viewId");

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    // android 9  and below(28), child of Action
    private static Class<?>  sClassSetOnClickPendingIntent;
    // PendingIntent
    private static Field sFieldSetOnClickPendingIntent_pendingIntent;


    // android 10 and above(29)
    private static Class<?> sClassRemoteResponse;
    // type PendingIntent
    private static Field sFieldRemoteResponse_mPendingIntent;
    // type IntArray
    private static Field sFieldRemoteResponse_mViewIds;
    // android 10 and above(29), child of Action
    private static Class<?> sClassSetOnClickResponse;
    // type RemoteResponse
    private static Field sFieldSetOnClickResponse_mResponse;




    private static Class<?> sClassIntArray;
    // type int[]
    private static Field sFieldIntArray_mValues;


    // all, child of Action
    private static Class<?>  sClassReflectionAction;


    // type Action
    private static Field sFieldRemoteViews_mActions;
    // type ApplicationInfo
    private static Field sFieldRemoteViews_mApplication;
    // type String
    private static Field sFieldReflectionAction_methodName;
    // type int
    private static Field sFieldAction_viewId;
    // type Object
    private static Field sFieldReflectionAction_value;

}
