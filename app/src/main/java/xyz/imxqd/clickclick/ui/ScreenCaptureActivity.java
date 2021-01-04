package xyz.imxqd.clickclick.ui;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
import android.os.IBinder;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import android.util.DisplayMetrics;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.service.ScreenShotService;
import xyz.imxqd.clickclick.utils.CapturePhotoUtils;
import xyz.imxqd.clickclick.utils.ScreenShotNotification;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenCaptureActivity extends Activity {

    public static final String ARGS_MODE = "capture_mode";

    public static final int MODE_DERECT = 1;
    public static final int MODE_SERVICE = 2;


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
    private Bitmap mScreenShotBitmap;

    private Image image = null;

    private ObservableEmitter<ImageReader> mEmitter;
    private Disposable mDisposable;

    private ScreenShotService.ScreenShotBinder mScreenShotService;

    private int mode = MODE_DERECT;

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mode = getIntent().getIntExtra(ARGS_MODE, MODE_DERECT);

        if (mode == MODE_DERECT) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
            mScreenDensity = metrics.densityDpi;
            mScreenWidth = metrics.widthPixels;
            mScreenHeight = metrics.heightPixels;
            mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 2);
            mCameraSound = new MediaActionSound();
            mCameraSound.load(MediaActionSound.SHUTTER_CLICK);
            mDisposable = Observable.create((ObservableOnSubscribe<ImageReader>) emitter -> mEmitter = emitter)
                    .debounce(400, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap((Function<ImageReader, ObservableSource<Bitmap>>) reader2 -> {
                        try {
                            LogUtils.d(reader2.toString());
                            image = reader2.acquireLatestImage();
                            if (image == null) {
                                if (mScreenShotBitmap != null && !mScreenShotBitmap.isRecycled()) {
                                    mScreenShotBitmap.recycle();
                                }
                                mScreenShotBitmap = null;
                                return Observable.just(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
                            }
                            Bitmap bitmap = null;
                            final Image.Plane[] planes = image.getPlanes();
                            final ByteBuffer buffer = planes[0].getBuffer();
                            if (buffer == null) {
                                if (mScreenShotBitmap != null && !mScreenShotBitmap.isRecycled()) {
                                    mScreenShotBitmap.recycle();
                                }
                                mScreenShotBitmap = null;
                                return Observable.just(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
                            }
                            int pixelStride = planes[0].getPixelStride();
                            int rowStride = planes[0].getRowStride();
                            int rowPadding = rowStride - pixelStride * image.getWidth();
                            bitmap = Bitmap.createBitmap(image.getWidth() + rowPadding / pixelStride, image.getHeight(), Bitmap.Config.ARGB_8888);
                            bitmap.copyPixelsFromBuffer(buffer);
                            Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
                            bitmap.recycle();
                            if (mScreenShotBitmap != null && !mScreenShotBitmap.isRecycled()) {
                                mScreenShotBitmap.recycle();
                            }
                            mScreenShotBitmap = bitmap2;
                            image.close();
                            image = null;
                            return Observable.just(bitmap2);
                        } catch (Throwable t) {
                            return Observable.just(mScreenShotBitmap);
                        }
                    }).subscribe(bitmap -> {
                        if (mScreenShotBitmap == null) {
                            return;
                        }
                        mImageReader.close();
                        if (PermissionChecker.checkCallingOrSelfPermission(App.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                            saveScreenShot();
                            finish();
                        } else {
                            ActivityCompat.requestPermissions(ScreenCaptureActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                        }

                    });


            mImageReader.setOnImageAvailableListener(reader -> {
                if (mEmitter != null) {
                    mEmitter.onNext(reader);
                }

            }, App.get().getHandler());
            startScreenCapture();
        } else if (mode == MODE_SERVICE) {
            Intent intent = new Intent(this, ScreenShotService.class);
            bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    mScreenShotService = (ScreenShotService.ScreenShotBinder) service;
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mScreenShotService = null;
                }
            }, BIND_AUTO_CREATE);
            startScreenCapture();
        } else {
            finish();
        }

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
            if (mode == MODE_DERECT) {
                setUpVirtualDisplay();
            }

        }
    }


    private void saveScreenShot() {
        if (mScreenShotBitmap == null) {
            return;
        }
        mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
        Uri uri = CapturePhotoUtils.insertImage(mScreenShotBitmap);
        if (uri != null) {
            ScreenShotNotification.notify(App.get(), mScreenShotBitmap, uri, 0);
        } else {
            LogUtils.e("can not save image to gallery!");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (PermissionChecker.checkCallingOrSelfPermission(App.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
            saveScreenShot();
            finish();
        } else {
            if (Looper.myLooper() != null) {
                Toast.makeText(this, R.string.no_permission_write, Toast.LENGTH_LONG).show();
            }
            finish();
        }
    }

    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
        if (mode == MODE_SERVICE && mScreenShotService != null) {
            mScreenShotService.setMediaProjectionManager(mMediaProjection);
        } else if (mode == MODE_SERVICE) {
            finish();
        }
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
            try {
                startActivityForResult(
                        mMediaProjectionManager.createScreenCaptureIntent(),
                        REQUEST_MEDIA_PROJECTION);
            } catch (Throwable t) {
                App.get().toastCenter(getString(R.string.run_failed));
                LogUtils.e(t.toString());
            }
        }
    }

    private void setUpVirtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay("ScreenCapture",
                mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mImageReader.getSurface(), null, null);
    }

    private void stopScreenCapture() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if (mode == MODE_DERECT) {
            tearDownMediaProjection();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScreenCapture();
        if (mEmitter != null) {
            mEmitter.onComplete();
            mEmitter = null;
        }
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
        if (image != null) {
            image.close();
            image = null;
        }
        if (mScreenShotBitmap != null && !mScreenShotBitmap.isRecycled()) {
            mScreenShotBitmap.recycle();
        }
        if (mImageReader != null) {
            mImageReader.close();
        }

    }
}
