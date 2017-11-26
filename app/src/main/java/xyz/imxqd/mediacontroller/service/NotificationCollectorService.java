package xyz.imxqd.mediacontroller.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.HashSet;

import xyz.imxqd.mediacontroller.R;
import xyz.imxqd.mediacontroller.utils.Constants;
import xyz.imxqd.mediacontroller.utils.ResUtil;

/**
 * Created by imxqd on 2016/12/11.
 */

public class NotificationCollectorService extends NotificationListenerService {

    private static final String TAG = "imxqd";

    private HashMap<String, Notification> mLastNotifications = new HashMap<>();

    private HashSet<String> mPackageNames = new HashSet<>();

    private boolean isConnected = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isConnected) {
            return START_STICKY_COMPATIBILITY;
        }
        if (Constants.ACTION_CLOUD_MUSIC_LIKE.equals(intent.getAction())) {
            StatusBarNotification[] ns = getActiveNotifications();
            Logger.d(ns.length);
        }
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "onListenerConnected");
        isConnected = true;
        mPackageNames.add(ResUtil.getString(R.string.cloud_music_package));
        mPackageNames.add(ResUtil.getString(R.string.qq_music_package));
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "onListenerDisconnected");
        isConnected = false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Logger.d(sbn.getPackageName());
        if (mPackageNames.contains(sbn.getPackageName())) {
            mLastNotifications.put(sbn.getPackageName(), sbn.getNotification());
        }
    }


    private boolean doAction(PendingIntent intent) {
        try {
            intent.send();
            return true;
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
        return false;
    }
    // 网易云音乐
    // 0 喜爱
    // 2 下一曲
    // 3 暂停
    // 4 词
    // 6 暂停
    // 7 关闭


    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "onNotificationRemoved");
        if (mLastNotifications.get(sbn.getPackageName()) != null
                && mLastNotifications.get(sbn.getPackageName()) == sbn.getNotification()) {
            mLastNotifications.put(sbn.getPackageName(), null);
        }
    }

}
