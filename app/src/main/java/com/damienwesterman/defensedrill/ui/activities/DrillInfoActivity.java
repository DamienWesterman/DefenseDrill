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

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.utils.Utils;
import com.damienwesterman.defensedrill.ui.view_models.DrillInfoViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Displays information about a Drill and allows users to modify and take actions relating to it.
 * <br><br>
 * This is a generic activity that can be launched in a couple of ways. It can be launched as the
 * last part of drill generation, in which case it proceeds {@link SubCategorySelectActivity} and is
 * provided with a categoryId and subCategoryId so that we can generate a drill. It can also be
 * launched from {@link ViewDrillsActivity} by providing a single drillId. Depending on which route
 * is taken to launch, different functionality will exist.
 * <br><br>
 * INTENTS: Expects to receive ONE of the following -
 * <ul>
 *    <li> A {@link Constants#INTENT_CATEGORY_CHOICE} AND {@link Constants#INTENT_SUB_CATEGORY_CHOICE}
 *         intent. These are then used to generate a drill.</li>
 *    <li> A {@link Constants#INTENT_DRILL_ID} intent.</li>
 * </ul>
 */
public class DrillInfoActivity extends AppCompatActivity {
    /** Enum saving the current state of the activity. */
    private enum ActivityState {
        /** The activity is displaying a generated drill. */
        GENERATED_DRILL,
        /** We started at {@link ActivityState#GENERATED_DRILL}, but user skipped at least one Drill */
        REGENERATED_DRILL,
        /** This drill was not generated and we are only displaying its information */
        DISPLAYING_DRILL
    }

    private DrillInfoViewModel viewModel;
    private ActivityState activityState;

    private View rootView;
    private ProgressBar drillProgressBar;
    private TextView drillName;
    private TextView lastDrilledLabel;
    private TextView lastDrilledDate;
    private TextView confidenceLabel;
    private Spinner confidenceSpinner;
    private Button editCategoriesButton;
    private Button editSubCategoriesButton;
    private TextView notesLabel;
    private EditText notes;
    private Button regenerateButton;
    private Button resetSkippedDrillsButton;
    private Button markAsPracticedButton;
    private Button saveDrillInfoButton;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_info);

        saveViews();
        setUiLoading(true);

        if (getIntent().hasExtra(Constants.INTENT_DRILL_ID)) {
            activityState = ActivityState.DISPLAYING_DRILL;
        } else if (getIntent().hasExtra(Constants.INTENT_CATEGORY_CHOICE)
                    && getIntent().hasExtra(Constants.INTENT_SUB_CATEGORY_CHOICE)) {
            activityState = ActivityState.GENERATED_DRILL;
        } else {
            // Did not receive the proper intents. Toast so it persists screens
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.confidence_levels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        confidenceSpinner.setAdapter(adapter);

        setUpViewModel();
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void editCategories(View view) {
        editCategoriesPopup(viewModel.getAllCategories());
    }

    public void editSubCategories(View view) {
        editSubCategoriesPopup(viewModel.getAllSubCategories());
    }

    public void regenerateDrill(View view) {
        setUiLoading(true);
        activityState = ActivityState.REGENERATED_DRILL;
        viewModel.regenerateDrill();
    }

    public void resetSkippedDrills(View view) {
        viewModel.resetSkippedDrills();
        Utils.displayDismissibleSnackbar(rootView, "Skipped drills have been reset");
    }

    public void saveDrillInfo(View view) {
        Drill drill = collectDrillInfo();
        viewModel.saveDrill(drill, new OperationCompleteCallback() { // this method handles null check
            @Override
            public void onSuccess() {
                runOnUiThread(() -> Utils.displayDismissibleSnackbar(
                        rootView, "Successfully saved changes!"
                ));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Utils.displayDismissibleSnackbar(
                        rootView, error
                ));
            }
        });
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

    public void goHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
    private void editCategoriesPopup(List<CategoryEntity> categoryEntities) {
        Drill drill = viewModel.getDrill().getValue();
        if (null == drill || null == categoryEntities) {
            Utils.displayDismissibleSnackbar(rootView, "Issue retrieving categories");
            return;
        } else if (0 == categoryEntities.size()) {
            Utils.displayDismissibleSnackbar(rootView, "No Categories in database");
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
                    viewModel.saveDrill(drill, new OperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> Utils.displayDismissibleSnackbar(
                                    rootView, "Successfully saved changes!"
                            ));
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> Utils.displayDismissibleSnackbar(
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
    private void editSubCategoriesPopup(List<SubCategoryEntity> subCategoryEntities) {
        Drill drill = viewModel.getDrill().getValue();
        if (null == drill || null == subCategoryEntities) {
            Utils.displayDismissibleSnackbar(rootView, "Issue retrieving sub-categories");
            return;
        } else if (0 == subCategoryEntities.size()) {
            Utils.displayDismissibleSnackbar(rootView, "No sub-Categories in database");
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
                    viewModel.saveDrill(drill, new OperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            runOnUiThread(() -> Utils.displayDismissibleSnackbar(
                                    rootView, "Successfully saved changes!"
                            ));
                        }

                        @Override
                        public void onFailure(String error) {
                            runOnUiThread(() -> Utils.displayDismissibleSnackbar(
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
        Drill drill = collectDrillInfo();

        if (null == drill) {
            Utils.displayDismissibleSnackbar(rootView, "Issue marking as practiced");
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
            drill.setNewDrill(false);
            viewModel.saveDrill(drill, new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> Utils.displayDismissibleSnackbar(
                            rootView, "Successfully saved changes!"
                    ));
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> Utils.displayDismissibleSnackbar(
                            rootView, error
                    ));
                }
            });
            whatNextPopup();
        });
        builder.setNegativeButton("Skip", (dialog, position) -> {
            drill.setLastDrilled(System.currentTimeMillis());
            drill.setNewDrill(false);
            viewModel.saveDrill(drill, new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    // Do nothing
                }

                @Override
                public void onFailure(String error) {
                    // Do nothing
                }
            });
            whatNextPopup();
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

        if (ActivityState.GENERATED_DRILL == activityState
                || ActivityState.REGENERATED_DRILL == activityState) {
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
                        Intent intent = new Intent(this, CategorySelectActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        break;
                }
            });
        }

        builder.setPositiveButton("Go Home", (dialog, position) -> {
           Intent intent = new Intent(this, HomeActivity.class);
           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
           startActivity(intent);
        });
        builder.setNegativeButton("Back", (dialog, position) -> {
            if (ActivityState.DISPLAYING_DRILL == activityState) {
                finish();
            }
            // else if we are in drill generation, just allow it to close the popup
        });
        if (ActivityState.DISPLAYING_DRILL == activityState) {
            builder.setNeutralButton("Close popup", (dialog, position) -> { });
        }

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
        builder.setPositiveButton("Go Home", (dialog, position) -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        switch(activityState) {
            case REGENERATED_DRILL:
                // Fallthrough intentional
                builder.setNeutralButton("Reset skipped Drills", (dialog, position) -> {
                    setUiLoading(true);
                    viewModel.resetSkippedDrills();
                    activityState = ActivityState.GENERATED_DRILL;
                    viewModel.regenerateDrill();
                });
            case GENERATED_DRILL:
                builder.setNegativeButton("Select different Category", (dialog, position) -> {
                    Intent intent = new Intent(this, CategorySelectActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                });
                break;
            case DISPLAYING_DRILL:
                // Fallthrough intentional
            default:
                builder.setNegativeButton("Back", (dialog, position) -> finish());
                break;
        }

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

        viewModel.getDrill().observe(this, drill -> runOnUiThread(() -> {
            if (null == drill) {
                alertNoDrillFound();
                return;
            }

            fillDrillInfo(drill);
            setUiLoading(false);

            // TODO: Eventually incorporate functionality to display the links for how to
            //       descriptions and videos
        }));

        viewModel.loadAllCategories();
        viewModel.loadAllSubCategories();

        Drill drill = viewModel.getDrill().getValue();
        if (null == drill) {
            // First time loading activity
            Intent intent = getIntent();
            if (ActivityState.DISPLAYING_DRILL == activityState) {
                long drillId = intent.getLongExtra(Constants.INTENT_DRILL_ID, -1);
                viewModel.populateDrill(drillId);
            } else if (ActivityState.GENERATED_DRILL == activityState) {
                long categoryId = intent.getLongExtra(Constants.INTENT_CATEGORY_CHOICE, -1);
                long subCategoryId = intent.getLongExtra(Constants.INTENT_SUB_CATEGORY_CHOICE, -1);
                viewModel.populateDrill(categoryId, subCategoryId);
            } else {
                // Not in a correct state. Toast so it persists screens
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
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
        lastDrilledLabel = findViewById(R.id.lastDrilledLabel);
        lastDrilledDate = findViewById(R.id.lastDrilledDate);
        confidenceLabel = findViewById(R.id.confidenceLabel);
        confidenceSpinner = findViewById(R.id.confidenceSpinner);
        editCategoriesButton = findViewById(R.id.editCategoriesButton);
        editSubCategoriesButton = findViewById(R.id.editSubCategoriesButton);
        notesLabel = findViewById(R.id.notesLabel);
        notes = findViewById(R.id.notes);
        regenerateButton = findViewById(R.id.regenerateButton);
        resetSkippedDrillsButton = findViewById(R.id.resetSkippedDrillsButton);
        markAsPracticedButton = findViewById(R.id.markAsPracticedButton);
        saveDrillInfoButton = findViewById(R.id.saveDrillInfoButton);
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
            lastDrilledLabel.setVisibility(View.GONE);
            lastDrilledDate.setVisibility(View.GONE);
            confidenceLabel.setVisibility(View.GONE);
            confidenceSpinner.setVisibility(View.GONE);
            editCategoriesButton.setVisibility(View.GONE);
            editSubCategoriesButton.setVisibility(View.GONE);
            notesLabel.setVisibility(View.GONE);
            notes.setVisibility(View.GONE);
            regenerateButton.setVisibility(View.GONE);
            resetSkippedDrillsButton.setVisibility(View.GONE);
            markAsPracticedButton.setVisibility(View.GONE);
            saveDrillInfoButton.setVisibility(View.GONE);
        } else {
            drillProgressBar.setVisibility(View.GONE);
            drillName.setVisibility(View.VISIBLE);
            lastDrilledLabel.setVisibility(View.VISIBLE);
            lastDrilledDate.setVisibility(View.VISIBLE);
            confidenceLabel.setVisibility(View.VISIBLE);
            confidenceSpinner.setVisibility(View.VISIBLE);
            editCategoriesButton.setVisibility(View.VISIBLE);
            editSubCategoriesButton.setVisibility(View.VISIBLE);
            notesLabel.setVisibility(View.VISIBLE);
            notes.setVisibility(View.VISIBLE);
            if (ActivityState.GENERATED_DRILL == activityState
                    || ActivityState.REGENERATED_DRILL == activityState) {
                // Do not set these to visible unless we are in an activity state that uses them
                regenerateButton.setVisibility(View.VISIBLE);
                resetSkippedDrillsButton.setVisibility(View.VISIBLE);
            }
            markAsPracticedButton.setVisibility(View.VISIBLE);
            saveDrillInfoButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Create a drill object based on the the current user input on the screen. Handles and displays
     * errors.
     *
     * @return Drill object or null on error.
     */
    private @Nullable Drill collectDrillInfo() {
        Drill drill = viewModel.getDrill().getValue();
        if (null == drill) {
            Utils.displayDismissibleSnackbar(rootView, "An error occurred");
            return null;
        }

        drill.setConfidence(
                Constants.confidencePositionToWeight(confidenceSpinner.getSelectedItemPosition()));

        String notesString = notes.getText().toString();
        // Arbitrary limit just for display purposes, does not represent any actual limit in the
        // database layer
        final int NOTES_CHARACTER_LIMIT = 2048;
        if (NOTES_CHARACTER_LIMIT <= notesString.length()) {
            Utils.displayDismissibleSnackbar(rootView, "Notes must be less than "
                    + NOTES_CHARACTER_LIMIT + " characters");
            return null;
        }
        drill.setNotes(notes.getText().toString());

        return drill;
    }

    /**
     * Populate the screen with information retrieved from the drill.
     *
     * @param drill Drill used to populate the screen.
     */
    private void fillDrillInfo(Drill drill) {
        drillName.setText(drill.getName());
        confidenceSpinner.setSelection(Constants.confidenceWeightToPosition(drill.getConfidence()));
        Date drilledDate = new Date(drill.getLastDrilled());
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        lastDrilledDate.setText(dateFormatter.format(drilledDate));
        notes.setText(drill.getNotes());
    }
}