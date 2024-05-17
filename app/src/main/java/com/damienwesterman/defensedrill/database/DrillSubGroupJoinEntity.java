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
 * TODO: Doc comments USED ONLY FOR ROOM DB
 */
@Entity(
        tableName = DrillSubGroupJoinEntity.TABLE_NAME,
        primaryKeys = {"drill_id", "sub_group_id"},
        foreignKeys = {
                @ForeignKey(entity = DrillEntity.class, parentColumns = "id",
                        childColumns = "drill_id", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE),
                @ForeignKey(entity = SubGroupEntity.class, parentColumns = "id",
                        childColumns = "sub_group_id", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"drill_id"}),
                @Index(value = {"sub_group_id"})
        }
)
public class DrillSubGroupJoinEntity {
    @Ignore
    public static final String TABLE_NAME = "drill_group_join";

    @ColumnInfo(name = "drill_id")
    private long drillId;
    @ColumnInfo(name = "sub_group_id")
    private long subGroupId;

    public DrillSubGroupJoinEntity(long drillId, long subGroupId) {
        this.drillId = drillId;
        this.subGroupId = subGroupId;
    }

    public long getDrillId() {
        return drillId;
    }

    public void setDrillId(long drillId) {
        this.drillId = drillId;
    }

    public long getSubGroupId() {
        return subGroupId;
    }

    public void setSubGroupId(long groupId) {
        this.subGroupId = groupId;
    }
}
