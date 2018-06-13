package xyz.imxqd.clickclick.widget;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;


public class GestureView extends AppCompatTextView {

    private static final float SWIPE_MIN_DISTANCE = 50;
    private static final float TAP_MAX_TIME = 200;
    private LinePath mPath;
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
            mPath = new LinePath();
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
                if (getDistance(downX, downY, event.getX(), event.getY()) <= SWIPE_MIN_DISTANCE && System.currentTimeMillis() - downTime <= TAP_MAX_TIME) {
                    if (mCallback != null) {
                        mCallback.onTap(downX, downY);
                    }
                } else {
                    if (mCallback != null) {
                        mCallback.onGesture(mPath, System.currentTimeMillis() - downTime);
                    }
                }
                break;
        }
        postInvalidate();
        return true;
    }

    public static class LinePath extends Path {
        private List<PointF> mLinePoints = new ArrayList<>();

        @Override
        public void lineTo(float x, float y) {
            super.lineTo(x, y);
            if (isNewPoint(x, y)) {
                mLinePoints.add(new PointF(x, y));
            }
        }

        private boolean isNewPoint(float x, float y) {
            if (mLinePoints.size() == 0) {
                return true;
            } else {
                PointF p = mLinePoints.get(mLinePoints.size() - 1);
                return !(p.x == x && p.y == y);
            }
        }

        @Override
        public void moveTo(float x, float y) {
            super.moveTo(x, y);
            mLinePoints.clear();
            mLinePoints.add(new PointF(x, y));
        }

        @Override
        public void reset() {
            super.reset();
            mLinePoints.clear();
        }

        public List<PointF> getPoints() {
            return mLinePoints;
        }
    }

    public interface GestureCreatedCallback {
        void onTap(float x, float y);
        void onGesture(LinePath path, long duration);
    }
}
