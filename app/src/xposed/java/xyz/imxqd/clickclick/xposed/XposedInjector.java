package xyz.imxqd.clickclick.xposed;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInjector implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage {

    private static final String TAG = "ClickClick_Xposed";

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        Log.d(TAG, "initZygote");
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        KeyEventMod.init(lpparam);
    }
}
