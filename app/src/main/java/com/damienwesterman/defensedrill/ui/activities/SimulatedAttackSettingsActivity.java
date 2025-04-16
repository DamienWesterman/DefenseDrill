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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.SimulatedAttackRepo;
import com.damienwesterman.defensedrill.manager.SimulatedAttackManager;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * TODO: doc comments
 */
@AndroidEntryPoint
public class SimulatedAttackSettingsActivity extends AppCompatActivity {
    // TODO: If every single slot is filled, don't show the add more button
    @Inject
    SimulatedAttackRepo repo; // TODO: Export to a view model
    @Inject
    SimulatedAttackManager simulatedAttackManager;
    @Inject
    SharedPrefs sharedPrefs; // TODO: Move this to the view model as well (?)

    private LinearLayout rootView;
    SwitchCompat enabledSwitch;
    ProgressBar progressBar;

    // =============================================================================================
    // Service Creation Methods
    // =============================================================================================
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, SimulatedAttackSettingsActivity.class);
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulated_attack_settings);
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Simulated Attack Settings");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rootView = findViewById(R.id.activitySimulatedAttackSettings);
        enabledSwitch = findViewById(R.id.enabledSwitch);
        progressBar = findViewById(R.id.progressBar);

        boolean simulatedAttacksEnabled = sharedPrefs.areSimulatedAttacksEnabled();
        enabledSwitch.setChecked(simulatedAttacksEnabled);
        enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPrefs.setSimulatedAttacksEnabled(isChecked);
            // TODO If switching from not checked to checked and the database is empty, then it means it's the first time so fill the database from 0 - 167 or whatever with blank ones
        });

        // TODO: Load all from view model and set to adapter for the recyclerview, and pass in the following
        if (!simulatedAttacksEnabled) {
            // TODO Tell the adapter view to gray out/disable each card
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.homeButton == item.getItemId()) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}