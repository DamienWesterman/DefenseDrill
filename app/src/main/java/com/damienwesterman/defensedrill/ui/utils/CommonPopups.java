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
 * Copyright 2025 Damien Westerman
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
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.authentication.AuthRepo;
import com.damienwesterman.defensedrill.data.remote.dto.LoginDTO;
import com.damienwesterman.defensedrill.data.remote.util.ServerHealthRepo;

/**
 * Common Popups used in multiple different activities.
 */
public class CommonPopups {
    /**
     * No need to have instances of this class.
     */
    private CommonPopups() { }

    // TODO: If this is only called from one place, remove from Utils
    /**
     * Display a popup for the user to input the backend server URL, and if successful save it.
     *
     * @param context Context
     * @param activity Activity
     * @param callback Callback, only calls onSuccess() upon successful save, never calls onFailure()
     */
    public static void displayServerSelectPopup(@NonNull Context context,
                                                @NonNull Activity activity,
                                                @Nullable OperationCompleteCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_server_url_popup, null);

        EditText urlText = dialogView.findViewById(R.id.urlText);
        TextView errorMessage = dialogView.findViewById(R.id.serverErrorMessage);
        ProgressBar progressBar = dialogView.findViewById(R.id.serverUrlProgressBar);
        urlText.setText(SharedPrefs.getInstance(context).getServerUrl());

        builder.setView(dialogView);
        builder.setTitle("Enter Server URL");
        builder.setIcon(R.drawable.cloud_icon);
        builder.setCancelable(true);
        builder.setPositiveButton("Confirm",null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog alert = builder.create();
        // Customize PositiveButton functionality so it does not always close the dialog
        alert.setOnShowListener(dialogInterface ->
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    // Disable user input and show spinner
                    urlText.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    errorMessage.setVisibility(View.GONE);

                    String enteredUrl = urlText.getText().toString();
                    ServerHealthRepo.isServerHealthy(enteredUrl, new OperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            SharedPrefs.getInstance(context).setServerUrl(enteredUrl);
                            if (null != callback) {
                                callback.onSuccess();
                            }
                            alert.dismiss();
                        }

                        @Override
                        public void onFailure(String error) {
                            urlText.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            errorMessage.setVisibility(View.VISIBLE);
                        }
                    });
                })
        );
        // TODO: Can we have an onClose listener or something and set a variable that if onFailure happened and we close, then call the caller's onFailure (-_-)

        alert.show();
    }

    /**
     * Display a popup for the user to log in to the backend server.
     *
     * @param context Context
     * @param activity Activity
     * @param callback Callback, only calls onSuccess() upon successful login, never calls onFailure()
     */
    // TODO: If this is only called from one place, remove from Utils
    public static void displayLoginPopup(@NonNull Context context,
                                         @NonNull Activity activity,
                                         @Nullable OperationCompleteCallback callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_login_popup, null);

        EditText usernameText = dialogView.findViewById(R.id.usernameText);
        EditText passwordText = dialogView.findViewById(R.id.passwordText);
        ProgressBar progressBar = dialogView.findViewById(R.id.loginProgressBar);
        TextView errorMessage = dialogView.findViewById(R.id.loginErrorMessage);

        builder.setView(dialogView);
        builder.setTitle("Login");
        builder.setIcon(R.drawable.blur_circle_icon);
        builder.setCancelable(true);
        builder.setPositiveButton("Login",null);
        builder.setNegativeButton("Cancel", null);

        AlertDialog alert = builder.create();
        // Customize PositiveButton functionality so it does not always close the dialog
        alert.setOnShowListener(dialogInterface ->
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                usernameText.setEnabled(false);
                passwordText.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                errorMessage.setVisibility(View.GONE);

                String enteredUsername = usernameText.getText().toString();
                String enteredPassword = passwordText.getText().toString();

                AuthRepo.attemptLogin(
                        SharedPrefs.getInstance(context).getServerUrl(),
                        new OperationCompleteCallback() {
                            @Override
                            public void onSuccess() {
                                if (null != callback) {
                                    callback.onSuccess();
                                }
                                alert.dismiss();
                            }

                            @Override
                            public void onFailure(String error) {
                                usernameText.setEnabled(true);
                                passwordText.setEnabled(true);
                                errorMessage.setText(error);
                                errorMessage.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                            }
                        },
                        jwt -> SharedPrefs.getInstance(context).setJwt(jwt),
                        context.getResources(),
                        LoginDTO.builder()
                                .username(enteredUsername)
                                .password(enteredPassword)
                                .build()
                );
                // TODO: Try to login
                // TODO: if we get a 401 (..or does it return with a redirect? Test this), then tell the
                // user that the credentials were invalid, ask them to try again (and re-enable
                // input fields) or close (in which case if null close dialog otherwise call fail
                // TODO: If we succeed, store the JWT and if callback null close otherwise call success
                // TODO: can we encrypt the JWT? Research if this is necessary
        }));

        // TODO: Can we have an onClose listener or something and set a variable that if onFailure happened and we close, then call the caller's onFailure (-_-)
        alert.show();
    }
}
