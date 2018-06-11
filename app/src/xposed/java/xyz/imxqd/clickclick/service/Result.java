package xyz.imxqd.clickclick.service;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Result implements Parcelable {
    public int what;
    public Bundle data;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.what);
        dest.writeBundle(this.data);
    }

    public Result() {
    }

    public Result(int what, Bundle data) {
        this.what = what;
        this.data = data;
    }

    protected Result(Parcel in) {
        this.what = in.readInt();
        this.data = in.readBundle();
    }

    public static final Parcelable.Creator<Result> CREATOR = new Parcelable.Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel source) {
            return new Result(source);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };
}
