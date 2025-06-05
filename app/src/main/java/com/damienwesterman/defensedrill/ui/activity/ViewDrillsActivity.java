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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.adapter.DrillAdapter;
import com.damienwesterman.defensedrill.ui.common.UiUtils;
import com.damienwesterman.defensedrill.ui.viewmodel.DrillListViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity to display, edit, and create drills.
 * <br><br>
 * This screen allows a user to view all Drills, edit them (by click), delete them (by long click),
 * or create a new one (by launching {@link CreateDrillActivity}).
 */
@AndroidEntryPoint
public class ViewDrillsActivity extends AppCompatActivity {
    private DrillListViewModel viewModel;

    private View rootView;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private Button sortButton;
    private Button resetFiltersButton;
    private Button categoryFilterButton;
    private Button subCategoryFilterButton;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start the ViewDrillsActivity.
     *
     * @param context   Context.
     */
    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, ViewDrillsActivity.class);
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drills);

        // Modify Toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Customize Database");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(DrillListViewModel.class);

        rootView = findViewById(R.id.activityAllDrills);
        progressBar = findViewById(R.id.allDrillsProgressBar);
        recyclerView = findViewById(R.id.allDrillsRecyclerView);
        sortButton = findViewById(R.id.sortButton);
        resetFiltersButton = findViewById(R.id.resetFiltersButton);
        categoryFilterButton = findViewById(R.id.categoryFilterButton);
        subCategoryFilterButton = findViewById(R.id.subCategoryFilterButton);

        setUpRecyclerView();
        viewModel.loadAllCategories();
        viewModel.loadAllSubCategories();
        viewModel.populateDrills();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        viewModel.populateDrills();
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
    public void filterByCategory(View view) {
        List<CategoryEntity> categories = viewModel.getAllCategories();
        if (null == categories) {
            UiUtils.displayDismissibleSnackbar(rootView, "Categories still loading, please try again...");
        } else {
            filterCategoriesPopup(categories);
        }
    }

    public void filterBySubCategory(View view) {
        List<SubCategoryEntity> subCategories = viewModel.getAllSubCategories();
        if (null == subCategories) {
            UiUtils.displayDismissibleSnackbar(rootView,
                    "Sub-categories still loading, please try again...");
        } else {
            filterSubCategoriesPopup(subCategories);
        }
    }

    public void resetFilters(View view) {
        viewModel.resetDrills();
    }

    public void sortDrills(View view) {
        sortDrillsPopup();
    }

    public void createDrill(View view) {
        CreateDrillActivity.startActivity(this);
    }

    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    /**
     * Create and show a popup that allows the user to select the sort oder for the drills list.
     * Then sets the sort order and refreshes the list.
     */
    private void sortDrillsPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String[] options = getResources().getStringArray(R.array.drills_sort_options);
        final int[] selectedOption = { sortOrderToIndex(viewModel.getSortOrder()) };

        builder.setTitle("Sort By:");
        builder.setIcon(R.drawable.sort_icon);
        builder.setCancelable(true);
        builder.setSingleChoiceItems(options, selectedOption[0], (dialog, position) -> selectedOption[0] = position);
        builder.setPositiveButton("Sort", (dialog, position) -> {
            DrillListViewModel.SortOrder selectedSortOrder = indexToSortOrder(selectedOption[0]);
            if (selectedSortOrder == viewModel.getSortOrder()) {
                // Do nothing, no change
                return;
            }

            viewModel.sortDrills(selectedSortOrder);
        });
        builder.setNegativeButton("Cancel", null);

        builder.create().show();
    }

    /**
     * Create and show a popup to allow the user to filter the drills list by categories.
     * <br><br>
     * Displays the passed in list of categories as check boxes so the user can select to filter by
     * multiple categories. Once accepted will then apply the filter and refresh the list.
     *
     * @param categories List of possible Categories to filter by.
     */
    private void filterCategoriesPopup(@NonNull List<CategoryEntity> categories) {
        final Set<Long> categoryFilterIds = viewModel.getCategoryFilterIds();
        final Set<Long> subCategoryFilterIds = viewModel.getSubCategoryFilterIds();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] categoryNames = categories
                .stream().map(CategoryEntity::getName).toArray(String[]::new);
        final List<Long> categoryIds = categories
                .stream().map(CategoryEntity::getId).collect(Collectors.toList());
        final boolean[] checkedCategories = new boolean[categoryNames.length];

        for (int i = 0; i < checkedCategories.length; i++) {
            if (categoryFilterIds.contains(categoryIds.get(i))) {
                checkedCategories[i] = true;
            }
        }

        builder.setTitle("Select Categories");
        builder.setIcon(R.drawable.filter_icon);
        builder.setMultiChoiceItems(categoryNames, checkedCategories,
                (dialog, position, isChecked) -> checkedCategories[position] = isChecked);
        builder.setCancelable(true);
        builder.setPositiveButton("Filter", null);
        builder.setNegativeButton("Back", null);
        builder.setNeutralButton("Clear All", null);

        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // "Filter"
                for (int i = 0; i < checkedCategories.length; i++) {
                    if (checkedCategories[i]) {
                        categoryFilterIds.add(categoryIds.get(i));
                    } else {
                        categoryFilterIds.remove(categoryIds.get(i));
                    }
                }
                viewModel.filterDrills(new ArrayList<>(categoryFilterIds),
                        !subCategoryFilterIds.isEmpty() ? new ArrayList<>(subCategoryFilterIds) : null);
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
     * Create and show a popup to allow the user to filter the drills list by sub-categories.
     * <br><br>
     * Displays the passed in list of sub-categories as check boxes so the user can select to filter
     * by multiple sub-categories. Once accepted will then apply the filter and refresh the list.
     *
     * @param subCategories List of possible SubCategories to filter by.
     */
    private void filterSubCategoriesPopup(@NonNull List<SubCategoryEntity> subCategories) {
        final Set<Long> categoryFilterIds = viewModel.getCategoryFilterIds();
        final Set<Long> subCategoryFilterIds = viewModel.getSubCategoryFilterIds();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] subCategoryNames = subCategories
                .stream().map(SubCategoryEntity::getName).toArray(String[]::new);
        final List<Long> subCategoryIds = subCategories
                .stream().map(SubCategoryEntity::getId).collect(Collectors.toList());
        final boolean[] checkedSubCategories = new boolean[subCategoryNames.length];

        for (int i = 0; i < checkedSubCategories.length; i++) {
            if (subCategoryFilterIds.contains(subCategoryIds.get(i))) {
                checkedSubCategories[i] = true;
            }
        }

        builder.setTitle("Select sub-Categories");
        builder.setIcon(R.drawable.filter_icon);
        builder.setMultiChoiceItems(subCategoryNames, checkedSubCategories,
                (dialog, position, isChecked) -> checkedSubCategories[position] = isChecked);
        builder.setCancelable(true);
        builder.setPositiveButton("Filter", null);
        builder.setNegativeButton("Back", null);
        builder.setNeutralButton("Clear All", null);

        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // "Filter"
                for (int i = 0; i < checkedSubCategories.length; i++) {
                    if (checkedSubCategories[i]) {
                        subCategoryFilterIds.add(subCategoryIds.get(i));
                    } else {
                        subCategoryFilterIds.remove(subCategoryIds.get(i));
                    }
                }
                viewModel.filterDrills( !categoryFilterIds.isEmpty() ? new ArrayList<>(categoryFilterIds) : null,
                        new ArrayList<>(subCategoryFilterIds));
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
     * Create and show a popup to confirm a user wants to delete a drill. Will then delete the drill
     * if accepted and refresh the list.
     *
     * @param drill Drill to potentially delete
     */
    private void deleteDrillPopup(@NonNull Drill drill) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to delete:");
        builder.setIcon(R.drawable.warning_icon);
        builder.setCancelable(true);
        builder.setMessage(drill.getName());
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Delete", (dialog, position) ->
                viewModel.deleteDrill(drill));
        builder.create().show();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Callback method for when the drills list has been loaded from the database. Sets the UI and
     * attaches click listeners.
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
        DrillAdapter adapter = new DrillAdapter(
                // Click listener
                id -> DrillInfoActivity.startActivity(this, id),
                // Long click listener
                id -> {
                    Drill drill = viewModel.findDrillById(id);
                    if (null != drill) {
                        deleteDrillPopup(drill);
                    } else {
                        UiUtils.displayDismissibleSnackbar(rootView, "Something went wrong");
                    }
                });
        recyclerView.setAdapter(adapter);
        viewModel.getUiDrillsList().observe(this, adapter::submitList);
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
            sortButton.setEnabled(false);
            resetFiltersButton.setEnabled(false);
            categoryFilterButton.setEnabled(false);
            subCategoryFilterButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            sortButton.setEnabled(true);
            resetFiltersButton.setEnabled(true);
            categoryFilterButton.setEnabled(true);
            subCategoryFilterButton.setEnabled(true);
        }
    }

    @NonNull
    private DrillListViewModel.SortOrder indexToSortOrder(int index) {
        switch(index) {
            case 1:
                return DrillListViewModel.SortOrder.SORT_NAME_DESCENDING;
            case 2:
                return DrillListViewModel.SortOrder.SORT_DATE_ASCENDING;
            case 3:
                return DrillListViewModel.SortOrder.SORT_DATE_DESCENDING;
            case 0:
                // Fallthrough intentional
            default:
                return DrillListViewModel.SortOrder.SORT_NAME_ASCENDING;
        }
    }

    private int sortOrderToIndex(@NonNull DrillListViewModel.SortOrder sortOrder) {
        switch (sortOrder) {
            case SORT_NAME_DESCENDING:
                return 1;
            case SORT_DATE_ASCENDING:
                return 2;
            case SORT_DATE_DESCENDING:
                return 3;
            case SORT_NAME_ASCENDING:
                // Fallthrough intentional
            default:
                return 0;
        }
    }
}