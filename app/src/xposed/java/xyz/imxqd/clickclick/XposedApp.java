package xyz.imxqd.clickclick;

import android.os.RemoteException;

import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.service.ClickService;
import xyz.imxqd.clickclick.service.Command;
import xyz.imxqd.clickclick.service.IClickCallback;
import xyz.imxqd.clickclick.utils.XposedSettingsUtil;

public class XposedApp extends App {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void post(int what, Object data) {
        super.post(what, data);
        if (what == EVENT_WHAT_APP_SWITCH_CHANGED) {
            if (XposedSettingsUtil.isWorkScreenOff()) {
                wakeLock();
            } else {
                releaseWakeLock();
            }
        }
    }

    public void wakeLock() {
        IClickCallback callback = ClickService.getXposed();
        if (callback != null) {
            try {
                callback.send(new Command(Command.WHAT_WAKELOCK, null));
            } catch (RemoteException e) {
                LogUtils.e(e.getMessage());
            }
        }
    }

    public void releaseWakeLock() {
        IClickCallback callback = ClickService.getXposed();
        if (callback != null) {
            try {
                callback.send(new Command(Command.WHAT_WAKELOCK_RELEASE, null));
            } catch (RemoteException e) {
                LogUtils.e(e.getMessage());
            }
        }
    }
}
