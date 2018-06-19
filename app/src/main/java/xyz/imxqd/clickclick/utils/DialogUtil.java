package xyz.imxqd.clickclick.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class DialogUtil {

    public static <T> void showList(Context context, List<T> list, OnItemClickListener<T> listener) {
        ArrayAdapter<T>  adapter =  new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, list);
        new AlertDialog.Builder(context)
                .setAdapter(adapter, (dialog, which) -> {
                    if (listener != null) {
                        listener.onItemClick(which, list.get(which));
                    }
                })
                .show();
    }

    public interface OnItemClickListener<T> {
        void onItemClick(int pos, T item);
    }
}
