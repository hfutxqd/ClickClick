package xyz.imxqd.clickclick.ui;

import android.app.Activity;
import android.os.Bundle;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;

public class NoDisplayActivity extends Activity {

    public static final String ARG_FUNC_ID = "func_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long funcId = getIntent().getLongExtra(ARG_FUNC_ID, -1);
        if (funcId >= 0) {
            IFunction function = FunctionFactory.getFuncById(funcId);
            if (function != null) {
                function.exec();
            } else {
                App.get().showToast(R.string.func_not_found);
            }
        }
        finish();
    }
}
