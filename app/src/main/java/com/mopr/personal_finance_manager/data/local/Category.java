package com.mopr.personal_finance_manager.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "categories")
public class Category implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;

    // INCOME or EXPENSE
    public String type;

    public int iconRes;

    public int colorRes;

    public boolean isSystem;

    public Integer parentId;

    public Category() {}

    public Category(String name, String type, int iconRes, int colorRes, boolean isSystem) {
        this.name = name;
        this.type = type;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
        this.isSystem = isSystem;
        this.parentId = null;
    }

    public Category(String name, String type, int iconRes, int colorRes, boolean isSystem, Integer parentId) {
        this.name = name;
        this.type = type;
        this.iconRes = iconRes;
        this.colorRes = colorRes;
        this.isSystem = isSystem;
        this.parentId = parentId;
    }
}
