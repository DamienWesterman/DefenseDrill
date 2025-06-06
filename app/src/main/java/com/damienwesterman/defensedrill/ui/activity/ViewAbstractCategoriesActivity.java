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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.adapter.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.common.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.common.UiUtils;
import com.damienwesterman.defensedrill.ui.viewmodel.AbstractCategoryViewModel;
import com.damienwesterman.defensedrill.ui.viewmodel.CategoryViewModel;
import com.damienwesterman.defensedrill.ui.viewmodel.SubCategoryViewModel;
import com.damienwesterman.defensedrill.common.Constants;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity to display, edit, and create categories or sub-categories (determined by the intent).
 * <br><br>
 * This is a generic activity that can be launched to display two different (and very similar)
 * screens. It can act as the screen for Customize Categories OR Customize Sub-Categories. Which one
 * is determined by the intent passed in. This screen allows a user to view all of an
 * AbstractCategory's entries, edit them (by click), delete them (by long click), or create a new
 * one (by popup).
 */
@AndroidEntryPoint
public class ViewAbstractCategoriesActivity extends AppCompatActivity {
    private static final String TAG = ViewAbstractCategoriesActivity.class.getSimpleName();
    // The following are abstract limits and do not represent any limits imposed by the database
    private static final int NAME_CHARACTER_LIMIT = 128;
    private static final int DESCRIPTION_CHARACTER_LIMIT = 512;

    private AbstractCategoryViewModel viewModel;
    private ActivityMode activityMode;

    private View rootView;
    private TextView title;
    private Button createButton;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    public enum ActivityMode {
        MODE_CATEGORIES,
        MODE_SUB_CATEGORIES
    }

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start the ViewAbstractCategoriesActivity for either Categories or SubCategories.
     *
     * @param context   Context.
     * @param mode      The ActivityMode to launch the activity in, either Category or Sub-Category.
     */
    public static void startActivity(@NonNull Context context, @NonNull ActivityMode mode) {
        Intent intent = new Intent(context, ViewAbstractCategoriesActivity.class);
        switch (mode) {
            case MODE_CATEGORIES:
                intent.putExtra(Constants.INTENT_EXTRA_VIEW_CATEGORIES, "");
                break;
            case MODE_SUB_CATEGORIES:
                intent.putExtra(Constants.INTENT_EXTRA_VIEW_SUB_CATEGORIES, "");
                break;
            default:
                // No idea how this would happen, but whatever
                Log.e(TAG, "Invalid mode for start activity: " + mode);
                throw new RuntimeException("Invalid mode for start activity: " + mode);
        }
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_abstract_categories);

        // Modify Toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Customize Database");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.INTENT_EXTRA_VIEW_CATEGORIES)) {
            viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
            activityMode = ActivityMode.MODE_CATEGORIES;
        } else if (intent.hasExtra(Constants.INTENT_EXTRA_VIEW_SUB_CATEGORIES)) {
            viewModel = new ViewModelProvider(this).get(SubCategoryViewModel.class);
            activityMode = ActivityMode.MODE_SUB_CATEGORIES;
        } else {
            // Toast so it persists screens
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

        rootView = findViewById(R.id.activityViewAbstractCategories);
        title = findViewById(R.id.allAbstractCategoriesText);
        createButton = findViewById(R.id.createAbstractCategoryButton);
        progressBar = findViewById(R.id.allAbstractCategoriesProgressBar);
        recyclerView = findViewById(R.id.allAbstractCategoriesRecyclerView);

        setScreenTextByMode();
        setUpRecyclerView();
        viewModel.populateAbstractCategories();
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
    public void createAbstractCategory(View view) {
        createAbstractCategoryPopup();
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    /**
     * Create and show a popup to create a new abstract category. Performs input validation.
     * Saves the abstract category if the user confirms and passes validation.
     */
    private void createAbstractCategoryPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_abstract_category_popup, null);

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

        builder.setView(dialogView);
        builder.setTitle(ActivityMode.MODE_CATEGORIES == activityMode ?
                "Create Category" : "Create sub-Category");
        builder.setIcon(R.drawable.add_circle_icon);
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Back", (dialog, position) -> {
            // Do nothing
        });
        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface ->
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            // "Save" button
            String name = nameEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            if (name.isEmpty()) {
                UiUtils.displayDismissibleSnackbar(dialogView,
                        "Name can not be empty");
                return; // Do not dismiss
            } else if (NAME_CHARACTER_LIMIT <= name.length()) {
                UiUtils.displayDismissibleSnackbar(dialogView,
                        "Name must be less than " + NAME_CHARACTER_LIMIT +  " characters");
                return; // Do not dismiss
            }

            if (description.isEmpty()) {
                UiUtils.displayDismissibleSnackbar(dialogView,
                        "Description can not be empty");
                return; // Do not dismiss
            } else if (DESCRIPTION_CHARACTER_LIMIT <= description.length()) {
                UiUtils.displayDismissibleSnackbar(dialogView,
                        "Description must be less than " + DESCRIPTION_CHARACTER_LIMIT +  " characters");
                return; // Do not dismiss
            }
            viewModel.saveAbstractEntity(name, description, new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    alert.dismiss();
                    UiUtils.displayDismissibleSnackbar(rootView,
                            "Save successful!");
                }

                @Override
                public void onFailure(@NonNull String error) {
                    UiUtils.displayDismissibleSnackbar(dialogView, error);
                    // Do not dismiss
                }
            });
        }));

        alert.show();
    }

    /**
     * Create and show a popup to edit an existing abstract category. Performs input validation.
     * Saves the abstract category if the user confirms and passes validation.
     *
     * @param entity AbstractCategoryEntity to view and edit.
     */
    private void viewEditAbstractCategoryPopup(@NonNull AbstractCategoryEntity entity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_abstract_category_popup, null);

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);
        nameEditText.setText(entity.getName());
        descriptionEditText.setText(entity.getDescription());

        builder.setView(dialogView);
        builder.setTitle(ActivityMode.MODE_CATEGORIES == activityMode ?
                "Edit Category" : "Edit sub-Category");
        builder.setIcon(R.drawable.edit_icon);
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Back", (dialog, position) -> {
            // Do nothing
        });
        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface ->
                alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            // "Save" button
            String name = nameEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            if (name.isEmpty()) {
                UiUtils.displayDismissibleSnackbar(dialogView,
                        "Name can not be empty");
                return; // Do not dismiss
            } else if (NAME_CHARACTER_LIMIT <= name.length()) {
                UiUtils.displayDismissibleSnackbar(dialogView,
                        "Name must be less than " + NAME_CHARACTER_LIMIT +  " characters");
                return; // Do not dismiss
            }

            if (description.isEmpty()) {
                UiUtils.displayDismissibleSnackbar(dialogView,
                        "Description can not be empty");
                return; // Do not dismiss
            } else if (DESCRIPTION_CHARACTER_LIMIT <= description.length()) {
                UiUtils.displayDismissibleSnackbar(dialogView,
                        "Description must be less than " + DESCRIPTION_CHARACTER_LIMIT +  " characters");
                return; // Do not dismiss
            }
            viewModel.updateAbstractEntity(entity, name, description, new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    alert.dismiss();
                    UiUtils.displayDismissibleSnackbar(rootView,
                            "Save successful!");
                }

                @Override
                public void onFailure(@NonNull String error) {
                    UiUtils.displayDismissibleSnackbar(dialogView, error);
                    // Do not dismiss
                }
            });
        }));

        alert.show();
    }

    /**
     * Create and show a popup to confirm to the user their intent to delete an abstract category.
     * Deletes the abstract category if the user says yes and refreshes the list.
     *
     * @param entity AbstractCategoryEntity to potentially delete.
     */
    private void deleteAbstractCategoryPopup(@NonNull AbstractCategoryEntity entity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to delete:");
        builder.setIcon(R.drawable.warning_icon);
        builder.setCancelable(true);
        builder.setMessage(entity.getName());
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Delete", (dialog, position) ->
                viewModel.deleteAbstractCategory(entity));
        builder.create().show();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Callback method for when the abstract categories list has been loaded from the database. Sets
     * the UI and attaches click listeners.
     */
    private void setUpRecyclerView() {
        setLoading(true);

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Once all the items are rendered: remove this listener, hide progress bar
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setLoading(false);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AbstractCategoryAdapter adapter = new AbstractCategoryAdapter(
            // Click listener
            id -> {
                AbstractCategoryEntity category = viewModel.findById(id);
                if (null != category) {
                    viewEditAbstractCategoryPopup(category);
                } else {
                    UiUtils.displayDismissibleSnackbar(rootView, "Something went wrong");
                }
            },
            // Long Click listener
            id -> {
                AbstractCategoryEntity category = viewModel.findById(id);
                if (null != category) {
                    deleteAbstractCategoryPopup(category);
                } else {
                    UiUtils.displayDismissibleSnackbar(rootView, "Something went wrong");
                }
            });
        recyclerView.setAdapter(adapter);
        viewModel.getUiAbstractCategoriesList().observe(this, adapter::submitList);
    }

    /**
     * Sets the screen's text to reflect if it is in Category mode or Sub-Category mode.
     */
    private void setScreenTextByMode() {
        if (ActivityMode.MODE_CATEGORIES == activityMode) {
            title.setText(R.string.categories);
            createButton.setText(R.string.create_new_category);
        } else {
            title.setText(R.string.sub_categories);
            createButton.setText(R.string.create_new_sub_category);
        }
    }

    /**
     * Update the UI if we are currently loading or displaying the list.
     *
     * @param loading True if we want to display loading screen, false if we want to display list.
     */
    private void setLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            createButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            createButton.setEnabled(true);
        }
    }
}