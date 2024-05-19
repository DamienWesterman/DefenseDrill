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
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Upsert;

import java.util.List;

@Dao
/* package-private */ interface DrillDao {
    @Transaction
    @Query("SELECT * FROM " + DrillEntity.TABLE_NAME)
    List<Drill> getAll();

    @Transaction
    @Query(
            "SELECT drill.* FROM " + DrillEntity.TABLE_NAME + " AS drill " +
            "JOIN " + DrillGroupJoinEntity.TABLE_NAME + " AS drillGroupJoin ON drill.id = drillGroupJoin.drill_id " +
            "JOIN " + GroupEntity.TABLE_NAME + " AS grp ON drillGroupJoin.group_id = grp.id " +
            "WHERE grp.id = :groupId"
    )
    List<Drill> findAllByGroup(long groupId);

    @Transaction
    @Query(
            "SELECT drill.* FROM " + DrillEntity.TABLE_NAME + " AS drill " +
            "JOIN " + DrillSubGroupJoinEntity.TABLE_NAME + " AS drillSubJoin ON drill.id = drillSubJoin.drill_id " +
            "JOIN " + SubGroupEntity.TABLE_NAME + " AS sub ON drillSubJoin.sub_group_id = sub.id " +
            "WHERE sub.id = :subGroupId"
    )
    List<Drill> findAllBySubGroup(long subGroupId);

    @Transaction
    @Query(
            "SELECT drill.* FROM " + SubGroupEntity.TABLE_NAME + " AS sub " +
            "JOIN " + DrillSubGroupJoinEntity.TABLE_NAME + " AS drillSubJoin ON sub.id = drillSubJoin.sub_group_id " +
            "JOIN " + DrillEntity.TABLE_NAME + " AS drill ON drillSubJoin.drill_id = drill.id " +
            "JOIN " + DrillGroupJoinEntity.TABLE_NAME + " AS drillGroupJoin ON drill.id = drillGroupJoin.drill_id " +
            "JOIN " + GroupEntity.TABLE_NAME + " AS grp ON drillGroupJoin.group_id = grp.id " +
            "WHERE grp.id = :groupId AND sub.id = :subGroupId"
    )
    List<Drill> findAllByGroupAndSubGroup(long groupId, long subGroupId);

    @Transaction
    @Query("SELECT * FROM " + DrillEntity.TABLE_NAME + " WHERE id = :id")
    Drill findById(long id);

    @Transaction
    @Query("SELECT * FROM " + DrillEntity.TABLE_NAME + " WHERE name = :name")
    Drill findByName(String name);

    @Upsert
    void upsert(DrillEntity... drills);
    @Upsert
    void upsert(DrillGroupJoinEntity... entities);
    @Upsert
    void upsert(DrillSubGroupJoinEntity... entities);

    @Delete
    void delete(DrillEntity... drills);
    @Delete
    void delete(DrillGroupJoinEntity... entities);
    @Delete
    void delete(DrillSubGroupJoinEntity... entities);
}
