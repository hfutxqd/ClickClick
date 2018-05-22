package xyz.imxqd.clickclick.utils;

import android.os.Handler;
import android.view.KeyEvent;

import java.util.List;
import java.util.Stack;

/**
 * Created by imxqd on 2017/11/24.
 */

public class KeyEventHandler {

    private static final int LONG_CLICK_TIME = 1000;
    private static final int QUICK_CLICK_TIME = 1000;

    private Callback mCallback;
    private Stack<KeyEvent> mLastEvent;
    public List<Integer> mLongClickKeyCodes;
    public List<Integer> mSingleClickKeyCodes;
    public List<Integer> mDoubleClickKeyCodes;
    public List<Integer> mTripleClickKeyCodes;

    private Handler mHandler;

    private int mKeyClickCount = 1;

    private boolean isKeyActionUp = false;

    public KeyEventHandler() {
        mLastEvent = new Stack<>();
        mHandler = new Handler();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public boolean inputKeyEvent(KeyEvent event) {
        if (shouldOverride(event.getKeyCode())) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                isKeyActionUp = true;
                doActon(event);
            } else {
                isKeyActionUp = false;
                if (!mLastEvent.empty() && !isSameKey(mLastEvent.peek(), event)) {
                    // 如果本次按键与上次不同，取消定时器，立即执行动作
                    mHandler.removeCallbacksAndMessages(null);
                    doActionInNeed(true);
                }

                computeKeyEventCount(event);
                mLastEvent.push(event);
                if (supportDoubleClick(event.getKeyCode())
                        || supportTripleClick(event.getKeyCode())) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doActionInNeed(false);
                        }
                    }, QUICK_CLICK_TIME);
                }
            }
            return true;
        } else {
            doActionInNeed(true);
            return false;
        }
    }

    private void doActon(KeyEvent event) {
        if (isLongClick(event)) {
            mCallback.onLongClick(event);
            mLastEvent.clear();
            mKeyClickCount = 1;
            mHandler.removeCallbacksAndMessages(null);
        } else if(isSingleClick(event)) {
            mCallback.onSingleClick(event);
            mLastEvent.clear();
            mKeyClickCount = 1;
            mHandler.removeCallbacksAndMessages(null);
        } else if (isDoubleClick(event)) {
            mCallback.onDoubleClick(event);
            mLastEvent.clear();
            mKeyClickCount = 1;
            mHandler.removeCallbacksAndMessages(null);
        } else if (isTripleClick(event)) {
            mCallback.onTripleClick(event);
            mLastEvent.clear();
            mKeyClickCount = 1;
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void doActionInNeed(boolean enforce) {
        if((!enforce && !isKeyActionUp) || mLastEvent.empty()) {
            return;
        }
        KeyEvent event = mLastEvent.peek();
        if (mKeyClickCount == 1 && supportSingleClick(event.getKeyCode())) {
            mCallback.onSingleClick(event);
            mLastEvent.clear();
            mKeyClickCount = 1;
            mHandler.removeCallbacksAndMessages(null);
        } else if (mKeyClickCount == 2 && supportDoubleClick(event.getKeyCode())) {
            mCallback.onDoubleClick(event);
            mLastEvent.clear();
            mKeyClickCount = 1;
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void computeKeyEventCount(KeyEvent event) {
        if (!mLastEvent.empty()) {
            KeyEvent last1 = mLastEvent.peek();
            if (isClickInTime(last1, event)) {
                mKeyClickCount = 2;
                KeyEvent tmp = mLastEvent.pop();
                if (!mLastEvent.empty()) {
                    KeyEvent last2 = mLastEvent.peek();
                    if (isClickInTime(last2, event)) {
                        mKeyClickCount = 3;
                    }
                }
                mLastEvent.push(tmp);
            } else {
                mKeyClickCount = 1;
            }
        }
    }

    private boolean isSameKey(KeyEvent a, KeyEvent b) {
        return a.getDeviceId() == b.getDeviceId()
                && a.getKeyCode() == b.getKeyCode();
    }

    private boolean isClickInTime(KeyEvent a, KeyEvent b) {
        return isSameKey(a, b) && b.getEventTime() - a.getEventTime() <= QUICK_CLICK_TIME;
    }

    private boolean shouldOverride(int keyCode) {
        boolean should = false;
        if (mSingleClickKeyCodes != null && mSingleClickKeyCodes.contains(keyCode)) {
            should = true;
        } else if (mDoubleClickKeyCodes != null && mDoubleClickKeyCodes.contains(keyCode)) {
            should = true;
        } else if (mTripleClickKeyCodes != null && mTripleClickKeyCodes.contains(keyCode)) {
            should = true;
        } else if (mLongClickKeyCodes != null && mLongClickKeyCodes.contains(keyCode)) {
            should = true;
        }
        return should;
    }

    private boolean supportSingleClickOnly(int keyCode) {
        return shouldOverride(keyCode) && !supportLongPress(keyCode)
                && !supportDoubleClick(keyCode) && !supportTripleClick(keyCode);
    }

    private boolean supportSingleClick(int keyCode) {
        if (mSingleClickKeyCodes == null) {
            return false;
        }
        return mSingleClickKeyCodes.contains(keyCode);
    }

    private boolean supportLongPress(int keyCode) {
        if (mLongClickKeyCodes == null) {
            return false;
        }
        return mLongClickKeyCodes.contains(keyCode);
    }

    private boolean supportDoubleClick(int keyCode) {
        if (mDoubleClickKeyCodes == null) {
            return false;
        }
        return mDoubleClickKeyCodes.contains(keyCode);
    }

    private boolean supportTripleClick(int keyCode) {
        if (mTripleClickKeyCodes == null) {
            return false;
        }
        return mTripleClickKeyCodes.contains(keyCode);
    }

    private boolean isLongClick(KeyEvent event) {
        if (!supportLongPress(event.getKeyCode())) {
            return false;
        }
        if (event.getEventTime() - event.getDownTime() >= LONG_CLICK_TIME) {
            return true;
        }
        return false;
    }

    private boolean isSingleClick(KeyEvent event) {
        if (!supportSingleClick(event.getKeyCode())) {
            return false;
        }
        if (supportSingleClickOnly(event.getKeyCode())) {
            return true;
        }
        if (!supportDoubleClick(event.getKeyCode())
                && !supportTripleClick(event.getKeyCode())) {
            return true;
        }
        return false;
    }

    private boolean isDoubleClick(KeyEvent event) {
        return false;
    }

    private boolean isTripleClick(KeyEvent event) {
        if (!supportTripleClick(event.getKeyCode())) {
            return false;
        }
        if (mKeyClickCount == 3) {
            return true;
        }
        return false;
    }


    public interface Callback {
        void onNormalKeyEvent(KeyEvent event);
        void onLongClick(KeyEvent event);
        void onSingleClick(KeyEvent event);
        void onDoubleClick(KeyEvent event);
        void onTripleClick(KeyEvent event);
    }

}
