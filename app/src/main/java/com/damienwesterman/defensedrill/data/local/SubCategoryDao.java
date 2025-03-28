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
/*
 * Copyright 2024 Damien Westerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.damienwesterman.defensedrill.data.local;

import androidx.annotation.NonNull;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;
import java.util.Optional;

@Dao
/* package-private */ interface SubCategoryDao {
    @Query("SELECT * FROM " + SubCategoryEntity.TABLE_NAME + " ORDER BY name")
    @NonNull
    List<SubCategoryEntity> getAll();

    @Transaction
    @Query(
            "SELECT * FROM " + SubCategoryEntity.TABLE_NAME +
                    " WHERE name IN (:names) ORDER BY name"
    )
    @NonNull
    List<SubCategoryEntity> findAllByName(@NonNull List<String> names);

    @Query(
            "SELECT DISTINCT sub.* FROM " + SubCategoryEntity.TABLE_NAME + " AS sub " +
            "JOIN " + DrillSubCategoryJoinEntity.TABLE_NAME + " AS drillSubJoin ON sub.id = drillSubJoin.sub_category_id " +
            "JOIN " + DrillEntity.TABLE_NAME + " AS drill ON drillSubJoin.drill_id = drill.id " +
            "JOIN " + DrillCategoryJoinEntity.TABLE_NAME + " AS drillCatJoin ON drill.id = drillCatJoin.drill_id " +
            "JOIN " + CategoryEntity.TABLE_NAME + " AS cat ON drillCatJoin.category_id = cat.id " +
            "WHERE cat.id = :categoryId ORDER BY sub.name"
    )
    @NonNull
    List<SubCategoryEntity> findAllByCategory(long categoryId);

    @Query("SELECT * FROM " + SubCategoryEntity.TABLE_NAME + " WHERE id = :id")
    @NonNull
    Optional<SubCategoryEntity> findById(long id);

    @Query("SELECT * FROM " + SubCategoryEntity.TABLE_NAME + " WHERE name = :name")
    @NonNull
    Optional<SubCategoryEntity> findByName(@NonNull String name);

    @Insert
    long[] insert(SubCategoryEntity... subCategories);

    @Update
    int update(SubCategoryEntity... subCategories);

    @Delete
    void delete(SubCategoryEntity... subCategories);
}
