package xyz.imxqd.clickclick.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.ui.fragments.FunctionFragment;
import xyz.imxqd.clickclick.ui.fragments.KeyEventMapFragment;
import xyz.imxqd.clickclick.ui.fragments.ProfileFragment;
import xyz.imxqd.clickclick.ui.fragments.SettingsFragment;

public class NaviActivity extends BaseActivity {

    @BindView(R.id.message)
    TextView vTitle;

    @BindView(R.id.navigation)
    BottomNavigationView vNavigation;

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
        ButterKnife.bind(this);
        initViews();
        switchPageTo(R.id.navigation_home);
    }

    private void initViews() {
        vNavigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private Fragment mCurrentFragment = null;

    private void switchPageTo(int id) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null) {
            fragments = new ArrayList<>();
        }
        Fragment fragmentTo = null;
        switch (id) {
            case R.id.navigation_home:
                vTitle.setText(R.string.title_home);
                fragmentTo = ProfileFragment.getInstance();
                break;
            case R.id.navigation_dashboard:
                vTitle.setText(R.string.title_dashboard);
                fragmentTo = FunctionFragment.getInstance();
                break;
            case R.id.navigation_settings:
                vTitle.setText(R.string.title_settings);
                fragmentTo = SettingsFragment.getInstance();
                break;
        }
        if (fragmentTo == null || mCurrentFragment == fragmentTo) {
            return;
        }
        if (fragments.contains(fragmentTo)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .hide(mCurrentFragment)
                    .show(fragmentTo)
                    .commit();
        } else {
            if (mCurrentFragment != null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .hide(mCurrentFragment)
                        .add(R.id.nav_container, fragmentTo)
                        .commit();
            } else {
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.nav_container, fragmentTo)
                        .commit();
            }
        }
        mCurrentFragment = fragmentTo;
    }

}
