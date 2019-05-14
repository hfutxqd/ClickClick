package xyz.imxqd.clickclick.ui;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.model.web.RemoteFunction;
import xyz.imxqd.clickclick.ui.adapters.AppListAdapter;

public class AppChooseActivity extends BaseActivity implements AppListAdapter.OnAppSelectedCallback {

    @BindView(R.id.app_list)
    RecyclerView mList;
    @BindView(R.id.app_loading)
    ProgressBar mLoading;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    AppListAdapter mAdapter = new AppListAdapter();

    Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_choose);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        }
        mList.setLayoutManager(new LinearLayoutManager(this));
        mList.setAdapter(mAdapter);
        mAdapter.setOnAppSelectedCallback(this);

        mDisposable = Observable.create((ObservableOnSubscribe<List<AppListAdapter.AppInfo>>) emitter -> {
            final PackageManager pm = getPackageManager();
            List<AppListAdapter.AppInfo> infos = new ArrayList<>();
            List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
            for (ApplicationInfo applicationInfo : packages) {
                AppListAdapter.AppInfo appInfo = new AppListAdapter.AppInfo();
                appInfo.name = applicationInfo.loadLabel(pm).toString();
                appInfo.icon = applicationInfo.loadIcon(pm);
                appInfo.packageName = applicationInfo.packageName;
                Intent intent = pm.getLaunchIntentForPackage(appInfo.packageName);
                if (intent != null) {
                    infos.add(appInfo);
                }
            }
            Comparator<AppListAdapter.AppInfo> cmp = (o1, o2) -> Collator.getInstance(Locale.getDefault()).compare(o1.name, o2.name);
            Collections.sort(infos, cmp);
            emitter.onNext(infos);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(applicationInfos -> {
                    mAdapter.setData(applicationInfos);
                    mAdapter.notifyDataSetChanged();
                    mLoading.setVisibility(View.GONE);
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mAdapter.filter(s);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    public void onAppSelect(AppListAdapter.AppInfo appInfo) {
        RemoteFunction f = new RemoteFunction();
        PackageManager pm = getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(appInfo.packageName);
        if (intent != null) {
            f.name = appInfo.name;
            f.description = getString(R.string.open) + appInfo.name;
            f.body = "action:intent://" + intent.toUri(0);
            f.updateTime = System.currentTimeMillis() / 1000;
            AddFunctionActivity.start(f, true, this);
        } else {
            App.get().showToast(R.string.app_can_not_open);
        }
        finish();
    }
}
