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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.CreateNewEntityCallback;
import com.damienwesterman.defensedrill.ui.utils.Utils;
import com.damienwesterman.defensedrill.ui.view_models.CreateDrillViewModel;
import com.damienwesterman.defensedrill.utils.Constants;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;

// TODO doc comments (note the required intents - none)
public class CreateDrillActivity extends AppCompatActivity {
    private CreateDrillViewModel viewModel;

    private EditText enteredName;
    private Spinner confidenceSpinner;
    private Button categoriesButton;
    private Button subCategoriesButton;
    private EditText enteredNotes;
    private Snackbar savingSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_drill);

        viewModel = new ViewModelProvider(this).get(CreateDrillViewModel.class);

        viewModel.loadAllCategories();
        viewModel.loadAllSubCategories();

        enteredName = findViewById(R.id.nameEditText);
        confidenceSpinner = findViewById(R.id.confidenceSpinner);
        categoriesButton = findViewById(R.id.addCategoriesButton);
        subCategoriesButton = findViewById(R.id.addSubCategoriesButton);
        enteredNotes = findViewById(R.id.notes);
        savingSnackbar = Snackbar.make(findViewById(R.id.activityCreateDrill),
                "Saving in progress...", Snackbar.LENGTH_INDEFINITE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.confidence_levels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        confidenceSpinner.setAdapter(adapter);
    }

    public void addCategories(View view) {
        addCategoriesPopup(viewModel.getAllCategories());
    }

    public void addSubCategories(View view) {
        addSubCategoriesPopup(viewModel.getAllSubCategories());
    }

    public void saveDrill(View view) {
        setUserEditable(false);
        Drill drill = generateDrillFromUserInput();
        // TODO maybe have some sort of snackbar saying loading or something
        
        if (null == drill) {
            // User notification is handled in generateDrillFromUserInput()
            setUserEditable(true);
        } else if (0 == drill.getCategories().size()) {
            // Will then check subCategories and call checkNamePopup()
            confirmNoCategoriesPopup(drill);
        } else if (0 == drill.getSubCategories().size()) {
            // Will then call checkNamePopup()
            confirmNoSubCategoriesPopup(drill);
        } else {
            // All checks passed, we are ready to save drill, have user double check name first
            checkNamePopup(drill);
        }
    }

    private void addCategoriesPopup(List<CategoryEntity> categories) {
        if (null == categories) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityCreateDrill),
                    "Issue retrieving categories");
            return;
        } else if (0 == categories.size()) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityCreateDrill),
                    "No Categories in database");
            return;
        }

        List<CategoryEntity> checkedCategoryEntities = viewModel.getCheckedCategoryEntities();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] categoryNames = categories
                .stream().map(CategoryEntity::getName).toArray(String[]::new);
        final boolean[] checkedCategories = new boolean[categoryNames.length];

        for (int i = 0; i < checkedCategories.length; i++) {
            if (checkedCategoryEntities.contains(categories.get(i))) {
                checkedCategories[i] = true;
            }
        }

        builder.setTitle("Select Categories");
        builder.setIcon(R.drawable.add_circle_icon);
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
                    if (checkedCategories[i] && !checkedCategoryEntities.contains(categories.get(i))) {
                        // Checked but not already in list - add
                        checkedCategoryEntities.add(categories.get(i));
                    } else if (!checkedCategories[i]) {
                        // Not checked but in list - remove
                        checkedCategoryEntities.remove(categories.get(i));
                    }
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

    private void addSubCategoriesPopup(List<SubCategoryEntity> subCategories) {
        if (null == subCategories) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityCreateDrill),
                    "Issue retrieving sub-categories");
            return;
        } else if (0 == subCategories.size()) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityCreateDrill),
                    "No sub-Categories in database");
            return;
        }

        List<SubCategoryEntity> checkedSubCategoryEntities = viewModel.getCheckedSubCategoryEntities();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] subCategoryNames = subCategories
                .stream().map(SubCategoryEntity::getName).toArray(String[]::new);
        final boolean[] checkedSubCategories = new boolean[subCategoryNames.length];

        for (int i = 0; i < checkedSubCategories.length; i++) {
            if (checkedSubCategoryEntities.contains(subCategories.get(i))) {
                checkedSubCategories[i] = true;
            }
        }

        builder.setTitle("Select sub-Categories");
        builder.setIcon(R.drawable.add_circle_icon);
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
                    if (checkedSubCategories[i] && !checkedSubCategoryEntities.contains(subCategories.get(i))) {
                        // Checked but not already in list - add
                        checkedSubCategoryEntities.add(subCategories.get(i));
                    } else if (!checkedSubCategories[i]) {
                        // Not checked but in list - remove
                        checkedSubCategoryEntities.remove(subCategories.get(i));
                    }
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

    private void confirmNoCategoriesPopup(Drill drill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Categories Selected");
        builder.setIcon(R.drawable.warning_icon);
        builder.setMessage("Are you sure you don't want this drill to belong to any categories?\n"
                + "This will prevent it from being selected during Drill Generation unless "
                + "random is selected.");
        builder.setCancelable(false);
        builder.setPositiveButton("I'm Sure", (dialog, position) -> {
            if (0 == drill.getSubCategories().size()) {
                confirmNoSubCategoriesPopup(drill);
            } else {
                checkNamePopup(drill);
            }
        });
        builder.setNegativeButton("Go Back", (dialog, position) -> setUserEditable(true));
        builder.create().show();
    }

    private void confirmNoSubCategoriesPopup(Drill drill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No sub-Categories Selected");
        builder.setIcon(R.drawable.warning_icon);
        builder.setMessage("Are you sure you don't want this drill to belong to any sub-categories?\n"
                + "This will prevent it from being selected during Drill Generation unless "
                + "random is selected.");
        builder.setCancelable(false);
        builder.setPositiveButton("I'm Sure", (dialog, position) -> checkNamePopup(drill));
        builder.setNegativeButton("Go Back", (dialog, position) -> setUserEditable(true));
        builder.create().show();
    }

    private void checkNamePopup(Drill drill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Double Check Name");
        builder.setIcon(R.drawable.warning_icon);
        builder.setMessage("Please double check the name, as it cannot be changed later:\n\""
                + drill.getName() + "\"");
        builder.setCancelable(false);
        builder.setPositiveButton("Looks Good", (dialog, position) -> viewModel.saveDrill(drill, new CreateNewEntityCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Utils.displayDismissibleSnackbar(findViewById(R.id.activityCreateDrill),
                            "Successfully saved");
                    whatNextPopup();
                });
            }

            @Override
            public void onFailure(String msg) {
                runOnUiThread(() -> {
                    Utils.displayDismissibleSnackbar(findViewById(R.id.activityCreateDrill),
                            "ERROR: Name already exists");
                    setUserEditable(true);
                });
            }
        }));
        builder.setNegativeButton("Change Name", (dialog, position) -> setUserEditable(true));
        builder.create().show();
    }

    private void whatNextPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("What next?");
        builder.setIcon(R.drawable.next_icon);
        builder.setCancelable(false);
        builder.setPositiveButton("Create Another", (dialog, position) -> {
            enteredName.setText(null);
            confidenceSpinner.setSelection(0);
            viewModel.getCheckedCategoryEntities().clear();
            viewModel.getCheckedSubCategoryEntities().clear();
            enteredNotes.setText(null);
            setUserEditable(true);
        });
        builder.setNegativeButton("Back", (dialog, position) -> finish());
        builder.setNeutralButton("Go Home", (dialog, position) -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        builder.create().show();
    }

    private void setUserEditable(boolean editable) {
        if (editable) {
            if (savingSnackbar.isShown()) {
                savingSnackbar.dismiss();
            }
        } else {
            savingSnackbar.show();
        }
        enteredName.setEnabled(editable);
        confidenceSpinner.setEnabled(editable);
        categoriesButton.setEnabled(editable);
        subCategoriesButton.setEnabled(editable);
        enteredNotes.setEnabled(editable);
    }

    // TODO Doc comments, also does input sanitation, nullable if issue
    private Drill generateDrillFromUserInput() {
        // Both of these are limits just for display purposes on other screen. They are a little
        // arbitrary and do not represent actual limits in the database layer.
        final int NAME_CHARACTER_LIMIT = 256;
        final int NOTES_CHARACTER_LIMIT = 2048;

        Drill drill;
        String name;
        String notes;

        name = enteredName.getText().toString();
        if (0 == name.length()) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityCreateDrill),
                    "Name cannot be empty");
            return null;
        } else if (NAME_CHARACTER_LIMIT <= name.length()) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityCreateDrill),
                    "Name must be less than " + NAME_CHARACTER_LIMIT + " characters");
            return null;
        }

        notes = enteredNotes.getText().toString();
        if (NOTES_CHARACTER_LIMIT <= notes.length()) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityCreateDrill),
                    "Notes must be less than " + NOTES_CHARACTER_LIMIT + " characters");
            return null;
        }
        drill = new Drill(
                name,
                System.currentTimeMillis(),
                true,
                Constants.confidencePositionToWeight(confidenceSpinner.getSelectedItemPosition()),
                notes,
                Drill.INVALID_SERVER_DRILL_ID,
                viewModel.getCheckedCategoryEntities(),
                viewModel.getCheckedSubCategoryEntities()
        );

        return drill;
    }
}