package xyz.imxqd.mediacontroller.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.mediacontroller.App;
import xyz.imxqd.mediacontroller.R;
import xyz.imxqd.mediacontroller.model.AppKeyEvent;
import xyz.imxqd.mediacontroller.utils.KeyEventUtil;

public class KeyEventActivity extends BaseActivity {

    public static final String ARG_KEY_EVENT = "key_event";

    private AppKeyEvent mKeyEvent = new AppKeyEvent();

    @BindView(R.id.key_tips)
    TextView mTvTips;
    @BindView(R.id.key_name)
    TextView mTvKeyName;
    @BindView(R.id.key_code)
    TextView mTvKeyCode;
    @BindView(R.id.key_device_name)
    TextView mTvDeviceName;
    @BindView(R.id.key_device_id)
    TextView mTvDeviceId;
    @BindView(R.id.key_btn_add)
    LinearLayout mBtnAdd;
    @BindView(R.id.key_info_layout)
    LinearLayout mInfoLayout;
    @BindView(R.id.key_ignore_device)
    CheckBox mCkIgnoreDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_event);
        setFinishOnTouchOutside(false);
        ButterKnife.bind(this);
        App.get().isServiceOn = false;
        mBtnAdd.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        App.get().isServiceOn = true;
        super.onDestroy();
    }

    @OnClick(R.id.key_btn_close)
    public void onCloseBtnClick() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.key_btn_add)
    public void onAddBtnClick() {
        Intent intent = new Intent();
        intent.putExtra(ARG_KEY_EVENT, mKeyEvent);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        mBtnAdd.setEnabled(true);
        mTvTips.setVisibility(View.GONE);
        mInfoLayout.setVisibility(View.VISIBLE);
        mKeyEvent.mDeviceId = event.getDeviceId();
        mKeyEvent.mKeyCode = event.getKeyCode();
        mKeyEvent.mDeviceName = event.getDevice().getName();
        mKeyEvent.ignoreDevice = mCkIgnoreDevice.isChecked();

        mTvKeyCode.setText(getString(R.string.key_code, event.getKeyCode()));
        mTvKeyName.setText(getString(R.string.key_name, KeyEventUtil.getKeyName(event.getKeyCode())));
        mTvDeviceName.setText(getString(R.string.key_device_name, event.getDevice().getName()));
        mTvDeviceId.setText(getString(R.string.key_device_id, event.getDeviceId()));
        return true;
    }
}
