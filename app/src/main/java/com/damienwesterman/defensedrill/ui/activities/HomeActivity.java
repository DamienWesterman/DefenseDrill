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

package com.damienwesterman.defensedrill.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.domain.CheckPhoneInternetConnection;
import com.damienwesterman.defensedrill.service.SimulatedAttackService;
import com.damienwesterman.defensedrill.ui.utils.UiUtils;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Home screen activity and entry point for the application. Displays the different general
 * functionalities of the app. CRUD operations in the database, Drill generation, and feedback.
 * <br><br>
 * INTENTS: None expected.
 */
@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {
    // TODO: On first startup, go through help screen. THEN prompt the user for notifications:
        // https://developer.android.com/develop/ui/views/notifications/notification-permission#best-practices
        // https://developer.android.com/training/permissions/requesting#request-permission

    private LinearLayout rootView;

    @Inject
    CheckPhoneInternetConnection internetConnection;

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.homeButton == item.getItemId()) {
            SimulatedAttackService.stopService(); // TODO: REMOVE
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.generateDrillCard == cardId) {
            Intent intent = new Intent(this, CategorySelectActivity.class);
            startActivity(intent);
        } else if (R.id.customizeDatabaseCard == cardId) {
            Intent intent = new Intent(this, CustomizeDatabaseActivity.class);
            startActivity(intent);
        } else if (R.id.webDrillOptionsCard == cardId) {
            if (!internetConnection.isNetworkConnected()) {
                UiUtils.displayDismissibleSnackbar(rootView, "No internet connection.");
            } else {
                Intent intent = new Intent(this, WebDrillOptionsActivity.class);
                startActivity(intent);
            }
        } else if (R.id.feedbackCard == cardId) {
            UiUtils.displayDismissibleSnackbar(rootView, "Feedback unimplemented");
            SimulatedAttackService.toggle(this); // TODO: Remove
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }
}