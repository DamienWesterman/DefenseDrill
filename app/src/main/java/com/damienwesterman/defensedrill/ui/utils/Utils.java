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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
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

    // TODO: doc comments
    // TODO: If this is only called from one place, remove from Utils
    public static void displayServerSelectPopup(@NonNull Context context,
                                                @NonNull Activity activity,
                                                @Nullable OperationCompleteCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_server_url_popup, null);

        EditText urlText = dialogView.findViewById(R.id.urlText);
        ProgressBar progressBar = dialogView.findViewById(R.id.serverUrlProgressBar);
        urlText.setText(SharedPrefs.getInstance(context).getServerUrl());

        builder.setView(dialogView);
        builder.setTitle("Enter Server URL");
        builder.setIcon(R.drawable.warning_icon); // TODO: Cloud icon
        builder.setCancelable(true);
        builder.setPositiveButton("Confirm",null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface ->
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    // Disable user input and show spinner
                    urlText.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);

                    // TODO: Try connecting and testing the field by trying to connect to actuator/up
                    // TODO: if it returns 200 then close dialog if callback is null, otherwise call success
                    // TODO: if fails, offer user to try again and let them know server could be down,
                    // otherwise close if callback is null or call on fail
                    alert.dismiss();
                })
        );

        alert.show();
    }

    // TODO: doc comments
    // TODO: If this is only called from one place, remove from Utils
    public static void displayLoginPopup(@NonNull Context context,
                                         @Nullable OperationCompleteCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Insert Mock Database?");
        builder.setIcon(R.drawable.warning_icon); // TODO: Lock icon?
        builder.setMessage("This will delete ALL drills/ categories/ sub-categories " +
                "and replace them with some demo ones. Are you sure you want to do this?");
        builder.setPositiveButton("Do It.", (dialog, position) -> {
            // TODO: make input field inaccessible and show loading thing
            // TODO: Try to login
            // TODO: if we get a 401 (..or does it return with a redirect? Test this), then tell the
                // user that the credentials were invalid, ask them to try again (and re-enable
                // input fields) or close (in which case if null close dialog otherwise call fail
            // TODO: If we succeed, store the JWT and if callback null close otherwise call success
        });
        builder.setNegativeButton("Never-mind", null);
        builder.create().show();
    }
}
