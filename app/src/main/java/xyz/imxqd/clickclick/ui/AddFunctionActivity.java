package xyz.imxqd.clickclick.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.DefinedFunction;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;
import xyz.imxqd.clickclick.model.web.RemoteFunction;
import xyz.imxqd.clickclick.utils.AlertUtil;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public class AddFunctionActivity extends BaseActivity {

    private static final String PATH_ADD = "add";
    private static final String PATH_SAVE = "save";
    private static final String PATH_RUN = "run";

    private static final String ARGS_EDITABLE = "key_add_func_editable";
    private static final String ARGS_REMOTE_FUNCATION = "key_add_func_remote";

    @BindView(R.id.add_func_name)
    AppCompatEditText etName;
    @BindView(R.id.add_func_description)
    AppCompatEditText etDescription;
    @BindView(R.id.add_func_code)
    AppCompatEditText etCode;

    private boolean isResumed = false;

    public void handleIntent(Intent intent) {
        if (intent.getData() != null) {
            Uri data = intent.getData();
            try {
                if (PATH_ADD.equals(data.getHost())) {
                    String bodyEncode = data.getEncodedPath().substring(1);
                    String bodyDecode = Uri.decode(bodyEncode);
                    Gson gson = new Gson();
                    DefinedFunction function = gson.fromJson(bodyDecode, DefinedFunction.class);
                    etName.setText(function.name);
                    etDescription.setText(function.description);
                    etCode.setText(function.body);
                } else if (PATH_SAVE.equals(data.getHost())) {
                    String bodyEncode = data.getEncodedPath().substring(1);
                    String bodyDecode = Uri.decode(bodyEncode);
                    Gson gson = new Gson();
                    DefinedFunction function = gson.fromJson(bodyDecode, DefinedFunction.class);
                    try {
                        function.save();
                        showToast(getString(R.string.save_successed));
                        App.get().post(App.EVENT_WHAT_REFRESH_UI, null);
                    }catch (SQLiteConstraintException e) {
                        showToast(getString(R.string.save_failed));
                    }
                    if (!isResumed) {
                        finish();
                    }
                } else if (PATH_RUN.equals(data.getHost())) {
                    String bodyEncode = data.getEncodedPath().substring(1);
                    String bodyDecode = Uri.decode(bodyEncode);
                    Gson gson = new Gson();
                    DefinedFunction function = gson.fromJson(bodyDecode, DefinedFunction.class);
                    try {
                        IFunction func = FunctionFactory.getFunc(function.body);
                        if (func != null && func.exec()) {
                            showToast(getString(R.string.run_successed));
                        } else if (func != null){
                            showToast(getString(R.string.run_failed));
                            App.get().showToast(func.getErrorInfo().getMessage(), true, true);
                        } else {
                            showToast(getString(R.string.run_failed));
                        }

                    }catch (SQLiteConstraintException e) {
                        showToast(getString(R.string.run_failed));
                    }
                    if (!isResumed) {
                        finish();
                    }
                }
            } catch (Exception e) {
                showToast(getString(R.string.open_error));
            }
        } else {
            boolean editable = intent.getBooleanExtra(ARGS_EDITABLE, true);
            if (SettingsUtil.displayDebug()) {
                editable = true;
            }
            etName.setEnabled(editable);
            etCode.setEnabled(editable);
            etDescription.setEnabled(editable);
            RemoteFunction f = intent.getParcelableExtra(ARGS_REMOTE_FUNCATION);
            if (f == null) {
                return;
            }
            if (f.checkDependencies() || SettingsUtil.displayDebug()) {
                etName.setText(f.name);
                etDescription.setText(f.description);
                etCode.setText(f.body);
            } else {
                StringBuilder messages = new StringBuilder();
                List<String> msgs = f.getDependenciesMessages();
                for (String s : msgs) {
                    messages.append(s);
                    messages.append("\n");
                }
                AlertUtil.show(messages.toString());
                finish();
            }
        }
    }

    public static void start(RemoteFunction function, boolean editable, Context context) {
        Intent intent = new Intent(context, AddFunctionActivity.class);
        intent.putExtra(ARGS_EDITABLE, editable);
        intent.putExtra(ARGS_REMOTE_FUNCATION, function);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void startForResult(RemoteFunction function, boolean editable, Activity context, int requestCode) {
        Intent intent = new Intent(context, AddFunctionActivity.class);
        intent.putExtra(ARGS_EDITABLE, editable);
        intent.putExtra(ARGS_REMOTE_FUNCATION, function);
        context.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_func);
        ButterKnife.bind(this);
        if (getIntent() != null) {
            handleIntent(getIntent());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }

    @OnClick(R.id.add_func_cancel)
    public void onCancelClick() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @OnClick(R.id.add_func_add)
    public void onAddClick() {
        if (checkEmpty()) {
            String name = etName.getText().toString();
            String des = etDescription.getText().toString();
            String code = etCode.getText().toString();
            DefinedFunction function = new DefinedFunction();
            function.name = name;
            function.description = des;
            function.body = code;
            function.order = 0;
            try {
                function.save();
                App.get().post(App.EVENT_WHAT_REFRESH_UI, null);
                setResult(RESULT_OK);
                finish();
            } catch (Exception e) {
                showToast(getString(R.string.save_failed));
            }
        }
    }

    @OnClick(R.id.add_func_run)
    public void onRunClick() {
        if (TextUtils.isEmpty(etCode.getText())) {
            etCode.setError(getString(R.string.can_not_be_empty));
            return;
        }
        String data = etCode.getText().toString();
        IFunction function = FunctionFactory.getFunc(data);
        if (function != null && function.exec()) {
            showToast(getString(R.string.run_successed));
        } else {
            showToast(getString(R.string.run_failed));
        }
    }

    private boolean checkEmpty() {
        if (TextUtils.isEmpty(etName.getText())) {
            etName.setError(getString(R.string.can_not_be_empty));
            return false;
        }
        if (TextUtils.isEmpty(etDescription.getText())) {
            etDescription.setError(getString(R.string.can_not_be_empty));
            return false;
        }

        if (TextUtils.isEmpty(etCode.getText())) {
            etCode.setError(getString(R.string.can_not_be_empty));
            return false;
        }
        return true;
    }

}
