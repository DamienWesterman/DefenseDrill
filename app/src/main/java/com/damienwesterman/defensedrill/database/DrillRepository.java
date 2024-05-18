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

/**
 * This class is used to interact with the SQLite database.
 * <br>
 * Call {@link DrillRepository#getInstance(Context)} to use the repo.
 * <br><br>
 * All methods are synchronized, and thus all calls are thread safe.
 */
public class DrillRepository {
    private static DrillRepository instance;
    private Context context;

    /**
     * Private constructor, access class with {@link #getInstance(Context context)}.
     */
    private DrillRepository() { };

    /**
     * Get running DrillRepository instance.
     *
     * @param context Application context.
     * @return DrillRepository instance.
     */
    public static DrillRepository getInstance(Context context) {
        if ( null == instance) {
            instance = new DrillRepository();
            instance.context = context.getApplicationContext();
        }

        return instance;
    }

    // TODO: Make sure everything is synchronized
}
