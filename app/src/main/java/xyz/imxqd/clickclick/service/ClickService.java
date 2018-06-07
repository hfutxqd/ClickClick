package xyz.imxqd.clickclick.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.AppEventManager;

public class ClickService extends Service {

    IClickIPC.Stub stub = new IClickIPC.Stub() {
        @Override
        public void hello(String str) throws RemoteException {
            LogUtils.d(str);
        }

        @Override
        public boolean onKeyEvent(KeyEvent event) throws RemoteException {
            boolean interrupt = AppEventManager.getInstance().shouldInterrupt(event);
            LogUtils.d("KeyCode: " + event.getKeyCode() + " Interrupt: " + interrupt);
            return interrupt;
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }
}
