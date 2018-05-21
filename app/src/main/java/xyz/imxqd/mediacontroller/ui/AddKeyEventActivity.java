package xyz.imxqd.mediacontroller.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.mediacontroller.App;
import xyz.imxqd.mediacontroller.R;
import xyz.imxqd.mediacontroller.dao.KeyMappingEvent;
import xyz.imxqd.mediacontroller.model.AppKeyEventType;
import xyz.imxqd.mediacontroller.utils.KeyEventUtil;

public class AddKeyEventActivity extends BaseActivity {

    public static final String ARG_KEY_EVENT = "key_event";

    private KeyMappingEvent mKeyEvent = new KeyMappingEvent();

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
    @BindView(R.id.key_event_type)
    Spinner mSpEventType;
    @BindView(R.id.key_function)
    Spinner mSpFunction;

    List<String> mEventTypeValues;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_event);
        setFinishOnTouchOutside(false);
        ButterKnife.bind(this);
        App.get().isServiceOn = false;
        mBtnAdd.setEnabled(false);
        List<String> spinnerArray =  new ArrayList<>();
        Collections.addAll(spinnerArray, getResources().getStringArray(R.array.event_type));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerArray);
        mSpEventType.setAdapter(adapter);

        mEventTypeValues = new ArrayList<>();
        Collections.addAll(mEventTypeValues, getResources().getStringArray(R.array.event_type_value));

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
        try {
            mKeyEvent.eventType = AppKeyEventType.valueOf(mEventTypeValues.get(mSpEventType.getSelectedItemPosition()));
            mKeyEvent.save();

            Intent intent = new Intent();
            intent.putExtra(ARG_KEY_EVENT, mKeyEvent);
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "error", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        mBtnAdd.setEnabled(true);
        mTvTips.setVisibility(View.GONE);
        mInfoLayout.setVisibility(View.VISIBLE);
        mKeyEvent.deviceId = event.getDeviceId();
        mKeyEvent.keyCode = event.getKeyCode();
        mKeyEvent.keyName = KeyEventUtil.getKeyName(mKeyEvent.keyCode);
        mKeyEvent.deviceName = event.getDevice().getName();
        mKeyEvent.ignoreDevice = mCkIgnoreDevice.isChecked();

        mTvKeyCode.setText(getString(R.string.key_code, event.getKeyCode()));
        mTvKeyName.setText(getString(R.string.key_name, KeyEventUtil.getKeyName(event.getKeyCode())));
        mTvDeviceName.setText(getString(R.string.key_device_name, event.getDevice().getName()));
        mTvDeviceId.setText(getString(R.string.key_device_id, event.getDeviceId()));
        return true;
    }
}
