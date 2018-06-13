package xyz.imxqd.clickclick.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class GestureView extends AppCompatTextView {

    private static final float SWIPE_MIN_DISTANCE = 80;
    private Path mPath;
    private Paint mPaint;

    private boolean init = false;

    private GestureCreatedCallback mCallback;

    public GestureView(Context context) {
        super(context);
        init();
    }

    public GestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (!init) {
            mPath = new Path();
            mPaint = new Paint();
            mPaint.setStrokeWidth(20f);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            init = true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawPoint(downX, downY, mPaint);
        canvas.drawPath(mPath, mPaint);
    }

    public void setGestureCreatedCallback(GestureCreatedCallback callback) {
        mCallback = callback;
    }

    private float getDistance(float x, float y, float x2, float y2) {
        double _x = Math.abs(x - x2);
        double _y = Math.abs(y - y2);
        return (float) Math.sqrt(_x * _x + _y * _y);
    }

    float downX = -1000, downY = -1000;
    long downTime = 0;

    private void reset() {
        downX = -1000;
        downY = -1000;
        downTime = 0;
        mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                reset();
                mPath.moveTo(event.getX(), event.getY());
                downX = event.getX();
                downY = event.getY();
                downTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.lineTo(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                mPath.lineTo(event.getX(), event.getY());
                if (getDistance(downX, downY, event.getX(), event.getY()) <= SWIPE_MIN_DISTANCE) {
                    if (mCallback != null) {
                        mCallback.onTap(downX, downY);
                    }
                } else {
                    if (mCallback != null) {
                        mCallback.onGesture(mPath);
                    }
                }
                break;
        }
        postInvalidate();
        return true;
    }

    public interface GestureCreatedCallback {
        void onTap(float x, float y);
        void onGesture(Path path);
    }
}
