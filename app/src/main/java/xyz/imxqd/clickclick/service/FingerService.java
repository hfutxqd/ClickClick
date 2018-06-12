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

    private FingerprintManager fm;
    private CancellationSignal signal;
    private Handler handler;
    private static boolean isRunning = false;

    private boolean isOpening = false;

    public static void init() {
        KeyMappingEvent event = KeyMappingEvent.getFingerPrintEvent();
        if (isRunning() && event == null) {
            stopService();
        } else if (event != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent intent = new Intent(App.get(), FingerService.class);
                App.get().startService(intent);
            }
        }
    }

    private static void stopService() {
        if (FingerService.isRunning()) {
            Intent intent = new Intent(App.get(), FingerService.class);
            App.get().stopService(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        handler.sendEmptyMessageDelayed(0, CHECK_INTERVAL);
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            stopSelf();
            return;
        }
        isRunning = true;
        initHandler();
        initManager();
        start();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initHandler() {
        Log.d(TAG, "initHandler");
        handler = new Handler(msg -> {
            switch (msg.what) {
                case 0:
                    restart();
                    handler.sendEmptyMessageDelayed(0, CHECK_INTERVAL);
                    return true;
                case 1:
                    initManager();
                    signal = null;
                    isOpening = false;
                    restart();
                    return true;
            }
            return false;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void reconnect() {
        Log.d(TAG, "reconnect");
        if (isOpening) {
            return;
        }
        isOpening = true;
        signal = new CancellationSignal();
        if (PermissionChecker.checkCallingOrSelfPermission(App.get(), Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            stop();
            stopSelf();
            return;
        }
        fm.authenticate(null, signal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                Log.d(TAG, "onAuthenticationError, " + errString + " -> " + errorCode);
                super.onAuthenticationError(errorCode, errString);
                isOpening = false;
                if (errorCode == 5) {
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
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
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "onAuthenticationSucceeded");
                isOpening = false;
            }

            @Override
            public void onAuthenticationFailed() {
                Log.d(TAG, "onAuthenticationFailed");
                super.onAuthenticationFailed();
                isOpening = false;

            }
        }, null);
        AppEventManager.getInstance().attachToFingerService(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initManager() {
        if (fm == null) {
            Log.d(TAG, "initManager");
            fm = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void restart() {
        if (!isOpening) {
            Log.d(TAG, "restart");
            stop();
            start();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void start() {
        reconnect();
    }

    private void stop() {
        Log.d(TAG, "stop");
        if (signal != null) {
            signal.cancel();
            signal = null;
        }
        isOpening = false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        stop();
        handler.removeCallbacksAndMessages(null);
        isRunning = false;
        AppEventManager.getInstance().detachFromFingerService();
    }

    public static boolean isRunning() {
        return isRunning;
    }
}
