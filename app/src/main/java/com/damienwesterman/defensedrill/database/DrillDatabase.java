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

import androidx.room.RoomDatabase;

/**
 * TODO: Doc comments
 */
public abstract class DrillDatabase extends RoomDatabase {
    private static DrillDatabase instance;

    /**
     * Get running DrillRepository instance.
     *
     * @param context Application context.
     * @return DrillRepository instance.
     */
    public static DrillDatabase getInstance(Context context) {
        // TODO: implements
        return instance;
    }
}
