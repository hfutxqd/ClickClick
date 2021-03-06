package xyz.imxqd.clickclick.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.raizlabs.android.dbflow.config.FlowManager;

import xyz.imxqd.clickclick.dao.DefinedFunction;

public class FunctionProvider extends ContentProvider {
    public FunctionProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not allowed");
    }

    @Override
    public String getType(Uri uri) {
        return "application/func";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not allowed");
    }

    @Override
    public boolean onCreate() {
        if (getContext() != null) {
            FlowManager.init(getContext());
        }

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return DefinedFunction.getOrderedCursor();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not allowed");
    }
}
