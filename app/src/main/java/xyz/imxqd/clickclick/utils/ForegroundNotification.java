package xyz.imxqd.clickclick.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import xyz.imxqd.clickclick.MyApp;
import xyz.imxqd.clickclick.R;

public class ForegroundNotification {
    private static final String NOTIFICATION_TAG = "foreground_service";

    private static final String NOTIFICATION_CHANNEL_ID = "foreground_service";


    public static Notification get(Context context, String title) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && nm != null) {
            CharSequence name = MyApp.get().getString(R.string.foreground_service_channel_name);
            String description = MyApp.get().getString(R.string.foreground_service_description);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(false);
            mChannel.setShowBadge(false);
            nm.createNotificationChannel(mChannel);
        }
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_screen_shot)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTicker(title)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);
        return builder.build();
    }


}
