package xyz.imxqd.clickclick.model;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.flyme.systemui.smarttouch.ISmartTouchService;

import xyz.imxqd.clickclick.MyApp;
import xyz.imxqd.clickclick.log.LogUtils;

import static android.content.Context.BIND_AUTO_CREATE;

public class FlymeSmartTouchHelper {
    private static FlymeSmartTouchHelper sHelper;

    private ServiceConnection mSmartTouchServiceConnection;
    private ISmartTouchService mSmartTouchService;

    private Runnable mPendingAction;

    private boolean isShowing = true;

    private FlymeSmartTouchHelper() {
    }

    public static FlymeSmartTouchHelper get() {
        if (sHelper == null) {
            synchronized (FlymeSmartTouchHelper.class) {
                sHelper = new FlymeSmartTouchHelper();
                sHelper.init();
            }
        }
        return sHelper;
    }

    private void init() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.android.systemui", "com.flyme.systemui.smarttouch.SmartTouchService"));
        mSmartTouchServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mSmartTouchService = ISmartTouchService.Stub.asInterface(service);
                if (mPendingAction != null) {
                    mPendingAction.run();
                    mPendingAction = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mSmartTouchService = null;
            }
        };
        MyApp.get().bindService(intent, mSmartTouchServiceConnection, BIND_AUTO_CREATE);
    }


    private boolean hideInternal() throws RemoteException {
        if (mSmartTouchService != null) {
            mSmartTouchService.forceHideSmartTouch(true);
            isShowing = false;
            return true;
        }
        return false;
    }
    public boolean isShowing() {
        return isShowing;
    }

    public void hide() throws RemoteException {
        if (!hideInternal()) {
            init();
            mPendingAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        hideInternal();
                    } catch (RemoteException e) {
                        LogUtils.e(e.getMessage());
                    }
                }
            };
        }
    }

    private boolean showInternal() throws RemoteException {
        if (mSmartTouchService != null) {
            mSmartTouchService.forceHideSmartTouch(false);
            isShowing = true;
            return true;
        }
        return false;
    }

    public void show() throws RemoteException {
        if (!showInternal()) {
            init();
            mPendingAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        showInternal();
                    } catch (RemoteException e) {
                        LogUtils.e(e.getMessage());
                    }
                }
            };
        }
    }
}
