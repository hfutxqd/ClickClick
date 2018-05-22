package xyz.imxqd.clickclick.func;

import android.media.AudioManager;
import android.view.KeyEvent;

import xyz.imxqd.clickclick.utils.KeyEventUtil;

public class MediaFunction extends AbstractFunction {
    public static final String PREFIX = "media";

    public MediaFunction(String funcData) {
        super(funcData);
    }

    @Override
    public void doFunction(String args) throws RuntimeException {

    }

    public void volumeUp(AudioManager manager) {
        manager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    public void volumeDown(AudioManager manager) {
        manager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }

    public void mute(AudioManager manager) {
        KeyEvent[] events = KeyEventUtil.makeKeyEventGroup(KeyEvent.KEYCODE_MUTE);
        manager.dispatchMediaKeyEvent(events[0]);
        manager.dispatchMediaKeyEvent(events[1]);
    }

    public void previous(AudioManager manager) {
        KeyEvent[] events = KeyEventUtil.makeKeyEventGroup(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
        manager.dispatchMediaKeyEvent(events[0]);
        manager.dispatchMediaKeyEvent(events[1]);
    }

    public void next(AudioManager manager) {
        KeyEvent[] events = KeyEventUtil.makeKeyEventGroup(KeyEvent.KEYCODE_MEDIA_NEXT);
        manager.dispatchMediaKeyEvent(events[0]);
        manager.dispatchMediaKeyEvent(events[1]);
    }

    public void playOrPause(AudioManager manager) {
        KeyEvent[] events = KeyEventUtil.makeKeyEventGroup(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        manager.dispatchMediaKeyEvent(events[0]);
        manager.dispatchMediaKeyEvent(events[1]);
    }
}
