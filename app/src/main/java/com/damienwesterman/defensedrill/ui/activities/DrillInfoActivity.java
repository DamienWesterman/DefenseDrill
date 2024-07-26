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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.Utils;
import com.damienwesterman.defensedrill.ui.view_models.DrillInfoViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
 *         intent. These are then used to generate a pick a drill.</li>
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

    ProgressBar drillProgressBar;
    TextView drillName;
    TextView lastDrilledLabel;
    TextView lastDrilledDate;
    TextView confidenceLabel;
    Spinner confidenceSpinner;
    Button editCategoriesButton;
    Button editSubCategoriesButton;
    TextView notesLabel;
    EditText notes;
    Button regenerateButton;
    Button resetSkippedDrillsButton;
    Button markAsPracticedButton;
    Button saveDrillInfoButton;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_info);

        if (getIntent().hasExtra(Constants.INTENT_DRILL_ID)) {
            activityState = ActivityState.DISPLAYING_DRILL;
        } else {
            activityState = ActivityState.GENERATED_DRILL;
        }

        saveViews();

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
        changeUiToDrillLoading();
        activityState = ActivityState.REGENERATED_DRILL;
        viewModel.regenerateDrill();
    }

    public void resetSkippedDrills(View view) {
        viewModel.resetSkippedDrills();
        Utils.displayDismissibleSnackbar(findViewById(R.id.activityDrillInfo),
                "Skipped drills have been reset");
    }

    public void saveDrillInfo(View view) {
        Drill drill = collectDrillInfo();
        viewModel.saveDrill(drill, this); // this method handles null check and feedback
    }

    /**
     * Asks the user to confirm a new confidence level, saves the drill, then goes to the
     * {@link #createWhatNextPopup()}.
     *
     * @param view View.
     */
    public void markAsPracticed(View view) {
        AlertDialog getConfidencePopup = createConfidencePopup();
        if (null != getConfidencePopup) {
            getConfidencePopup.setOnDismissListener(dialog -> {
                AlertDialog whatNextPopup = createWhatNextPopup();
                whatNextPopup.show();
            });
            getConfidencePopup.show();
        }
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
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityDrillInfo),
                    "Issue retrieving categories");
            return;
        } else if (0 == categoryEntities.size()) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityDrillInfo),
                    "No Categories in database");
            return;
        }
        List<CategoryEntity> categories = drill.getCategories();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] categoryNames = categoryEntities
                .stream().map(CategoryEntity::getName).toArray(String[]::new);
        final boolean[] checkedCategories = new boolean[categoryNames.length];

        for (int i = 0; i < checkedCategories.length; i++) {
            if (categories.contains(categoryEntities.get(i))) {
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
                    if (checkedCategories[i] && !categories.contains(categoryEntities.get(i))) {
                        // Checked but not already in list - add
                        drill.addCategory(categoryEntities.get(i));
                    } else if (!checkedCategories[i]) {
                        // Not checked but in list - remove
                        drill.removeCategory(categoryEntities.get(i));
                    }
                    viewModel.saveDrill(drill, this);
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
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityDrillInfo),
                    "Issue retrieving sub-categories");
            return;
        } else if (0 == subCategoryEntities.size()) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityDrillInfo),
                    "No sub-Categories in database");
            return;
        }
        List<SubCategoryEntity> subCategories = drill.getSubCategories();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] subCategoryNames = subCategoryEntities
                .stream().map(SubCategoryEntity::getName).toArray(String[]::new);
        final boolean[] checkedSubCategories = new boolean[subCategoryNames.length];

        for (int i = 0; i < checkedSubCategories.length; i++) {
            if (subCategories.contains(subCategoryEntities.get(i))) {
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
                    if (checkedSubCategories[i] && !subCategories.contains(subCategoryEntities.get(i))) {
                        // Checked but not already in list - add
                        drill.addSubCategory(subCategoryEntities.get(i));
                    } else if (!checkedSubCategories[i]) {
                        // Not checked but in list - remove
                        drill.removeSubCategory(subCategoryEntities.get(i));
                    }
                    viewModel.saveDrill(drill, this);
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
     * Create and return a fully configured AlertDialog for a "create confidence" popup.
     * <br><br>
     * The popup will have a radio button group of
     * confidence levels for the user to select from. It will then save the new confidence level and
     * launch {@link #createWhatNextPopup()}.
     *
     * @return AlertDialog object or null if there is an issue getting the drill.
     */
    private @Nullable AlertDialog createConfidencePopup() {
        Drill drill = collectDrillInfo();

        if (null == drill) {
            return null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = getResources().getStringArray(R.array.confidence_levels);
        final int[] selectedOption = { Constants.confidenceWeightToPosition(drill.getConfidence())};

        builder.setTitle("Enter new confidence level:");
        builder.setIcon(R.drawable.thumbs_up_down_icon);
        // TODO allow the user to go back or something without saving, why did I do this??
        builder.setCancelable(false);
        builder.setSingleChoiceItems(options, selectedOption[0], (dialog, position) -> selectedOption[0] = position);
        builder.setPositiveButton("Save", (dialog, position) -> {
            drill.setConfidence(Constants.confidencePositionToWeight(selectedOption[0]));
            drill.setLastDrilled(System.currentTimeMillis());
            drill.setNewDrill(false);
            viewModel.saveDrill(drill, this);
            dialog.dismiss();
        });
        builder.setNegativeButton("Skip", (dialog, position) -> {
            drill.setLastDrilled(System.currentTimeMillis());
            drill.setNewDrill(false);
            viewModel.saveDrill(drill, null);
           dialog.dismiss();
        });

        return builder.create();
    }

    /**
     * Create and return a fully configured AlertDialog for a "what's next" popup.
     * <br><br>
     * Gives the user the option of what to do next, dependant on the activity type. WARNING: This
     * function may not return and may send us to another screen.
     *
     * @return AlertDialog object.
     */
    private AlertDialog createWhatNextPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] options = {"Next Drill", "New Category"};
        builder.setTitle("What next?");
        builder.setIcon(R.drawable.next_icon);
        builder.setCancelable(true);
        if (ActivityState.DISPLAYING_DRILL != activityState) {
            builder.setItems(options, (dialog, position) -> {
                switch (position) {
                    case 0:
                        // Next Drill
                        changeUiToDrillLoading();
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
        });
        if (ActivityState.DISPLAYING_DRILL == activityState) {
            builder.setNeutralButton("Close popup", (dialog, position) -> { });
        }

        return builder.create();
    }

    /**
     * Create and return a fully configured AlertDialog for when there is no drill to display.
     * <br><br>
     * This should be called in the error condition that there is no drill to display. Can be caused
     * by drill generation failing or a bad drill id. User feedback should be provided in the
     * message so the user will know what is going on.
     *
     * @param message   String error message to display to the user.
     * @return          AlertDialog object.
     */
    private AlertDialog createNoDrillPopup(@NonNull String message) {
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
                    changeUiToDrillLoading();
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
                builder.setNegativeButton("Back", (dialog, position) -> finish());
            default:

        }

        return builder.create();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Launch an intent to go to the home screen.
     *
     * @param view View.
     */
    private void goHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

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
            changeUiToDrillInfoShown();

            // TODO: Eventually incorporate functionality to display the links for how to
            //       descriptions and videos
        }));

        viewModel.loadAllCategories();
        viewModel.loadAllSubCategories();

        changeUiToDrillLoading();
        Drill drill = viewModel.getDrill().getValue();
        if (null == drill) {
            // First time loading activity
            Intent intent = getIntent();
            if (intent.hasExtra(Constants.INTENT_DRILL_ID)) {
                long drillId = intent.getLongExtra(Constants.INTENT_DRILL_ID, -1);
                viewModel.populateDrill(drillId);
            } else {
                long categoryId = intent.getLongExtra(Constants.INTENT_CATEGORY_CHOICE, -1);
                long subCategoryId = intent.getLongExtra(Constants.INTENT_SUB_CATEGORY_CHOICE, -1);
                viewModel.populateDrill(categoryId, subCategoryId);
            }
        } else {
            // Screen rotation or something, re-load existing drill from viewModel
            fillDrillInfo(drill);
            changeUiToDrillInfoShown();
        }
    }

    /**
     * Find and save all the views on the screen.
     */
    @SuppressLint("CutPasteId")
    private void saveViews() {
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
        if (ActivityState.DISPLAYING_DRILL != activityState) {
            regenerateButton = findViewById(R.id.regenerateButton);
            resetSkippedDrillsButton = findViewById(R.id.resetSkippedDrillsButton);
        }
        markAsPracticedButton = findViewById(R.id.markAsPracticedButton);
        saveDrillInfoButton = findViewById(R.id.saveDrillInfoButton);
    }

    /**
     * Wrapper to call {@link #createNoDrillPopup(String)} and determine the appropriate error
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
        createNoDrillPopup(alertMessage).show();
    }

    /**
     * Set the UI to hide all Drill elements and display loading bar.
     */
    private void changeUiToDrillLoading() {
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
        if (ActivityState.DISPLAYING_DRILL != activityState) {
            regenerateButton.setVisibility(View.GONE);
            resetSkippedDrillsButton.setVisibility(View.GONE);
        }
        markAsPracticedButton.setVisibility(View.GONE);
        saveDrillInfoButton.setVisibility(View.GONE);
    }

    /**
     * Display all drill elements and hide the loading bar.
     */
    private void changeUiToDrillInfoShown() {
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
        if (ActivityState.DISPLAYING_DRILL != activityState) {
            regenerateButton.setVisibility(View.VISIBLE);
            resetSkippedDrillsButton.setVisibility(View.VISIBLE);
        }
        markAsPracticedButton.setVisibility(View.VISIBLE);
        saveDrillInfoButton.setVisibility(View.VISIBLE);
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
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityDrillInfo),
                    "An error occurred");
            return null;
        }

        drill.setConfidence(
                Constants.confidencePositionToWeight(confidenceSpinner.getSelectedItemPosition()));

        String notesString = notes.getText().toString();
        // Arbitrary limit just for display purposes, does not represent any actual limit in the
        // database layer
        final int NOTES_CHARACTER_LIMIT = 2048;
        if (NOTES_CHARACTER_LIMIT <= notesString.length()) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityDrillInfo),
                    "Notes must be less than " + NOTES_CHARACTER_LIMIT + " characters");
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