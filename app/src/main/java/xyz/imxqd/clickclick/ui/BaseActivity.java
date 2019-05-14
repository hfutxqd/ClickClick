package xyz.imxqd.clickclick.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;


import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.service.KeyEventService;
import xyz.imxqd.clickclick.log.LogUtils;

/**
 * Created by imxqd on 2017/11/25.
 */

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements App.AppEventCallback {

    private Toast mToast;

    protected void showToast(String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(this, str, Toast.LENGTH_LONG);
        mToast.show();
    }

    @Override
    public void onEvent(int what, Object data) {

    }
}
