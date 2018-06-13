package xyz.imxqd.clickclick.ui;

import android.content.Intent;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.util.Locale;

import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.web.RemoteFunction;
import xyz.imxqd.clickclick.utils.NavigationBarUtils;
import xyz.imxqd.clickclick.widget.GestureView;

public class CreateGestureActivity extends AppCompatActivity implements GestureView.GestureCreatedCallback {

    private static final int REQUEST_CODE = 2333;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int visibility = getWindow().getDecorView().getSystemUiVisibility();
        getWindow().getDecorView().setSystemUiVisibility(visibility | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        NavigationBarUtils.hideNavigationBar(this);

        GestureView content = new GestureView(this);
        content.setText(Html.fromHtml(getString(R.string.create_gesture_hint)));
        content.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        content.setGravity(Gravity.CENTER);
        setContentView(content);

        content.setGestureCreatedCallback(this);
    }

    @Override
    public void onTap(float x, float y) {
        RemoteFunction f = new RemoteFunction();
        f.name = "";
        f.description = "";
        f.body = String.format(Locale.getDefault(), "internal:tap(%.0f,%.0f)", x, y);
        AddFunctionActivity.startForResult(f, true, this, REQUEST_CODE);
    }

    @Override
    public void onGesture(Path path) {
        PathMeasure pm = new PathMeasure(path, false);
        // TODO: 2018/6/13 get all points
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            finish();
        }
    }
}
