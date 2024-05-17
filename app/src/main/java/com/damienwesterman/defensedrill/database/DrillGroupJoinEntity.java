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
        tableName = DrillGroupJoinEntity.TABLE_NAME,
        primaryKeys = {"drill_id", "group_id"},
        foreignKeys = {
                @ForeignKey(entity = DrillEntity.class, parentColumns = "id",
                        childColumns = "drill_id", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE),
                @ForeignKey(entity = GroupEntity.class, parentColumns = "id",
                        childColumns = "group_id", onDelete = ForeignKey.CASCADE,
                        onUpdate = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = {"drill_id"}),
                @Index(value = {"group_id"})
        }
)
public class DrillGroupJoinEntity {
    @Ignore
    public static final String TABLE_NAME = "drill_sub_group_join";

    @ColumnInfo(name = "drill_id")
    private long drillId;
    @ColumnInfo(name = "group_id")
    private long groupId;

    public DrillGroupJoinEntity(long drillId, long groupId) {
        this.drillId = drillId;
        this.groupId = groupId;
    }

    public long getDrillId() {
        return drillId;
    }

    public void setDrillId(long drillId) {
        this.drillId = drillId;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }
}
