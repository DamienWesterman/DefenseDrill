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

// TODO Doc comments
public class Utils {
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
