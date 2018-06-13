package xyz.imxqd.clickclick.utils;

import android.view.KeyEvent;

public class AppKey {
    public int keyCode;
    public int deviceId;

    public AppKey(int keyCode, int deviceId) {
        this.keyCode = keyCode;
        this.deviceId = deviceId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AppKey) {
            AppKey o = (AppKey) obj;
            return o.deviceId == deviceId && o.keyCode == keyCode;
        } else if (obj instanceof KeyEvent) {
            KeyEvent o = (KeyEvent) obj;
            return o.getDeviceId() == deviceId && o.getKeyCode() == keyCode;
        }
        return super.equals(obj);
    }

    public String getAppKeyId() {
        return String.format("%d:%d", deviceId, keyCode);
    }
}
