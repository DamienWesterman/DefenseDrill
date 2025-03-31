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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.domain.CheckPhoneInternetConnection;
import com.damienwesterman.defensedrill.ui.utils.CommonPopups;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.utils.UiUtils;
import com.damienwesterman.defensedrill.ui.view_models.DrillApiViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WebDrillOptionsActivity extends AppCompatActivity {
    private LinearLayout rootView;
    @Inject
    SharedPrefs sharedPrefs;
    @Inject
    CommonPopups commonPopups;

    private DrillApiViewModel viewModel;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_drill_options);

        rootView = findViewById(R.id.activityWebDrillOptions);

        viewModel = new ViewModelProvider(this).get(DrillApiViewModel.class);
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.downloadFromDatabaseCard == cardId) {
            handleDownloadDrills();
        } else if (R.id.checkForUpdatesCard == cardId) {
            // TODO: Launch this activity (subsequent PR)
            UiUtils.displayDismissibleSnackbar(rootView, "Unimplemented: checkForUpdatesCard");
        } else if (R.id.loginCard == cardId) {
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
        } else if (R.id.logoutCard == cardId) {
            logoutPopup();
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
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

    // TODO: Doc comments
    private void loadAllDrillsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_loading_popup, null);

        TextView loadingText = dialogView.findViewById(R.id.pleaseWaitLabel);
        ProgressBar progressBar = dialogView.findViewById(R.id.loadingProgressBar);
        TextView errorMessage = dialogView.findViewById(R.id.loadingErrorMessage);

        builder.setView(dialogView);
        builder.setTitle("Downloading Drills");
        builder.setIcon(R.drawable.cloud_icon);
        builder.setCancelable(false);
        builder.setNegativeButton("Cancel", (dialogInterface, i) -> viewModel.stopDownload());

        AlertDialog alert = builder.create();

        viewModel.downloadDb(new OperationCompleteCallback() {
            @Override
            public void onSuccess() {
                alert.dismiss();
                UiUtils.displayDismissibleSnackbar(rootView, "Download Successful!");
            }

            @Override
            public void onFailure(String error) {
                loadingText.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                errorMessage.setText(error);
                errorMessage.setVisibility(View.VISIBLE);
            }
        });

        alert.show();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    // TODO: Doc comments
    private void handleDownloadDrills() {
        if (!CheckPhoneInternetConnection.isNetworkConnected(this)) {
            UiUtils.displayDismissibleSnackbar(rootView, "No internet connection");
            return;
        }

        if (sharedPrefs.getJwt().isEmpty()) {
            commonPopups.displayLoginPopup(new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    loadAllDrillsPopup();
                    UiUtils.displayDismissibleSnackbar(rootView, "Login Successful!");
                }

                @Override
                public void onFailure(String error) {
                    UiUtils.displayDismissibleSnackbar(rootView, error);
                }
            });
        } else {
            loadAllDrillsPopup();
        }
    }
}