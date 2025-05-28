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

package com.damienwesterman.defensedrill.ui.util;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

/**
 * Static utilities for the UI layer.
 */
public class UiUtils {
    /**
     * No need to have instances of this class.
     */
    private UiUtils() { }

    /**
     * Create and display a snackbar. The snackbar will have a single "OK" button that dismisses the
     * snackbar.
     *
     * @param rootView  View object to display the snackbar in.
     * @param message   String message to display to the user.
     */
    public static void displayDismissibleSnackbar(@NonNull View rootView, @NonNull String message) {
        Snackbar snackbar = Snackbar.make(rootView,
                message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("OK", (callingView) -> snackbar.dismiss());
        snackbar.show();
    }
}
