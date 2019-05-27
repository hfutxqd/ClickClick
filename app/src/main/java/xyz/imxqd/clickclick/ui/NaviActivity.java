package xyz.imxqd.clickclick.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.ArrayMap;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.ui.fragments.FunctionFragment;
import xyz.imxqd.clickclick.ui.fragments.OnRefreshUI;
import xyz.imxqd.clickclick.ui.fragments.ProfileFragment;
import xyz.imxqd.clickclick.ui.fragments.SettingsFragment;
import xyz.imxqd.clickclick.utils.SettingsUtil;

public class NaviActivity extends BaseActivity {

    @BindView(R.id.message)
    TextView vTitle;

    @BindView(R.id.navigation)
    BottomNavigationView vNavigation;

    private Set<OnRefreshUI> mOnRefreshUICallbacks = new HashSet<>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switchPageTo(item.getItemId());
                return true;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navi);
        if (savedInstanceState != null) {
            currentTabId = savedInstanceState.getInt("currentTabId");
        }
        ButterKnife.bind(this);
        initViews();
        if (currentTabId == 0) {
            switchPageTo(R.id.navigation_home);
        } else {
            clearFragments();
            switchPageTo(currentTabId);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentTabId", currentTabId);
    }

    @Override
    public void finish() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.finishAndRemoveTask();
        }
        else {
            super.finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragments.clear();
        currentTabId = 0;
    }


    @Override
    protected void onResume() {
        super.onResume();
        App.get().getHandler().postDelayed(this::showSnackBarInNeed, 100);
    }

    private Snackbar mSnackbar;
    private void showSnackBarInNeed() {
        if (AppEventManager.getInstance().getService() == null) {
            if (SettingsUtil.isAccessibilitySettingsOn(this) && SettingsUtil.isServiceOn()) {
                mSnackbar = Snackbar.make(findViewById(R.id.nav_container), R.string.snack_bar_accessibility_error, Snackbar.LENGTH_INDEFINITE);
                mSnackbar.setAction(R.string.re_turn_on, v -> SettingsUtil.startAccessibilitySettings(NaviActivity.this));
                mSnackbar.getView().setBackgroundResource(R.color.snackbar_error_bg);
                mSnackbar.setActionTextColor(ContextCompat.getColor(this, R.color.snackbar_error_accent));
                mSnackbar.show();
                return;
            }
            if (mSnackbar != null && mSnackbar.isShown()) {
                return;
            }
            mSnackbar = Snackbar.make(findViewById(R.id.nav_container), R.string.snack_bar_app_off, Snackbar.LENGTH_INDEFINITE);
            mSnackbar.setAction(R.string.to_turn_on, v -> vNavigation.setSelectedItemId(R.id.navigation_settings));
            mSnackbar.setActionTextColor(ContextCompat.getColor(this, R.color.snackbar_error_accent));
            mSnackbar.show();
        } else if (mSnackbar != null && mSnackbar.isShownOrQueued()){
            mSnackbar.dismiss();
        }
    }

    private void initViews() {
        vNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mFragments.put(R.id.navigation_home, ProfileFragment.newInstance());
        mFragments.put(R.id.navigation_dashboard, FunctionFragment.newInstance());
        mFragments.put(R.id.navigation_settings, SettingsFragment.newInstance());
    }

    public void requestRefreshUI() {
        for (OnRefreshUI ui : mOnRefreshUICallbacks) {
            ui.onRefreshUI();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if(fragment instanceof ProfileFragment) {
            mFragments.put(R.id.navigation_home, fragment);
        } else if (fragment instanceof  FunctionFragment) {
            mFragments.put(R.id.navigation_dashboard, fragment);
        } else if (fragment instanceof  SettingsFragment) {
            mFragments.put(R.id.navigation_settings, fragment);
        }
        if (fragment instanceof OnRefreshUI) {
            mOnRefreshUICallbacks.add((OnRefreshUI) fragment);
        }
    }

    private int currentTabId = 0;
    private ArrayMap<Integer, Fragment> mFragments = new ArrayMap<>();

    public void clearFragments() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        for (Fragment f : fragments) {
            transaction.remove(f);
        }
        transaction.commitNowAllowingStateLoss();
    }

    private void switchPageTo(int id) {
        Fragment fragmentTo = mFragments.get(id);
        if (fragmentTo == null) {
            return;
        }
        switch (id) {
            case R.id.navigation_home:
                vTitle.setText(R.string.title_home);
                break;
            case R.id.navigation_dashboard:
                vTitle.setText(R.string.title_dashboard);
                break;
            case R.id.navigation_settings:
                vTitle.setText(R.string.title_settings);
                break;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        Fragment current = mFragments.get(currentTabId);
        FragmentTransaction transaction = fragmentManager
                .beginTransaction();
        if (current != null && fragments.contains(current)){
            transaction.hide(current);
        }
        if (fragmentTo.isAdded()) {
            transaction.show(fragmentTo);
        } else {
            transaction.add(R.id.nav_container, fragmentTo);
        }
        transaction.commit();
        currentTabId = id;
    }

    @Override
    public void onEvent(int what, Object data) {
        if (what == App.EVENT_WHAT_REFRESH_UI) {
            requestRefreshUI();
        } else if (what == App.EVENT_WHAT_APP_SWITCH_CHANGED) {
            App.get().getHandler().postDelayed(this::showSnackBarInNeed, 100);
        }
    }
}
