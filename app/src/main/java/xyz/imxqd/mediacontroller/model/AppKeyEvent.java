package xyz.imxqd.mediacontroller.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by imxqd on 2017/11/25.
 */

public class AppKeyEvent implements Parcelable {
    public int mKeyCode;
    public int mDeviceId;
    public String mDeviceName;
    public boolean ignoreDevice = true;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mKeyCode);
        dest.writeInt(this.mDeviceId);
        dest.writeString(this.mDeviceName);
        dest.writeByte(this.ignoreDevice ? (byte) 1 : (byte) 0);
    }

    public AppKeyEvent() {
    }

    protected AppKeyEvent(Parcel in) {
        this.mKeyCode = in.readInt();
        this.mDeviceId = in.readInt();
        this.mDeviceName = in.readString();
        this.ignoreDevice = in.readByte() != 0;
    }

    public static final Parcelable.Creator<AppKeyEvent> CREATOR = new Parcelable.Creator<AppKeyEvent>() {
        @Override
        public AppKeyEvent createFromParcel(Parcel source) {
            return new AppKeyEvent(source);
        }

        @Override
        public AppKeyEvent[] newArray(int size) {
            return new AppKeyEvent[size];
        }
    };
}
