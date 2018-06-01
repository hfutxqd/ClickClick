package xyz.imxqd.clickclick.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.BuildConfig;
import xyz.imxqd.clickclick.R;

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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }
}
