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
 * TODO doc comments
 * TODO Notate the required intents
 */
public class DrillInfoActivity extends AppCompatActivity {
    // TODO doc comments
    private enum ScreenType {
        GENERATED_DRILL,
        REGENERATED_DRILL,
        DISPLAYING_DRILL
    }

    private DrillInfoViewModel viewModel;
    private ScreenType screenType;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_info);

        if (getIntent().hasExtra(Constants.INTENT_DRILL_ID)) {
            screenType = ScreenType.DISPLAYING_DRILL;
        } else {
            screenType = ScreenType.GENERATED_DRILL;
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

    public void editCategories(View view) {
        editCategoriesPopup(viewModel.getAllCategories());
    }

    public void editSubCategories(View view) {
        editSubCategoriesPopup(viewModel.getAllSubCategories());
    }

    public void regenerateDrill(View view) {
        changeUiToDrillLoading();
        screenType = ScreenType.REGENERATED_DRILL;
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

    public void goHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

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
        if (ScreenType.DISPLAYING_DRILL != screenType) {
            regenerateButton = findViewById(R.id.regenerateButton);
            resetSkippedDrillsButton = findViewById(R.id.resetSkippedDrillsButton);
        }
        markAsPracticedButton = findViewById(R.id.markAsPracticedButton);
        saveDrillInfoButton = findViewById(R.id.saveDrillInfoButton);
    }

    private void alertNoDrillFound() {
        AlertDialog noDrillPopup;
        String alertMessage;

        switch(screenType) {
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
        noDrillPopup = createNoDrillPopup(alertMessage);

        if (null != noDrillPopup) {
            noDrillPopup.show();
        }
    }

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
        if (ScreenType.DISPLAYING_DRILL != screenType) {
            regenerateButton.setVisibility(View.GONE);
            resetSkippedDrillsButton.setVisibility(View.GONE);
        }
        markAsPracticedButton.setVisibility(View.GONE);
        saveDrillInfoButton.setVisibility(View.GONE);
    }

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
        if (ScreenType.DISPLAYING_DRILL != screenType) {
            regenerateButton.setVisibility(View.VISIBLE);
            resetSkippedDrillsButton.setVisibility(View.VISIBLE);
        }
        markAsPracticedButton.setVisibility(View.VISIBLE);
        saveDrillInfoButton.setVisibility(View.VISIBLE);
    }

    /**
     *
     * @return May return null // TODO
     */
    private Drill collectDrillInfo() {
        Drill drill = viewModel.getDrill().getValue();
        if (null != drill) {
            drill.setConfidence(
                    Constants.confidencePositionToWeight(confidenceSpinner.getSelectedItemPosition()));
            drill.setNotes(notes.getText().toString());
        }
        return drill;
    }

    private void fillDrillInfo(Drill drill) {
        drillName.setText(drill.getName());
        confidenceSpinner.setSelection(Constants.confidenceWeightToPosition(drill.getConfidence()));
        Date drilledDate = new Date(drill.getLastDrilled());
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        lastDrilledDate.setText(dateFormatter.format(drilledDate));
        notes.setText(drill.getNotes());
    }

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

    // TODO Doc comments nullable
    private AlertDialog createConfidencePopup() {
        Drill drill = collectDrillInfo();

        if (null == drill) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityDrillInfo),
                    "Issue marking as practiced");
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = getResources().getStringArray(R.array.confidence_levels);
        final int[] selectedOption = { Constants.confidenceWeightToPosition(drill.getConfidence())};

        builder.setTitle("Enter new confidence level:");
        builder.setIcon(R.drawable.thumbs_up_down_icon);
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

    // TODO Doc comments may not return
    private AlertDialog createWhatNextPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence[] options = {"Next Drill", "New Category"};
        builder.setTitle("What next?");
        builder.setIcon(R.drawable.next_icon);
        builder.setCancelable(true);
        if (ScreenType.DISPLAYING_DRILL != screenType) {
            builder.setItems(options, (dialog, position) -> {
                switch (position) {
                    case 0:
                        // Next Drill
                        changeUiToDrillLoading();
                        screenType = ScreenType.REGENERATED_DRILL;
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
            if (ScreenType.DISPLAYING_DRILL == screenType) {
                finish();
            }
        });
        if (ScreenType.DISPLAYING_DRILL == screenType) {
            builder.setNeutralButton("Close popup", (dialog, position) -> { });
        }

        return builder.create();
    }

    private AlertDialog createNoDrillPopup(String message) {
        if (null == message) {
            return null;
        }

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
        switch(screenType) {
            case REGENERATED_DRILL:
                // Fallthrough intentional
                builder.setNeutralButton("Reset skipped Drills", (dialog, position) -> {
                    changeUiToDrillLoading();
                    viewModel.resetSkippedDrills();
                    screenType = ScreenType.GENERATED_DRILL;
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
}