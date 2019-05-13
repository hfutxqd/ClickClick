package xyz.imxqd.clickclick.ui.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.disposables.Disposable;
import xyz.imxqd.clickclick.App;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.DefinedFunction;
import xyz.imxqd.clickclick.dao.KeyMappingEvent;
import xyz.imxqd.clickclick.model.AppEventManager;
import xyz.imxqd.clickclick.ui.FunctionsActivity;
import xyz.imxqd.clickclick.utils.DialogUtil;

/**
 * Created by imxqd on 2017/11/26.
 */

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.KeyMapHolder> {

    private List<KeyMappingEvent> mEvents;
    private ProfileChangeCallback mCallback;
    private ObservableEmitter<Object> savePositionEmitter;

    private Disposable mDisposable = null;

    @SuppressLint("CheckResult")
    public ProfileAdapter() {
        mEvents = KeyMappingEvent.getOrderedAll();
        mDisposable = Observable.create(emitter -> savePositionEmitter = emitter).debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    for (int i = 0; i < mEvents.size(); i++) {
                        mEvents.get(i).order = i + 1;
                        mEvents.get(i).save();
                    }
                });
    }

    public void setCheckChangeCallback(ProfileChangeCallback callback) {
        mCallback = callback;
    }

    public int getEnableCount() {
        int count = 0;
        for (KeyMappingEvent e : mEvents) {
            if (e.enable) {
                count++;
            }
        }
        return count;
    }

    public KeyMappingEvent getItem(int pos) {
        return mEvents.get(pos);
    }

    public void refreshData() {
        List<KeyMappingEvent> events = KeyMappingEvent.getOrderedAll();
        mEvents.clear();
        mEvents.addAll(events);
    }

    public void destroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    public void swap(int index1, int index2) {
        Collections.swap(mEvents, index1, index2);
    }

    public void savePosition() {
        if (savePositionEmitter != null) {
            savePositionEmitter.onNext(new Object());
        }
    }

    @Override
    public ProfileAdapter.KeyMapHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_profile, parent, false);
        return new KeyMapHolder(v);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ProfileAdapter.KeyMapHolder holder, int position) {
        KeyMappingEvent event = mEvents.get(position);
        holder.title.setText(event.funcName);
        if (event.ignoreDevice) {
            holder.subTitle.setText(event.keyName + "  " + event.eventType.getName());
        } else {
            holder.subTitle.setText(App.get().getResources().getString(R.string.key_device_name, event.deviceName) + "\n" + event.keyName + "  " + event.eventType.getName());
        }
        holder.enable.setChecked(event.enable);
        holder.deleteAlpha(0f);
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }

    public class KeyMapHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.profile_drag_handle)
        ImageView handle;
        @BindView(R.id.profile_title)
        TextView title;
        @BindView(R.id.profile_sub_title)
        TextView subTitle;
        @BindView(R.id.profile_switch)
        SwitchCompat enable;
        @BindView(R.id.profile_delete_hint)
        TextView deleteHint;
        @BindView(R.id.profile_item_content)
        View content;
        @SuppressLint("ClickableViewAccessibility")
        public KeyMapHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            enable.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int pos = getAdapterPosition();
                mEvents.get(pos).enable = isChecked;
                mEvents.get(pos).async().save();
                if (mCallback != null) {
                    mCallback.onCheckedChanged(isChecked);
                }
            });
            handle.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mCallback != null) {
                        mCallback.onStartDrag(KeyMapHolder.this);
                    }
                }
                return true;
            });
            itemView.setOnClickListener(v -> {
                List<String> list = new ArrayList<>();
                list.add(App.get().getString(R.string.change_function));
                list.add(App.get().getString(R.string.delete));

                DialogUtil.showList(itemView.getContext(), list, (pos, item) -> {
                    switch (pos) {
                        case 0:
                            showFunctions();
                            break;
                        case 1:
                            int index = getAdapterPosition();
                            getItem(index).delete();
                            mEvents.remove(index);
                            notifyItemRemoved(index);
                            if (mCallback != null) {
                                mCallback.onCheckedChanged(false);
                            }
                            break;
                    }
                });
            });
        }


        private void showFunctions() {
            final FunctionSpinnerAdapter adapter = new FunctionSpinnerAdapter();
            new AlertDialog.Builder(itemView.getContext())
                    .setAdapter(adapter, (dialog, which) -> {
                        DefinedFunction function = (DefinedFunction) adapter.getItem(which);
                        if (function.id == -1) {
                            Intent intent = new Intent(itemView.getContext(), FunctionsActivity.class);
                            itemView.getContext().startActivity(intent);
                            AppEventManager.getInstance().refreshKeyMappingEvents();
                        } else {
                            KeyMappingEvent event = getItem(getAdapterPosition());
                            event.funcId = function.id;
                            event.funcName = function.name;
                            event.save();
                            notifyItemChanged(getAdapterPosition());
                            AppEventManager.getInstance().refreshKeyMappingEvents();
                        }

                    })
                    .show();
        }

        public void deleteAlpha(float alpha) {
            deleteHint.setAlpha(alpha);
            content.setAlpha(1 - alpha);
        }
    }

    public interface ProfileChangeCallback {
        void onCheckedChanged(boolean isChecked);
        void onStartDrag(RecyclerView.ViewHolder holder);
    }
}
