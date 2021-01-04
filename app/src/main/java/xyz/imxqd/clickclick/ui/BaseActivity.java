package xyz.imxqd.clickclick.ui;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Toast;


import xyz.imxqd.clickclick.App;

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
