package xyz.imxqd.clickclick;

import android.app.Application;
import android.support.annotation.NonNull;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import xyz.imxqd.clickclick.dao.AppDatabase;
import xyz.imxqd.clickclick.model.AppEventManager;

/**
 * Created by imxqd on 2017/11/24.
 */

public class App extends Application {

    public boolean isServiceOn = true;

    private static App mApp;

    @Override
    public void onCreate() {
        super.onCreate();
        mApp = this;
        FlowManager.init(this);
        AppEventManager.getInstance().init(this);
        Logger.addLogAdapter(new AndroidLogAdapter());

    }

    public static App get() {
        return mApp;
    }


}
