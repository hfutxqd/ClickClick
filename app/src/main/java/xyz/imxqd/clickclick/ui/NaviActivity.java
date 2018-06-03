package xyz.imxqd.clickclick.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.ArrayMap;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.ui.fragments.FunctionFragment;
import xyz.imxqd.clickclick.ui.fragments.OnRefreshUI;
import xyz.imxqd.clickclick.ui.fragments.ProfileFragment;
import xyz.imxqd.clickclick.ui.fragments.SettingsFragment;

public class NaviActivity extends BaseActivity implements App.AppEventCallback{

    @BindView(R.id.message)
    TextView vTitle;

    @BindView(R.id.navigation)
    BottomNavigationView vNavigation;

    private Set<OnRefreshUI> mOnRefreshUICallbacks = new HashSet<>();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switchPageTo(item.getItemId());
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

//        if (RxFingerprint.isAvailable(this)) {
//            Disposable disposable = RxFingerprint.decrypt(EncryptionMethod.AES, this, "name", "tode")
//                    .subscribe(fingerprintAuthenticationResult -> {
//                        switch (fingerprintAuthenticationResult.getResult()) {
//                            case FAILED:
//                                LogUtils.d("Fingerprint not recognized, try again!");
//                                break;
//                            case HELP:
//                                LogUtils.d(fingerprintAuthenticationResult.getMessage());
//                                break;
//                            case AUTHENTICATED:
//                                LogUtils.d("Successfully authenticated!");
//                                break;
//                        }
//                    }, throwable -> {
//                        LogUtils.e("ERROR : authenticate", throwable);
//                    });
//        } else {
//            // fingerprint is not available
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentTabId", currentTabId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFragments.clear();
        currentTabId = 0;
    }

    private void initViews() {
        vNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mFragments.put(R.id.navigation_home, ProfileFragment.newInstance());
        mFragments.put(R.id.navigation_dashboard, FunctionFragment.newInstance());
        mFragments.put(R.id.navigation_settings, SettingsFragment.newInstance());
    }

    public void requestRefreshUI() {
        for (OnRefreshUI ui : mOnRefreshUICallbacks) {
            ui.onDataChanged();
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

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        Fragment current = mFragments.get(currentTabId);
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        if (fragments.contains(current)){
            transaction.hide(current);
        }
        if (fragments.contains(fragmentTo)) {
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
        }
    }
}
