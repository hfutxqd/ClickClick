package xyz.imxqd.clickclick.service;

import android.accessibilityservice.AccessibilityService;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public class KeyEventService extends AccessibilityService {

    private Toast mToast;

    @Override
    protected void onServiceConnected() {

        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_service_success));
        }
        AppEventManager.getInstance().attachToAccessibilityService(this);
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
        Logger.d("onInterrupt");
        if (SettingsUtil.displayDebug()) {
            showToast(getString(R.string.open_service_interrupt));
        }
        AppEventManager.getInstance().dettachFromAccessibilityService();
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        return AppEventManager.getInstance().shouldInterrupt(event);
    }


    private void showToast(String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG);
        mToast.show();
    }
}
