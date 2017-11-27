package xyz.imxqd.mediacontroller.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Parcelable;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.security.Key;
import java.util.ArrayList;

import xyz.imxqd.mediacontroller.App;
import xyz.imxqd.mediacontroller.R;
import xyz.imxqd.mediacontroller.utils.KeyEventHandler;
import xyz.imxqd.mediacontroller.utils.KeyEventUtil;
import xyz.imxqd.mediacontroller.utils.SettingsUtil;

public class KeyEventService extends AccessibilityService implements  KeyEventHandler.Callback{

    private AudioManager mAudioManager;
    private KeyEventHandler mKeyEventHandler;
    private Toast mToast;

    @Override
    protected void onServiceConnected() {

        showToast(getString(R.string.open_service_success));

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mKeyEventHandler = new KeyEventHandler();
        mKeyEventHandler.mLongClickKeyCodes = new ArrayList<>();
        mKeyEventHandler.mLongClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_DOWN);
        mKeyEventHandler.mLongClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_UP);
        mKeyEventHandler.mSingleClickKeyCodes = new ArrayList<>();
        mKeyEventHandler.mSingleClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_DOWN);
        mKeyEventHandler.mSingleClickKeyCodes.add(KeyEvent.KEYCODE_VOLUME_UP);
        mKeyEventHandler.setCallback(this);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        
    }

    @Override
    public void onInterrupt() {
        Logger.d("onInterrupt");
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (App.get().isServiceOn && SettingsUtil.isServiceOn()) {
            return mKeyEventHandler.inputKeyEvent(event);
        } else {
            return false;
        }
    }

    @Override
    public void onNormalKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            performGlobalAction(GLOBAL_ACTION_BACK);
        }
    }

    @Override
    public void onLongClick(KeyEvent event) {
        Logger.d(event);
        if (SettingsUtil.displayDebug()) {
            showToast("onLongClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            KeyEvent[] events = KeyEventUtil.makeKeyEventGroup(KeyEvent.KEYCODE_MEDIA_NEXT);
            mAudioManager.dispatchMediaKeyEvent(events[0]);
            mAudioManager.dispatchMediaKeyEvent(events[1]);
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            KeyEvent[] events = KeyEventUtil.makeKeyEventGroup(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            mAudioManager.dispatchMediaKeyEvent(events[0]);
            mAudioManager.dispatchMediaKeyEvent(events[1]);
        }
    }

    @Override
    public void onSingleClick(KeyEvent event) {
        Logger.d(event);
        if (SettingsUtil.displayDebug()) {
            showToast("onSingleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP) {
            mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
        }

    }

    @Override
    public void onDoubleClick(KeyEvent event) {
        Logger.d(event);
        if (SettingsUtil.displayDebug()) {
            showToast("onDoubleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }
    }

    @Override
    public void onTripleClick(KeyEvent event) {
        Logger.d(event);
        if (SettingsUtil.displayDebug()) {
            showToast("onTripleClick :" + KeyEventUtil.getKeyName(event.getKeyCode()));
        }

    }

    private void showToast(String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        mToast.show();
    }
}
