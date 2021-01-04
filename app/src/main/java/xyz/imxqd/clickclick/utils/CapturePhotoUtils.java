package xyz.imxqd.clickclick.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Random;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.log.LogUtils;

public class CapturePhotoUtils {
    public static Uri insertImage(@NonNull Bitmap source) {
        String title = "screenshot_" + System.currentTimeMillis() + new Random().nextLong() + ".jpg";
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(dir, title);
        try {
            try (OutputStream imageOut = new FileOutputStream(imageFile)) {
                source.compress(Bitmap.CompressFormat.JPEG, 80, imageOut);
            }
            Uri photoURI = FileProvider.getUriForFile(App.get(), App.get().getPackageName() + ".fileprovider", imageFile);
            Intent intentScanner = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intentScanner.setData(Uri.fromFile(imageFile));
            App.get().sendBroadcast(intentScanner);
            return photoURI;
        } catch (Throwable e) {
            LogUtils.e(e.toString());
            return null;
        }
    }
}
