package xyz.imxqd.clickclick.dao;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.structure.BaseModel;

@Table(name = "defined_function", database = AppDatabase.class)
public class DefinedFunction extends BaseModel {

    @PrimaryKey(autoincrement = true)
    public long id;

    @NotNull
    @Unique
    @Column(name = "name")
    public String name;

    @NotNull
    @Unique
    @Column(name = "body")
    public String body;

    @NotNull
    @Column(name = "description")
    public String description;
}
