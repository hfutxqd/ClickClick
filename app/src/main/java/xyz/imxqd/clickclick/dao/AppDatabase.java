package xyz.imxqd.clickclick.dao;

import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.io.InputStream;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by imxqd on 2017/12/21.
 */
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
public class AppDatabase {

    private static final String TAG = "AppDatabase";

    public static final String NAME = "ClickClick";
    public static final int VERSION = 1;


    @Migration(version = 0, database = AppDatabase.class)
    public static class InitDatabase extends BaseMigration {

        @Override
        public void migrate(@NonNull DatabaseWrapper database) {
            initData(database);
        }
    }

    private static void initData(DatabaseWrapper database) {
        Log.d(TAG, "Init Database...");
        Locale locale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            locale = Resources.getSystem().getConfiguration().locale;
        }
        String sqlFile = null;
        if (locale.equals(Locale.SIMPLIFIED_CHINESE)) {
            sqlFile = "migrations/zh-rCN/0.sql";
        } else if (locale.equals(Locale.TRADITIONAL_CHINESE)) {
            sqlFile = "migrations/zh-rTW/0.sql";
        } else {
            sqlFile = "migrations/en/0.sql";
        }

        try {
            database.beginTransaction();
            InputStream in = FlowManager.getContext().getAssets().open(sqlFile);
            Scanner scanner = new Scanner(in);
            while (scanner.hasNextLine()) {
                String sql = scanner.nextLine();
                if (!TextUtils.isEmpty(sql)) {
                    database.execSQL(sql);
                }
            }
            scanner.close();
            in.close();
            database.setTransactionSuccessful();
            Log.d(TAG, "Database Initialized.");
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
            Log.d(TAG, "Database Init Error!");
        } finally {
            database.endTransaction();
        }
    }

}
