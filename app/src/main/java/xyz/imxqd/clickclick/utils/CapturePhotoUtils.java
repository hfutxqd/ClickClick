package xyz.imxqd.clickclick.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.log.LogUtils;

public class CapturePhotoUtils {
    public static Uri insertImage(@NonNull Bitmap source) {
        ContentValues values = new ContentValues();
        String title = "screenshot_" + System.currentTimeMillis() + ".jpg";
        values.put(MediaStore.Images.Media.TITLE, title);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, title);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Screenshot file by ClickClick");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        // Add the date meta data to ensure the image is added at the front of the gallery
        values.put(MediaStore.Images.Media.WIDTH, source.getWidth());
        values.put(MediaStore.Images.Media.HEIGHT, source.getHeight());
        values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile = new File(dir, title);
        values.put(MediaStore.MediaColumns.DATA, imageFile.getPath());
        try {
            try (OutputStream imageOut = new FileOutputStream(imageFile)) {
                source.compress(Bitmap.CompressFormat.JPEG, 80, imageOut);
            }
            Uri photoURI = FileProvider.getUriForFile(App.get(), App.get().getPackageName() + ".fileprovider", imageFile);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                Intent intentScanner = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intentScanner.setData(photoURI);
                App.get().sendBroadcast(intentScanner);
            } else {
                MediaScannerConnection.scanFile(App.get(), new String[]{Environment.DIRECTORY_PICTURES}, null, (path, uri) -> {
                });
            }
            return photoURI;
        } catch (Throwable e) {
            LogUtils.e(e.toString());
            return null;
        }
    }

    /**
     * A copy of the Android internals StoreThumbnail method, it used with the insertImage to
     * populate the android.provider.MediaStore.Images.Media#insertImage with all the correct
     * meta data. The StoreThumbnail method is private so it must be duplicated here.
     *
     * @see android.provider.MediaStore.Images.Media (StoreThumbnail private method)
     */
    private static final Bitmap storeThumbnail(
            ContentResolver cr,
            Bitmap source,
            long id,
            float width,
            float height,
            int kind) {

        // create the matrix to scale it
        Matrix matrix = new Matrix();

        float scaleX = width / source.getWidth();
        float scaleY = height / source.getHeight();

        matrix.setScale(scaleX, scaleY);

        Bitmap thumb = Bitmap.createBitmap(source, 0, 0,
                source.getWidth(),
                source.getHeight(), matrix,
                true
        );

        ContentValues values = new ContentValues(4);
        values.put(MediaStore.Images.Thumbnails.KIND, kind);
        values.put(MediaStore.Images.Thumbnails.IMAGE_ID, (int) id);
        values.put(MediaStore.Images.Thumbnails.HEIGHT, thumb.getHeight());
        values.put(MediaStore.Images.Thumbnails.WIDTH, thumb.getWidth());

        Uri url = cr.insert(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, values);

        try {
            if (url == null) {
                return null;
            }
            try (OutputStream thumbOut = cr.openOutputStream(url)) {
                thumb.compress(Bitmap.CompressFormat.JPEG, 100, thumbOut);
            }
            return thumb;
        } catch (IOException ex) {
            return null;
        }
    }
}
