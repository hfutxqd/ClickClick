package xyz.imxqd.clickclick.dao;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

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

    @Column(name = "order", defaultValue = "0")
    public int order = 0;

    public static List<DefinedFunction> getOrderedAll() {
        return new Select()
                .from(DefinedFunction.class)
                .orderBy(DefinedFunction_Table.order, true)
                .orderBy(DefinedFunction_Table.id, false)
                .queryList();
    }

    @Override
    public boolean delete() {
        SQLite.delete(KeyMappingEvent.class)
                .where(KeyMappingEvent_Table.func_id.eq(id))
                .execute();
        return super.delete();
    }

    public static boolean checkHas(String body) {
        return new Select()
                .from(DefinedFunction.class)
                .where(DefinedFunction_Table.body.eq(body))
                .querySingle() != null;
    }
}
