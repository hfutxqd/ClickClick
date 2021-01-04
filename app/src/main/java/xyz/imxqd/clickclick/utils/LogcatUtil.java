package xyz.imxqd.clickclick.utils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LogcatUtil {
    public static @NonNull String getLogs() {
        Process process = null;
        try {
            process = new ProcessBuilder()
                    .command("logcat", "-d")
                    .redirectErrorStream(true)
                    .start();
            StringBuilder builder = new StringBuilder();
            InputStream in = process.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int readLen = 0;
            char[] buffer = new char[1024];
            do {
                readLen = reader.read(buffer);
                if (readLen > 0) {
                    builder.append(buffer, 0, readLen);
                }
            } while (readLen > 0);
            return builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return "";
    }
}
