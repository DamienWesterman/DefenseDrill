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
    @Query("SELECT * FROM " + DrillEntity.TABLE_NAME + " ORDER BY name")
    List<Drill> getAllDrills();

    @Transaction
    @Query(
            "SELECT drill.* FROM " + DrillEntity.TABLE_NAME + " AS drill " +
            "JOIN " + DrillCategoryJoinEntity.TABLE_NAME + " AS drillcatJoin ON drill.id = drillcatJoin.drill_id " +
            "JOIN " + CategoryEntity.TABLE_NAME + " AS cat ON drillcatJoin.category_id = cat.id " +
            "WHERE cat.id = :categoryId ORDER BY drill.name"
    )
    List<Drill> findAllDrillsByCategory(long categoryId);

    @Transaction
    @Query(
            "SELECT drill.* FROM " + DrillEntity.TABLE_NAME + " AS drill " +
            "JOIN " + DrillSubCategoryJoinEntity.TABLE_NAME + " AS drillSubJoin ON drill.id = drillSubJoin.drill_id " +
            "JOIN " + SubCategoryEntity.TABLE_NAME + " AS sub ON drillSubJoin.sub_category_id = sub.id " +
            "WHERE sub.id = :subCategoryId ORDER BY drill.name"
    )
    List<Drill> findAllDrillsBySubCategory(long subCategoryId);

    @Transaction
    @Query(
            "SELECT drill.* FROM " + SubCategoryEntity.TABLE_NAME + " AS sub " +
            "JOIN " + DrillSubCategoryJoinEntity.TABLE_NAME + " AS drillSubJoin ON sub.id = drillSubJoin.sub_category_id " +
            "JOIN " + DrillEntity.TABLE_NAME + " AS drill ON drillSubJoin.drill_id = drill.id " +
            "JOIN " + DrillCategoryJoinEntity.TABLE_NAME + " AS drillCatJoin ON drill.id = drillCatJoin.drill_id " +
            "JOIN " + CategoryEntity.TABLE_NAME + " AS cat ON drillCatJoin.category_id = cat.id " +
            "WHERE cat.id = :categoryId AND sub.id = :subCategoryId ORDER BY drill.name"
    )
    List<Drill> findAllDrillsByCategoryAndSubCategory(long categoryId, long subCategoryId);

    @Transaction
    @Query("SELECT * FROM " + DrillEntity.TABLE_NAME + " WHERE id = :id")
    Drill findDrillById(long id);

    @Transaction
    @Query("SELECT * FROM " + DrillEntity.TABLE_NAME + " WHERE name = :name")
    Drill findDrillByName(String name);

    @Query("SELECT * FROM " + DrillCategoryJoinEntity.TABLE_NAME)
    List<DrillCategoryJoinEntity> getAllCategoryJoin();

    @Query("SELECT * FROM " + DrillCategoryJoinEntity.TABLE_NAME + " WHERE drill_id = :drillId")
    List<DrillCategoryJoinEntity> findAllCategoryJoinByDrillId(long drillId);

    @Query("SELECT * FROM " + DrillCategoryJoinEntity.TABLE_NAME + " WHERE category_id = :categoryId")
    List<DrillCategoryJoinEntity> findAllCategoryJoinByCategoryId(long categoryId);

    @Query("SELECT * FROM " + DrillSubCategoryJoinEntity.TABLE_NAME)
    List<DrillSubCategoryJoinEntity> getAllSubCategoryJoin();

    @Query("SELECT * FROM " + DrillSubCategoryJoinEntity.TABLE_NAME + " WHERE drill_id = :drillId")
    List<DrillSubCategoryJoinEntity> findAllSubCategoryJoinByDrillId(long drillId);

    @Query("SELECT * FROM " + DrillSubCategoryJoinEntity.TABLE_NAME + " WHERE sub_category_id = :subCategoryId")
    List<DrillSubCategoryJoinEntity> findAllSubCategoryJoinByCategoryId(long subCategoryId);

    // Allow DrillEntity inserts to throw if there is an issue. Join tables should just replace for
    // ease of insertions, as there are only two fields and no event observers, should not cause a
    // problem.
    @Insert
    long[] insert(DrillEntity... drills);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insert(DrillCategoryJoinEntity... entities);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insert(DrillSubCategoryJoinEntity... entities);

    @Update
    int update(DrillEntity... drills);
    @Update
    int update(DrillCategoryJoinEntity... entities);
    @Update
    int update(DrillSubCategoryJoinEntity... entities);

    @Delete
    void delete(DrillEntity... drills);
    @Delete
    void delete(DrillCategoryJoinEntity... entities);
    @Delete
    void delete(DrillSubCategoryJoinEntity... entities);
}
