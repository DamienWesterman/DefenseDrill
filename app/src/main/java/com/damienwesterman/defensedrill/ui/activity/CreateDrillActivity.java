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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.common.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.common.UiUtils;
import com.damienwesterman.defensedrill.ui.viewmodel.CreateDrillViewModel;
import com.damienwesterman.defensedrill.common.Constants;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity to create a new Drill.
 */
@AndroidEntryPoint
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
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start the CreateDrillActivity.
     *
     * @param context   Context.
     */
    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, CreateDrillActivity.class);
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_drill);

        // Modify Toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Customize Database");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(CreateDrillViewModel.class);

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
    public void addCategories(View view) {
        if (null != viewModel.getAllCategories()) {
            addCategoriesPopup(viewModel.getAllCategories());
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Issue retrieving categories");
        }
    }

    public void addSubCategories(View view) {
        if (null != viewModel.getAllSubCategories()) {
            addSubCategoriesPopup(viewModel.getAllSubCategories());
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Issue retrieving sub categories");
        }
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
        setViewsEnabled(false);
        Drill drill = generateDrillFromUserInput();
        
        if (null == drill) {
            // User error feedback is handled in generateDrillFromUserInput()
            setViewsEnabled(true);
        } else if (drill.getCategories().isEmpty()) {
            // Will then check subCategories and call checkNamePopup()
            confirmNoCategoriesPopup(drill);
        } else if (drill.getSubCategories().isEmpty()) {
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
    private void addCategoriesPopup(@NonNull List<CategoryEntity> categories) {
        if (categories.isEmpty()) {
            UiUtils.displayDismissibleSnackbar(rootView, "No Categories in database");
            return;
        }

        Set<CategoryEntity> checkedCategoryEntities = viewModel.getCheckedCategoryEntities();

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
                        // Not checked - remove if in list
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
    private void addSubCategoriesPopup(@NonNull List<SubCategoryEntity> subCategories) {
        if (subCategories.isEmpty()) {
            UiUtils.displayDismissibleSnackbar(rootView, "No sub-Categories in database");
            return;
        }

        Set<SubCategoryEntity> checkedSubCategoryEntities = viewModel.getCheckedSubCategoryEntities();

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
                        // Not checked - remove if in list
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
            if (drill.getSubCategories().isEmpty()) {
                confirmNoSubCategoriesPopup(drill);
            } else {
                checkNamePopup(drill);
            }
        });
        builder.setNegativeButton("Go Back", (dialog, position) -> setViewsEnabled(true));
        builder.create().show();
    }

    /**
     * Create and show a popup to confirm the user intended to create a drill with no sub-categories.
     *
     * @param drill Drill being created.
     */
    private void confirmNoSubCategoriesPopup(@NonNull Drill drill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No sub-Categories Selected");
        builder.setIcon(R.drawable.warning_icon);
        builder.setMessage("Are you sure you don't want this drill to belong to any sub-categories?\n"
                + "This will prevent it from being selected during Drill Generation unless "
                + "random is selected.");
        builder.setCancelable(false);
        builder.setPositiveButton("I'm Sure", (dialog, position) -> checkNamePopup(drill));
        builder.setNegativeButton("Go Back", (dialog, position) -> setViewsEnabled(true));
        builder.create().show();
    }

    /**
     * Create and show a popup to confirm the name of the drill with the user. Saves drill in the
     * database if user accepts the name.
     *
     * @param drill Drill being created.
     */
    private void checkNamePopup(@NonNull Drill drill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Double Check Name");
        builder.setIcon(R.drawable.warning_icon);
        builder.setMessage("Please double check the name, as it cannot be changed later:\n\""
                + drill.getName() + "\"");
        builder.setCancelable(false);
        builder.setPositiveButton("Looks Good", (dialog, position) -> viewModel.saveDrill(drill, new OperationCompleteCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    UiUtils.displayDismissibleSnackbar(rootView, "Successfully saved");
                    whatNextPopup();
                });
            }

            @Override
            public void onFailure(@NonNull String error) {
                runOnUiThread(() -> {
                    UiUtils.displayDismissibleSnackbar(rootView, error);
                    setViewsEnabled(true);
                });
            }
        }));
        builder.setNegativeButton("Change Name", (dialog, position) -> setViewsEnabled(true));
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
            clearUserInputFields();
            setViewsEnabled(true);
        });
        builder.setNeutralButton("Done", (dialog, position) -> finish());
        builder.create().show();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Change the state of the UI to allow a user to edit input views.
     * <br><br>
     * If saving is in progress, then we want to display a snackbar that says so, and make sure the
     * views are not editable.
     *
     * @param enabled boolean if we are in the process of saving.
     */
    private void setViewsEnabled(boolean enabled) {
        if (enabled) {
            if (savingSnackbar.isShown()) {
                savingSnackbar.dismiss();
            }
        } else {
            savingSnackbar.show();
        }
        enteredName.setEnabled(enabled);
        confidenceSpinner.setEnabled(enabled);
        categoriesButton.setEnabled(enabled);
        subCategoriesButton.setEnabled(enabled);
        enteredNotes.setEnabled(enabled);
    }

    /**
     * Set all fields to their defaults.
     */
    private void clearUserInputFields() {
        enteredName.setText(null);
        confidenceSpinner.setSelection(0);
        viewModel.getCheckedCategoryEntities().clear();
        viewModel.getCheckedSubCategoryEntities().clear();
        enteredNotes.setText(null);
    }

    /**
     * Gather all the user input from the fields and convert it into a Drill object. Performs input
     * sanitation.
     * <br><br>
     * Will display an error message to the user if input sanitation failed.
     *
     * @return Drill object created from user input or null if input sanitation failed.
     */
    @Nullable
    private Drill generateDrillFromUserInput() {
        // Both of these are limits just for display purposes on other screens. They are a little
        // arbitrary and do not represent actual limits in the database layer.
        final int NAME_CHARACTER_LIMIT = 256;
        final int NOTES_CHARACTER_LIMIT = 2048;

        Drill drill;
        String name;
        String notes;

        name = enteredName.getText().toString();
        if (name.isEmpty()) {
            UiUtils.displayDismissibleSnackbar(rootView, "Name cannot be empty");
            return null;
        } else if (NAME_CHARACTER_LIMIT <= name.length()) {
            UiUtils.displayDismissibleSnackbar(rootView, "Name must be less than "
                    + NAME_CHARACTER_LIMIT + " characters");
            return null;
        }

        notes = enteredNotes.getText().toString();
        if (NOTES_CHARACTER_LIMIT <= notes.length()) {
            UiUtils.displayDismissibleSnackbar(rootView, "Notes must be less than "
                    + NOTES_CHARACTER_LIMIT + " characters");
            return null;
        }
        drill = new Drill(
                name,
                0, // Last drilled date
                Constants.confidencePositionToWeight(confidenceSpinner.getSelectedItemPosition()),
                notes,
                null,
                true,
                new ArrayList<>(viewModel.getCheckedCategoryEntities()),
                new ArrayList<>(viewModel.getCheckedSubCategoryEntities())
        );

        return drill;
    }
}