package xyz.imxqd.clickclick.dao;

import android.os.Parcel;
import android.os.Parcelable;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.annotation.UniqueGroup;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

import xyz.imxqd.clickclick.model.AppKeyEventType;

@Table(name = "key_mapping_event", database = AppDatabase.class, uniqueColumnGroups = {@UniqueGroup(groupNumber = 1)})
public class KeyMappingEvent extends BaseModel implements Parcelable {

    @PrimaryKey(autoincrement = true)
    public long id;

    @NotNull
    @Unique(unique = false, uniqueGroups = 1)
    @Column(name = "key_code")
    public int keyCode;

    @NotNull
    @Column(name = "key_name")
    public String keyName;

    @Column(name = "device_id")
    public int deviceId;

    @Column(name = "device_name")
    public String deviceName;

    @Column(name = "ignore_device")
    public boolean ignoreDevice;

    @NotNull
    @Unique(unique = false, uniqueGroups = 1)
    @Column(name = "event_type")
    public AppKeyEventType eventType;

    @NotNull
    @Column(name = "func_id")
    public long funcId;

    @NotNull
    @Column(name = "func_name")
    public String funcName;

    @NotNull
    @Column(name = "enable")
    public boolean enable = true;

    public static List<KeyMappingEvent> getEnabledItems() {
        return new Select().from(KeyMappingEvent.class)
                .where(KeyMappingEvent_Table.enable.eq(true))
                .queryList();
    }

    public static void deleteById(long id) {
        new Delete().from(KeyMappingEvent.class)
                .where(KeyMappingEvent_Table.id.eq(id))
                .execute();
    }

    public KeyMappingEvent() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.keyCode);
        dest.writeString(this.keyName);
        dest.writeInt(this.deviceId);
        dest.writeString(this.deviceName);
        dest.writeByte(this.ignoreDevice ? (byte) 1 : (byte) 0);
        dest.writeInt(this.eventType == null ? -1 : this.eventType.ordinal());
        dest.writeLong(this.funcId);
        dest.writeString(this.funcName);
        dest.writeByte(this.enable ? (byte) 1 : (byte) 0);
    }

    protected KeyMappingEvent(Parcel in) {
        this.id = in.readLong();
        this.keyCode = in.readInt();
        this.keyName = in.readString();
        this.deviceId = in.readInt();
        this.deviceName = in.readString();
        this.ignoreDevice = in.readByte() != 0;
        int tmpEventType = in.readInt();
        this.eventType = tmpEventType == -1 ? null : AppKeyEventType.values()[tmpEventType];
        this.funcId = in.readLong();
        this.funcName = in.readString();
        this.enable = in.readByte() != 0;
    }

    public static final Creator<KeyMappingEvent> CREATOR = new Creator<KeyMappingEvent>() {
        @Override
        public KeyMappingEvent createFromParcel(Parcel source) {
            return new KeyMappingEvent(source);
        }

        @Override
        public KeyMappingEvent[] newArray(int size) {
            return new KeyMappingEvent[size];
        }
    };
}
