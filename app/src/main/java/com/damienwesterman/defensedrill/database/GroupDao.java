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

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
/* package-private */ interface GroupDao {
    @Query("SELECT * FROM " + GroupEntity.TABLE_NAME)
    List<GroupEntity> getAll();

    @Query("SELECT * FROM " + GroupEntity.TABLE_NAME + " WHERE id = :id")
    GroupEntity findById(long id);

    @Query("SELECT * FROM " + GroupEntity.TABLE_NAME + " WHERE name = :name")
    GroupEntity findByName(String name);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insert(GroupEntity... groups);

    @Update
    void update(GroupEntity... groups);

    @Delete
    void delete(GroupEntity... groups);
}
