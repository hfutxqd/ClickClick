package xyz.imxqd.clickclick.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import android.view.InflateException;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.BuildConfig;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.log.LogUtils;

public class CopyRightActivity extends BaseActivity {

    @BindView(R.id.copy_webview)
    WebView mWebView;
    @BindView(R.id.copy_title)
    TextView mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_copy_right);
            ButterKnife.bind(this);
            mTitle.setText(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
            mWebView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("mailto:")) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
                            startActivity(intent);
                        } catch (Throwable t) {
                            App.get().showToast(R.string.no_email_app);
                        }
                    } else {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(intent);
                        } catch (Throwable t) {
                            App.get().showToast(R.string.no_browser_app);
                            LogUtils.e(t.getMessage());
                        }
                    }
                    view.reload();


                    return true;
                }
            });
            mWebView.loadUrl("file:///android_asset/copyright.html");
        } catch (InflateException e) {
            App.get().showToast(e.getMessage(), false);
            finish();
        } catch (Throwable t) {
            LogUtils.e(t.getMessage());
            finish();
        }
    }

    @OnClick(R.id.copy_ok)
    public void onOkayClick() {
        finish();
    }

    private int mClickCount = 0;
    @OnClick(R.id.copy_title)
    public void onClickTitle() {
        if (mClickCount == 0) {
            App.get().getHandler().postDelayed(() -> {
                App.get().getHandler().removeCallbacksAndMessages(null);
                mClickCount = 0;
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
