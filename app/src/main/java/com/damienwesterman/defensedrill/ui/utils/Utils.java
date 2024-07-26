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

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

/**
 * Static utilities for the UI layer.
 */
public class Utils {
    /**
     * Create and display a snackbar. The snackbar will have a single "OK" button that dismisses the
     * snackbar.
     *
     * @param rootView  View object to display the snackbar in.
     * @param message   String message to display to the user.
     */
    public static void displayDismissibleSnackbar(View rootView, String message) {
        if (null == rootView || null == message) {
            return;
        }

        Snackbar snackbar = Snackbar.make(rootView,
                message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", (callingView) -> snackbar.dismiss());
        snackbar.show();
    }
}
