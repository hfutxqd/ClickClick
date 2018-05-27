package xyz.imxqd.clickclick.utils;

import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import xyz.imxqd.clickclick.App;

public class RawUtil {
    public static String getString(@RawRes int id) throws IOException {
        InputStream is = App.get().getResources().openRawResource(id);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }

        return writer.toString();
    }

    public static String getString(String name) throws IOException {
        int id = App.get().getResources().getIdentifier(name, "raw", App.get().getPackageName());
        return getString(id);
    }
}
