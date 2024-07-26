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
    void onSuccess();
    void onFailure(String msg);
}
