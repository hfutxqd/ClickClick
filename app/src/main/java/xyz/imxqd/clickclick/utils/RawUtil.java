package xyz.imxqd.clickclick.utils;

import android.support.annotation.RawRes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import xyz.imxqd.clickclick.App;

public class RawUtil {
    public static String getString(@RawRes int id) throws IOException {
        Writer writer = new StringWriter();
        try (InputStream is = App.get().getResources().openRawResource(id)) {
            char[] buffer = new char[1024];
            Reader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return writer.toString();
    }

    public static String getString(String name) throws IOException {
        int id = App.get().getResources().getIdentifier(name, "raw", App.get().getPackageName());
        return getString(id);
    }
}
