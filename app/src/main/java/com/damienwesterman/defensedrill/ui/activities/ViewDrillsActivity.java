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
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.adapters.DrillAdapter;
import com.damienwesterman.defensedrill.ui.utils.Utils;
import com.damienwesterman.defensedrill.ui.view_models.DrillListViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Activity to display, edit, and create drills.
 * <br><br>
 * This screen allows a user to view all Drills, edit them (by click), delete them (by long click),
 * or create a new one (by launching {@link CreateDrillActivity}).
 * <br><br>
 * INTENTS: None expected.
 */
public class ViewDrillsActivity extends AppCompatActivity {
    private DrillListViewModel viewModel;
    private DrillListViewModel.SortOrder sortOrder;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private Button sortButton;
    private Button resetFiltersButton;
    private Button categoryFilterButton;
    private Button subCategoryFilterButton;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drills);

        viewModel = new ViewModelProvider(this).get(DrillListViewModel.class);
        sortOrder = viewModel.getSortOrder();
        progressBar = findViewById(R.id.allDrillsProgressBar);
        recyclerView = findViewById(R.id.allDrillsRecyclerView);
        sortButton = findViewById(R.id.sortButton);
        resetFiltersButton = findViewById(R.id.resetFiltersButton);
        categoryFilterButton = findViewById(R.id.categoryFilterButton);
        subCategoryFilterButton = findViewById(R.id.subCategoryFilterButton);

        setLoading(true);
        viewModel.getDrills().observe(this, this::setUpRecyclerView);
        viewModel.loadAllCategories();
        viewModel.loadAllSubCategories();
        viewModel.populateDrills();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setLoading(true);
        viewModel.rePopulateDrills();
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void filterByCategory(View view) {
        filterCategoriesPopup(viewModel.getAllCategories());
    }

    public void filterBySubCategory(View view) {
        filterSubCategoriesPopup(viewModel.getAllSubCategories());
    }

    public void resetFilters(View view) {
        viewModel.setCategoryFilterIds(null);
        viewModel.setSubCategoryFilterIds(null);
        viewModel.resetDrills();
    }

    public void sortDrills(View view) {
        sortDrillsPopup();
    }

    public void createDrill(View view) {
        Intent intent = new Intent(this, CreateDrillActivity.class);
        startActivity(intent);
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
        final int[] selectedOption = { sortOrderToIndex(sortOrder) };

        builder.setTitle("Sort By:");
        builder.setIcon(R.drawable.sort_icon);
        builder.setCancelable(true);
        builder.setSingleChoiceItems(options, selectedOption[0], (dialog, position) -> selectedOption[0] = position);
        builder.setPositiveButton("Sort", (dialog, position) -> {
            DrillListViewModel.SortOrder selectedSortOrder = indexToSortOrder(selectedOption[0]);
            if (selectedSortOrder == sortOrder) {
                // Do nothing, no change
                return;
            }

            setLoading(true);
            viewModel.setSortOrder(selectedSortOrder);
            DrillListViewModel.SortOrder newSortOrder = viewModel.getSortOrder();

            if (newSortOrder == sortOrder) {
                // Indicates an error and we didn't switch
                Utils.displayDismissibleSnackbar(findViewById(R.id.activityAllDrills),
                        "Could not switch sort order");
                setLoading(false);
            } else {
                sortOrder = newSortOrder;
                // RecyclerView callback will take care of setLoading(false)
            }
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
    private void filterCategoriesPopup(List<CategoryEntity> categories) {
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
                        null != subCategoryFilterIds ? new ArrayList<>(subCategoryFilterIds) : null);
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
    private void filterSubCategoriesPopup(List<SubCategoryEntity> subCategories) {
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
                viewModel.filterDrills( null != categoryFilterIds ? new ArrayList<>(categoryFilterIds) : null,
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
    private void deleteDrillPopup(Drill drill) {
        if (null == drill) {
            Utils.displayDismissibleSnackbar(findViewById(R.id.activityAllDrills),
                    "Something went wrong trying to delete");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to delete:");
        builder.setIcon(R.drawable.warning_icon);
        builder.setCancelable(true);
        builder.setMessage(drill.getName());
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Delete", (dialog, position) -> {
            setLoading(true);
            viewModel.deleteDrill(drill);
        });
        builder.create().show();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Callback method for when the drills list has been loaded from the database. Sets the UI and
     * attaches click listeners.
     *
     * @param drills List of Drill objects.
     */
    private void setUpRecyclerView(List<Drill> drills) {
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
        recyclerView.setAdapter(new DrillAdapter(drills,
                // Click listener
                id -> {
            Intent intent = new Intent(this, DrillInfoActivity.class);
            intent.putExtra(Constants.INTENT_DRILL_ID, id);
            startActivity(intent);
        },
                // Long click listener
                id -> deleteDrillPopup(viewModel.findDrillById(id))));
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

    private int sortOrderToIndex(DrillListViewModel.SortOrder sortOrder) {
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