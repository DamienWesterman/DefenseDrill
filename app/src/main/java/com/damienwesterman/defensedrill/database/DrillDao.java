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
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
/* package-private */ interface DrillDao {
    @Transaction
    @Query("SELECT * FROM " + DrillEntity.TABLE_NAME)
    List<Drill> getAllDrills();

    @Transaction
    @Query(
            "SELECT drill.* FROM " + DrillEntity.TABLE_NAME + " AS drill " +
            "JOIN " + DrillGroupJoinEntity.TABLE_NAME + " AS drillGroupJoin ON drill.id = drillGroupJoin.drill_id " +
            "JOIN " + GroupEntity.TABLE_NAME + " AS grp ON drillGroupJoin.group_id = grp.id " +
            "WHERE grp.id = :groupId"
    )
    List<Drill> findAllDrillsByGroup(long groupId);

    @Transaction
    @Query(
            "SELECT drill.* FROM " + DrillEntity.TABLE_NAME + " AS drill " +
            "JOIN " + DrillSubGroupJoinEntity.TABLE_NAME + " AS drillSubJoin ON drill.id = drillSubJoin.drill_id " +
            "JOIN " + SubGroupEntity.TABLE_NAME + " AS sub ON drillSubJoin.sub_group_id = sub.id " +
            "WHERE sub.id = :subGroupId"
    )
    List<Drill> findAllDrillsBySubGroup(long subGroupId);

    @Transaction
    @Query(
            "SELECT drill.* FROM " + SubGroupEntity.TABLE_NAME + " AS sub " +
            "JOIN " + DrillSubGroupJoinEntity.TABLE_NAME + " AS drillSubJoin ON sub.id = drillSubJoin.sub_group_id " +
            "JOIN " + DrillEntity.TABLE_NAME + " AS drill ON drillSubJoin.drill_id = drill.id " +
            "JOIN " + DrillGroupJoinEntity.TABLE_NAME + " AS drillGroupJoin ON drill.id = drillGroupJoin.drill_id " +
            "JOIN " + GroupEntity.TABLE_NAME + " AS grp ON drillGroupJoin.group_id = grp.id " +
            "WHERE grp.id = :groupId AND sub.id = :subGroupId"
    )
    List<Drill> findAllDrillsByGroupAndSubGroup(long groupId, long subGroupId);

    @Transaction
    @Query("SELECT * FROM " + DrillEntity.TABLE_NAME + " WHERE id = :id")
    Drill findDrillById(long id);

    @Transaction
    @Query("SELECT * FROM " + DrillEntity.TABLE_NAME + " WHERE name = :name")
    Drill findDrillByName(String name);

    @Query("SELECT * FROM " + DrillGroupJoinEntity.TABLE_NAME)
    List<DrillGroupJoinEntity> getAllGroupJoin();

    @Query("SELECT * FROM " + DrillGroupJoinEntity.TABLE_NAME + " WHERE drill_id = :drillId")
    List<DrillGroupJoinEntity> findAllGroupJoinByDrillId(long drillId);

    @Query("SELECT * FROM " + DrillGroupJoinEntity.TABLE_NAME + " WHERE group_id = :groupId")
    List<DrillGroupJoinEntity> findAllGroupJoinByGroupId(long groupId);

    @Query("SELECT * FROM " + DrillSubGroupJoinEntity.TABLE_NAME)
    List<DrillSubGroupJoinEntity> getAllSubGroupJoin();

    @Query("SELECT * FROM " + DrillSubGroupJoinEntity.TABLE_NAME + " WHERE drill_id = :drillId")
    List<DrillSubGroupJoinEntity> findAllSubGroupJoinByDrillId(long drillId);

    @Query("SELECT * FROM " + DrillSubGroupJoinEntity.TABLE_NAME + " WHERE sub_group_id = :subGroupId")
    List<DrillSubGroupJoinEntity> findAllSubGroupJoinByGroupId(long subGroupId);

    // Allow DrillEntity inserts to throw if there is an issue. Join tables should just replace for
    // ease of insertions, as there are only two fields and no event observers, should not cause a
    // problem.
    @Insert
    long[] insert(DrillEntity... drills);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insert(DrillGroupJoinEntity... entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insert(DrillSubGroupJoinEntity... entities);

    @Update
    int update(DrillEntity... drills);
    @Update
    int update(DrillGroupJoinEntity... entities);
    @Update
    int update(DrillSubGroupJoinEntity... entities);

    @Delete
    void delete(DrillEntity... drills);
    @Delete
    void delete(DrillGroupJoinEntity... entities);
    @Delete
    void delete(DrillSubGroupJoinEntity... entities);
}
