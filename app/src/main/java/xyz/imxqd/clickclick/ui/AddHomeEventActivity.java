package xyz.imxqd.clickclick.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.DefinedFunction;
import xyz.imxqd.clickclick.dao.KeyMappingEvent;
import xyz.imxqd.clickclick.model.AppKeyEventType;
import xyz.imxqd.clickclick.service.FingerService;
import xyz.imxqd.clickclick.ui.adapters.FunctionSpinnerAdapter;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.utils.KeyEventUtil;

public class AddHomeEventActivity extends AppCompatActivity {

    public static final String ARG_KEY_EVENT = "key_event";

    public static final String ARG_IS_ADD_FINGERPRINT = "is_fingerprint";

    private boolean isAddFringerprint = false;

    private KeyMappingEvent mKeyEvent = new KeyMappingEvent();

    @BindView(R.id.key_event_type)
    Spinner mSpEventType;
    @BindView(R.id.key_function)
    Spinner mSpFunction;

    List<String> mEventTypeValues;

    FunctionSpinnerAdapter mFuncAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_home_event);
        ButterKnife.bind(this);

        isAddFringerprint = getIntent().getBooleanExtra(ARG_IS_ADD_FINGERPRINT, false);

        List<String> spinnerArray =  new ArrayList<>();
        Collections.addAll(spinnerArray, getResources().getStringArray(R.array.home_event_type));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, spinnerArray);
        mSpEventType.setAdapter(adapter);

        if (isAddFringerprint) {
            mSpEventType.setVisibility(View.GONE);
        }

        mEventTypeValues = new ArrayList<>();
        Collections.addAll(mEventTypeValues, getResources().getStringArray(R.array.home_event_type_value));

        mFuncAdapter = new FunctionSpinnerAdapter();
        mSpFunction.setAdapter(mFuncAdapter);
    }

    @OnClick(R.id.key_btn_close)
    public void onCloseBtnClick() {
        setResult(RESULT_CANCELED);
        finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }

    @OnClick(R.id.key_btn_add)
    public void onAddBtnClick() {
        try {

            if (isAddFringerprint) {
                DefinedFunction function = (DefinedFunction) mSpFunction.getSelectedItem();
                mKeyEvent.keyCode = KeyEventUtil.KEY_CODE_FINGERPRINT;
                mKeyEvent.keyName = "Fingerprint";
                mKeyEvent.deviceId = -1;
                mKeyEvent.deviceName = "System";
                mKeyEvent.funcName = function.name;
                mKeyEvent.funcId = function.id;
                mKeyEvent.eventType = AppKeyEventType.SingleClick;
                mKeyEvent.save();
                FingerService.init();
            } else {
                DefinedFunction function = (DefinedFunction) mSpFunction.getSelectedItem();
                mKeyEvent.keyCode = KeyEvent.KEYCODE_HOME;
                mKeyEvent.keyName = "HOME";
                mKeyEvent.deviceId = -1;
                mKeyEvent.deviceName = "System";
                mKeyEvent.funcName = function.name;
                mKeyEvent.funcId = function.id;
                mKeyEvent.eventType = AppKeyEventType.valueOf(mEventTypeValues.get(mSpEventType.getSelectedItemPosition()));
                mKeyEvent.save();
                FingerService.init();
            }

            Intent intent = new Intent();
            intent.putExtra(ARG_KEY_EVENT, mKeyEvent);
            setResult(RESULT_OK, intent);
            finish();
        } catch (Exception e) {
            LogUtils.e(e.getMessage());
            Toast.makeText(this, R.string.add_key_event_failed, Toast.LENGTH_LONG).show();
        }
    }
}
