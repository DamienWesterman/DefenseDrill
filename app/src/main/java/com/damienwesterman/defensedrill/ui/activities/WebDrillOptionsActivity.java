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
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
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
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
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