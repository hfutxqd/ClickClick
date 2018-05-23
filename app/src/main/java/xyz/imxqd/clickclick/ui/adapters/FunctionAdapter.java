package xyz.imxqd.clickclick.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import xyz.imxqd.clickclick.R;
import xyz.imxqd.clickclick.dao.DefinedFunction;
import xyz.imxqd.clickclick.dao.DefinedFunction_Table;

/**
 * Created by imxqd on 2017/11/26.
 */

public class FunctionAdapter extends RecyclerView.Adapter<FunctionAdapter.FunctionHolder> {

    List<DefinedFunction> mFuncList;

    public FunctionAdapter() {
        mFuncList = new Select()
                .from(DefinedFunction.class)
                .orderBy(DefinedFunction_Table.id, false)
                .queryList();
    }

    @Override
    public FunctionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_func, parent, false);
        return new FunctionHolder(v);
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

    static class FunctionHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.func_title)
        TextView title;
        @BindView(R.id.func_sub_title)
        TextView subTitle;

        public FunctionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
