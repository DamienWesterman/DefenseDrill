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

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
/* package-private */ interface SubCategoryDao {
    @Query("SELECT * FROM " + SubCategoryEntity.TABLE_NAME + " ORDER BY name")
    List<SubCategoryEntity> getAll();

    @Query(
            "SELECT sub.* FROM " + SubCategoryEntity.TABLE_NAME + " AS sub " +
            "JOIN " + DrillSubCategoryJoinEntity.TABLE_NAME + " AS drillSubJoin ON sub.id = drillSubJoin.sub_category_id " +
            "JOIN " + DrillEntity.TABLE_NAME + " AS drill ON drillSubJoin.drill_id = drill.id " +
            "JOIN " + DrillCategoryJoinEntity.TABLE_NAME + " AS drillCatJoin ON drill.id = drillCatJoin.drill_id " +
            "JOIN " + CategoryEntity.TABLE_NAME + " AS cat ON drillCatJoin.category_id = cat.id " +
            "WHERE cat.id = :categoryId ORDER BY sub.name"
    )
    List<SubCategoryEntity> findAllByCategory(long categoryId);

    @Query("SELECT * FROM " + SubCategoryEntity.TABLE_NAME + " WHERE id = :id")
    SubCategoryEntity findById(long id);

    @Query("SELECT * FROM " + SubCategoryEntity.TABLE_NAME + " WHERE name = :name")
    SubCategoryEntity findByName(String name);

    @Insert
    long[] insert(SubCategoryEntity... subCategories);

    @Update
    int update(SubCategoryEntity... subCategories);

    @Delete
    void delete(SubCategoryEntity... subCategories);
}
