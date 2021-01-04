package xyz.imxqd.clickclick.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.receiver.EventReceiver;

/**
 * Helper class for showing and canceling screen shot
 * notifications.
 * <p>
 * This class makes heavy use of the {@link NotificationCompat.Builder} helper
 * class to create notifications in a backward-compatible way.
 */
public class ScreenShotNotification {

    private static final String NOTIFICATION_TAG = "ScreenShot";

    private static final String NOTIFICATION_CHANNEL_ID = "click_screen_shot";

    public static void notify(final Context context,
                              final Bitmap bitmap, Uri uri, final int number) {
        final Resources res = context.getResources();

        final String title = res.getString(
                R.string.screen_shot_notification_title);
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(uri, "image/*");
        openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        shareIntent = Intent.createChooser(shareIntent, context.getText(R.string.send_to));

        Intent deleteIntent = new Intent(context, EventReceiver.class);
        deleteIntent.setAction(EventReceiver.EVENT_DELETE_PICTURE);
        deleteIntent.setData(uri);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)

                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_stat_screen_shot)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setLargeIcon(bitmap)
                .setTicker(title)
                .setNumber(number)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .addAction(0, context.getText(R.string.notification_aciton_share), PendingIntent.getActivity(
                        context,
                        0,
                        shareIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .addAction(0, context.getText(R.string.delete), PendingIntent.getBroadcast(
                        context,
                        0,
                        deleteIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setContentIntent(
                        PendingIntent.getActivity(
                        context,
                        0,
                        openIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                        .setBigContentTitle(title))

                .setAutoCancel(true);

        notify(context, builder.build());
    }

    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = App.get().getString(R.string.screen_shot_channel_name);
            String description = App.get().getString(R.string.screen_shot_channel_description);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            mChannel.setDescription(description);
            mChannel.enableLights(false);
            mChannel.setShowBadge(false);
            nm.createNotificationChannel(mChannel);
        }
        nm.notify(NOTIFICATION_TAG, 0, notification);
    }

    public static void cancel(final Context context) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }
        nm.cancel(NOTIFICATION_TAG, 0);
    }
}
