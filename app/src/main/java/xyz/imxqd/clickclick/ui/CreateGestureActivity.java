package xyz.imxqd.clickclick.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.web.RemoteFunction;

public class CreateGestureActivity extends AppCompatActivity {

    private static final float SWIPE_MIN_DISTANCE = 80;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView content = new TextView(this);
        content.setText(Html.fromHtml(getString(R.string.create_gesture_hint)));
        content.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.setGravity(Gravity.CENTER);
        setContentView(content);
    }

    public float getDistance(float x, float y, float x2, float y2) {
        double _x = Math.abs(x - x2);
        double _y = Math.abs(y - y2);
        return (float) Math.sqrt(_x * _x + _y * _y);
    }

    float lastX = -1, lastY = -1;
    long lastTime = 0;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastX = ev.getRawX();
                lastY = ev.getRawY();
                lastTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (getDistance(lastX, lastY, ev.getRawX(), ev.getRawY()) <= SWIPE_MIN_DISTANCE) {
                    RemoteFunction f = new RemoteFunction();
                    f.name = "";
                    f.description = "";
                    f.body = String.format(Locale.getDefault(), "internal:tap(%.0f,%.0f)", lastX, lastY);
                    AddFunctionActivity.start(f, true, this);
                    finish();
                } else {
                    RemoteFunction f = new RemoteFunction();
                    f.name = "";
                    f.description = "";
                    f.body = String.format(Locale.getDefault(), "internal:swipe(%.0f,%.0f,%.0f,%.0f,%d)",
                            lastX, lastY, ev.getRawX(), ev.getRawY(), System.currentTimeMillis() - lastTime);
                    AddFunctionActivity.start(f, true, this);
                    finish();
                }
                break;
            default:

        }
        return true;
    }
}
