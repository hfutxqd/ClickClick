package xyz.imxqd.clickclick.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.webkit.WebView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.BuildConfig;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public class CopyRightActivity extends AppCompatActivity {

    @BindView(R.id.copy_webview)
    WebView mWebView;
    @BindView(R.id.copy_title)
    TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copy_right);
        ButterKnife.bind(this);
        mTitle.setText(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        mWebView.loadUrl("file:///android_asset/copyright.html");
    }

    @OnClick(R.id.copy_ok)
    public void onOkayClick() {
        finish();
    }

    private int mClickCount = 0;
    @OnClick(R.id.copy_title)
    public void onClickTitle() {
        if (mClickCount == 0) {
            App.get().getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    App.get().getHandler().removeCallbacksAndMessages(null);
                    mClickCount = 0;
                }
            }, 1500);
        }
        mClickCount++;
        if (mClickCount == 7) {
            SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(App.get());
            shp.edit().putBoolean(getString(R.string.pref_key_app_debug), true).apply();
            App.get().showToast(R.string.settings_key_app_debug_title);
            App.get().initLogger();
            App.get().post(App.EVENT_WHAT_REFRESH_UI, null);
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }
}
