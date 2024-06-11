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
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
/* package-private */ interface CategoryDao {
    @Query("SELECT * FROM " + CategoryEntity.TABLE_NAME + " ORDER BY name")
    List<CategoryEntity> getAll();

    @Query("SELECT * FROM " + CategoryEntity.TABLE_NAME + " WHERE id = :id")
    CategoryEntity findById(long id);

    @Query("SELECT * FROM " + CategoryEntity.TABLE_NAME + " WHERE name = :name")
    CategoryEntity findByName(String name);

    @Insert
    long[] insert(CategoryEntity... categories);

    @Update
    int update(CategoryEntity... categories);

    @Delete
    void delete(CategoryEntity... categories);
}
