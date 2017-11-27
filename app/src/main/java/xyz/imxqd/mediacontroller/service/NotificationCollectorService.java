package xyz.imxqd.mediacontroller.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.HashSet;

import xyz.imxqd.mediacontroller.R;
import xyz.imxqd.mediacontroller.utils.Constants;
import xyz.imxqd.mediacontroller.utils.ResUtil;
import xyz.imxqd.mediacontroller.utils.SettingsUtil;

/**
 * Created by imxqd on 2016/12/11.
 */

public class NotificationCollectorService extends NotificationListenerService {

    private static final String TAG = "imxqd";
    private Toast mToast;
    private HashSet<String> mPackageNames = new HashSet<>();

    private boolean isConnected = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isConnected) {
            return START_STICKY_COMPATIBILITY;
        }
        if (Constants.ACTION_CLOUD_MUSIC_LIKE.equals(intent.getAction())) {
            Notification n = getNotificationByPackage(getString(R.string.cloud_music_package));
            Logger.d(n);
        }
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onListenerConnected() {
        Log.d(TAG, "onListenerConnected");
        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_notification_service_success));
        }
        isConnected = true;
        mPackageNames.add(ResUtil.getString(R.string.cloud_music_package));
        mPackageNames.add(ResUtil.getString(R.string.qq_music_package));
    }

    @Override
    public void onListenerDisconnected() {
        Log.d(TAG, "onListenerDisconnected");
        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_notification_service_disconnected));
        }
        isConnected = false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Logger.d(sbn.getPackageName());
    }

    private Notification getNotificationByPackage(String packageName) {
        StatusBarNotification[] ns = getActiveNotifications();
        if (ns == null) {
            return null;
        }
        for (StatusBarNotification sn : ns) {
            if (sn.getPackageName().equals(packageName)) {
                return sn.getNotification();
            }
        }
        return null;
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
    }

    private void showToast(String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        mToast.show();
    }

}
