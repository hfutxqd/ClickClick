package xyz.imxqd.clickclick.model.web;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import xyz.imxqd.clickclick.BuildConfig;
import xyz.imxqd.clickclick.utils.KeyEventUtil;
import xyz.imxqd.clickclick.utils.PackageUtil;

public class RemoteFunction implements Parcelable {

    public static class Dependency implements Parcelable {
        public static final String TYPE_API_LEVEL = "api_level";
        public static final String TYPE_APP_VER_CODE = "ver_code";
        public static final String TYPE_INSTALLED_PACKAGE = "installed_package";

        private static final String REGEX_INSTALLED_PACKAGE = "([a-zA-Z0-9_]+\\.{1})+[a-zA-Z0-9_]+:[0-9]+";
        private static final Pattern INSTALLED_PACKAGE_PATTERN = Pattern.compile(REGEX_INSTALLED_PACKAGE);

        public String type;
        public String value;
        public String message;

        public boolean check() {
            switch (type) {
                case TYPE_APP_VER_CODE:
                    int verCode = Integer.valueOf(value);
                    if (BuildConfig.VERSION_CODE < verCode) {
                        return false;
                    }
                    break;
                case TYPE_API_LEVEL:
                    int apiLevel = Integer.valueOf(value);
                    if (Build.VERSION.SDK_INT < apiLevel) {
                        return false;
                    }
                    break;
                case TYPE_INSTALLED_PACKAGE:
                    if (INSTALLED_PACKAGE_PATTERN.matcher(value).matches()) {
                        String[] data = value.split(":");
                        int versionCode = Integer.valueOf(data[1]);
                        int packageVerCode = PackageUtil.getPackageVersionCode(data[0]);
                        if (packageVerCode < versionCode) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                    break;
                default:
            }
            return true;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.type);
            dest.writeString(this.value);
            dest.writeString(this.message);
        }

        public Dependency() {
        }

        protected Dependency(Parcel in) {
            this.type = in.readString();
            this.value = in.readString();
            this.message = in.readString();
        }

        public static final Creator<Dependency> CREATOR = new Creator<Dependency>() {
            @Override
            public Dependency createFromParcel(Parcel source) {
                return new Dependency(source);
            }

            @Override
            public Dependency[] newArray(int size) {
                return new Dependency[size];
            }
        };
    }

    @SerializedName("name")
    public String name;

    @SerializedName("description")
    public String description;

    @SerializedName("body")
    public String body;

    @SerializedName("author")
    public String author;

    @SerializedName("update_time")
    public long updateTime;

    @SerializedName("dependencies")
    public List<Dependency> dependencies;


    public boolean checkDependencies() {
        if (dependencies == null || dependencies.size() == 0) {
            return true;
        }
        for (Dependency d : dependencies) {
            if (!d.check()) {
                return false;
            }
        }
        return true;
    }

    public List<String> getDependenciesMessages() {
        List<String> messages = new ArrayList<>();
        if (dependencies == null || dependencies.size() == 0) {
            return messages;
        }
        for (Dependency d : dependencies) {
            if (!d.check()) {
                messages.add(d.message);
            }
        }
        return messages;
    }

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
            list.add(f);
        }
        Gson gson = new Gson();
        String json = gson.toJson(list);
        System.out.println(json);
    }

    public RemoteFunction() {
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
        dest.writeString(this.author);
        dest.writeLong(this.updateTime);
        dest.writeList(this.dependencies);
    }

    protected RemoteFunction(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
        this.body = in.readString();
        this.author = in.readString();
        this.updateTime = in.readLong();
        this.dependencies = new ArrayList<Dependency>();
        in.readList(this.dependencies, Dependency.class.getClassLoader());
    }

    public static final Creator<RemoteFunction> CREATOR = new Creator<RemoteFunction>() {
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
