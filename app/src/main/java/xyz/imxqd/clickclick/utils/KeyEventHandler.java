package xyz.imxqd.clickclick.utils;

import android.os.Handler;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;

/**
 * Created by imxqd on 2017/11/24.
 */

public class KeyEventHandler {

    private static int LONG_CLICK_TIME = 1000;
    private static int QUICK_CLICK_TIME = 1000;

    private Callback mCallback;
    private Stack<KeyEvent> mLastEvent;
    public final List<Integer> mLongClickKeyCodes = new ArrayList<>();
    public final List<String> mLongClickDevices = new ArrayList<>();

    public final List<Integer> mSingleClickKeyCodes = new ArrayList<>();
    public final List<String> mSingleClickDevices = new ArrayList<>();

    public final List<Integer> mDoubleClickKeyCodes = new ArrayList<>();
    public final List<String> mDoubleClickDevices = new ArrayList<>();

    public final List<Integer> mTripleClickKeyCodes = new ArrayList<>();
    public final List<String> mTripleClickDevices = new ArrayList<>();

    public final List<Integer> mLongClickIgdKeyCodes = new ArrayList<>();

    public final List<Integer> mSingleClickIgdKeyCodes = new ArrayList<>();

    public final List<Integer> mDoubleClickIgdKeyCodes = new ArrayList<>();

    public final List<Integer> mTripleClickIgdKeyCodes = new ArrayList<>();


    public List<Integer> mInputModeKeyCodes = new ArrayList<>();
    public final List<String> mInputModeDevices = new ArrayList<>();
    public List<Integer> mInputModeIgdKeyCodes = new ArrayList<>();

    private Handler mHandler;

    private int mKeyClickCount = 1;

    private boolean isKeyActionUp = false;

    public KeyEventHandler() {
        mLastEvent = new Stack<>();
        mHandler = new Handler();
    }

    public static void initClickTimes(int quickClick, int longClick) {
        LONG_CLICK_TIME = longClick;
        QUICK_CLICK_TIME = quickClick;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public boolean inputKeyEvent(KeyEvent event) {
        if (shouldOverride(event)) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                isKeyActionUp = true;
                doActon(event);
            } else if (event.getAction() == KeyEvent.ACTION_DOWN){
                isKeyActionUp = false;
                if (!mLastEvent.empty() && !isSameKey(mLastEvent.peek(), event)) {
                    // 如果本次按键与上次不同，取消定时器，立即执行动作
                    mHandler.removeCallbacksAndMessages(null);
                    doActionInNeed(true);
                }

                computeKeyEventCount(event);
                mLastEvent.push(event);
                if (supportDoubleClick(event)
                        || supportTripleClick(event)) {
                    mHandler.postDelayed(() -> doActionInNeed(false), QUICK_CLICK_TIME);
                }

                if (supportLongPress(event)) {
                    mHandler.postDelayed(() -> doActionInNeed(true), LONG_CLICK_TIME);
                }
            }
            return true;
        } else {
            doActionInNeed(true);
            return false;
        }
    }

    private void clearAction() {
        mLastEvent.clear();
        mKeyClickCount = 1;
        mHandler.removeCallbacksAndMessages(null);
    }

    private void doActon(KeyEvent event) {
        if(isSingleClick(event)) {
            mCallback.onSingleClick(event);
            clearAction();
        } else if (isDoubleClick(event)) {
            mCallback.onDoubleClick(event);
            clearAction();
        } else if (isTripleClick(event)) {
            mCallback.onTripleClick(event);
            clearAction();
        } else if (!supportDoubleClick(event) && !supportTripleClick(event)) {
            if (!mLastEvent.empty() && mLastEvent.peek().getAction() == KeyEvent.ACTION_DOWN) {
                mCallback.onNormalKeyEvent(event);
            }
            clearAction();
        }
    }

    private void doActionInNeed(boolean enforce) {
        if((!enforce && !isKeyActionUp) || mLastEvent.empty()) {
            return;
        }
        KeyEvent event = mLastEvent.peek();
        if (mKeyClickCount == 1 && supportLongPress(event) && !isKeyActionUp) {
            mCallback.onLongClick(event);
            clearAction();
            return;
        }
        if (mKeyClickCount == 1 && supportSingleClick(event)) {
            mCallback.onSingleClick(event);
            clearAction();
        } else if (mKeyClickCount == 2 && supportDoubleClick(event)) {
            mCallback.onDoubleClick(event);
            clearAction();
        } else {
            mCallback.onNormalKeyEvent(event);
            clearAction();
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

    private boolean findIt(KeyEvent keyEvent, List<Integer> igdKeyCodes, List<Integer> keyCodes, List<String> devices) {
        if (igdKeyCodes.contains(keyEvent.getKeyCode())) {
            return true;
        }
        int index = keyCodes.indexOf(keyEvent.getKeyCode());
        if (index == -1) {
            return false;
        } else if (keyEvent.getDevice() != null){
            return index == devices.indexOf(keyEvent.getDevice().getName());
        } else {
            return false;
        }
    }

    private boolean shouldOverride(KeyEvent keyEvent) {
        boolean shouldOverride = false;
        if (AppEventManager.getInstance().isInputMode()) {
            shouldOverride = mInputModeIgdKeyCodes.contains(keyEvent.getKeyCode());
        } else {
            shouldOverride = findIt(keyEvent, mSingleClickIgdKeyCodes, mSingleClickKeyCodes, mSingleClickDevices)
                    || findIt(keyEvent, mDoubleClickIgdKeyCodes, mDoubleClickKeyCodes, mDoubleClickDevices)
                    || findIt(keyEvent, mTripleClickIgdKeyCodes, mTripleClickKeyCodes, mTripleClickDevices)
                    || findIt(keyEvent, mLongClickIgdKeyCodes, mLongClickKeyCodes, mLongClickDevices);
        }
        LogUtils.d("shouldOverride=" + shouldOverride);
        return shouldOverride;
    }

    private boolean supportSingleClickOnly(KeyEvent keyEvent) {
        return shouldOverride(keyEvent) && !supportLongPress(keyEvent)
                && !supportDoubleClick(keyEvent) && !supportTripleClick(keyEvent);
    }

    private boolean supportSingleClick(KeyEvent keyEvent) {
        return findIt(keyEvent, mSingleClickIgdKeyCodes, mSingleClickKeyCodes, mSingleClickDevices);
    }

    private boolean supportLongPress(KeyEvent keyEvent) {
        return findIt(keyEvent, mLongClickIgdKeyCodes, mLongClickKeyCodes, mLongClickDevices);
    }

    private boolean supportDoubleClick(KeyEvent keyEvent) {
        return findIt(keyEvent, mDoubleClickIgdKeyCodes, mDoubleClickKeyCodes, mDoubleClickDevices);
    }

    private boolean supportTripleClick(KeyEvent keyEvent) {
        return findIt(keyEvent, mTripleClickIgdKeyCodes, mTripleClickKeyCodes, mTripleClickDevices);
    }

    private boolean isLongClick(KeyEvent event) {
        if (!supportLongPress(event)) {
            return false;
        }
        if (event.getEventTime() - event.getDownTime() >= LONG_CLICK_TIME) {
            return true;
        }
        return false;
    }

    private boolean isSingleClick(KeyEvent event) {
        if (mLastEvent.empty() || mLastEvent.peek().getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }
        if (!supportSingleClick(event)) {
            return false;
        }
        if (supportSingleClickOnly(event)) {
            return true;
        }
        if (!supportDoubleClick(event)
                && !supportTripleClick(event)) {
            return true;
        }
        return false;
    }

    private boolean isDoubleClick(KeyEvent event) {
        if (supportDoubleClick(event)
                && !supportTripleClick(event)
                && mKeyClickCount == 2) {
            return true;
        }
        return false;
    }

    private boolean isTripleClick(KeyEvent event) {
        if (!supportTripleClick(event)) {
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
