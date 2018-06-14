package xyz.imxqd.clickclick.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaActionSound;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.PermissionChecker;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.Toast;

import org.reactivestreams.Subscriber;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.internal.operators.flowable.FlowableFlatMap;
import io.reactivex.schedulers.Schedulers;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.utils.CapturePhotoUtils;
import xyz.imxqd.clickclick.utils.ScreenShotNotification;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenCaptureActivity extends Activity {

    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";

    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private static final int REQUEST_PERMISSION = 1;

    private int mScreenDensity;
    private int mScreenWidth;
    private int mScreenHeight;

    private int mResultCode;
    private Intent mResultData;


    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private ImageReader mImageReader;
    private MediaActionSound mCameraSound;

    private boolean isScreenShotDone = false;

    private Bitmap mScreenShotBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
        mCameraSound = new MediaActionSound();
        mCameraSound.load(MediaActionSound.SHUTTER_CLICK);
        mImageReader.setOnImageAvailableListener(reader -> {
            if (isScreenShotDone) {
                return;
            }
            LogUtils.d(reader.toString());
            Image image = null;
            image = reader.acquireLatestImage();
            if (image == null) {
                return;
            }
            isScreenShotDone = true;
            Observable.just(image)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Function<Image, ObservableSource<Bitmap>>) image1 -> {
                        Bitmap bitmap = null;
                        final Image.Plane[] planes = image1.getPlanes();
                        final ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * image1.getWidth();
                        bitmap = Bitmap.createBitmap(image1.getWidth() + rowPadding / pixelStride, image1.getHeight(), Bitmap.Config.ARGB_8888);
                        bitmap.copyPixelsFromBuffer(buffer);
                        Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, image1.getWidth(), image1.getHeight());
                        image1.close();
                        return Observable.just(bitmap2);
                    }).subscribe(bitmap -> {
                        if (PermissionChecker.checkCallingOrSelfPermission(App.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                            mScreenShotBitmap = bitmap;
                            saveScreenShot();
                            stopScreenCapture();
                            finish();
                        } else {
                            ActivityCompat.requestPermissions(ScreenCaptureActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                        }

            });


        }, App.get().getHandler());
        startScreenCapture();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                LogUtils.d("User cancelled");
                return;
            }
            LogUtils.d("Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;
            setUpMediaProjection();
            setUpVirtualDisplay();
        }
    }


    private void saveScreenShot() {
        mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
        String url = CapturePhotoUtils.insertImage(getContentResolver(), mScreenShotBitmap, "screenshot by ClickClick", "screenshot file by ClickClick");
        ScreenShotNotification.notify(App.get(), mScreenShotBitmap, Uri.parse(url), 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionChecker.checkCallingOrSelfPermission(App.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            saveScreenShot();
            finish();
        } else {
            Toast.makeText(this, R.string.no_permission_write, Toast.LENGTH_LONG).show();
        }
    }

    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    private void startScreenCapture() {
        if (mMediaProjection != null) {
            setUpVirtualDisplay();
        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection();
            setUpVirtualDisplay();
        } else {
            LogUtils.d("Requesting confirmation");
            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    private void setUpVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        tearDownMediaProjection();
    }
}
