package xyz.imxqd.clickclick.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.text.TextUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.DefinedFunction;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;

public class AddFunctionActivity extends BaseActivity {

    @BindView(R.id.add_func_name)
    TextInputEditText etName;
    @BindView(R.id.add_func_description)
    TextInputEditText etDescription;
    @BindView(R.id.add_func_code)
    TextInputEditText etCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_func);
        ButterKnife.bind(this);
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
            function.save();
            setResult(RESULT_OK);
            finish();
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
