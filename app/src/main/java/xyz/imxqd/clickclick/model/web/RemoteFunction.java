package xyz.imxqd.clickclick.model.web;

import com.google.gson.annotations.SerializedName;

public class RemoteFunction {

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
}
