package xyz.imxqd.clickclick.ui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.service.NotificationCollectorService;
import xyz.imxqd.clickclick.ui.BaseActivity;
import xyz.imxqd.clickclick.utils.LogUtils;
import xyz.imxqd.clickclick.utils.NotificationAccessUtil;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener, OnRefreshUI {


    private static final String TAG = "SettingsFragment";
    private Handler mHandler = new Handler();

    public SettingsFragment() {
        LogUtils.d("SettingsFragment new instance");
    }


    public static SettingsFragment newInstance() {
        return new SettingsFragment();
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
        findPreference(getString(R.string.pref_key_quick_click_time)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.pref_key_long_click_time)).setOnPreferenceChangeListener(this);
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
            if (mPendingNotificationOn && (NotificationAccessUtil.isEnabled(getContext()) || AppEventManager.getInstance().getNotificationService() != null)) {
                notificationSwitch.setChecked(true);
            }
            mPendingSwitchOn = false;
        }
        if (!NotificationAccessUtil.isEnabled(getContext())) {
            ((SwitchPreference)findPreference(getString(R.string.pref_key_notification_switch))).setChecked(false);
        }
        initClickTimes();

        boolean debug = getPreferenceManager()
                .getSharedPreferences()
                .getBoolean(getString(R.string.pref_key_app_debug), false);
        if (debug) {
            addDebugSettings();
        }
    }

    private void initClickTimes() {
        String[] summaries = getResources().getStringArray(R.array.click_speed);
        String[] quickTimes = getResources().getStringArray(R.array.quick_click_speed_time);
        String[] longTimes = getResources().getStringArray(R.array.long_click_speed_time);
        String quickTime = SettingsUtil.getStringVal(getString(R.string.pref_key_quick_click_time), getString(R.string.quick_click_speed_time_default));
        String longTime = SettingsUtil.getStringVal(getString(R.string.pref_key_long_click_time), getString(R.string.long_click_speed_time_default));

        int quickPos = 1;
        for (int i = 0; i < quickTimes.length; i++) {
            if (quickTime.equals(quickTimes[i])) {
                quickPos = i;
            }
        }
        int longPos = 1;
        for (int i = 0; i < longTimes.length; i++) {
            if (longTime.equals(longTimes[i])) {
                longPos = i;
            }
        }
        findPreference(getString(R.string.pref_key_quick_click_time)).setSummary(summaries[quickPos]);
        findPreference(getString(R.string.pref_key_long_click_time)).setSummary(summaries[longPos]);
    }

    private void addDebugSettings() {
        if (findPreference(getString(R.string.pref_key_app_debug)) == null) {
            addPreferencesFromResource(R.xml.settings_debug);
            findPreference(getString(R.string.pref_key_app_debug)).setOnPreferenceChangeListener(this);
        }
    }

    private boolean mPendingSwitchOn = false;
    private boolean mPendingNotificationOn = false;

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        assert  getContext() != null;
        if (getString(R.string.pref_key_app_switch).equals(preference.getKey())) {
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
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        assert getContext() != null;
        if (getString(R.string.pref_key_app_switch).equals(preference.getKey())) {
            LogUtils.d( "KeyEventService is " + newValue);
        } else if (getString(R.string.pref_key_notification_switch).equals(preference.getKey())) {
            if (!NotificationAccessUtil.isEnabled(getContext())) {
                LogUtils.d( "NotificationService is disabled");
                ((SwitchPreference)preference).setChecked(false);
                NotificationAccessUtil.openNotificationAccess(getContext());
            } else {
                ((SwitchPreference)preference).setChecked(true);
                getContext().startService(new Intent(getActivity(), NotificationCollectorService.class));
            }
        } else if (getString(R.string.pref_key_long_click_time).equals(preference.getKey())) {
            ((ListPreference)preference).setValue((String) newValue);
            AppEventManager.getInstance().updateClickTime();
        }  else if (getString(R.string.pref_key_quick_click_time).equals(preference.getKey())) {
            ((ListPreference)preference).setValue((String) newValue);
            AppEventManager.getInstance().updateClickTime();
        } else if (getString(R.string.pref_key_app_debug).equals(preference.getKey())) {
            getPreferenceManager()
                    .getSharedPreferences()
                    .edit()
                    .putBoolean(getString(R.string.pref_key_app_debug), (Boolean) newValue)
                    .apply();
            App.get().initLogger();
        }
        initPrefs();
        return true;
    }

    @Override
    public void onDataChanged() {
        initPrefs();
    }
}
