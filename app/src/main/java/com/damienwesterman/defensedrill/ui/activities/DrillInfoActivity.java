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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.view_models.DrillInfoViewModel;
import com.damienwesterman.defensedrill.utils.Constants;
import com.google.android.material.snackbar.Snackbar;

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
    private static final int LOW_CONFIDENCE_POSITION = 0;
    private static final int MEDIUM_CONFIDENCE_POSITION = 1;
    private static final int HIGH_CONFIDENCE_POSITION = 2;

    // TODO doc comments
    private enum ScreenType {
        GeneratedDrill,
        DisplayingDrill
    }

    private DrillInfoViewModel viewModel;
    private ScreenType screenType;

    ProgressBar drillProgressBar;
    TextView drillName;
    View drillInfoDivider;
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

    Snackbar categoriesLoadingSnackbar;
    Snackbar subCategoriesLoadingSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_info);

        if (getIntent().hasExtra(Constants.INTENT_DRILL_ID)) {
            screenType = ScreenType.DisplayingDrill;
        } else {
            screenType = ScreenType.GeneratedDrill;
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
        categoriesLoadingSnackbar.show();
        viewModel.loadAllCategories();
    }

    public void editSubCategories(View view) {
        subCategoriesLoadingSnackbar.show();
        viewModel.loadAllSubCategories();
    }

    public void regenerateDrill(View view) {
        changeUiToDrillLoading();
        viewModel.regenerateDrill();
    }

    public void resetSkippedDrills(View view) {
        viewModel.resetSkippedDrills();
        Toast.makeText(this, "Skipped drills have been reset", Toast.LENGTH_SHORT).show();
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

        viewModel.getAllCategories().observe(this, this::editCategoriesPopup);

        viewModel.getAllSubCategories().observe(this, this::editSubCategoriesPopup);

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.INTENT_DRILL_ID)) {
            long drillId = intent.getLongExtra(Constants.INTENT_DRILL_ID, -1);
            viewModel.populateDrill(drillId);
        } else {
            long categoryId = intent.getLongExtra(Constants.INTENT_CATEGORY_CHOICE, -1);
            long subCategoryId = intent.getLongExtra(Constants.INTENT_SUB_CATEGORY_CHOICE, -1);
            viewModel.populateDrill(categoryId, subCategoryId);
        }
    }

    @SuppressLint("CutPasteId")
    private void saveViews() {
        drillProgressBar = findViewById(R.id.drillProgressBar);
        drillName = findViewById(R.id.drillName);
        drillInfoDivider = findViewById(R.id.drillInfoDivider);
        lastDrilledLabel = findViewById(R.id.lastDrilledLabel);
        lastDrilledDate = findViewById(R.id.lastDrilledDate);
        confidenceLabel = findViewById(R.id.confidenceLabel);
        confidenceSpinner = findViewById(R.id.confidenceSpinner);
        editCategoriesButton = findViewById(R.id.editCategoriesButton);
        editSubCategoriesButton = findViewById(R.id.editSubCategoriesButton);
        notesLabel = findViewById(R.id.notesLabel);
        notes = findViewById(R.id.notes);
        if (ScreenType.GeneratedDrill == screenType) {
            regenerateButton = findViewById(R.id.regenerateButton);
            resetSkippedDrillsButton = findViewById(R.id.resetSkippedDrillsButton);
        }
        markAsPracticedButton = findViewById(R.id.markAsPracticedButton);
        saveDrillInfoButton = findViewById(R.id.saveDrillInfoButton);
        categoriesLoadingSnackbar = Snackbar.make(findViewById(R.id.activityDrillInfo),
                "Loading Categories...", Snackbar.LENGTH_INDEFINITE);
        subCategoriesLoadingSnackbar = Snackbar.make(findViewById(R.id.activityDrillInfo),
                "Loading sub-Categories...", Snackbar.LENGTH_INDEFINITE);
    }

    private void alertNoDrillFound() {
        // TODO Check what screenType we are in and change what we display, and offer to go home or reset skipped drills (and regenerate) or go back
        // Avenues: ID not found | No matching drill from Cat / SubCat | Skipped drill and none left | Mark as practiced and none left
    }

    private void changeUiToDrillLoading() {
        drillProgressBar.setVisibility(View.VISIBLE);
        drillName.setVisibility(View.GONE);
        drillInfoDivider.setVisibility(View.GONE);
        lastDrilledLabel.setVisibility(View.GONE);
        lastDrilledDate.setVisibility(View.GONE);
        confidenceLabel.setVisibility(View.GONE);
        confidenceSpinner.setVisibility(View.GONE);
        editCategoriesButton.setVisibility(View.GONE);
        editSubCategoriesButton.setVisibility(View.GONE);
        notesLabel.setVisibility(View.GONE);
        notes.setVisibility(View.GONE);
        if (ScreenType.GeneratedDrill == screenType) {
            regenerateButton.setVisibility(View.GONE);
            resetSkippedDrillsButton.setVisibility(View.GONE);
        }
        markAsPracticedButton.setVisibility(View.GONE);
        saveDrillInfoButton.setVisibility(View.GONE);
    }

    private void changeUiToDrillInfoShown() {
        drillProgressBar.setVisibility(View.GONE);
        drillName.setVisibility(View.VISIBLE);
        drillInfoDivider.setVisibility(View.VISIBLE);
        lastDrilledLabel.setVisibility(View.VISIBLE);
        lastDrilledDate.setVisibility(View.VISIBLE);
        confidenceLabel.setVisibility(View.VISIBLE);
        confidenceSpinner.setVisibility(View.VISIBLE);
        editCategoriesButton.setVisibility(View.VISIBLE);
        editSubCategoriesButton.setVisibility(View.VISIBLE);
        notesLabel.setVisibility(View.VISIBLE);
        notes.setVisibility(View.VISIBLE);
        if (ScreenType.GeneratedDrill == screenType) {
            regenerateButton.setVisibility(View.VISIBLE);
            resetSkippedDrillsButton.setVisibility(View.VISIBLE);
        }
        markAsPracticedButton.setVisibility(View.VISIBLE);
        saveDrillInfoButton.setVisibility(View.VISIBLE);
    }

    private int confidenceWeightToPosition(int weight) {
        int position;
        switch(weight) {
            case Drill.HIGH_CONFIDENCE:
                position = HIGH_CONFIDENCE_POSITION;
                break;
            case Drill.MEDIUM_CONFIDENCE:
                position = MEDIUM_CONFIDENCE_POSITION;
                break;
            case Drill.LOW_CONFIDENCE:
            default:
                position = LOW_CONFIDENCE_POSITION;
                break;
        }

        return position;
    }

    private int confidencePositionToWeight(int position) {
        int confidence;
        switch(position) {
            case HIGH_CONFIDENCE_POSITION:
                confidence = Drill.HIGH_CONFIDENCE;
                break;
            case MEDIUM_CONFIDENCE_POSITION:
                confidence = Drill.MEDIUM_CONFIDENCE;
                break;
            case LOW_CONFIDENCE_POSITION:
            default:
                confidence = Drill.LOW_CONFIDENCE;
                break;
        }

        return confidence;
    }

    /**
     *
     * @return May return null // TODO
     */
    private Drill collectDrillInfo() {
        Drill drill = viewModel.getDrill().getValue();
        if (null != drill) {
            drill.setConfidence(
                    confidencePositionToWeight(confidenceSpinner.getSelectedItemPosition()));
            drill.setNotes(notes.getText().toString());
        }
        return drill;
    }

    private void fillDrillInfo(Drill drill) {
        drillName.setText(drill.getName());
        confidenceSpinner.setSelection(confidenceWeightToPosition(drill.getConfidence()));
        Date drilledDate = new Date(drill.getLastDrilled());
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        lastDrilledDate.setText(dateFormatter.format(drilledDate));
        notes.setText(drill.getNotes());
    }

    private void editCategoriesPopup(List<CategoryEntity> categoryEntities) {
        Drill drill = viewModel.getDrill().getValue();
        if (null == drill) {
            Toast.makeText(this, "Issue retrieving categories", Toast.LENGTH_SHORT).show();
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
        builder.setPositiveButton("Save", (dialog, position) -> {
            for (int i = 0; i < checkedCategories.length; i++) {
                if (checkedCategories[i] && !categories.contains(categoryEntities.get(i))) {
                    // Checked but not already in list - add
                    drill.addCategory(categoryEntities.get(i));
                } else if (!checkedCategories[i] && categories.contains(categoryEntities.get(i))) {
                    // Not checked but in list - remove
                    drill.removeCategory(categoryEntities.get(i));
                }
                viewModel.saveDrill(drill, this);
            }
        });
        builder.setNegativeButton("Back", (dialog, position) -> {
            // Intentionally left black
        });
        builder.setNeutralButton("Clear All", (dialog, position) -> Arrays.fill(checkedCategories, false));
        builder.create().show();
        categoriesLoadingSnackbar.dismiss();
    }

    private void editSubCategoriesPopup(List<SubCategoryEntity> subCategoryEntities) {
        Drill drill = viewModel.getDrill().getValue();
        if (null == drill) {
            Toast.makeText(this, "Issue retrieving sub-categories", Toast.LENGTH_SHORT).show();
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
        builder.setPositiveButton("Save", (dialog, position) -> {
            for (int i = 0; i < checkedSubCategories.length; i++) {
                if (checkedSubCategories[i] && !subCategories.contains(subCategoryEntities.get(i))) {
                    // Checked but not already in list - add
                    drill.addSubCategory(subCategoryEntities.get(i));
                } else if (!checkedSubCategories[i] && subCategories.contains(subCategoryEntities.get(i))) {
                    // Not checked but in list - remove
                    drill.removeSubCategory(subCategoryEntities.get(i));
                }
                viewModel.saveDrill(drill, this);
            }
        });
        builder.setNegativeButton("Back", (dialog, position) -> {
            // Intentionally left black
        });
        builder.setNeutralButton("Clear All", (dialog, position) -> Arrays.fill(checkedSubCategories, false));
        builder.create().show();
        subCategoriesLoadingSnackbar.dismiss();
    }

    // TODO Doc comments nullable
    private AlertDialog createConfidencePopup() {
        Drill drill = collectDrillInfo();

        if (null == drill) {
            Toast.makeText(this, "Issue marking as practiced", Toast.LENGTH_SHORT).show();
            return null;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = getResources().getStringArray(R.array.confidence_levels);
        final int[] selectedOption = { confidenceWeightToPosition(drill.getConfidence())};

        builder.setTitle("Enter new confidence level:");
        builder.setIcon(R.drawable.thumbs_up_down_icon);
        builder.setCancelable(false);
        builder.setSingleChoiceItems(options, selectedOption[0], (dialog, position) -> selectedOption[0] = position);
        builder.setPositiveButton("Save", (dialog, position) -> {
            drill.setConfidence(confidencePositionToWeight(selectedOption[0]));
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
        builder.setItems(options, (dialog, position) -> {
            switch(position) {
                case 0:
                    viewModel.regenerateDrill();
                    break;
                case 1:
                    Intent intent = new Intent(this, CategorySelectActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    break;
            }
        });
        builder.setPositiveButton("Go Home", (dialog, position) -> {
           Intent intent = new Intent(this, HomeActivity.class);
           intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
           startActivity(intent);
        });
        builder.setNegativeButton("Back", (dialog, position) -> {});

        return builder.create();
    }
}