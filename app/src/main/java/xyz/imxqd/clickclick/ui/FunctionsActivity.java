package xyz.imxqd.clickclick.ui;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.BuildConfig;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.DefinedFunction;
import xyz.imxqd.clickclick.model.web.HomePage;
import xyz.imxqd.clickclick.model.web.HttpResult;
import xyz.imxqd.clickclick.model.web.RemoteFunction;
import xyz.imxqd.clickclick.model.web.ServerApi;
import xyz.imxqd.clickclick.utils.RawUtil;

public class FunctionsActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    @BindView(R.id.functions_container)
    ViewPager mViewPager;
    @BindView(R.id.functions_tab)
    TabLayout mTabLayout;
    @BindView(R.id.action_title)
    TextView mTitle;

    private boolean isLocalMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_functions);
        ButterKnife.bind(this);
        initViews();
    }

    @OnClick(R.id.action_back)
    public void onBackClick() {
        finish();
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        super.finish();
    }

    private void loadFromRaw() {
        try {
            Gson gson = new Gson();
            String pagesJson = RawUtil.getString(R.raw.pages);
            FuncPage[] pages = gson.fromJson(pagesJson, FuncPage[].class);
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), Arrays.asList(pages));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFromNetwork() {
        List<FuncPage> list = new ArrayList<>();
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), list);
    }

    private void initViews() {
        mTitle.setText(getTitle());
        if (isLocalMode) {
            loadFromRaw();
        } else {
            loadFromNetwork();
        }
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    public static class FunctionListFragment extends Fragment {
        private static final String ARG_SECTION_URL = "section_url";
        private static final String ARG_IS_LOCAL = "section_is_local";

        @BindView(R.id.functions_list)
        RecyclerView mListView;
        @BindView(R.id.empty_view)
        FrameLayout mEmpty;
        @BindView(R.id.functions_fab)
        FloatingActionButton mFab;

        private String mUrl;
        private boolean isLocalMode = true;
        private RemoteFuncListAdapter mAdapter;
        private Animation mRotation;

        public static class RemoteFuncListAdapter extends RecyclerView.Adapter<RemoteFuncListAdapter.RemoteFuncHolder> {

            private HomePage mPage;

            public RemoteFuncListAdapter() {
                mPage = new HomePage();
                mPage.total = 0;
                mPage.data = new ArrayList<>();
            }

            public void setData(HomePage page) {
                mPage.total = page.total;
                mPage.data.clear();
                mPage.data.addAll(page.data);
            }

            @NonNull
            @Override
            public RemoteFuncHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_item_remote_func, parent, false);
                return new RemoteFuncHolder(v);
            }

            @Override
            public void onBindViewHolder(@NonNull RemoteFuncHolder holder, int position) {
                RemoteFunction f = mPage.data.get(position);
                holder.name.setText(f.name);
                holder.author.setText(f.author);
                holder.description.setText(f.description);
                SimpleDateFormat format = new SimpleDateFormat("yyyy M/d H:mm");
                holder.time.setText(format.format(new Date(f.updateTime)));
                if (DefinedFunction.checkHas(f.body)) {
                    holder.add.setImageResource(R.drawable.ic_done_black_24dp);
                    holder.add.setEnabled(false);
                } else {
                    holder.add.setImageResource(R.drawable.ic_add_black_24dp);
                    holder.add.setEnabled(true);
                }
            }

            @Override
            public int getItemCount() {
                return mPage.data.size();
            }

            public class RemoteFuncHolder extends RecyclerView.ViewHolder {

                @BindView(R.id.remote_func_title)
                TextView name;
                @BindView(R.id.remote_func_sub_title)
                TextView description;
                @BindView(R.id.remote_func_author)
                TextView author;
                @BindView(R.id.remote_func_time)
                TextView time;
                @BindView(R.id.remote_func_add)
                ImageView add;

                public RemoteFuncHolder(View itemView) {
                    super(itemView);
                    ButterKnife.bind(this, itemView);
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            RemoteFunction f = mPage.data.get(getAdapterPosition());
                            AddFunctionActivity.start(f, false, App.get());
                        }
                    });
                }

                @OnClick({R.id.remote_func_add})
                public void onAddClick() {
                    RemoteFunction f = mPage.data.get(getAdapterPosition());
                    if (f.versionCode > BuildConfig.VERSION_CODE) {
                        App.get().showToast(App.get().getString(R.string.app_version_too_old, f.versionName));
                    } else {
                        DefinedFunction function = new DefinedFunction();
                        function.body = f.body;
                        function.name = f.name;
                        function.description = f.description;
                        function.order = 0;
                        try {
                            function.save();
                            App.get().showToast(R.string.save_successed);
                        } catch (Exception e) {
                            App.get().showToast(R.string.save_failed);
                        }
                        notifyItemChanged(getAdapterPosition());
                    }
                }
            }
        }

        public FunctionListFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static FunctionListFragment newInstance(String url, boolean isLocalMode) {
            FunctionListFragment fragment = new FunctionListFragment();
            Bundle args = new Bundle();
            args.putString(ARG_SECTION_URL, url);
            args.putBoolean(ARG_IS_LOCAL, isLocalMode);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAdapter = new RemoteFuncListAdapter();
            mUrl = getArguments().getString(ARG_SECTION_URL);
            isLocalMode = getArguments().getBoolean(ARG_IS_LOCAL);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_functions, container, false);
        }

        @OnClick(R.id.functions_fab)
        public void onFabClick() {
            reload();
        }

        private void reload() {
            mRotation = AnimationUtils.loadAnimation(getContext(), R.anim.rotation);
            mRotation.setRepeatCount(Animation.INFINITE);
            mFab.startAnimation(mRotation);
            if (isLocalMode) {
                loadLocal();
            } else {
                loadRemote();
            }
        }

        @SuppressLint("CheckResult")
        private void loadLocal() {
            Observable.create(new ObservableOnSubscribe<HomePage>() {
                @Override
                public void subscribe(ObservableEmitter<HomePage> emitter) throws Exception {
                    try {
                        String json = RawUtil.getString(mUrl);
                        Gson gson = new Gson();
                        HomePage page = gson.fromJson(json, HomePage.class);
                        emitter.onNext(page);
                    } catch (IOException e) {
                        e.printStackTrace();
                        emitter.onError(e);
                    }
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<HomePage>() {
                @Override
                public void accept(HomePage homePage) throws Exception {
                    mRotation.cancel();
                    mAdapter.setData(homePage);
                    mAdapter.notifyDataSetChanged();
                    mListView.setVisibility(View.VISIBLE);
                    mEmpty.setVisibility(View.GONE);
                }
            });
        }

        private void loadRemote() {
            ServerApi api = new Retrofit.Builder()
                    .baseUrl(ServerApi.BASE_URL)
                    .build().create(ServerApi.class);
            api.loadList(mUrl)
                    .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<HttpResult<HomePage>>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onNext(HttpResult<HomePage> homePageHttpResult) {

                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            ButterKnife.bind(this, view);
            mListView.setLayoutManager(new LinearLayoutManager(getContext()));
            mListView.setAdapter(mAdapter);
            mListView.addItemDecoration(new RecyclerView.ItemDecoration() {
                @Override
                public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                    int pos = parent.getChildAdapterPosition(view);
                    int count = mAdapter.getItemCount();
                    if (pos == count - 1) { // the last one
                        outRect.bottom = getResources().getDimensionPixelSize(R.dimen.list_last_bottom);
                    } else {
                        outRect.bottom = 0;
                    }
                }
            });
            mListView.setVisibility(View.GONE);
            mEmpty.setVisibility(View.VISIBLE);
            reload();
        }
    }

    public static class FuncPage {
        public String url;
        public String title;
    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        List<FuncPage> mData;

        public SectionsPagerAdapter(FragmentManager fm, List<FuncPage> pages) {
            super(fm);
            mData = pages;
        }

        @Override
        public FunctionListFragment getItem(int position) {

            return FunctionListFragment.newInstance(mData.get(position).url, isLocalMode);
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mData.get(position).title;
        }
    }
}
