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
import android.view.ViewTreeObserver;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.manager.SimulatedAttackManager;
import com.damienwesterman.defensedrill.ui.adapters.PolicyAdapter;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.utils.UiUtils;
import com.damienwesterman.defensedrill.ui.view_models.SimulatedAttackSettingsViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * TODO: doc comments
 */
@AndroidEntryPoint
public class SimulatedAttackSettingsActivity extends AppCompatActivity {
    private static final String TAG = SimulatedAttackSettingsActivity.class.getSimpleName();

    @Inject
    SimulatedAttackManager simulatedAttackManager;
    @Inject
    SharedPrefs sharedPrefs;
    private SimulatedAttackSettingsViewModel viewModel;

    private LinearLayout rootView;
    private ProgressBar progressBar;
    private Button addPolicyButton;
    private TextView modifyInstructions;
    private RecyclerView existingPoliciesRecyclerView;

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

        rootView = findViewById(R.id.activitySimulatedAttackSettings);
        SwitchCompat enabledSwitch = findViewById(R.id.enabledSwitch);
        progressBar = findViewById(R.id.progressBar);
        addPolicyButton = findViewById(R.id.addPolicyButton);
        modifyInstructions = findViewById(R.id.modifyInstructions);
        existingPoliciesRecyclerView = findViewById(R.id.existingPoliciesRecyclerView);

        boolean simulatedAttacksEnabled = sharedPrefs.areSimulatedAttacksEnabled();
        enabledSwitch.setChecked(simulatedAttacksEnabled);
        enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPrefs.setSimulatedAttacksEnabled(isChecked);
            showPolicies(isChecked);
            // TODO: If turning to checked then start the alarm manager
        });

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
        policyDetailsPopup(null);
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    public void deletePolicyPopup(@NonNull String policyName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to delete:");
        builder.setIcon(R.drawable.warning_icon);
        builder.setCancelable(true);
        builder.setMessage(policyName);
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Delete", (dialog, position) -> {
            setLoading(true);
            List<WeeklyHourPolicyEntity> policies = viewModel.getPoliciesByName().get(policyName);
            if (null != policies) {
                viewModel.removePolicies(
                        policies.stream()
                                .map(WeeklyHourPolicyEntity::getWeeklyHour)
                                .collect(Collectors.toList()),
                        new OperationCompleteCallback() {
                            @Override
                            public void onSuccess() {
                                UiUtils.displayDismissibleSnackbar(rootView,
                                        policyName + " has been deleted!");
                            }

                            @Override
                            public void onFailure(String error) {
                                UiUtils.displayDismissibleSnackbar(rootView, error);
                            }
                        });
            }
        });
        builder.create().show();
    }

    /**
     * TODO doc comments
     *
     * @param policyBeingModified If modifying an existing policy, the name of that policy,
     *                            otherwise leave null if creating a new policy(ies)
     */
    public void policyDetailsPopup(@Nullable String policyBeingModified) {
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

        if (null != policyBeingModified) {
            // Set fields to existing values
            policyNameEditText.setText(policyBeingModified);

            List<WeeklyHourPolicyEntity> policies = viewModel.getPoliciesByName().get(policyBeingModified);
            if (null == policies || policies.isEmpty()) {
                Log.e(TAG, "Policies were null or were empty");
                UiUtils.displayDismissibleSnackbar(rootView, "Something went wrong");
                return;
            }
            policies.forEach(policy -> {
                int dayOfWeek = policy.getWeeklyHour() / 24;
                checkBoxes.get(dayOfWeek).setChecked(true);
            });

            // - 1 because of the default NO_ATTACKS frequency
            frequencySpinner.setSelection(policies.get(0).getFrequency().ordinal() - 1);

            // Set Time Window
            policies.sort(Comparator.comparingInt(WeeklyHourPolicyEntity::getWeeklyHour));
            int startingHour = policies.get(0).getWeeklyHour() % 24;
            /*
             Now we iterate through the sorted list of policies until we find the first one that is not
             contiguous.
             */
            int endingHour = startingHour;
            for (WeeklyHourPolicyEntity policy : policies) {
                if ((policy.getWeeklyHour() % 24) > (endingHour + 1)) {
                    // We have found the non-contiguous policy, so the previous ending hour is correct
                    break;
                }
                endingHour = policy.getWeeklyHour() % 24;
            }
            // Make sure that we include the last full hour
            endingHour += 1;

            beginningHourSpinner.setSelection(startingHour);
            endingHourSpinner.setSelection(endingHour);
        }

        builder.setView(dialogView);
        builder.setIcon(R.drawable.notification_w_sound_icon);
        builder.setTitle("Notification Details");
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Cancel", null);
        builder.setNeutralButton("Reset", null);

        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // "Save"
                policyNameEditText.setEnabled(false);
                checkBoxes.forEach(checkBox -> checkBox.setEnabled(false));
                beginningHourSpinner.setEnabled(false);
                endingHourSpinner.setEnabled(false);
                frequencySpinner.setEnabled(false);
                savingPolicyProgressBar.setVisibility(View.VISIBLE);
                errorMessage.setVisibility(View.GONE);

                List<WeeklyHourPolicyEntity> policies = extractPolicies(dialogView, policyBeingModified, error -> {
                    errorMessage.setText(error);
                    errorMessage.setVisibility(View.VISIBLE);

                    policyNameEditText.setEnabled(true);
                    checkBoxes.forEach(checkBox -> checkBox.setEnabled(true));
                    beginningHourSpinner.setEnabled(true);
                    endingHourSpinner.setEnabled(true);
                    frequencySpinner.setEnabled(true);
                    savingPolicyProgressBar.setVisibility(View.GONE);
                    // Do not dismiss
                });
                if (!policies.isEmpty()) {
                    viewModel.savePolicies(
                        policies,
                        null != policyBeingModified,
                        true,
                        new OperationCompleteCallback() {
                            @Override
                            public void onSuccess() {
                                runOnUiThread(() -> setLoading(true));
                                alert.dismiss();
                                UiUtils.displayDismissibleSnackbar(rootView,
                                        "Alarm saved successfully!");
                            }

                            @Override
                            public void onFailure(String error) {
                                errorMessage.setText(error);
                                errorMessage.setVisibility(View.VISIBLE);

                                policyNameEditText.setEnabled(true);
                                checkBoxes.forEach(checkBox -> checkBox.setEnabled(true));
                                beginningHourSpinner.setEnabled(true);
                                endingHourSpinner.setEnabled(true);
                                frequencySpinner.setEnabled(true);
                                savingPolicyProgressBar.setVisibility(View.GONE);
                                // Do not dismiss
                            }
                        }
                    );
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
    // TODO: Doc comments
    private synchronized void setLoading(boolean isLoading) {
        if (!sharedPrefs.areSimulatedAttacksEnabled()) {
            showPolicies(false);
        } else if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            existingPoliciesRecyclerView.setVisibility(View.GONE);
            addPolicyButton.setEnabled(false);
            modifyInstructions.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            existingPoliciesRecyclerView.setVisibility(View.VISIBLE);
            addPolicyButton.setEnabled(true);
            modifyInstructions.setVisibility(View.VISIBLE);
        }
    }

    // TODO: Doc comments
    private synchronized void showPolicies(boolean arePoliciesEnabled) {
        if (arePoliciesEnabled) {
            existingPoliciesRecyclerView.setVisibility(View.VISIBLE);
            addPolicyButton.setEnabled(true);
            modifyInstructions.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
            existingPoliciesRecyclerView.setVisibility(View.GONE);
            addPolicyButton.setEnabled(false);
            modifyInstructions.setVisibility(View.GONE);
        }
    }

    // TODO: Doc comments
    private void setUpViewModel() {
        viewModel.getPolicies().observe(this, policies -> {
            if (policies.isEmpty()) {
                // Set up the database with defaults
                viewModel.populateDefaultPolicies();
            } else {
                setUpRecyclerView();
            }
        });

        viewModel.loadPolicies();
    }

    public void setUpRecyclerView() {
        setLoading(true);
        existingPoliciesRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Once all the items are rendered: remove this listener, hide progress bar, show view
                existingPoliciesRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setLoading(false);
            }
        });

        existingPoliciesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        Map<String, List<WeeklyHourPolicyEntity>> policiesByName = viewModel.getPoliciesByName();
        existingPoliciesRecyclerView.setAdapter(new PolicyAdapter(policiesByName,
                // On Click Listener -> Modify Policy
                this::policyDetailsPopup,
                // Long Click Listener -> Delete Policy
                this::deletePolicyPopup,
                (policyName, isChecked) -> {
                    // Radio button clicked, change activeness
                    List<WeeklyHourPolicyEntity> policies = viewModel.getPoliciesByName().get(policyName);
                    if (null != policies) {
                        policies.forEach(policy -> policy.setActive(isChecked));
                        viewModel.savePolicies(
                            policies,
                            false,
                            false,
                            new OperationCompleteCallback() {
                                @Override
                                public void onSuccess() {
                                    // Do nothing
                                }

                                @Override
                                public void onFailure(String error) {
                                    UiUtils.displayDismissibleSnackbar(rootView, error);
                                }
                            }
                        );
                    }
                }
        ));
    }

    // TODO: Doc comments (View should be of layout_policy_details_popup), explain why list return
    // TODO: Does input validation. CurrPolicy is nullable for if you are modifying a policy
    @NonNull
    private List<WeeklyHourPolicyEntity> extractPolicies(@NonNull View view,
                                                         @Nullable String policyBeingModified,
                                                         @NonNull Consumer<String> errorConsumer) {
        int[] checkBoxIds = {
                R.id.sundayCheckBox, R.id.mondayCheckBox, R.id.tuesdayCheckBox,
                R.id.wednesdayCheckBox, R.id.thursdayCheckBox, R.id.fridayCheckBox,
                R.id.saturdayCheckBox
        };
        List<WeeklyHourPolicyEntity> ret = new ArrayList<>(checkBoxIds.length);

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
            return List.of();
        }

        // Check Policy Name Criteria
        String policyName = policyNameEditText.getText().toString();
        if (policyName.isEmpty()) {
            errorConsumer.accept("Alarm Name cannot be left blank.");
            return List.of();
        }
        if (32 < policyName.length()) {
            // 32 is just arbitrary, there are no database constraints
            errorConsumer.accept("Alarm Name exceed 32 characters.");
            return List.of();
        }

        // Check if policy name already exists
        Set<String> existingPolicyNames = viewModel.getPoliciesByName().keySet();
        boolean nameAlreadyInUse = existingPolicyNames.contains(policyName);

        if (null != policyBeingModified) {
            if (nameAlreadyInUse && policyName.equals(policyBeingModified)) {
                // The name is already in use because we are modifying it
                nameAlreadyInUse = false;
            }
        }

        if (nameAlreadyInUse) {
            errorConsumer.accept("Alarm Name already exists.");
            return List.of();
        }

        // Check Notification Frequency Criteria
        int frequencyPosition = frequencySpinner.getSelectedItemPosition();
        // + 1 because of the first option being NO_ATTACKS
        Constants.SimulatedAttackFrequency frequency =
                Constants.SimulatedAttackFrequency.values()[frequencyPosition + 1];
        int alertHours = endingHourSpinner.getSelectedItemPosition()
                - beginningHourSpinner.getSelectedItemPosition();
        if (alertHours < frequency.getMinHoursNeeded()) {
            if (0 > alertHours) {
                // User has selected an overnight time window, which is not currently supported
                errorConsumer.accept("Overnight alarms not currently supported, must create two separate alarms.");
            } else {
                errorConsumer.accept("Time window must be at least " + frequency.getMinHoursNeeded()
                        + " hour(s) for selected frequency.");
            }
            return List.of();
        }

        // Check Weekly Hour Criteria
        List<WeeklyHourPolicyEntity> existingPolicies = viewModel.getPolicies().getValue();
        List<Integer> dailyHoursSelected = IntStream.range(
                    beginningHourSpinner.getSelectedItemPosition(),
                    endingHourSpinner.getSelectedItemPosition())
                .boxed().collect(Collectors.toList());
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isChecked()) {
                for (Integer hourOfDay : dailyHoursSelected) {
                    int hourOfWeek = (i * 24) + hourOfDay;
                    if (null != existingPolicies) {
                        boolean weeklyHourPolicyAlreadyExists = false;

                        WeeklyHourPolicyEntity weeklyHourPolicy = existingPolicies.get(hourOfWeek);
                        if (!weeklyHourPolicy.getPolicyName().isBlank()) {
                            weeklyHourPolicyAlreadyExists = true;

                            if (null != policyBeingModified
                                    && policyBeingModified.equals(weeklyHourPolicy.getPolicyName())) {
                                // Yes this time overlaps because we are actively modifying it
                                weeklyHourPolicyAlreadyExists = false;
                            }
                        }

                        if (weeklyHourPolicyAlreadyExists) {
                            errorConsumer.accept("Time frame overlaps with another alarm.");
                            return List.of();
                        }
                    }

                    // All checks have passed, add it to the list of new policies
                    WeeklyHourPolicyEntity newPolicy = WeeklyHourPolicyEntity.builder()
                            .weeklyHour(hourOfWeek)
                            .frequency(frequency)
                            .active(true)
                            .policyName(policyName)
                            .build();
                    ret.add(newPolicy);
                }
            }
        }

        if (ret.isEmpty()) {
            errorConsumer.accept("Must select at least one day of the week.");
            return List.of();
        }

        return ret;
    }
}