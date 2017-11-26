package xyz.imxqd.mediacontroller.ui.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.mediacontroller.R;
import xyz.imxqd.mediacontroller.model.AppKeyEvent;
import xyz.imxqd.mediacontroller.service.NotificationCollectorService;
import xyz.imxqd.mediacontroller.ui.KeyEventActivity;
import xyz.imxqd.mediacontroller.ui.adapters.KeyEventMapAdapter;
import xyz.imxqd.mediacontroller.utils.Constants;
import xyz.imxqd.mediacontroller.utils.NotificationAccessUtil;
import xyz.imxqd.mediacontroller.utils.ScreenUtl;


public class KeyEventMapFragment extends Fragment {

    private static final int REQUEST_CODE_ADD_KEY_EVENT = 1;
    private volatile static KeyEventMapFragment mInstance;

    @BindView(android.R.id.list)
    RecyclerView vList;

    KeyEventMapAdapter mAdapter;

    public KeyEventMapFragment() {
        // Required empty public constructor
    }

    public static KeyEventMapFragment getInstance() {
        if (mInstance == null ) {
            synchronized (KeyEventMapFragment.class) {
                mInstance = new KeyEventMapFragment();
            }
        }
        return mInstance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_key_event_map, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new KeyEventMapAdapter();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mInstance = null;
    }

    @OnClick(R.id.action_add)
    public void onAddClick() {
        Intent intent = new Intent(getActivity(), KeyEventActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
