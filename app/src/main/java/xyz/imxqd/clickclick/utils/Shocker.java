package xyz.imxqd.clickclick.utils;

import android.app.Service;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import java.util.Arrays;

import xyz.imxqd.clickclick.MyApp;
import xyz.imxqd.clickclick.execution.DeviceNotSupportException;
import xyz.imxqd.clickclick.execution.IgnoreException;
import xyz.imxqd.clickclick.log.LogUtils;

/**
 * Created by imxqd on 17-4-2.
 */

public class Shocker {

    private volatile static boolean isShocking = false;

    public static void shock(long[] ms) throws Exception {
        Vibrator vibrator = (Vibrator) MyApp.get().getSystemService(Service.VIBRATOR_SERVICE);
        if (vibrator == null || !vibrator.hasVibrator()) {
            throw new DeviceNotSupportException("no vibrator found");
        }
        if (isShocking) {
            LogUtils.i("ignore shock: it is shocking");
            throw new IgnoreException("ignore shock: it is shocking");
        }

        LogUtils.i(Arrays.toString(ms));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(ms, -1));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            AudioAttributes AUDIO_ATTRIBUTES = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MOVIE)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build();
            vibrator.vibrate(ms, -1, AUDIO_ATTRIBUTES);
        } else {
            vibrator.vibrate(ms, -1);
        }
        isShocking = true;
        long duration = 0;
        for (long d : ms) {
            duration += d;
        }
        final long dur = duration;
        new Thread(() -> {
            try {
                Thread.sleep(dur);
            } catch (Throwable t) {
            } finally {
                isShocking = false;
            }
        }).start();
    }
}