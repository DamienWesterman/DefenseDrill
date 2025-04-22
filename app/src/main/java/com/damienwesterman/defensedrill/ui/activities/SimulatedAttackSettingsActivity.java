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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.manager.SimulatedAttackManager;
import com.damienwesterman.defensedrill.ui.view_models.SimulatedAttackSettingsViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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

        addPolicyButton.setEnabled(simulatedAttacksEnabled);
        // TODO: recyclerView.setVisible(simulatedAttacks ? Visible : Gone);

        setUpViewModel();
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
        addPolicyPopup(null);
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    /**
     * TODO doc comments
     *
     * @param policyBeingModified If modifying an existing policy, the name of that policy,
     *                            otherwise leave null if creating a new policy(ies)
     */
    public void addPolicyPopup(@Nullable String policyBeingModified) {
        // TODO: FINISH implement popup
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_policy_details_popup, null);


        int[] checkBoxIds = {
                R.id.sundayCheckBox, R.id.mondayCheckBox, R.id.tuesdayCheckBox,
                R.id.wednesdayCheckBox, R.id.thursdayCheckBox, R.id.fridayCheckBox,
                R.id.saturdayCheckBox
        };
        EditText policyNameEditText = dialogView.findViewById(R.id.policyName);
        List<CheckBox> checkBoxes = Arrays.stream(checkBoxIds)
                .mapToObj(id -> (CheckBox) dialogView.findViewById(id))
                .collect(Collectors.toList());
        Spinner beginningHourSpinner = dialogView.findViewById(R.id.beginningHourSpinner);
        Spinner endingHourSpinner = dialogView.findViewById(R.id.endingHourSpinner);
        Spinner frequencySpinner = dialogView.findViewById(R.id.frequencySpinner);
        ProgressBar savingPolicyProgressBar = dialogView.findViewById(R.id.savingPolicyProgressBar);
        TextView errorMessage = dialogView.findViewById(R.id.errorMessage);

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

        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // "Save"
                // TODO: Disable all input fields, remove error message, and display progress bar

                List<WeeklyHourPolicyEntity> policies = extractPolicies(dialogView, policyBeingModified, error -> {
                    errorMessage.setText(error);
                    errorMessage.setVisibility(View.VISIBLE);
                    // TODO: Set everything else visible and remove progress bar
                });
                if (!policies.isEmpty()) {
                    // TODO: Finish
                    policies.forEach(policy -> {
                        Log.i("DxTag", policy.toString());
                        int dayOfWeek = policy.getWeeklyHour() / 24;
                        int hourOfDay = policy.getWeeklyHour() % 24;
                        Log.i("DxTag", "Day: " + dayOfWeek + " | Hour: " + hourOfDay);
                    });

                    // TODO: show a snackbar saying the save was successful if so
                    alert.dismiss(); // TODO: move this into an operation complete callback in save
                }
            });

            alert.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                // "Reset"
                errorMessage.setVisibility(View.GONE);
                policyNameEditText.setText(R.string.default_policy_name);
                checkBoxes.forEach(checkBox -> checkBox.setChecked(false));
                beginningHourSpinner.setSelection(0);
                endingHourSpinner.setSelection(0);
                frequencySpinner.setSelection(0);
                // Do not dismiss
            });
        });

        alert.show();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    private void setUpViewModel() {
        viewModel.getPolicies().observe(this, weeklyHourPolicyEntities -> {
            // TODO: fill the recycler view with adapters or whatever and set the information
            progressBar.setVisibility(View.GONE);
        });

        viewModel.loadPolicies();
    }

    // TODO: Doc comments (View should be of layout_policy_details_popup), explain why list
    // TODO: Does input validation. CurrPolicy is nullable for if you are modifying a policy
    @NonNull
    private List<WeeklyHourPolicyEntity> extractPolicies(@NonNull View view,
                                                         @Nullable String policyBeingModified,
                                                         @NonNull Consumer<String> errorConsumer) {
        final List<WeeklyHourPolicyEntity> ret = new ArrayList<>(7);

        int[] checkBoxIds = {
                R.id.sundayCheckBox, R.id.mondayCheckBox, R.id.tuesdayCheckBox,
                R.id.wednesdayCheckBox, R.id.thursdayCheckBox, R.id.fridayCheckBox,
                R.id.saturdayCheckBox
        };
        EditText policyNameEditText = view.findViewById(R.id.policyName);
        List<CheckBox> checkBoxes = Arrays.stream(checkBoxIds)
                .mapToObj(id -> (CheckBox) view.findViewById(id))
                .collect(Collectors.toList());
        Spinner beginningHourSpinner = view.findViewById(R.id.beginningHourSpinner);
        Spinner endingHourSpinner = view.findViewById(R.id.endingHourSpinner);
        Spinner frequencySpinner = view.findViewById(R.id.frequencySpinner);

        if (null == policyNameEditText
                || checkBoxes.stream().anyMatch(Objects::isNull)
                || null == beginningHourSpinner
                || null == endingHourSpinner
                || null == frequencySpinner) {
            errorConsumer.accept("There was an issue saving the new alarm.");
            return ret;
        }

        // TODO: Verify the notification name is not already in use (Default to New Notification), also make sure if editing them it is not in use by another  - cap name to like 32 characters (unless policyName .isEqual() policyBeingModified)
        String policyName = policyNameEditText.getText().toString();

        final int frequency = frequencySpinner.getSelectedItemPosition();

        // TODO: Verify That the time frames do not overlap with existing ones (unless they are of the same existing OLD policyName)
        // TODO: Verify time frame is at least as big as frequency
        // TODO: Verify we do not start at the SECOND midnight option
        // TODO: Verify the times are linear (cannot do 5PM - 2 AM, "Overnight alarms not currently supported, must create two separate alarms") and is more than 1 hour (can't be 5PM - 5PM, lump the error message in with the frequency requirement))
        int[] timesOfDay = { beginningHourSpinner.getSelectedItemPosition() };

        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isChecked()) {
                final int finalI = i;
                Arrays.stream(timesOfDay).forEach(timeOfDay -> {
                    WeeklyHourPolicyEntity newPolicy = WeeklyHourPolicyEntity.builder()
                            .weeklyHour( (finalI * 24) + timeOfDay )
                            // + 1 because of the first option being NO_ATTACKS
                            .frequency(Constants.SimulatedAttackFrequency.values()[frequency + 1])
                            .active(true)
                            .policyName(policyName)
                            .build();
                    ret.add(newPolicy);
                });
            }
        }

        if (ret.isEmpty()) {
            errorConsumer.accept("Must select at least one day of the week.");
        }

        return ret;
    }
}