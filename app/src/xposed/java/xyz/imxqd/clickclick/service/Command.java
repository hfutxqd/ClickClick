package xyz.imxqd.clickclick.service;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class Command implements Parcelable {

    public static final int WHAT_HELLO = 0;


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

    public Command(int what, Bundle data) {
        this.what = what;
        this.data = data;
    }

    public Command() {
    }

    protected Command(Parcel in) {
        this.what = in.readInt();
        this.data = in.readBundle();
    }

    public static final Parcelable.Creator<Command> CREATOR = new Parcelable.Creator<Command>() {
        @Override
        public Command createFromParcel(Parcel source) {
            return new Command(source);
        }

        @Override
        public Command[] newArray(int size) {
            return new Command[size];
        }
    };
}
