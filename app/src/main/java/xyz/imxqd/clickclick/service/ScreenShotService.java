package xyz.imxqd.clickclick.service;

import android.Manifest;
import android.app.IntentService;
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
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.ui.ScreenCaptureActivity;
import xyz.imxqd.clickclick.utils.CapturePhotoUtils;
import xyz.imxqd.clickclick.utils.ScreenShotNotification;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenShotService extends IntentService {

    private static final String ACTION_SCREEN_SHOT = "xyz.imxqd.clickclick.service.action.SCREEN_SHOT";

    private int mScreenDensity;
    private int mScreenWidth;
    private int mScreenHeight;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ImageReader mImageReader;
    private MediaActionSound mCameraSound;
    private Bitmap mScreenShotBitmap;

    private Image image = null;

    private ObservableEmitter<ImageReader> mEmitter;
    private Disposable mDisposable;

    private ScreenShotBinder mBinder = new ScreenShotBinder();


    public class ScreenShotBinder extends Binder {
        public void setMediaProjectionManager(MediaProjection projection) {
            mMediaProjection = projection;
            setUpVirtualDisplay();
        }

    }
    public ScreenShotService() {
        super("ScreenShotService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mScreenWidth = metrics.widthPixels;
        mScreenHeight = metrics.heightPixels;
        mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);
        mCameraSound = new MediaActionSound();
        mCameraSound.load(MediaActionSound.SHUTTER_CLICK);
        mDisposable = Observable.create((ObservableOnSubscribe<ImageReader>) emitter -> mEmitter = emitter)
                .debounce(400, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap((Function<ImageReader, ObservableSource<Bitmap>>) reader2 -> {
                    LogUtils.d(reader2.toString());
                    image = reader2.acquireNextImage();
                    if (image == null) {
                        mScreenShotBitmap = null;
                        return Observable.just(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
                    }
                    Bitmap bitmap = null;
                    final Image.Plane[] planes = image.getPlanes();
                    final ByteBuffer buffer = planes[0].getBuffer();
                    if (buffer == null) {
                        mScreenShotBitmap = null;
                        return Observable.just(Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8));
                    }
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * image.getWidth();
                    bitmap = Bitmap.createBitmap(image.getWidth() + rowPadding / pixelStride, image.getHeight(), Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);
                    Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, image.getWidth(), image.getHeight());
                    mScreenShotBitmap = bitmap;
                    image.close();
                    image = null;
                    return Observable.just(bitmap2);
                }).subscribe(bitmap -> {
                    if (mScreenShotBitmap == null) {
                        return;
                    }
                    mImageReader.close();
                    if (PermissionChecker.checkCallingOrSelfPermission(App.get(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PermissionChecker.PERMISSION_GRANTED) {
                        saveScreenShot();
                    } else {
                        // TODO: 2018/6/15 no permission
                    }

                });


        mImageReader.setOnImageAvailableListener(reader -> {
            if (mEmitter != null) {
                mEmitter.onNext(reader);
            }

        }, App.get().getHandler());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopScreenCapture();
    }

    private void saveScreenShot() {
        if (mScreenShotBitmap == null) {
            return;
        }
        mCameraSound.play(MediaActionSound.SHUTTER_CLICK);
        Uri url = CapturePhotoUtils.insertImage(mScreenShotBitmap);
        if (url != null) {
            ScreenShotNotification.notify(App.get(), mScreenShotBitmap, url, 0);
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
        tearDownMediaProjection();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    public static void capture(Context context) {
        Intent intent = new Intent(context, ScreenShotService.class);
        intent.setAction(ACTION_SCREEN_SHOT);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SCREEN_SHOT.equals(action)) {
                handleActionScreenShot();
            }
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void handleActionScreenShot() {
        if (mMediaProjection == null) {
            Intent intent = new Intent(getApplication(), ScreenCaptureActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(ScreenCaptureActivity.ARGS_MODE, ScreenCaptureActivity.MODE_SERVICE);
            startActivity(intent);
        } else {

        }
    }

}
