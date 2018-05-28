package xyz.imxqd.clickclick.model.web;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import xyz.imxqd.clickclick.utils.KeyEventUtil;

public class RemoteFunction implements Parcelable {

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("body")
    public String body;

    @SerializedName("version_code")
    public int versionCode;

    @SerializedName("version_name")
    public String versionName;

    @SerializedName("author")
    public String author;

    @SerializedName("update_time")
    public long updateTime;

    public static void main(String ... args) {
        List<RemoteFunction> list = new ArrayList<>();
        for (int i = 1; i < 284; i++) {
            String name = KeyEventUtil.getKeyName(i);
            RemoteFunction f = new RemoteFunction();
            f.name = "按键" + name;
            f.author = "IMXQD";
            f.body = "keyevent:" + i;
            f.description = "按键" + name;
            f.updateTime = 1527414422000L;
            f.versionCode = 1;
            f.versionName = "1.0";
            list.add(f);
        }
        Gson gson = new Gson();
        String json = gson.toJson(list);
        System.out.println(json);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeString(this.body);
        dest.writeInt(this.versionCode);
        dest.writeString(this.versionName);
        dest.writeString(this.author);
        dest.writeLong(this.updateTime);
    }

    public RemoteFunction() {
    }

    protected RemoteFunction(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
        this.body = in.readString();
        this.versionCode = in.readInt();
        this.versionName = in.readString();
        this.author = in.readString();
        this.updateTime = in.readLong();
    }

    public static final Parcelable.Creator<RemoteFunction> CREATOR = new Parcelable.Creator<RemoteFunction>() {
        @Override
        public RemoteFunction createFromParcel(Parcel source) {
            return new RemoteFunction(source);
        }

        @Override
        public RemoteFunction[] newArray(int size) {
            return new RemoteFunction[size];
        }
    };
}
