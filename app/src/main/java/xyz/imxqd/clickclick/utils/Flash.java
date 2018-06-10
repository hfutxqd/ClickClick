package xyz.imxqd.clickclick.utils;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.log.LogUtils;

public class Flash implements Closeable {

    private static Flash sFlash;

    private static boolean isFlashOn = false;

    private Flash() {
    }

    public static Flash get() {
        if (sFlash == null) {
            synchronized (Flash.class) {
                sFlash = new Flash();
            }
        }
        return sFlash;
    }

    private static class CameraHolder {
        public final Camera camera;
        private final Camera.Parameters cameraParameters;

        public CameraHolder(Camera camera) {
            this.camera = camera;
            this.cameraParameters = camera.getParameters();
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        }

        public void on() {
            camera.setParameters(cameraParameters);
            camera.startPreview();
        }
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<CameraHolder> futureCamera;

    public static class CameraCallable implements Callable<CameraHolder> {
        private final Camera oldCamera;

        public CameraCallable(Camera camera) {
            oldCamera = camera;
        }
        @Override
        public CameraHolder call() throws Exception {
            if (oldCamera != null) {
                oldCamera.release();
                isFlashOn = false;
            }
            return new CameraHolder(Camera.open());
        }
    }

    public static synchronized boolean isFlashOn() {
        return isFlashOn;
    }

    public synchronized void off() throws CameraAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager manager = (CameraManager) App.get().getSystemService(Context.CAMERA_SERVICE);
            String[] list = manager.getCameraIdList();
            manager.setTorchMode(list[0], false);
            isFlashOn = false;
        } else {
            if (futureCamera == null) {
                futureCamera = executorService.submit(new CameraCallable(null));
            } else if (futureCamera.isDone()) {
                Camera prev;
                try {
                    prev = futureCamera.get().camera;
                } catch (Exception e) {
                    prev = null;
                }
                futureCamera = executorService.submit(new CameraCallable(prev));
            }
        }
    }

    public synchronized boolean on() throws CameraAccessException {
        LogUtils.i("Got button press");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager manager = (CameraManager) App.get().getSystemService(Context.CAMERA_SERVICE);
            String[] list = manager.getCameraIdList();
            manager.setTorchMode(list[0], true);
            isFlashOn = true;
        } else {
            if (futureCamera == null) {
                LogUtils.d("Camera is null");
                futureCamera = executorService.submit(new CameraCallable(null));
            }
            if (!futureCamera.isDone()) {
                LogUtils.d( "Waiting for camera");
            }
            try {
                futureCamera.get().on();
                isFlashOn = true;
                LogUtils.d( "Light is on");
            } catch (Exception e) {
                LogUtils.e( "Failed to activate flash : " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public synchronized void close() {
        if (futureCamera != null) {
            try {
                futureCamera.get().camera.release();
            } catch (Exception e) {
                LogUtils.e( "Failed to release camera : " + e.getMessage());
            }
        }
        futureCamera = null;
    }

}