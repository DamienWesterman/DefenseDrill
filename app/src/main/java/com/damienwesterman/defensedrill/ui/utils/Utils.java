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

package com.damienwesterman.defensedrill.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.util.ServerHealthRepo;
import com.google.android.material.snackbar.Snackbar;

/**
 * Static utilities for the UI layer.
 */
public class Utils {
    /**
     * No need to have instances of this class.
     */
    private Utils() { }

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
