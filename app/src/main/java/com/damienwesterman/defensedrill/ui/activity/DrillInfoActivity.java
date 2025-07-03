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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
import com.damienwesterman.defensedrill.data.remote.dto.InstructionsDTO;
import com.damienwesterman.defensedrill.data.remote.dto.RelatedDrillDTO;
import com.damienwesterman.defensedrill.ui.common.CommonPopups;
import com.damienwesterman.defensedrill.common.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.common.OnboardingUtils;
import com.damienwesterman.defensedrill.ui.common.UiUtils;
import com.damienwesterman.defensedrill.ui.viewmodel.DrillInfoViewModel;
import com.damienwesterman.defensedrill.common.Constants;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Displays information about a Drill and allows users to modify and take actions relating to it.
 * <br><br>
 * This is a generic activity that can be launched in a couple of ways. It can be launched as the
 * last part of drill generation, in which case it proceeds {@link SubCategorySelectActivity} and is
 * provided with a categoryId and subCategoryId so that we can generate a drill. It can also be
 * launched from {@link ViewDrillsActivity} by providing a single drillId. Depending on which route
 * is taken to launch, different functionality will exist.
 */
@AndroidEntryPoint
public class DrillInfoActivity extends AppCompatActivity {
    /** Enum saving the current state of the activity. */
    private enum ActivityState {
        /** The activity is displaying a generated drill. */
        GENERATED_DRILL,
        /** Similar to {@link ActivityState#GENERATED_DRILL}, but this is for onboarding and will
         * use a set dummy drill and walk the user through the screen. */
        ONBOARDING_DRILL,
        /** We started at {@link ActivityState#GENERATED_DRILL}, but user skipped at least one Drill. */
        REGENERATED_DRILL,
        /** This drill was not generated and we are only displaying its information. */
        DISPLAYING_DRILL,
        /** Same as {@link ActivityState#DISPLAYING_DRILL} but started from a simulated attack. */
        SIMULATED_ATTACK_DRILL
    }

    private DrillInfoViewModel viewModel;
    private ActivityState activityState;
    private Context context;
    @Inject
    SharedPrefs sharedPrefs;
    @Inject
    CommonPopups loginPopup;

    private View rootView;
    private ProgressBar drillProgressBar;
    private TextView drillName;
    private LinearLayout instructionsSelect;
    private Spinner instructionsSpinner;
    private LinearLayout relatedDrillsSelect;
    private Spinner relatedDrillsSpinner;
    private View divider;
    private ScrollView drillInfoDetails;
    private TextView lastDrilledDate;
    private Spinner confidenceSpinner;
    private EditText notes;
    private Button regenerateButton;
    private Button resetSkippedDrillsButton;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start DrillInfoActivity using a selected category and subcategory.
     *
     * @param context       Context.
     * @param categoryId    Category ID.
     * @param subCategoryId SubCategory ID.
     */
    public static void startActivity(@NonNull Context context, long categoryId, long subCategoryId) {
        Intent intent = new Intent(context, DrillInfoActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_CATEGORY_CHOICE, categoryId);
        intent.putExtra(Constants.INTENT_EXTRA_SUB_CATEGORY_CHOICE, subCategoryId);
        context.startActivity(intent);
    }

    /**
     * Starting DrillInfoActivity for a specific Drill.
     *
     * @param context   Context.
     * @param drillId   Drill ID.
     */
    public static void startActivity(@NonNull Context context, long drillId) {
        Intent intent = new Intent(context, DrillInfoActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_DRILL_ID, drillId);
        context.startActivity(intent);
    }

    /**
     * Create an intent to start the DrillInfoActivity for a specific Drill. Also indicates this is
     * from a simulated attack notification, meaning it will show a popup for instructions and offer
     * a help icon to show the popup again.
     *
     * @param context   Context.
     * @param drillId   Drill ID.
     * @return          Intent that can then start the DrillInfoActivity.
     */
    public static Intent createIntentToStartActivityFromSimulatedAttack(@NonNull Context context,
                                                                        long drillId) {
        Intent intent = new Intent(context, DrillInfoActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_DRILL_ID, drillId);
        intent.putExtra(Constants.INTENT_EXTRA_SIMULATED_ATTACK, "");
        return intent;
    }

    /**
     * Start the DrillInfoActivity in the Onboarding state.
     *
     * @param context   Context.
     */
    public static void startOnboardingActivity(@NonNull Context context) {
        Intent intent = new Intent(context, DrillInfoActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_CATEGORY_CHOICE, Constants.USER_RANDOM_SELECTION);
        intent.putExtra(Constants.INTENT_EXTRA_SUB_CATEGORY_CHOICE, Constants.USER_RANDOM_SELECTION);
        intent.putExtra(Constants.INTENT_EXTRA_START_ONBOARDING, "");
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_info);

        context = this;
        saveViews();
        setUiLoading(true);

        if (getIntent().hasExtra(Constants.INTENT_EXTRA_START_ONBOARDING)) {
            activityState = ActivityState.ONBOARDING_DRILL;
        } else if (getIntent().hasExtra(Constants.INTENT_EXTRA_SIMULATED_ATTACK)
                && getIntent().hasExtra(Constants.INTENT_EXTRA_DRILL_ID)) {
            activityState = ActivityState.SIMULATED_ATTACK_DRILL;
        } else if (getIntent().hasExtra(Constants.INTENT_EXTRA_DRILL_ID)) {
            activityState = ActivityState.DISPLAYING_DRILL;
        } else if (getIntent().hasExtra(Constants.INTENT_EXTRA_CATEGORY_CHOICE)
                    && getIntent().hasExtra(Constants.INTENT_EXTRA_SUB_CATEGORY_CHOICE)) {
            activityState = ActivityState.GENERATED_DRILL;
        } else {
            // Did not receive the proper intents. Toast so it persists screens
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Modify Toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        String toolbarTitle;
        if (ActivityState.GENERATED_DRILL == activityState
                || ActivityState.ONBOARDING_DRILL == activityState) {
            toolbarTitle = "Generate Drill";
        } else {
            toolbarTitle = "Drill Details";
        }
        appToolbar.setTitle(toolbarTitle);
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.confidence_levels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        confidenceSpinner.setAdapter(adapter);
        confidenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int confidence, long l) {
                Drill drill = viewModel.getUiCurrentDrill().getValue();
                // Only save if this changed
                if (null != drill &&
                        confidence != Constants.confidenceWeightToPosition(drill.getConfidence())) {
                    saveDrillInfo(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        setUpViewModel();

        if (ActivityState.SIMULATED_ATTACK_DRILL == activityState
                && sharedPrefs.isSimulatedAttackPopupDefault()) {
            simulatedAttackInstructionsPopup();
        }

        if (ActivityState.ONBOARDING_DRILL == activityState) {
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

        if (ActivityState.SIMULATED_ATTACK_DRILL == activityState) {
            menu.add(Menu.NONE, R.id.simulatedAttackInstructionsButton, Menu.NONE, "Attack Instructions")
                    .setIcon(R.drawable.danger_alert_icon)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.homeButton == item.getItemId()) {
            HomeActivity.startActivity(this);
            finish();
            return true;
        } else if (ActivityState.SIMULATED_ATTACK_DRILL == activityState
                && R.id.simulatedAttackInstructionsButton == item.getItemId()) {
            simulatedAttackInstructionsPopup();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveDrillInfo(false);
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void editCategories(View view) {
        if (null != viewModel.getAllCategories()) {
            editCategoriesPopup(viewModel.getAllCategories());
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Issue retrieving categories");
        }
    }

    public void editSubCategories(View view) {
        if (null != viewModel.getAllSubCategories()) {
            editSubCategoriesPopup(viewModel.getAllSubCategories());
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Issue retrieving sub-categories");
        }
    }

    public void regenerateDrill(View view) {
        setUiLoading(true);
        activityState = ActivityState.REGENERATED_DRILL;
        viewModel.regenerateDrill();
    }

    public void resetSkippedDrills(View view) {
        viewModel.resetSkippedDrills();
        UiUtils.displayDismissibleSnackbar(rootView, "Skipped drills have been reset");
    }

    /**
     * Asks the user to confirm a new confidence level, saves the drill, then goes to the
     * {@link #whatNextPopup()}.
     *
     * @param view View.
     */
    public void markAsPracticed(View view) {
        confidencePopup();
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    /**
     * Create and show a popup to edit the categories.
     * <br><br>
     * Displays the passed list as a popup of checked boxes, then saves the
     * checked categories to the drill.
     *
     * @param categoryEntities List of categories.
     */
    private void editCategoriesPopup(@NonNull List<CategoryEntity> categoryEntities) {
        Drill drill = viewModel.getUiCurrentDrill().getValue();
        if (null == drill) {
            UiUtils.displayDismissibleSnackbar(rootView, "Issue retrieving categories");
            return;
        } else if (categoryEntities.isEmpty()) {
            UiUtils.displayDismissibleSnackbar(rootView, "No Categories in database");
            return;
        }
        Set<CategoryEntity> checkedCategoryEntities = new HashSet<>(drill.getCategories());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] categoryNames = categoryEntities
                .stream().map(CategoryEntity::getName).toArray(String[]::new);
        final boolean[] checkedCategories = new boolean[categoryNames.length];

        for (int i = 0; i < checkedCategories.length; i++) {
            if (checkedCategoryEntities.contains(categoryEntities.get(i))) {
                checkedCategories[i] = true;
            }
        }

        builder.setTitle("Select Categories");
        builder.setIcon(R.drawable.edit_icon);
        builder.setMultiChoiceItems(categoryNames, checkedCategories,
                (dialog, position, isChecked) -> checkedCategories[position] = isChecked);
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Back", null);
        builder.setNeutralButton("Clear All", null);
        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // "Save"
                for (int i = 0; i < checkedCategories.length; i++) {
                    if (checkedCategories[i] && !checkedCategoryEntities.contains(categoryEntities.get(i))) {
                        // Checked but not already in list - add
                        drill.addCategory(categoryEntities.get(i));
                    } else if (!checkedCategories[i]) {
                        // Not checked - remove if in list
                        drill.removeCategory(categoryEntities.get(i));
                    }
                    viewModel.saveDrill(drill, false, new OperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> UiUtils.displayDismissibleSnackbar(
                                    rootView, "Successfully saved changes!"
                            ));
                        }

                        @Override
                        public void onFailure(@NonNull String error) {
                            runOnUiThread(() -> UiUtils.displayDismissibleSnackbar(
                                    rootView, "Failed to update categories"
                            ));
                        }
                    });
                }
                alert.dismiss();
            });
            alert.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view -> {
                // "Back"
                alert.dismiss();
            });
            alert.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                // "Clear All"
                Arrays.fill(checkedCategories, false);

                ListView listView = alert.getListView();
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, false);
                }
                // Do not dismiss
            });
        });

        alert.show();
    }

    /**
     * Create and show a popup to edit the sub-categories.
     * <br><br>
     * Displays the passed list as a popup of checked boxes, then saves the
     * checked sub-categories to the drill.
     *
     * @param subCategoryEntities List of sub-categories.
     */
    private void editSubCategoriesPopup(@NonNull List<SubCategoryEntity> subCategoryEntities) {
        Drill drill = viewModel.getUiCurrentDrill().getValue();
        if (null == drill) {
            UiUtils.displayDismissibleSnackbar(rootView, "Issue retrieving sub-categories");
            return;
        } else if (subCategoryEntities.isEmpty()) {
            UiUtils.displayDismissibleSnackbar(rootView, "No sub-Categories in database");
            return;
        }
        Set<SubCategoryEntity> checkedSubCategoryEntities = new HashSet<>(drill.getSubCategories());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] subCategoryNames = subCategoryEntities
                .stream().map(SubCategoryEntity::getName).toArray(String[]::new);
        final boolean[] checkedSubCategories = new boolean[subCategoryNames.length];

        for (int i = 0; i < checkedSubCategories.length; i++) {
            if (checkedSubCategoryEntities.contains(subCategoryEntities.get(i))) {
                checkedSubCategories[i] = true;
            }
        }

        builder.setTitle("Select sub-Categories");
        builder.setIcon(R.drawable.edit_icon);
        builder.setMultiChoiceItems(subCategoryNames, checkedSubCategories,
                (dialog, position, isChecked) -> checkedSubCategories[position] = isChecked);
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Back", null);
        builder.setNeutralButton("Clear All", null);
        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // "Save"
                for (int i = 0; i < checkedSubCategories.length; i++) {
                    if (checkedSubCategories[i] && !checkedSubCategoryEntities.contains(subCategoryEntities.get(i))) {
                        // Checked but not already in list - add
                        drill.addSubCategory(subCategoryEntities.get(i));
                    } else if (!checkedSubCategories[i]) {
                        // Not checked - remove if in list
                        drill.removeSubCategory(subCategoryEntities.get(i));
                    }
                    viewModel.saveDrill(drill, false, new OperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> UiUtils.displayDismissibleSnackbar(
                                    rootView, "Successfully saved changes!"
                            ));
                        }

                        @Override
                        public void onFailure(@NonNull String error) {
                            runOnUiThread(() -> UiUtils.displayDismissibleSnackbar(
                                    rootView, "Failed to update sub-categories"
                            ));
                        }
                    });
                }
                alert.dismiss();
            });
            alert.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view -> {
                // "Back"
                alert.dismiss();
            });
            alert.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                // "Clear All"
                Arrays.fill(checkedSubCategories, false);

                ListView listView = alert.getListView();
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, false);
                }
                // Do not dismiss
            });
        });

        alert.show();
    }

    /**
     * Create and show a popup to allow the user to set a new confidence level.
     * <br><br>
     * The popup will have a radio button group of confidence levels for the user to select from
     * after marking a drill as practiced. It will then save the new confidence level and launch
     * {@link #whatNextPopup()}.
     */
    private void confidencePopup() {
        Drill drill = collectDrillInfo(true);

        if (null == drill) {
            // User feedback offered in collectDrillInfo()
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = getResources().getStringArray(R.array.confidence_levels);
        final int[] selectedOption = { Constants.confidenceWeightToPosition(drill.getConfidence())};

        builder.setTitle("Enter new confidence level:");
        builder.setIcon(R.drawable.thumbs_up_down_icon);
        builder.setCancelable(true);
        builder.setSingleChoiceItems(options, selectedOption[0], (dialog, position) -> selectedOption[0] = position);
        builder.setPositiveButton("Save", (dialog, position) -> {
            drill.setConfidence(Constants.confidencePositionToWeight(selectedOption[0]));
            drill.setLastDrilled(System.currentTimeMillis());
            viewModel.saveDrill(drill, true, new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> UiUtils.displayDismissibleSnackbar(
                            rootView, "Successfully saved changes!"
                    ));
                }

                @Override
                public void onFailure(@NonNull String error) {
                    runOnUiThread(() -> UiUtils.displayDismissibleSnackbar(
                            rootView, error
                    ));
                }
            });
            if (ActivityState.GENERATED_DRILL == activityState
                    || ActivityState.REGENERATED_DRILL == activityState) {
                whatNextPopup();
            }
        });
        builder.setNegativeButton("Skip", (dialog, position) -> {
            drill.setLastDrilled(System.currentTimeMillis());
            viewModel.saveDrill(drill, true, new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    // Do nothing
                }

                @Override
                public void onFailure(@NonNull String error) {
                    // Do nothing
                }
            });
            if (ActivityState.GENERATED_DRILL == activityState
                    || ActivityState.REGENERATED_DRILL == activityState) {
                whatNextPopup();
            }
        });

        builder.create().show();
    }

    /**
     * Create and show a popup allowing the user to decide what to do next.
     * <br><br>
     * Gives the user the option of what to do next, dependant on the activity type. WARNING: This
     * function may not return and may send us to another screen.
     */
    private void whatNextPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] options = {"Next Drill", "New Category"};
        builder.setTitle("What next?");
        builder.setIcon(R.drawable.next_icon);
        builder.setCancelable(true);

        builder.setItems(options, (dialog, position) -> {
            switch (position) {
                case 0:
                    // Next Drill
                    setUiLoading(true);
                    activityState = ActivityState.REGENERATED_DRILL;
                    viewModel.regenerateDrill();
                    break;
                case 1:
                    // New Category
                    CategorySelectActivity.startActivityClearTop(this);
                    break;
            }
        });

        builder.setNegativeButton("Back", null);

        builder.create().show();
    }

    /**
     * Create and show popup for when there is no drill to display.
     * <br><br>
     * This should be called in the error condition that there is no drill to display. Can be caused
     * by drill generation failing or a bad drill id. User feedback should be provided in the
     * message so the user will know what is going on.
     *
     * @param message   String error message to display to the user.
     */
    private void noDrillPopup(@NonNull String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Drill not found!");
        builder.setIcon(R.drawable.warning_icon);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("Go Home", (dialog, position) ->
                HomeActivity.startActivity(this));
        switch(activityState) {
            case REGENERATED_DRILL:
                builder.setNeutralButton("Reset skipped Drills", (dialog, position) -> {
                    setUiLoading(true);
                    viewModel.resetSkippedDrills();
                    activityState = ActivityState.GENERATED_DRILL;
                    viewModel.regenerateDrill();
                });
                // Fallthrough intentional
            case GENERATED_DRILL:
                builder.setNegativeButton("Select different Category", (dialog, position) ->
                        CategorySelectActivity.startActivityClearTop(this));
                break;
            case DISPLAYING_DRILL:
                // Fallthrough intentional
            case SIMULATED_ATTACK_DRILL:
                // Fallthrough intentional
            default:
                builder.setNegativeButton("Back", (dialog, position) -> finish());
                break;
        }

        builder.create().show();
    }

    /**
     * Display a popup with helpful instructions for dealing with a simulated attack. Allows the
     * user to set the default behavior if this popup is shown for every simulated attack.
     */
    private void simulatedAttackInstructionsPopup() {
        boolean showPopupDefault = sharedPrefs.isSimulatedAttackPopupDefault();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Instructions");
        builder.setIcon(R.drawable.danger_alert_icon);
        builder.setCancelable(true);
        builder.setMessage(R.string.simulated_attack_instructions);
        builder.setNeutralButton(showPopupDefault ? "Don't Show Every Time" : "Show Every Time",
            ((dialogInterface, i) -> {
                if (showPopupDefault) {
                    // Don't show every time
                    sharedPrefs.setSimulatedAttackPopupDefault(false);
                    UiUtils.displayDismissibleSnackbar(rootView, "To see Instructions, click the Alert Button at the top right.");
                } else {
                    // Show every time
                    sharedPrefs.setSimulatedAttackPopupDefault(true);
                    UiUtils.displayDismissibleSnackbar(rootView, "Instructions will show every time!");
                }
            }));
        builder.setPositiveButton("OK", null);
        builder.create().show();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Set up the view model and populate the screen.
     */
    private void setUpViewModel() {
        viewModel = new ViewModelProvider(this).get(DrillInfoViewModel.class);

        viewModel.getUiCurrentDrill().observe(this, drill -> runOnUiThread(() -> {
            if (null == drill) {
                alertNoDrillFound();
                return;
            }

            fillDrillInfo(drill);
            setUiLoading(false);

            if (null != drill.getServerDrillId() && !sharedPrefs.getJwt().isEmpty()) {
                // Keep the spinner visible until instructions and related drills are loaded (or not)
                drillProgressBar.setVisibility(View.VISIBLE);
                instructionsSelect.setVisibility(View.GONE);
                relatedDrillsSelect.setVisibility(View.GONE);
                viewModel.loadNetworkLinks(
                        () -> loginPopup.displayLoginPopup(new OperationCompleteCallback() {
                            @Override
                            public void onSuccess() {
                                // User successfully logged back in, restart the activity
                                onRestart();
                            }

                            @Override
                            public void onFailure(@NonNull String error) {
                                UiUtils.displayDismissibleSnackbar(rootView,
                                        "You are not signed in.");
                                /*
                                User has manually selected not to log in or cannot log
                                in. Clear out the jwt so we do not keep doing a popup.
                                 */
                                sharedPrefs.setJwt("");
                                drillProgressBar.setVisibility(View.GONE);
                            }
                        }),
                        errorMessage -> {
                            drillProgressBar.setVisibility(View.GONE);
                            UiUtils.displayDismissibleSnackbar(rootView, errorMessage);
                        }
                );
            }
        }));

        viewModel.getUiInstructionsList().observe(this, instructions ->
                runOnUiThread(() -> setUpInstructions(instructions)));

        viewModel.getUiRelatedDrillsList().observe(this, relatedDrills ->
                runOnUiThread(() -> setUpRelatedDrills(relatedDrills)));

        viewModel.loadAllCategories();
        viewModel.loadAllSubCategories();

        Drill drill = viewModel.getUiCurrentDrill().getValue();
        if (null == drill) {
            // First time loading activity
            Intent intent = getIntent();
            switch (activityState) {
                case SIMULATED_ATTACK_DRILL:
                    // Fallthrough intentional
                case DISPLAYING_DRILL:
                    long drillId = intent.getLongExtra(Constants.INTENT_EXTRA_DRILL_ID, -1);
                    viewModel.populateDrill(drillId);
                    break;
                case GENERATED_DRILL:
                    long categoryId =intent
                            .getLongExtra(Constants.INTENT_EXTRA_CATEGORY_CHOICE, -1);
                    long subCategoryId = intent
                            .getLongExtra(Constants.INTENT_EXTRA_SUB_CATEGORY_CHOICE, -1);
                    viewModel.populateDrill(categoryId, subCategoryId);
                    break;
                case ONBOARDING_DRILL:
                    final long dummyTime = 1750781942000L; // June 24th, 2025 16:19 GMT
                    Drill dummyDrill = new Drill("Jab", dummyTime, Drill.MEDIUM_CONFIDENCE,
                            "Remember to keep your back hand up!", null, true, List.of(), List.of());
                    viewModel.getUiCurrentDrill().setValue(dummyDrill);
                    break;
                case REGENERATED_DRILL:
                    // Fallthrough intentional
                default:
                    // Not in a correct state. Toast so it persists screens
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        } else {
            // Screen rotation or something, re-load existing drill from viewModel
            fillDrillInfo(drill);
            setUiLoading(false);
        }
    }

    /**
     * Find and save all the views on the screen.
     */
    @SuppressLint("CutPasteId")
    private void saveViews() {
        rootView = findViewById(R.id.activityDrillInfo);
        drillProgressBar = findViewById(R.id.drillProgressBar);
        drillName = findViewById(R.id.drillName);
        instructionsSelect = findViewById(R.id.instructionsSelect);
        instructionsSpinner = findViewById(R.id.instructionsSpinner);
        relatedDrillsSelect = findViewById(R.id.relatedDrillsSelect);
        relatedDrillsSpinner = findViewById(R.id.relatedDrillsSpinner);
        divider = findViewById(R.id.drillInfoDivider);
        drillInfoDetails = findViewById(R.id.drillInfoDetails);
        lastDrilledDate = findViewById(R.id.lastDrilledDate);
        confidenceSpinner = findViewById(R.id.confidenceSpinner);
        notes = findViewById(R.id.notes);
        regenerateButton = findViewById(R.id.regenerateButton);
        resetSkippedDrillsButton = findViewById(R.id.resetSkippedDrillsButton);
    }

    /**
     * Wrapper to call {@link #noDrillPopup(String)} and determine the appropriate error
     * message.
     */
    private void alertNoDrillFound() {
        String alertMessage;

        switch(activityState) {
            case GENERATED_DRILL:
                alertMessage = getString(R.string.no_drill_by_category_sub_category);
                break;
            case REGENERATED_DRILL:
                alertMessage = getString(R.string.no_drills_left);
                break;
            case DISPLAYING_DRILL:
                // Fallthrough intentional
            case SIMULATED_ATTACK_DRILL:
                // Fallthrough intentional
            default:
                alertMessage = getString(R.string.no_drill_found_by_id);
                break;
        }
        noDrillPopup(alertMessage);
    }

    /**
     * Set the UI to hide all Drill elements and display loading bar or vise versa.
     *
     * @param loading boolean if we want to have the UI as loading or not.
     */
    private void setUiLoading(boolean loading) {
        if (loading) {
            drillProgressBar.setVisibility(View.VISIBLE);
            drillName.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            drillInfoDetails.setVisibility(View.GONE);
        } else {
            drillProgressBar.setVisibility(View.GONE);
            drillName.setVisibility(View.VISIBLE);
            divider.setVisibility(View.VISIBLE);
            drillInfoDetails.setVisibility(View.VISIBLE);
            if (ActivityState.GENERATED_DRILL == activityState
                    || ActivityState.ONBOARDING_DRILL == activityState
                    || ActivityState.REGENERATED_DRILL == activityState) {
                // Do not set these to visible unless we are in an activity state that uses them
                regenerateButton.setVisibility(View.VISIBLE);
                resetSkippedDrillsButton.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Create a drill object based on the the current user input on the screen. Handles and displays
     * errors.
     *
     * @param displayError  Should we display error message to the user.
     * @return              Drill object or null on error.
     */
    @Nullable
    private Drill collectDrillInfo(boolean displayError) {
        Drill drill = viewModel.getUiCurrentDrill().getValue();
        if (null == drill) {
            if (displayError) {
                UiUtils.displayDismissibleSnackbar(rootView, "An error occurred");
            }
            return null;
        }

        drill.setConfidence(
                Constants.confidencePositionToWeight(confidenceSpinner.getSelectedItemPosition()));

        String notesString = notes.getText().toString();
        // Arbitrary limit just for display purposes, does not represent any actual limit in the
        // database layer
        final int NOTES_CHARACTER_LIMIT = 2048;
        if (NOTES_CHARACTER_LIMIT <= notesString.length()) {
            if (displayError) {
                UiUtils.displayDismissibleSnackbar(rootView, "Notes must be less than "
                        + NOTES_CHARACTER_LIMIT + " characters");
            }
            return null;
        }
        drill.setNotes(notes.getText().toString());

        return drill;
    }

    /**
     * Collect and save the current drill info on screen to the database.
     *
     * @param displayFeedback   Should we display success/error messages to the user.
     */
    public void saveDrillInfo(boolean displayFeedback) {
        Drill drill = collectDrillInfo(displayFeedback);
        if (null != drill) {
            viewModel.saveDrill(drill, false, new OperationCompleteCallback() { // this method handles null check
                @Override
                public void onSuccess() {
                    if (displayFeedback) {
                        UiUtils.displayDismissibleSnackbar(rootView, "Successfully saved changes!");
                    }
                }

                @Override
                public void onFailure(@NonNull String error) {
                    if (displayFeedback) {
                        UiUtils.displayDismissibleSnackbar(rootView, error);
                    }
                }
            });
        } else {
            if (displayFeedback) {
                UiUtils.displayDismissibleSnackbar(rootView, "Something went wrong.");
            }
        }
    }

    /**
     * Populate the screen with information retrieved from the drill.
     *
     * @param drill Drill used to populate the screen.
     */
    private void fillDrillInfo(Drill drill) {
        drillName.setText(drill.getName());
        confidenceSpinner.setSelection(Constants.confidenceWeightToPosition(drill.getConfidence()));
        if (!drill.isNewDrill()) {
            Date drilledDate = new Date(drill.getLastDrilled());
            DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            lastDrilledDate.setText(dateFormatter.format(drilledDate));
        } else {
            lastDrilledDate.setText("-");
        }
        notes.setText(drill.getNotes());
    }

    /**
     * Launch the activity to view a specific set of instructions.
     *
     * @param instructionsIndex Index position of the instructions in the list returned by
*    *                          {@link DrillInfoViewModel#getUiInstructionsList()}.
     */
    public void viewInstructions(int instructionsIndex) {
        DrillDTO drillDTO = viewModel.getDrillDTO();
        if (null == drillDTO
                || 0 > instructionsIndex
                || drillDTO.getInstructions().size() <= instructionsIndex) {
            UiUtils.displayDismissibleSnackbar(rootView, "Issue loading Instructions");
            return;
        }

        InstructionsActivity.startActivity(this, drillDTO, instructionsIndex);
    }

    /**
     * Launch the activity to view a specific related drill.
     *
     * @param relatedDrillIndex Index position of the related drill in the list returned by
     *                          {@link DrillInfoViewModel#getUiRelatedDrillsList()}.
     */
    public void viewRelatedDrills(int relatedDrillIndex) {
        DrillDTO drillDTO = viewModel.getDrillDTO();
        if (null == drillDTO
                || 0 > relatedDrillIndex
                || drillDTO.getRelatedDrills().size() <= relatedDrillIndex) {
            UiUtils.displayDismissibleSnackbar(rootView, "Issue loading Related Drill");
            return;
        }

        viewModel.findDrillIdByServerId(
            drillDTO.getRelatedDrills().get(relatedDrillIndex).getId(),
            localDrillId -> {
                if (null == localDrillId) {
                    UiUtils.displayDismissibleSnackbar(rootView, "Issue loading Related Drill");
                } else {
                    DrillInfoActivity.startActivity(this, localDrillId);
                }
            });
    }

    /**
     * Set up the UI for the instructions drop down menu.
     *
     * @param instructions List of InstructionDTO objects.
     */
    private void setUpInstructions(@NonNull List<InstructionsDTO> instructions) {
        if (!instructions.isEmpty()) {
            List<String> formattedInstructions = new ArrayList<>(instructions.size() + 1);
            formattedInstructions.add("Select one...");
            formattedInstructions.addAll(instructions.stream()
                    .map(InstructionsDTO::getDescription)
                    .collect(Collectors.toList()));

            ArrayAdapter<String> arr = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    formattedInstructions);
            arr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            instructionsSpinner.setAdapter(arr);
            instructionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                    // First index position is "Select One"
                    if (0 < pos) {
                        viewInstructions(pos - 1);
                    }
                    // Reset back to original position for activity re-load
                    instructionsSpinner.setSelection(0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Do nothing
                }
            });

            instructionsSelect.setVisibility(View.VISIBLE);
        }

        drillProgressBar.setVisibility(View.GONE);
    }

    /**
     * Set up the UI for the related drills drop down menu.
     *
     * @param relatedDrills List of RelatedDrillDTO objects
     */
    private void setUpRelatedDrills(@NonNull List<RelatedDrillDTO> relatedDrills) {
        if (!relatedDrills.isEmpty()) {
            List<String> formattedRelatedDrills = new ArrayList<>(relatedDrills.size() + 1);
            formattedRelatedDrills.add("Select one...");
            formattedRelatedDrills.addAll(relatedDrills.stream()
                    .map(RelatedDrillDTO::getName)
                    .collect(Collectors.toList()));

            ArrayAdapter<String> arr = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    formattedRelatedDrills);
            arr.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            relatedDrillsSpinner.setAdapter(arr);
            relatedDrillsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                    // First index position is "Select One"
                    if (0 < pos) {
                        viewRelatedDrills(pos - 1);
                    }
                    // Reset back to original position for activity re-load
                    relatedDrillsSpinner.setSelection(0);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                    // Do nothing
                }
            });

            relatedDrillsSelect.setVisibility(View.VISIBLE);
        }

        drillProgressBar.setVisibility(View.GONE);
    }

    // =============================================================================================
    // Onboarding Methods
    // =============================================================================================
    /**
     * Start the onboarding process for this activity, walking the user through the screen and
     * explaining how it works. Preceded by
     * {@link SubCategorySelectActivity#startOnboardingActivity(Context)}, proceeded by
     * {@link HomeActivity#continueOnboardingActivity(Context, Class)} from DrillInfoActivity.class.
     */
    private void startOnboarding() {
        boolean cancelable = sharedPrefs.isOnboardingComplete();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        TapTarget confidenceTapTarget = OnboardingUtils.createTapTarget(
                findViewById(R.id.confidenceContainer),
                "Confidence",
                getString(R.string.onboarding_confidence_description),
                cancelable)
                // This view is a little awkwardly shaped, so encompass the entire view
                .targetRadius(calculateTargetRadius(findViewById(R.id.confidenceContainer)));
        TapTarget lastDrilledTapTarget = OnboardingUtils.createTapTarget(
                findViewById(R.id.lastDrilledContainer),
                "Last Drilled Date",
                getString(R.string.onboarding_last_drilled_description),
                cancelable)
                // This view is a little awkwardly shaped, so encompass the entire view
                .targetRadius(calculateTargetRadius(findViewById(R.id.lastDrilledContainer)));
        TapTarget regenerateTapTarget = OnboardingUtils.createTapTarget(
                regenerateButton,
                "Skip Drill",
                getString(R.string.onboarding_skip_drill_description),
                cancelable);
        TapTarget resetSkippedDrillsTapTarget = OnboardingUtils.createTapTarget(
                resetSkippedDrillsButton,
                "Reset Skipped Drills",
                getString(R.string.onboarding_reset_skipped_drills_description),
                cancelable);
        TapTarget markAsPracticedTapTarget = OnboardingUtils.createTapTarget(
                findViewById(R.id.markAsPracticed),
                "Mark as Practiced",
                getString(R.string.onboarding_mark_as_practiced_description),
                cancelable);
        TapTarget homeTapTarget = OnboardingUtils.createToolbarHomeTapTarget(context,
                findViewById(R.id.appToolbar),
                cancelable);

        TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(confidenceTapTarget, lastDrilledTapTarget, regenerateTapTarget,
                        resetSkippedDrillsTapTarget, markAsPracticedTapTarget, homeTapTarget)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        HomeActivity.continueOnboardingActivity(context, DrillInfoActivity.class);
                    }

                    @Override
                    public void onSequenceStep(TapTarget lastTarget, boolean targetClicked) {

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
     * @param onCancelCallback  Runnable for if the user cancels the callback. Only applicable if
     *                          cancelable is true.
     */
    private void activityUsePopup(boolean cancelable,
                                  @Nullable Runnable onDialogFinish,
                                  @Nullable Runnable onCancelCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Drill Information");
        builder.setIcon(R.drawable.ic_launcher_foreground);
        builder.setCancelable(cancelable);
        builder.setMessage(R.string.onboarding_drill_info_activity_details);
        builder.setPositiveButton("Continue", (dialogInterface, i) -> {
            if (null != onDialogFinish) {
                onDialogFinish.run();
            }
        });
        if (cancelable) {
            builder.setNeutralButton("Exit", (dialogInterface, i) -> {
                if (null != onCancelCallback) {
                    onCancelCallback.run();
                }
            });
        }

        builder.create().show();
    }

    /**
     * Calculate target radius in dp for TapTarget from a view.
     * @param view  View to calculate the TapTarget radius for.
     * @return      TapTarget radius in dp.
     */
    private int calculateTargetRadius(View view) {
        int radiusPx = view.getWidth() / 2;
        return (int) (radiusPx / getResources().getDisplayMetrics().density);
    }
}
