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
 * TODO: Doc comments
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
