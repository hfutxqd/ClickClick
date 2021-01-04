package xyz.imxqd.clickclick.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import androidx.annotation.Nullable;
import android.view.KeyEvent;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.XposedSettingsUtil;

public class ClickService extends Service {

    private static ClickService sClickService;

    IClickCallback mCallback = null;


    IClickIPC.Stub stub = new IClickIPC.Stub() {
        @Override
        public void hello() throws RemoteException {
            LogUtils.d("Hello from xposed.");
        }

        @Override
        public boolean onKeyEvent(KeyEvent event) throws RemoteException {
            boolean interrupt = AppEventManager.getInstance().shouldInterrupt(event);
            LogUtils.d("KeyCode: " + event.getKeyCode() + " Interrupt: " + interrupt);
            return interrupt;
        }


        @Override
        public void registerCallback(IClickCallback callback) throws RemoteException {
            LogUtils.d("registerCallback");
            mCallback = callback;
            LogUtils.d("ping from clickclick");
            Result result = mCallback.send(new Command(Command.WHAT_HELLO, null));
            if (result != null && result.what == Command.WHAT_HELLO) {
                LogUtils.d("response from xposed");
            }
//            App.get().getHandler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (XposedSettingsUtil.isWorkScreenOff()) {
//                        wakeLock();
//                    } else {
//                        releaseWakeLock();
//                    }
//                }
//            }, 30000);
        }

        @Override
        public void unregisterCallback() throws RemoteException {
            LogUtils.d("unregisterCallback");
            mCallback = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sClickService = this;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
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

    public static IClickCallback getXposed() {
        if (sClickService != null) {
            return sClickService.mCallback;
        } else {
            return null;
        }
    }
}
