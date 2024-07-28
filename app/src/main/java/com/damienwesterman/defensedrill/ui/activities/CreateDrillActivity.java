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

import androidx.annotation.Nullable;
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

/**
 * Activity to create a new Drill.
 * <br><br>
 * INTENTS: None required.
 */
public class CreateDrillActivity extends AppCompatActivity {
    private CreateDrillViewModel viewModel;

    private View rootView;
    private EditText enteredName;
    private Spinner confidenceSpinner;
    private Button categoriesButton;
    private Button subCategoriesButton;
    private EditText enteredNotes;
    private Snackbar savingSnackbar;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_drill);

        viewModel = new ViewModelProvider(this).get(CreateDrillViewModel.class);

        viewModel.loadAllCategories();
        viewModel.loadAllSubCategories();

        rootView = findViewById(R.id.activityCreateDrill);
        enteredName = findViewById(R.id.nameEditText);
        confidenceSpinner = findViewById(R.id.confidenceSpinner);
        categoriesButton = findViewById(R.id.addCategoriesButton);
        subCategoriesButton = findViewById(R.id.addSubCategoriesButton);
        enteredNotes = findViewById(R.id.notes);
        savingSnackbar = Snackbar.make(rootView,
                "Saving in progress...", Snackbar.LENGTH_INDEFINITE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.confidence_levels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        confidenceSpinner.setAdapter(adapter);
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void addCategories(View view) {
        addCategoriesPopup(viewModel.getAllCategories());
    }

    public void addSubCategories(View view) {
        addSubCategoriesPopup(viewModel.getAllSubCategories());
    }

    /**
     * Do input sanitation and save a new drill to the database.
     * <br><br>
     * The popup process for saving a drill is as follows (when applicable):
     * <ol>
     *     <li>{@link #confirmNoCategoriesPopup(Drill)}</li>
     *     <li>{@link #confirmNoSubCategoriesPopup(Drill)}</li>
     *     <li>{@link #checkNamePopup(Drill)}</li>
     *     <li>{@link #whatNextPopup()}</li>
     * </ol>
     *
     * @param view View.
     */
    public void saveDrill(View view) {
        setUserEditable(false);
        Drill drill = generateDrillFromUserInput();
        
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

    // =============================================================================================
    // Popup Methods
    // =============================================================================================
    /**
     * Create and show a popup to add categories.
     * <br><br>
     * Displays the passed list as a popup of checked boxes, then saves the
     * checked categories to the viewModel for later access.
     *
     * @param categories List of categories.
     */
    private void addCategoriesPopup(List<CategoryEntity> categories) {
        if (null == categories) {
            Utils.displayDismissibleSnackbar(rootView, "Issue retrieving categories");
            return;
        } else if (0 == categories.size()) {
            Utils.displayDismissibleSnackbar(rootView, "No Categories in database");
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

    /**
     * Create and show a popup to add sub-categories.
     * <br><br>
     * Displays the passed list as a popup of checked boxes, then saves the
     * checked sub-categories to the viewModel for later access.
     *
     * @param subCategories List of categories.
     */
    private void addSubCategoriesPopup(List<SubCategoryEntity> subCategories) {
        if (null == subCategories) {
            Utils.displayDismissibleSnackbar(rootView, "Issue retrieving sub-categories");
            return;
        } else if (0 == subCategories.size()) {
            Utils.displayDismissibleSnackbar(rootView, "No sub-Categories in database");
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

    /**
     * Create and show a popup to confirm the user intended to create a drill with no categories.
     *
     * @param drill Drill being created.
     */
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

    /**
     * Create and show a popup to confirm the user intended to create a drill with no sub-categories.
     *
     * @param drill Drill being created.
     */
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

    /**
     * Create and show a popup to confirm the name of the drill with the user. Saves drill in the
     * database if user accepts the name.
     *
     * @param drill Drill being created.
     */
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
                    Utils.displayDismissibleSnackbar(rootView, "Successfully saved");
                    whatNextPopup();
                });
            }

            @Override
            public void onFailure(String msg) {
                runOnUiThread(() -> {
                    Utils.displayDismissibleSnackbar(rootView, "ERROR: Name already exists");
                    setUserEditable(true);
                });
            }
        }));
        builder.setNegativeButton("Change Name", (dialog, position) -> setUserEditable(true));
        builder.create().show();
    }

    /**
     * Create a show a popup asking the user what they want to do next.
     * <br><br>
     * User can choose to:
     * <ul>
     *     <li>Create another drill</li>
     *     <li>Go back [to the previous activity]</li>
     *     <li>Go to the home screen</li>
     * </ul>
     */
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

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================

    /**
     * Set the UI state to accepting user input or not allowing the user to modify the drill info
     * fields.
     *
     * @param editable boolean if user is able to edit the drill info fields.
     */
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

    /**
     * Gather all the user input from the fields and convert it into a Drill object. Performs input
     * sanitation.
     * <br><br>
     * Will display an error message to the user if input sanitation failed.
     *
     * @return Drill object created from user input or null if input sanitation failed.
     */
    private @Nullable Drill generateDrillFromUserInput() {
        // Both of these are limits just for display purposes on other screen. They are a little
        // arbitrary and do not represent actual limits in the database layer.
        final int NAME_CHARACTER_LIMIT = 256;
        final int NOTES_CHARACTER_LIMIT = 2048;

        Drill drill;
        String name;
        String notes;

        name = enteredName.getText().toString();
        if (0 == name.length()) {
            Utils.displayDismissibleSnackbar(rootView, "Name cannot be empty");
            return null;
        } else if (NAME_CHARACTER_LIMIT <= name.length()) {
            Utils.displayDismissibleSnackbar(rootView, "Name must be less than "
                    + NAME_CHARACTER_LIMIT + " characters");
            return null;
        }

        notes = enteredNotes.getText().toString();
        if (NOTES_CHARACTER_LIMIT <= notes.length()) {
            Utils.displayDismissibleSnackbar(rootView, "Notes must be less than "
                    + NOTES_CHARACTER_LIMIT + " characters");
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