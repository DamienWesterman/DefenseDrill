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

package com.damienwesterman.defensedrill.ui.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
import android.widget.Switch;
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
import com.damienwesterman.defensedrill.ui.adapter.PolicyAdapter;
import com.damienwesterman.defensedrill.common.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.common.OnboardingUtils;
import com.damienwesterman.defensedrill.ui.common.UiUtils;
import com.damienwesterman.defensedrill.ui.viewmodel.SimulatedAttackSettingsViewModel;
import com.damienwesterman.defensedrill.common.Constants;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen that lets the user turn on or off simulated attack notifications. Also allows them to
 * modify the times and frequencies of these notification.
 */
@AndroidEntryPoint
public class SimulatedAttackSettingsActivity extends AppCompatActivity {
    private static final String TAG = SimulatedAttackSettingsActivity.class.getSimpleName();

    @Inject
    SimulatedAttackManager simulatedAttackManager;
    @Inject
    SharedPrefs sharedPrefs;
    private SimulatedAttackSettingsViewModel viewModel;

    private Context context;

    private LinearLayout rootView;
    private ProgressBar progressBar;
    private Button addPolicyButton;
    private TextView modifyInstructions;
    private RecyclerView existingPoliciesRecyclerView;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, SimulatedAttackSettingsActivity.class);
        context.startActivity(intent);
    }

    /**
     * Start the SimulatedAttackSettingsActivity in the Onboarding state.
     *
     * @param context   Context.
     */
    public static void startOnboardingActivity(@NonNull Context context) {
        Intent intent = new Intent(context, SimulatedAttackSettingsActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_START_ONBOARDING, "");
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

        this.context = this;
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

            // TODO: Future PR - Check if notifications are enabled, if not prompt to enable them (and if they don't, return and mark this as unchecked)

            viewModel.checkForSelfDefenseDrills(
                categoryExists -> {
                    boolean policiesExist = !viewModel.getPoliciesByName().isEmpty();

                    if (isChecked) {
                        if (categoryExists && policiesExist) {
                            // We have self defense drills and policies, start the manager
                            SimulatedAttackManager.start(this);
                        } else if (categoryExists) {
                            // We have self defense drills but no policies, do nothing
                        } else if (policiesExist) {
                            // We have policies but no self defense drills, show popup
                            runOnUiThread(this::noSelfDefenseDrillsPopup);
                        }
                    } else {
                        SimulatedAttackManager.stop(this);
                    }
                });
        });

        setUpRecyclerView();
        viewModel.loadPolicies();

        if (getIntent().hasExtra(Constants.INTENT_EXTRA_START_ONBOARDING)) {
            /*
            We need to wait for the toolbar to be finished loading before calling onboarding, as we
            want to access one of the buttons.
             */
            appToolbar.post(this::startOnboarding);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.homeButton == item.getItemId()) {
            HomeActivity.startActivity(this);
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

                            /*
                            Check to see if we deleted the last remaining policy. We have to check
                            this way because the viewModel will not have updated its lists yet.
                             */
                            if (1 == viewModel.getPoliciesByName().size()
                                    && viewModel.getPoliciesByName().containsKey(policyName)) {
                                // We deleted the last one
                                SimulatedAttackManager.stop(context);
                            } else {
                                SimulatedAttackManager.restart(context);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull String error) {
                            UiUtils.displayDismissibleSnackbar(rootView, error);
                        }
                    });
            } else {
                UiUtils.displayDismissibleSnackbar(rootView, "Something went wrong");
                Log.e(TAG, "Failed to find policy for deletion: " + policyName);
            }
        });
        builder.create().show();
    }

    /**
     * Popup to create or modify a policy.
     *
     * @param policyBeingModified   If modifying an existing policy, the name of that policy,
     *                              otherwise leave null if creating a new policy.
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
                        policyBeingModified,
                        true,
                        new OperationCompleteCallback() {
                            @Override
                            public void onSuccess() {
                                CountDownLatch latch = new CountDownLatch(1);
                                runOnUiThread(latch::countDown);
                                try {
                                    boolean finished = latch.await(250, TimeUnit.MILLISECONDS);
                                    if (!finished) {
                                        // We timed out, continue but log it
                                        Log.e(TAG, "Timed out waiting for UI update");
                                    }
                                } catch (InterruptedException e) {
                                    // Not much we can do, log it and continue
                                    Log.e(TAG, "Timed out waiting for UI update", e);
                                }

                                alert.dismiss();
                                UiUtils.displayDismissibleSnackbar(rootView,
                                        "Alarm saved successfully!");
                                SimulatedAttackManager.restart(context);
                            }

                            @Override
                            public void onFailure(@NonNull String error) {
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

    /**
     * Create a display a popup for when there is no Category in the database titled "Self Defense",
     * and alert the user that simulated attacks will not work and give them options to rectify the
     * issue.
     */
    public void noSelfDefenseDrillsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] options = {"Download Drills from Database", "Create \"Self Defense\" Category"};
        builder.setTitle("No Self Defense Category!");
        builder.setIcon(R.drawable.warning_icon);
        builder.setCancelable(true);

        builder.setItems(options, ((dialogInterface, position) -> {
            switch (position) {
                case 0:
                    // Download Drills from Database
                    WebDrillOptionsActivity.startActivity(this);
                    break;
                case 1:
                    // Create Self Defense Category
                    viewModel.createDefaultSelfDefenseCategory(new OperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            Snackbar snackbar = Snackbar.make(rootView,
                                    "Category Created!", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("Create Drills", (callingView) -> {
                                CreateDrillActivity.startActivity(context);
                                snackbar.dismiss();
                            });
                            snackbar.show();
                        }

                        @Override
                        public void onFailure(@NonNull String error) {
                            UiUtils.displayDismissibleSnackbar(rootView, "An error occurred");
                        }
                    });
                    break;
            }
        }));

        builder.setNegativeButton("Back", null);

        builder.create().show();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Set the UI to a loading state.
     *
     * @param isLoading boolean if the UI should be loading or loaded.
     */
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

    /**
     * Set the UI to display a list of existing policies or not.
     *
     * @param arePoliciesEnabled boolean if the policies should be shown in the UI.
     */
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

    /**
     * Fill the recycler view with the drills and set up the adapters and their callbacks.
     */
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
        PolicyAdapter adapter = new PolicyAdapter(
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
                                null,
                                false,
                                new OperationCompleteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        SimulatedAttackManager.restart(context);
                                    }

                                    @Override
                                    public void onFailure(@NonNull String error) {
                                        UiUtils.displayDismissibleSnackbar(rootView, error);
                                    }
                                }
                        );
                    } else {
                        UiUtils.displayDismissibleSnackbar(rootView, "Something went wrong");
                        Log.e(TAG, "Policy activeness change failed for " + policyName
                                + ", policies retrieved from viewModel was null");
                    }
                }
        );
        existingPoliciesRecyclerView.setAdapter(adapter);
        viewModel.getUiPoliciesList().observe(this, list -> {
            if (viewModel.getPolicies().isEmpty()) {
                // Set up the database with defaults
                viewModel.populateDefaultPolicies();
            }
            adapter.submitList(list);
        });
    }

    /**
     * Extract a list of policies from a popup. Performs input validation.
     *
     * @param view                  View object for layout_policy_details_popup.xml.
     * @param policyBeingModified   Policy name being modified, or null if creating a new one.
     * @param errorConsumer         Callback for error conditions.
     * @return                      List of correlated WeeklyHourPolicyEntity objects for a single
     *                              policy name.
     */
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
            errorConsumer.accept("There was an issue saving the alarm.");
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
            errorConsumer.accept("Alarm Name cannot exceed 32 characters.");
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
        int numAlertHours = endingHourSpinner.getSelectedItemPosition()
                - beginningHourSpinner.getSelectedItemPosition();
        if (numAlertHours < frequency.getMinimumHoursNeeded()) {
            if (0 > numAlertHours) {
                // User has selected an overnight time window, which is not currently supported
                errorConsumer.accept("Overnight alarms not currently supported, must create two separate alarms.");
            } else {
                errorConsumer.accept("Time window must be at least " + frequency.getMinimumHoursNeeded()
                        + " hour(s) for selected frequency.");
            }
            return List.of();
        }

        // Check Weekly Hour Criteria
        List<WeeklyHourPolicyEntity> existingPolicies = viewModel.getPolicies();
        List<Integer> dailyHoursSelected = IntStream.range(
                    beginningHourSpinner.getSelectedItemPosition(),
                    endingHourSpinner.getSelectedItemPosition())
                .boxed().collect(Collectors.toList());
        for (int dayOfWeek = 0; dayOfWeek < checkBoxes.size(); dayOfWeek++) {
            if (checkBoxes.get(dayOfWeek).isChecked()) {
                for (Integer hourOfDay : dailyHoursSelected) {
                    int hourOfWeek = (dayOfWeek * 24) + hourOfDay;
                    boolean weeklyHourPolicyAlreadyExists = false;

                    WeeklyHourPolicyEntity weeklyHourPolicy = existingPolicies.get(hourOfWeek);
                    if (!weeklyHourPolicy.getPolicyName().isBlank()
                            || Constants.SimulatedAttackFrequency.NO_ATTACKS != weeklyHourPolicy.getFrequency()) {
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

    // =============================================================================================
    // Onboarding Methods
    // =============================================================================================
    /**
     * Start the onboarding process for this activity, walking the user through the screen and
     * explaining how it works. Preceded by
     * {@link HomeActivity#continueOnboardingActivity(Context, Class)}, proceeded by
     * {@link HomeActivity#continueOnboardingActivity(Context, Class)} from
     * SimulatedAttackSettingsActivity.
     */
    private void startOnboarding() {
        boolean cancelable = sharedPrefs.isOnboardingComplete();
        SwitchCompat enabledSwitch = findViewById(R.id.enabledSwitch);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        enabledSwitch.setOnCheckedChangeListener(null);
        enabledSwitch.setChecked(false);

        TapTarget enableSwitchTapTarget = OnboardingUtils.createTapTarget(
                enabledSwitch,
                "Enable Simulated Attacks",
                getString(R.string.onboarding_enable_simulated_attacks_description),
                cancelable
        );
        TapTarget addPolicyTapTarget = OnboardingUtils.createTapTarget(
                addPolicyButton,
                "Add New Alarm",
                getString(R.string.onboarding_add_new_alarm_description),
                cancelable
        );
        TapTarget homeTapTarget = OnboardingUtils.createToolbarHomeTapTarget(context,
                findViewById(R.id.appToolbar),
                cancelable);

        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(enableSwitchTapTarget, addPolicyTapTarget, homeTapTarget)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        HomeActivity.continueOnboardingActivity(context,
                                SimulatedAttackSettingsActivity.class);
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {
                        if (lastTarget == enableSwitchTapTarget) {
                            enabledSwitch.setChecked(true);
                            addPolicyButton.setEnabled(true);
                        }
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {

                    }
                });

        activityUsePopup(cancelable, sequence::start, sequence::cancel);
    }

    /**
     * Explain the purpose of this activity.
     *
     * @param cancelable        true if this popup can be canceled by the user.
     * @param onDialogFinish    Runnable once the user has finished the popup.
     * @param onDialogCancel    Runnable if the user cancels the popup.
     */
    private void activityUsePopup(boolean cancelable,
                                  @Nullable Runnable onDialogFinish,
                                  @Nullable Runnable onDialogCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("SSimulated Attack Notifications");
        builder.setIcon(R.drawable.ic_launcher_foreground);
        builder.setCancelable(cancelable);
        builder.setMessage(R.string.simulated_attacks_description);
        builder.setPositiveButton("Continue", (dialogInterface, i) -> {
            if (null != onDialogFinish) {
                onDialogFinish.run();
            }
        });
        if (cancelable) {
            builder.setNeutralButton("Exit", ((dialogInterface, i) -> {
                if (null != onDialogCancel) {
                    onDialogCancel.run();
                }
            }));
        }

        builder.create().show();
    }
}
