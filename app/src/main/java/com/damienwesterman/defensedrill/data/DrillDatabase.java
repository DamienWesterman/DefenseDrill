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

package com.damienwesterman.defensedrill.data;

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
        DrillSubCategoryJoinEntity.class
}, version = 1, exportSchema = false)
/* package-private */ abstract class DrillDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "drill_database";
    private static DrillDatabase instance;

    /**
     * Default constructor. Do not use. Access class with {@link #getInstance(Context context)}.
     */
    protected DrillDatabase() { }

    /**
     * Get running DrillRepository instance.
     *
     * @param context Application context.
     * @return DrillRepository instance.
     */
    public synchronized static DrillDatabase getInstance(Context context) {
        if ( null == instance) {
            instance = Room.databaseBuilder(context.getApplicationContext(), DrillDatabase.class,
                    DATABASE_NAME).build();
        }

        return instance;
    }

    public abstract DrillDao getDrillDao();
    public abstract CategoryDao getCategoryDao();
    public abstract SubCategoryDao getSubCategoryDao();
}
