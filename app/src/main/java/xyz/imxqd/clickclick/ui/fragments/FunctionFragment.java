package xyz.imxqd.clickclick.ui.fragments;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.func.InternalFunction;
import xyz.imxqd.clickclick.log.LogUtils;
import xyz.imxqd.clickclick.model.web.RemoteFunction;
import xyz.imxqd.clickclick.ui.AddFunctionActivity;
import xyz.imxqd.clickclick.ui.AppChooseActivity;
import xyz.imxqd.clickclick.ui.CodeEditorActivity;
import xyz.imxqd.clickclick.ui.CreateGestureActivity;
import xyz.imxqd.clickclick.ui.FunctionsActivity;
import xyz.imxqd.clickclick.ui.adapters.FunctionAdapter;
import xyz.imxqd.clickclick.utils.ScreenUtl;


public class FunctionFragment extends BaseFragment implements FunctionAdapter.EventCallback, OnRefreshUI{

    private static final int REQUEST_ADD_FUNC = 60001;
    private static final int REQUEST_ADD_SHORTCUT = 60002;
    private static final int REQUEST_CHOOSE_SHORTCUT = 60003;

    @BindView(android.R.id.list)
    RecyclerView vList;

    @BindView(R.id.function_state)
    TextView vState;

    @BindView(R.id.empty_view)
    FrameLayout mEmpty;

    FunctionAdapter mAdapter;
    ItemTouchHelper mItemTouchHelper;

    ArrayAdapter<String> mMenuAdapter;

    public FunctionFragment() {
        LogUtils.d("FunctionFragment new instance");
    }

    public static FunctionFragment newInstance() {
        return new FunctionFragment();
    }

    ItemTouchHelper.Callback mCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN|ItemTouchHelper.UP, 0) {

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            mAdapter.swap(fromPosition, toPosition);
            mAdapter.notifyItemMoved(fromPosition, toPosition);
            mAdapter.savePosition();
            return true;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
                ViewCompat.animate(viewHolder.itemView)
                        .setDuration(100)
                        .translationZ(15f)
                        .start();
            }

        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            ViewCompat.animate(viewHolder.itemView)
                    .translationZ(0f)
                    .setDuration(150)
                    .start();

        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }


    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_function, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        assert getActivity() != null;
        assert getContext() != null;

        mAdapter = new FunctionAdapter();
        List<String> list = new ArrayList<>();
        list.add(getString(R.string.add_internal_func));
        list.add(getString(R.string.add_shortcut));
        list.add(getString(R.string.open_application));
        list.add(getString(R.string.add_notification_func));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            list.add(getString(R.string.add_tap_or_swipe));
        }
        list.add(getString(R.string.add_func_by_self));
        list.add(getString(R.string.add_lua_script));
        mMenuAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);
        mAdapter.setOnStartDragCallback(this);
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
        vList.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        mItemTouchHelper = new ItemTouchHelper(mCallback);
        mItemTouchHelper.attachToRecyclerView(vList);
        initStateText();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.destroy();
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void initStateText() {
        if (mAdapter == null || vState == null) {
            return;
        }
        int count = mAdapter.getItemCount();
        String state = getString(R.string.function_current_state, count);
        CharSequence text = getBigNumberText(state);
        vState.setText(text);
        if (count == 0) {
            mEmpty.setVisibility(View.VISIBLE);
        } else {
            mEmpty.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.action_add)
    public void onAddClick() {
        new AlertDialog.Builder(getContext())
                .setAdapter(mMenuAdapter, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            startFunctionsActivity();
                            break;
                        case 1:
                            startAddShortcut();
                            break;
                        case 2:
                            startAddApplication();
                            break;
                        case 3:
                            addNotificationFunc();
                            break;
                        case 4:
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                createGesture();
                            } else {
                                startAddFuncActivity();
                            }
                            break;
                        case 5:
                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                startAddFuncActivity();
                            } else {
                                startCodeEditor();
                            }
                            break;
                        case 6:
                            startCodeEditor();
                            break;
                         default:
                    }
                })
                .show();
    }

    public void startCodeEditor() {
        startActivity(new Intent(getActivity(), CodeEditorActivity.class));
    }

    public void createGesture() {
        startActivity(new Intent(getActivity(), CreateGestureActivity.class));
    }

    public void startAddShortcut() {
        Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.add_shortcut));
        try {
            startActivityForResult(intent, REQUEST_ADD_SHORTCUT);
        } catch (Throwable e) {
            App.get().showToast(R.string.no_app_shortcut_available);
        }
    }

    public void startAddApplication() {
        startActivity(new Intent(getActivity(), AppChooseActivity.class));
    }

    public void addNotificationFunc() {
        InternalFunction f = new InternalFunction("internal:notify_helper()");
        f.exec();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_ADD_FUNC) {
                mAdapter.refreshData();
                mAdapter.notifyDataSetChanged();
                mAdapter.savePosition();
            } else if (requestCode == REQUEST_ADD_SHORTCUT && data != null) {
                try {
                    startActivityForResult(data, REQUEST_CHOOSE_SHORTCUT);
                } catch (Throwable t) {
                    App.get().showToast(getString(R.string.create_shortcut_failed), false);
                    LogUtils.e(t.toString());
                }
            } else if (requestCode == REQUEST_CHOOSE_SHORTCUT && data != null) {
                String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                Intent i = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                try {
                    RemoteFunction f = new RemoteFunction();
                    f.name = name;
                    f.description = getString(R.string.open) + name;
                    f.body = "action:intent://" + i.toUri(0);
                    f.updateTime = System.currentTimeMillis() / 1000;
                    AddFunctionActivity.start(f, true, getContext());
                } catch (Exception e) {
                    LogUtils.e(e.getMessage());
                    App.get().showToast(R.string.save_failed);
                }
            }

        }
    }

    private void startFunctionsActivity() {
        Intent intent = new Intent(getActivity(), FunctionsActivity.class);
        startActivityForResult(intent, REQUEST_ADD_FUNC);
    }

    private void startAddFuncActivity() {
        Intent intent = new Intent(getActivity(), AddFunctionActivity.class);
        startActivityForResult(intent, REQUEST_ADD_FUNC);
    }

    @Override
    public void onStartDrag(FunctionAdapter.FunctionHolder holder) {
        if (mItemTouchHelper != null) {
            mItemTouchHelper.startDrag(holder);
        }
    }

    @Override
    public void onDataChanged() {
        initStateText();
        if (mAdapter != null) {
            mAdapter.savePosition();
        }
    }

    @Override
    public void onRefreshUI() {
        if (mAdapter != null) {
            mAdapter.refreshData();
            mAdapter.notifyDataSetChanged();
            mAdapter.savePosition();
        }
    }
}
