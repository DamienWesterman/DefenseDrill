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

package com.damienwesterman.defensedrill.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

/**
 * Join table entity for the {@link DrillEntity} and {@link SubCategoryEntity} entities. This should
 * only be used by RoomDB internally.
 */
@Entity(
        tableName = DrillSubCategoryJoinEntity.TABLE_NAME,
        primaryKeys = {"drill_id", "sub_category_id"},
        foreignKeys = {
                @ForeignKey(entity = DrillEntity.class, parentColumns = "id",
                        childColumns = "drill_id", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE),
                @ForeignKey(entity = SubCategoryEntity.class, parentColumns = "id",
                        childColumns = "sub_category_id", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"drill_id"}),
                @Index(value = {"sub_category_id"})
        }
)
/* package-private */ class DrillSubCategoryJoinEntity {
    @Ignore
    public static final String TABLE_NAME = "drill_sub_category_join";

    @ColumnInfo(name = "drill_id")
    private long drillId;
    @ColumnInfo(name = "sub_category_id")
    private long subCategoryId;

    public DrillSubCategoryJoinEntity(long drillId, long subCategoryId) {
        this.drillId = drillId;
        this.subCategoryId = subCategoryId;
    }

    public long getDrillId() {
        return drillId;
    }

    public void setDrillId(long drillId) {
        this.drillId = drillId;
    }

    public long getSubCategoryId() {
        return subCategoryId;
    }

    public void setSubCategoryId(long subCategoryId) {
        this.subCategoryId = subCategoryId;
    }
}
