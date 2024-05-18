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

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(indices = {@Index(value = {"name"}, unique = true)}, tableName = GroupEntity.TABLE_NAME)
public class GroupEntity extends AbstractGroupEntity {
    @Ignore
    public static final String TABLE_NAME = "drill_group"; // "group" is reserved word in SQL
}
