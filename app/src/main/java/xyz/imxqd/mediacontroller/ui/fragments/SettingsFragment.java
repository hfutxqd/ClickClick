package xyz.imxqd.mediacontroller.ui.fragments;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import xyz.imxqd.mediacontroller.R;
import xyz.imxqd.mediacontroller.ui.BaseActivity;

public class SettingsFragment extends PreferenceFragmentCompat {

    private static volatile SettingsFragment mInstance;

    private Handler mHandler = new Handler();

    public SettingsFragment() {
        // Required empty public constructor
    }


    public static SettingsFragment getInstance() {
        if (mInstance == null ) {
            synchronized (SettingsFragment.class) {
                mInstance = new SettingsFragment();
            }
        }
        return mInstance;
    }

    @Override
    public void onResume() {
        super.onResume();
        initPrefs();
    }


    @Override
    public void onCreatePreferencesFix(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settgings_screen);
        initPrefs();
    }

    private void initPrefs() {
        SwitchPreference appSwitch = (SwitchPreference) findPreference(getString(R.string.pref_key_app_switch));
        BaseActivity activity = (BaseActivity)getActivity();
        if (appSwitch.isChecked()) {
            if (!activity.isAccessibilitySettingsOn()) {
                appSwitch.setChecked(false);
            }
        } else if (mPendingSwitchOn && activity.isAccessibilitySettingsOn()) {
            appSwitch.setChecked(true);
        }

        boolean debug = getPreferenceManager()
                .getSharedPreferences()
                .getBoolean(getString(R.string.pref_key_app_debug), false);
        if (debug) {
            addDebugSettings();
        }
    }

    private void addDebugSettings() {
        if (findPreference(getString(R.string.pref_key_app_debug)) == null) {
            addPreferencesFromResource(R.xml.settings_debug);
        }
    }

    private int mClickCount = 0;
    private boolean mPendingSwitchOn = false;

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {

        if (getString(R.string.pref_key_version).equals(preference.getKey())) {
            if (mClickCount == 0) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mHandler.removeCallbacksAndMessages(null);
                        mClickCount = 0;
                    }
                }, 1500);
            }
            mClickCount++;
            if (mClickCount == 7) {
                addDebugSettings();
            }
        } else if (getString(R.string.pref_key_app_switch).equals(preference.getKey())) {
            SwitchPreference p = (SwitchPreference) preference;
            if (p.isChecked()) {
                BaseActivity activity = (BaseActivity)getActivity();
                if (!activity.isAccessibilitySettingsOn()) {
                    mPendingSwitchOn = true;
                    activity.startAccessibilitySettings();
                    p.setChecked(false);
                }
            } else {
                mPendingSwitchOn = false;
            }
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInstance = null;
    }
}
