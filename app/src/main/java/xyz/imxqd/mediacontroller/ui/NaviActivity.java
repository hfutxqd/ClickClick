package xyz.imxqd.mediacontroller.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.view.MenuItem;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.imxqd.mediacontroller.R;
import xyz.imxqd.mediacontroller.ui.fragments.FunctionFragment;
import xyz.imxqd.mediacontroller.ui.fragments.KeyEventMapFragment;
import xyz.imxqd.mediacontroller.ui.fragments.SettingsFragment;

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

    private void switchPageTo(int id) {
        switch (id) {
            case R.id.navigation_home:
                vTitle.setText(R.string.title_home);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_container, KeyEventMapFragment.getInstance())
                        .commit();
                break;
            case R.id.navigation_dashboard:
                vTitle.setText(R.string.title_dashboard);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_container, FunctionFragment.getInstance())
                        .commit();
                break;
            case R.id.navigation_settings:
                vTitle.setText(R.string.title_settings);
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.nav_container, SettingsFragment.getInstance())
                        .commit();
                break;
        }
    }

}
