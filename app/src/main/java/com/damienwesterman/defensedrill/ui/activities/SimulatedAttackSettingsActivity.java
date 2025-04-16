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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.SimulatedAttackRepo;
import com.damienwesterman.defensedrill.manager.SimulatedAttackManager;
import com.damienwesterman.defensedrill.ui.view_models.SimulatedAttackSettingsViewModel;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * TODO: doc comments
 */
@AndroidEntryPoint
public class SimulatedAttackSettingsActivity extends AppCompatActivity {
    // TODO: If every single slot is filled, don't show the add more button
    /*
        TODO:
        Requirements:
            - User can select 1 alerts per x minutes/hours (x options: 15 min, 30 min, 1 hr, 1.5hr, 2 hr, 3 hr, 5 hr, 6 hr, 12 hr)
            - Make sure that the time frame selected is at least x min/hr (can't have a 2 hr time frame from 1pm-2pm)
            - Each time frame is like +/- a certain amount of time (maybe like, 20%), so the option to the user would look like "1 alert per 30 minutes (+/- 6 minutes)
     */
    @Inject
    SimulatedAttackManager simulatedAttackManager;
    @Inject
    SharedPrefs sharedPrefs;
    private SimulatedAttackSettingsViewModel viewModel;

    private LinearLayout rootView;
    SwitchCompat enabledSwitch;
    ProgressBar progressBar;
    Button addPolicyButton;

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

        viewModel = new ViewModelProvider(this).get(SimulatedAttackSettingsViewModel.class);

        // TODO: Bring this into a helper method and get EVERYTHING with an ID
        rootView = findViewById(R.id.activitySimulatedAttackSettings);
        enabledSwitch = findViewById(R.id.enabledSwitch);
        progressBar = findViewById(R.id.progressBar);
        addPolicyButton = findViewById(R.id.addPolicyButton);

        boolean simulatedAttacksEnabled = sharedPrefs.areSimulatedAttacksEnabled();
        enabledSwitch.setChecked(simulatedAttacksEnabled);
        enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPrefs.setSimulatedAttacksEnabled(isChecked);
            // TODO If switching from not checked to checked and the database is empty, then it means it's the first time so fill the database from 0 - 167 or whatever with blank ones
            // TODO: when switching, also enable/disable below the div (by this I mean switch all adapter views to be turned off, graying them out but still leaving them editable, but disabling the radio button)
            addPolicyButton.setEnabled(isChecked); // TODO: refactor this out alongside the recyclerview
        });

        // TODO: Load all from view model and set to adapter for the recyclerview, and pass in the following
        if (simulatedAttacksEnabled) {
            addPolicyButton.setEnabled(true);
        } else {
            // TODO Tell the adapter view to gray out/disable each card (by this I mean switch all adapter views to be turned off, graying them out but still leaving them editable, but disabling the radio button)
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

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void addPolicy(View view) {
        addPolicyPopup();
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    /**
     * TODO doc comments
     */
    public void addPolicyPopup() {
        // TODO: FINISH implement popup
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_policy_details_popup, null);

        // TODO: Get ALL of the things
        Spinner beginningHourSpinner = dialogView.findViewById(R.id.beginningHourSpinner);
        Spinner endingHourSpinner = dialogView.findViewById(R.id.endingHourSpinner);
        Spinner frequencySpinner = dialogView.findViewById(R.id.frequencySpinner);

        ArrayAdapter<CharSequence> hoursAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.daily_hours,
                android.R.layout.simple_spinner_item
        );
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        beginningHourSpinner.setAdapter(hoursAdapter);
        endingHourSpinner.setAdapter(hoursAdapter);

        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.frequency_options,
                android.R.layout.simple_spinner_item
        );
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);

        builder.setView(dialogView);
        builder.setIcon(R.drawable.notification_w_sound_icon);
        builder.setTitle("Add Notification");
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("Reset", null);

        // TODO: setOnShowListener for save (report error conditions) and reset (do not close)
        builder.create().show();

        // TODO: Verify That the time frames do not overlap with existing ones
        // TODO: Verify the times are linear (cannot do 5PM - 2 AM) and is more than 1 hour (can't be 5PM - 5PM)
        // TODO: Verify all dropdowns are selected (cannot be default)
        // TODO: Verify at least one day is checked
        // TODO: Verify the notification name is not already in use (Default to New Notification) - cap name to like 32 characters
    }

    // TODO: Doc comments
    public void modifyPolicyPopup() {
        // TODO: FINISH implement popup
        // TODO: Verify That the time frames do not overlap with existing ones
        // TODO: Verify the times are linear (cannot do 5PM - 2 AM) and is more than 1 hour (can't be 5PM - 5PM)
        // TODO: Verify all dropdowns are selected (cannot be default)
        // TODO: Verify at least one day is checked
        // TODO: Verify the notification name is not already in use BY ANOTHER NOTIFICATION - cap name to like 32 characters
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
}