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

package com.damienwesterman.defensedrill.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.domain.CheckPhoneInternetConnection;
import com.damienwesterman.defensedrill.manager.SimulatedAttackManager;
import com.damienwesterman.defensedrill.service.CheckServerUpdateService;
import com.damienwesterman.defensedrill.ui.util.UiUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Home screen activity and entry point for the application. Displays the different general
 * functionalities of the app. CRUD operations in the database, Drill generation, and feedback.
 */
@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {
    // TODO: On first startup, go through help screen. THEN prompt the user for notifications:
        // https://developer.android.com/develop/ui/views/notifications/notification-permission#best-practices
        // https://developer.android.com/training/permissions/requesting#request-permission
    // TODO: THEN prompt the user for unrestricted background usage:
        // https://stackoverflow.com/a/54852199

    // TODO: Refactor to do list:
        /*
        - Any methods that return nullable, surround calls in optionals
        - Update RecyclerViews using solution provided by  https://chatgpt.com/c/682ceac4-64d0-800d-ade0-d3ec6f26b1a5
        - Check all Doc comments for params/return lining up and having periods
        - Can we just use onCardClick for every activity?
        - Refactor utils into appropriate packages
        - Replace callback classes with lambdas? Maybe keep OperationCompleteCallback
        - Simplify insert operations like in SimulatedAttackRepo
         */
    private static boolean isUpdateServiceStarted = false;

    private LinearLayout rootView;

    @Inject
    CheckPhoneInternetConnection internetConnection;
    @Inject
    SimulatedAttackManager simulatedAttackManager;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start the HomeActivity. Clears the activity stack so home is now the top.
     *
     * @param context   Context.
     */
    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Defense Drill Home");
        setSupportActionBar(appToolbar);

        rootView = findViewById(R.id.activityHome);

        // Have to do this here so service is not started when the app is launched in the background
        if (!isUpdateServiceStarted) {
            CheckServerUpdateService.startService(this);
            isUpdateServiceStarted = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(@NonNull View view) {
        int cardId = view.getId();
        if (R.id.generateDrillCard == cardId) {
            CategorySelectActivity.startActivityClearTop(this);
        } else if (R.id.customizeDatabaseCard == cardId) {
            CustomizeDatabaseActivity.startActivity(this);
        } else if (R.id.simulatedAttackSettings == cardId) {
            SimulatedAttackSettingsActivity.startActivity(this);
        } else if (R.id.webDrillOptionsCard == cardId) {
            if (!internetConnection.isNetworkConnected()) {
                UiUtils.displayDismissibleSnackbar(rootView, "No internet connection.");
            } else {
                WebDrillOptionsActivity.startActivity(this);
            }
        } else if (R.id.feedbackCard == cardId) {
            UiUtils.displayDismissibleSnackbar(rootView, "Feedback unimplemented");
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }
}