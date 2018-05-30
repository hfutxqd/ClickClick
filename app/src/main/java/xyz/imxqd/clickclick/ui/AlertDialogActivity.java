package xyz.imxqd.clickclick.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.web.RemoteFunction;
import xyz.imxqd.clickclick.utils.PackageUtil;

public class AlertDialogActivity extends AppCompatActivity {

    public static final String ARG_TYPE = "alert_type";

    public static final int TYPE_NOTIFY = 1;
    public static final String ARG_NOTIFY_PACKAGE = "notify_package";
    public static final String ARG_NOTIFY_VIEW_ID = "notify_view_id";

    private int type = 0;

    @BindView(R.id.alert_btn_1)
    TextView btn1;
    @BindView(R.id.alert_btn_2)
    TextView btn2;
    @BindView(R.id.alert_btn_3)
    TextView btn3;
    @BindView(R.id.alert_message)
    TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert_dialog);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        type = intent.getIntExtra(ARG_TYPE, 0);
        if (type == TYPE_NOTIFY) {
            try {
                btn2.setVisibility(View.GONE);
                String packageName = intent.getStringExtra(ARG_NOTIFY_PACKAGE);
                String viewId = intent.getStringExtra(ARG_NOTIFY_VIEW_ID);
                String appName = PackageUtil.getAppName(packageName);
                message.setText(Html.fromHtml(getString(R.string.alert_notify_message, appName, packageName, viewId)));
                btn3.setText(R.string.add_to_func);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.alert_btn_1)
    public void onBtn1Click() {
        if (type == TYPE_NOTIFY) {
            finish();
        }
    }

    @OnClick(R.id.alert_btn_2)
    public void onBtn2Click() {

    }

    @OnClick(R.id.alert_btn_3)
    public void onBtn3Click() {
        if (type == TYPE_NOTIFY) {
            RemoteFunction function = new RemoteFunction();
            function.name = "";
            function.description = "";
            function.body = "notification:" + getIntent().getStringExtra(ARG_NOTIFY_PACKAGE) + ":@id/" + getIntent().getStringExtra(ARG_NOTIFY_VIEW_ID);
            AddFunctionActivity.start(function, true, this);
        }
    }
}
