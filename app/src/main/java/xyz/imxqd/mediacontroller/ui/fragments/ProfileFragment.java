package xyz.imxqd.mediacontroller.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.mediacontroller.R;
import xyz.imxqd.mediacontroller.model.AppKeyEvent;
import xyz.imxqd.mediacontroller.service.NotificationCollectorService;
import xyz.imxqd.mediacontroller.ui.KeyEventActivity;
import xyz.imxqd.mediacontroller.ui.adapters.ProfileAdapter;
import xyz.imxqd.mediacontroller.utils.Constants;
import xyz.imxqd.mediacontroller.utils.NotificationAccessUtil;
import xyz.imxqd.mediacontroller.utils.ScreenUtl;


public class ProfileFragment extends Fragment {

    private static final int REQUEST_CODE_ADD_KEY_EVENT = 1;
    private volatile static ProfileFragment mInstance;

    @BindView(android.R.id.list)
    RecyclerView vList;

    @BindView(R.id.profile_state)
    TextView vState;

    ProfileAdapter mAdapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment getInstance() {
        if (mInstance == null ) {
            synchronized (ProfileFragment.class) {
                mInstance = new ProfileFragment();
            }
        }
        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new ProfileAdapter();
        vList.setLayoutManager(new LinearLayoutManager(getContext()));
        vList.setAdapter(mAdapter);
        vList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int pos = parent.getChildAdapterPosition(view);
                if (pos == 0) {
                    outRect.top = ScreenUtl.dp2px(5);
                }
            }
        });

        if (!NotificationAccessUtil.isEnabled(getContext())) {
            NotificationAccessUtil.openNotificationAccess(getContext());
        } else {
            getActivity().startService(new Intent(getActivity(), NotificationCollectorService.class));
        }
        Intent intent = new Intent(getContext(), NotificationCollectorService.class);
        intent.setAction(Constants.ACTION_CLOUD_MUSIC_LIKE);
        getActivity().startService(intent);

        vState.setText(getStateText());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInstance = null;
    }

    private CharSequence getStateText() {
        Spannable spannable = new SpannableString("当前开启 5 个场景");
        spannable.setSpan(new RelativeSizeSpan(2f), 5, 6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    @OnClick(R.id.action_add)
    public void onAddClick() {
        Intent intent = new Intent(getActivity(), KeyEventActivity.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        startActivityForResult(intent, REQUEST_CODE_ADD_KEY_EVENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        AppKeyEvent event;
        if (resultCode == Activity.RESULT_OK) {
            event = data.getParcelableExtra(KeyEventActivity.ARG_KEY_EVENT);
        }
    }
}
