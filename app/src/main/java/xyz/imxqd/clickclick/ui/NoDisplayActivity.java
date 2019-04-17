package xyz.imxqd.clickclick.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.BuildConfig;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.utils.LogcatUtil;

public class NoDisplayActivity extends Activity {

    public static final String ARG_FUNC_ID = "func_id";

    private static final int REQUEST_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() == null) {
            startActivity(new Intent(this, NaviActivity.class));
            finish();
            return;
        }
        if ((BuildConfig.APPLICATION_ID + ".run").equals(getIntent().getAction())) {
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
        } else if ("xyz.imxqd.clickclick.get_logcat".equals(getIntent().getAction())) {

            if (checkWritePermission()) {
                dumpLogs();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
            finish();
        } else {
            finish();
        }

    }

    private void dumpLogs() {
        String logs = LogcatUtil.getLogs();
        try {
            File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "ClickClick");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File logFile = new File(dir, "log_" + System.currentTimeMillis() + ".txt");
            FileOutputStream out = new FileOutputStream(logFile);
            out.write(logs.getBytes());
            out.close();
            Toast.makeText(this, R.string.dump_logs_success, Toast.LENGTH_LONG).show();
        } catch (Throwable t) {
            LogUtils.e(t.getMessage());
        }
    }

    private boolean checkWritePermission() {
        return PermissionChecker.checkCallingOrSelfPermission(App.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED;
    }

}
