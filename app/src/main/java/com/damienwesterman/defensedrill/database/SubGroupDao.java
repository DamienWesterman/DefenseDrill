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
/* package-private */ interface SubGroupDao {
    @Query("SELECT * FROM " + SubGroupEntity.TABLE_NAME)
    List<SubGroupEntity> getAll();

    @Query(
            "SELECT sub.* FROM " + SubGroupEntity.TABLE_NAME + " AS sub " +
            "JOIN " + DrillSubGroupJoinEntity.TABLE_NAME + " AS drillSubJoin ON sub.id = drillSubJoin.sub_group_id " +
            "JOIN " + DrillEntity.TABLE_NAME + " AS drill ON drillSubJoin.drill_id = drill.id " +
            "JOIN " + DrillGroupJoinEntity.TABLE_NAME + " AS drillGroupJoin ON drill.id = drillGroupJoin.drill_id " +
            "JOIN " + GroupEntity.TABLE_NAME + " AS grp ON drillGroupJoin.group_id = grp.id " +
            "WHERE grp.id = :groupId"
    )
    List<SubGroupEntity> findAllByGroup(long groupId);

    @Query("SELECT * FROM " + SubGroupEntity.TABLE_NAME + " WHERE id = :id")
    SubGroupEntity findById(long id);

    @Query("SELECT * FROM " + SubGroupEntity.TABLE_NAME + " WHERE name = :name")
    SubGroupEntity findByName(String name);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insert(SubGroupEntity... groups);

    @Update
    void update(SubGroupEntity... groups);

    @Delete
    void delete(SubGroupEntity... groups);
}
