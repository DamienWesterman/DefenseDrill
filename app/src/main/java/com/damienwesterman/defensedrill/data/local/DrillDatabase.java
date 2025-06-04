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

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;


/**
 * Database class to access all the DAOs. Should only be used internally, database interaction
 * should be done through {@link DrillRepository}.
 */
@Database(entities = {
        DrillEntity.class,
        CategoryEntity.class,
        SubCategoryEntity.class,
        DrillCategoryJoinEntity.class,
        DrillSubCategoryJoinEntity.class,
        WeeklyHourPolicyEntity.class
}, version = 1, exportSchema = false)
/* package-private */ abstract class DrillDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "drill_database";

    /**
     * Build the DrillDatabase object.
     *
     * @param applicationContext    Application Context.
     * @return                      DrillDatabase Object.
     */
    /* package-private */ static DrillDatabase instantiate(Context applicationContext) {
        return Room.databaseBuilder(applicationContext, DrillDatabase.class,
                DATABASE_NAME).build();
    }

    /* package-private */ abstract DrillDao getDrillDao();
    /* package-private */ abstract CategoryDao getCategoryDao();
    /* package-private */ abstract SubCategoryDao getSubCategoryDao();
    /* package-private */ abstract WeeklyHourPolicyDao getWeeklyHourPolicyDao();
}
