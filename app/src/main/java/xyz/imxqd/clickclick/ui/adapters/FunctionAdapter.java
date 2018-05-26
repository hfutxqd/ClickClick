package xyz.imxqd.clickclick.ui.adapters;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.DefinedFunction;
import xyz.imxqd.clickclick.func.FunctionFactory;
import xyz.imxqd.clickclick.func.IFunction;

/**
 * Created by imxqd on 2017/11/26.
 */

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FunctionHolder> {

    List<DefinedFunction> mFuncList;
    EventCallback callback;

    ObservableEmitter<Object> savePositionEmitter;


    @SuppressLint("CheckResult")
    public FunctionAdapter() {
        mFuncList = DefinedFunction.getOrderedAll();
        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                savePositionEmitter = emitter;
            }
        }).debounce(800, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        for (int i = 0; i < mFuncList.size(); i++) {
                            mFuncList.get(i).order = i;
                            mFuncList.get(i).save();
                        }
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
        function.order = position;
    }

    @Override
    public int getItemCount() {
        return mFuncList.size();
    }

    public class FunctionHolder extends RecyclerView.ViewHolder implements DialogInterface.OnClickListener{

        @BindView(R.id.func_drag_handle)
        ImageView handle;
        @BindView(R.id.func_title)
        TextView title;
        @BindView(R.id.func_sub_title)
        TextView subTitle;

        public FunctionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(v.getContext())
                            .setTitle(mFuncList.get(getAdapterPosition()).name)
                            .setMessage(R.string.dialog_what_intent_message)
                            .setPositiveButton(R.string.run_it, FunctionHolder.this)
                            .setNegativeButton(R.string.delete, FunctionHolder.this)
                            .setNeutralButton(R.string.cancel, FunctionHolder.this)
                            .show();
                }
            });
            handle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (callback != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                        callback.onStartDrag(FunctionHolder.this);
                    } else {
                        return false;
                    }
                    return true;
                }
            });
        }


        @Override
        public void onClick(DialogInterface dialog, int which) {
            DefinedFunction f = mFuncList.get(getAdapterPosition());
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    IFunction function = FunctionFactory.getFuncById(f.id);
                    if (function != null) {
                        function.exec();
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    mFuncList.remove(f);
                    f.delete();
                    if (callback != null) {
                        callback.onDataChanged();
                    }
                    notifyItemRemoved(getAdapterPosition());
                    break;
                case DialogInterface.BUTTON_NEUTRAL:

                    break;
                default:
            }
        }
    }

    public interface EventCallback {
        void onStartDrag(FunctionHolder holder);
        void onDataChanged();
    }
}
