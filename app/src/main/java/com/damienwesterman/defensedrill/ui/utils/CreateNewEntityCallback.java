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

package com.damienwesterman.defensedrill.ui.utils;

/**
 * Generic callback for adding to the database, allowing the caller to define what happens if the
 * create operation succeeds or fails.
 */
public interface CreateNewEntityCallback {
    /**
     * Callback for when an object was successfully added to the database.
     */
    void onSuccess();

    /**
     * Callback for when an object failed to be added to the database.
     *
     * @param error Error message of what went wrong.
     */
    void onFailure(String error);
}
