package xyz.imxqd.clickclick.utils;

import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import androidx.annotation.RequiresApi;

public class GestureUtil {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static GestureDescription makeTap(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        return new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, 50))
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static GestureDescription makeSwipe(float x, float y, float x2, float y2, int dur) {
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x2, y2);
        return new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, dur))
                .build();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public static GestureDescription makeGesture(Path path, int dur) {
        return new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 0, dur))
                .build();
    }
}
