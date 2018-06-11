package xyz.imxqd.clickclick.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.dao.KeyMappingEvent;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;
import xyz.imxqd.clickclick.model.AppEventManager;

public class FingerService extends Service {

    private static final String TAG = "FingerService";
    private static int CHECK_INTERVAL = 1000;

    private boolean isOpen = false;
    private FingerprintManager fm;
    private CancellationSignal signal;
    private Handler handler;
    private static boolean isRunning = false;

    public static void init() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(App.get(), FingerService.class);
            App.get().startService(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(() -> {
            while (true) {
                if (!isOpen) {
                    handler.sendEmptyMessage(0);
                }
                boolean c = false;
                if (CHECK_INTERVAL > 1000) {
                    c = true;
                }
                SystemClock.sleep(CHECK_INTERVAL);
                if (c) {
                    CHECK_INTERVAL = 1000;
                }
            }
        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            stopSelf();
            return;
        }
        isRunning = true;
        initHandler();
        initManager();
        reconnect();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initHandler() {
        handler = new Handler(msg -> {
            switch (msg.what) {
                case 0:
                    initManager();
                    reconnect();
                    return true;
            }
            return false;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void reconnect() {
        if (isOpen) {
            return;
        }
        isOpen = true;
        signal = new CancellationSignal();
        if (PermissionChecker.checkCallingOrSelfPermission(App.get(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        fm.authenticate(null, signal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errMsgId, CharSequence errString) {
                Log.d(TAG, "onAuthenticationError");
                super.onAuthenticationError(errMsgId, errString);
                CHECK_INTERVAL = 60000;
                stop();
                reconnect();
            }

            @Override
            public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
                super.onAuthenticationHelp(helpMsgId, helpString);
                Log.d(TAG, "onAuthenticationHelp : " + helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                KeyMappingEvent event = KeyMappingEvent.getFingerPrintEvent();
                if (event != null) {
                    IFunction f = FunctionFactory.getFuncById(event.funcId);
                    if (!f.exec()) {
                        App.get().showToast(f.getErrorInfo().getMessage(), false);
                    }
                }
                stop();
                reconnect();
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "onAuthenticationSucceeded");
            }

            @Override
            public void onAuthenticationFailed() {
                Log.d(TAG, "onAuthenticationFailed");
                stop();
                reconnect();
                super.onAuthenticationFailed();

            }
        }, null);
        AppEventManager.getInstance().attachToFingerService(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initManager() {
        if (fm == null) {
            fm = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        }
    }

    private void stop() {
        if (signal != null) {
            signal.cancel();
            signal = null;
        }
        isOpen = false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        isRunning = false;
        AppEventManager.getInstance().detachFromFingerService();
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
