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
        GroupEntity.class,
        SubGroupEntity.class,
        DrillGroupJoinEntity.class,
        DrillSubGroupJoinEntity.class
}, version = 1, exportSchema = false)
public abstract class DrillDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "drill_database";
    private static DrillDatabase instance;

    /**
     * Default constructor. Do not use. Access class with {@link #getInstance(Context context)}.
     */
    public DrillDatabase() { }

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
    public abstract GroupDao getGroupDao();
    public abstract SubGroupDao getSubGroupDao();
}
