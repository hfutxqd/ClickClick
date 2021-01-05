package xyz.imxqd.clickclick.ui.adapters;

import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
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
import xyz.imxqd.clickclick.MyApp;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.DefinedFunction;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;
import xyz.imxqd.clickclick.ui.AddFunctionActivity;
import xyz.imxqd.clickclick.utils.DialogUtil;
import xyz.imxqd.clickclick.utils.ShortcutUtil;

/**
 * Created by imxqd on 2017/11/26.
 */

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FunctionHolder> {

    private List<DefinedFunction> mFuncList;
    private EventCallback callback;

    private ObservableEmitter<Object> savePositionEmitter;

    private Disposable mDisposable = null;


    @SuppressLint("CheckResult")
    public FunctionAdapter() {
        mFuncList = DefinedFunction.getOrderedAll();
        mDisposable = Observable.create(emitter -> savePositionEmitter = emitter).debounce(400, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    for (int i = 0; i < mFuncList.size(); i++) {
                        mFuncList.get(i).order = i + 1;
                        mFuncList.get(i).save();
                    }
                });
    }

    public void refreshData() {
        mFuncList.clear();
        mFuncList.addAll(DefinedFunction.getOrderedAll());
        if (callback != null) {
            callback.onDataChanged();
        }
    }

    public void destroy() {
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }


    @Override
    public FunctionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_func, parent, false);
        return new FunctionHolder(v);
    }

    public void swap(int index1, int index2) {
        Collections.swap(mFuncList, index1, index2);
    }

    public void setOnStartDragCallback(EventCallback callback) {
        this.callback = callback;
    }

    public void savePosition() {
        if (savePositionEmitter != null) {
            savePositionEmitter.onNext(new Object());
        }

    }

    @Override
    public void onBindViewHolder(FunctionHolder holder, int position) {
        DefinedFunction function = mFuncList.get(position);
        holder.title.setText(function.name);
        holder.subTitle.setText(function.description);
    }

    @Override
    public int getItemCount() {
        return mFuncList.size();
    }

    public class FunctionHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.func_drag_handle)
        ImageView handle;
        @BindView(R.id.func_title)
        TextView title;
        @BindView(R.id.func_sub_title)
        TextView subTitle;

        @SuppressLint("ClickableViewAccessibility")
        public FunctionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> list = new ArrayList<>();
                    list.add(MyApp.get().getString(R.string.run_it));
                    list.add(MyApp.get().getString(R.string.add_to_home));
                    list.add(MyApp.get().getString(R.string.edit_or_view_it));
                    list.add(MyApp.get().getString(R.string.delete));


                    DialogUtil.showList(v.getContext(), list, new DialogUtil.OnItemClickListener<String>() {
                        @Override
                        public void onItemClick(int which, String item) {
                            int pos = getAdapterPosition();
                            if (pos < 0 || pos >= mFuncList.size()) {
                                return;
                            }
                            DefinedFunction f = mFuncList.get(pos);
                            switch (which) {
                                case 0:
                                    IFunction function = FunctionFactory.getFuncById(f.id);
                                    if (function != null && function.exec()) {
                                        MyApp.get().showToast(R.string.run_successed);
                                    } else if (function != null){
                                        MyApp.get().showToast(R.string.run_failed);
                                        MyApp.get().showToast(function.getErrorInfo().getMessage(), true, true);
                                    } else {
                                        MyApp.get().showToast(R.string.run_failed);
                                    }
                                    break;
                                case 1:
                                    ShortcutUtil.createRunFunc(f.id, f.name);
                                    MyApp.get().showToast(R.string.added_to_home);
                                    break;
                                case 2:
                                    AddFunctionActivity.start(f.id, true, v.getContext());
                                    break;
                                case 3:
                                    mFuncList.remove(f);
                                    f.delete();
                                    if (callback != null) {
                                        callback.onDataChanged();
                                    }
                                    notifyItemRemoved(getAdapterPosition());
                                    MyApp.get().post(MyApp.EVENT_WHAT_REFRESH_UI, null);
                                    break;
                                default:
                            }
                        }
                    });
                }
            });
            handle.setOnTouchListener((v, event) -> {
                if (callback != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                    callback.onStartDrag(FunctionHolder.this);
                } else {
                    return false;
                }
                return true;
            });
        }

    }

    public interface EventCallback {
        void onStartDrag(FunctionHolder holder);
        void onDataChanged();
    }
}
