package xyz.imxqd.clickclick.utils;

import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.RequiresApi;

public class GestureUtil {

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static GestureDescription makeTap(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        return new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 100, 50))
                .build();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static GestureDescription makeSwipe(float x, float y, float x2, float y2) {
        Path path = new Path();
        path.moveTo(x, y);
        path.lineTo(x2, y2);
        return new GestureDescription.Builder()
                .addStroke(new GestureDescription.StrokeDescription(path, 100, 200))
                .build();
    }
}
