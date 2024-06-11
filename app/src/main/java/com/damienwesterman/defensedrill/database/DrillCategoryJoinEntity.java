/****************************\
 *      ________________      *
 *     /  _             \     *
 *     \   \ |\   _  \  /     *
 *      \  / | \ / \  \/      *
 *      /  \ | / | /  /\      *
 *     /  _/ |/  \__ /  \     *
 *     \________________/     *
 *                            *
 \****************************/

package com.damienwesterman.defensedrill.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

/**
 * Join table entity for the {@link DrillEntity} and {@link CategoryEntity} entities. This should only
 * be used by RoomDB internally.
 */
@Entity(
        tableName = DrillCategoryJoinEntity.TABLE_NAME,
        primaryKeys = {"drill_id", "category_id"},
        foreignKeys = {
                @ForeignKey(entity = DrillEntity.class, parentColumns = "id",
                        childColumns = "drill_id", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE),
                @ForeignKey(entity = CategoryEntity.class, parentColumns = "id",
                        childColumns = "category_id", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"drill_id"}),
                @Index(value = {"category_id"})
        }
)
/* package-private */ class DrillCategoryJoinEntity {
    @Ignore
    public static final String TABLE_NAME = "drill_category_join";

    @ColumnInfo(name = "drill_id")
    private long drillId;
    @ColumnInfo(name = "category_id")
    private long categoryId;

    public DrillCategoryJoinEntity(long drillId, long categoryId) {
        this.drillId = drillId;
        this.categoryId = categoryId;
    }

    public long getDrillId() {
        return drillId;
    }

    public void setDrillId(long drillId) {
        this.drillId = drillId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }
}
