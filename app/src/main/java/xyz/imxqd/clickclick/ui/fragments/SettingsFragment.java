package xyz.imxqd.clickclick.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;

import com.orhanobut.logger.Logger;
import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.ui.BaseActivity;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

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
        findPreference(getString(R.string.pref_key_app_switch)).setOnPreferenceChangeListener(this);
    }

    private void initPrefs() {
        assert getContext() != null;
        SwitchPreference appSwitch = (SwitchPreference) findPreference(getString(R.string.pref_key_app_switch));
        BaseActivity activity = (BaseActivity)getActivity();
        if (appSwitch.isChecked()) {
            assert activity != null;
            if (!activity.isAccessibilitySettingsOn()) {
                appSwitch.setChecked(false);
            }
        } else {
            assert activity != null;
            if (mPendingSwitchOn && activity.isAccessibilitySettingsOn()) {
                appSwitch.setChecked(true);
            }
            mPendingSwitchOn = false;
            SwitchPreference notificationSwitch = (SwitchPreference) findPreference(getString(R.string.pref_key_notification_switch));
            if (mPendingNotificationOn && NotificationAccessUtil.isEnabled(getContext())) {
                notificationSwitch.setChecked(true);
            }
            mPendingSwitchOn = false;
        }
        if (!NotificationAccessUtil.isEnabled(getContext())) {
            ((SwitchPreference)findPreference(getString(R.string.pref_key_notification_switch))).setChecked(false);
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
    private boolean mPendingNotificationOn = false;

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        assert  getContext() != null;
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
                assert activity != null;
                if (!activity.isAccessibilitySettingsOn()) {
                    mPendingSwitchOn = true;
                    activity.startAccessibilitySettings();
                    p.setChecked(false);
                }
            } else {
                mPendingSwitchOn = false;
            }
            return true;
        } else if (getString(R.string.pref_key_notification_switch).equals(preference.getKey())) {
            SwitchPreference p = (SwitchPreference) preference;
            if (!NotificationAccessUtil.isEnabled(getContext())) {
                p.setChecked(false);
                NotificationAccessUtil.openNotificationAccess(getContext());
                mPendingNotificationOn = true;
            } else {
                mPendingNotificationOn = false;
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

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        assert getContext() != null;
        if (getString(R.string.pref_key_app_switch).equals(preference.getKey())) {
            findPreference(getString(R.string.pref_key_notification_switch)).setEnabled((Boolean) newValue);
//            findPreference(getString(R.string.pref_key_root_mode_switch)).setEnabled((Boolean) newValue);
        } else if (getString(R.string.pref_key_notification_switch).equals(preference.getKey())) {
            if (!NotificationAccessUtil.isEnabled(getContext())) {
                Logger.d("NotificationAccess is disabled.");
                ((SwitchPreference)preference).setChecked(false);
                NotificationAccessUtil.openNotificationAccess(getContext());
            } else {
                ((SwitchPreference)preference).setChecked(true);
                getContext().startService(new Intent(getActivity(), NotificationCollectorService.class));
            }
        }
        return true;
    }
}
