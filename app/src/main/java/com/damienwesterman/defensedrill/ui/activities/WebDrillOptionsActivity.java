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

package com.damienwesterman.defensedrill.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.domain.CheckPhoneInternetConnection;
import com.damienwesterman.defensedrill.data.remote.ServerHealthRepo;
import com.damienwesterman.defensedrill.ui.utils.CommonPopups;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.utils.UiUtils;
import com.damienwesterman.defensedrill.ui.view_models.DrillApiViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WebDrillOptionsActivity extends AppCompatActivity {
    private LinearLayout rootView;
    private Context context;
    private Activity activity;
    @Inject
    SharedPrefs sharedPrefs;
    @Inject
    CommonPopups commonPopups;
    @Inject
    ServerHealthRepo serverHealthRepo; // TODO: remove and move to view-model

    private DrillApiViewModel viewModel;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_drill_options);

        rootView = findViewById(R.id.activityWebDrillOptions);
        context = this;
        activity = this;

        viewModel = new ViewModelProvider(this).get(DrillApiViewModel.class);

        // Need to make sure a server is set before most interactions
        if (sharedPrefs.getServerUrl().isEmpty()) {
            serverSelectPopup(false);
        }
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.downloadFromDatabaseCard == cardId) {
            // TODO: Launch this activity
            // TODO: Make sure to check JWT first so we can prompt user, or something because we also need to know when it returns 401, maybe an exception then?
            viewModel.downloadDb();
            UiUtils.displayDismissibleSnackbar(rootView, "Unimplemented: downloadFromDatabaseCard");
        } else if (R.id.checkForUpdatesCard == cardId) {
            // TODO: Launch this activity (subsequent PR)
            UiUtils.displayDismissibleSnackbar(rootView, "Unimplemented: checkForUpdatesCard");
        } else if (R.id.loginCard == cardId) {
            loginPopup();
        } else if (R.id.logoutCard == cardId) {
            logoutPopup();
        } else if (R.id.serverUrlCard == cardId) {
            serverSelectPopup(true);
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    /**
     * Display the popup for Server Selection.
     *
     * @param isCancelable Can the user cancel the popup
     */
    private void serverSelectPopup(boolean isCancelable) {
        if (!CheckPhoneInternetConnection.isNetworkConnected(context)) {
            UiUtils.displayDismissibleSnackbar(rootView, "No internet connection.");
            if (!isCancelable) {
                finish();
            }
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_server_url_popup, null);

        EditText urlText = dialogView.findViewById(R.id.urlText);
        TextView errorMessage = dialogView.findViewById(R.id.serverErrorMessage);
        ProgressBar progressBar = dialogView.findViewById(R.id.serverUrlProgressBar);
        String currentServerUrl = sharedPrefs.getServerUrl();
        urlText.setText(currentServerUrl);

        builder.setView(dialogView);
        builder.setTitle("Enter Server URL");
        builder.setIcon(R.drawable.cloud_icon);
        builder.setCancelable(isCancelable);
        builder.setPositiveButton("Confirm",null);
        if (isCancelable) {
            builder.setNegativeButton("Cancel", null);
        } else {
            builder.setNegativeButton("Cancel", (dialogInterface, i) -> finish());
        }
        if (!currentServerUrl.isEmpty()) {
            builder.setNeutralButton("Delete Saved URL", (dialogInterface, i) -> {
                    sharedPrefs.setServerUrl("");
                    finish();
            });
        }

        AlertDialog alert = builder.create();
        // Customize PositiveButton functionality so it does not always close the dialog
        alert.setOnShowListener(dialogInterface ->
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                    // Disable user input and show spinner
                    urlText.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    errorMessage.setVisibility(View.GONE);

                    String enteredUrl = urlText.getText().toString();
                    // TODO Remove and move to view-model
                    serverHealthRepo.isServerHealthy(enteredUrl, new OperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            sharedPrefs.setServerUrl(enteredUrl);
                            UiUtils.displayDismissibleSnackbar(rootView, "Saved Server URL");
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
        //                    UiUtils.displayDismissibleSnackbar(rootView, error);

        alert.show();
    }

    /**
     * Display the popup for user login.
     */
    private void loginPopup() {
        commonPopups.displayLoginPopup(new OperationCompleteCallback() {
            @Override
            public void onSuccess() {
                // On Login Success
                UiUtils.displayDismissibleSnackbar(rootView, "Login Successful!");
            }

            @Override
            public void onFailure(String error) {
                // On Login Failure
                UiUtils.displayDismissibleSnackbar(rootView, error);
            }
        });
    }

    /**
     * Display the popup for a user to log out.
     */
    private void logoutPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setIcon(R.drawable.warning_icon);
        builder.setMessage("Are you sure you want to log out?");
        builder.setPositiveButton("Log Out", (dialog, position) -> {
            new Thread(() -> sharedPrefs.setJwt("")).start();
            UiUtils.displayDismissibleSnackbar(rootView, "Logout Successful!");
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }
}