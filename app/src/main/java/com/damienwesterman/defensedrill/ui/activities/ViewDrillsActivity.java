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
import android.view.ViewTreeObserver;
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
import com.damienwesterman.defensedrill.ui.view_models.DrillListViewModel;
import com.damienwesterman.defensedrill.utils.Constants;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO Doc comments
 */
public class ViewDrillsActivity extends AppCompatActivity {
    private DrillListViewModel viewModel;
    private Set<Long> categoryFilterIds;
    private Set<Long> subCategoryFilterIds;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    Snackbar categoriesLoadingSnackbar;
    Snackbar subCategoriesLoadingSnackbar;

    @Override
    @SuppressLint("CutPasteId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drills);

        viewModel = new ViewModelProvider(this).get(DrillListViewModel.class);
        progressBar = findViewById(R.id.allDrillsProgressBar);
        recyclerView = findViewById(R.id.allDrillsRecyclerView);
        categoriesLoadingSnackbar = Snackbar.make(findViewById(R.id.activityAllDrills),
                "Loading Categories...", Snackbar.LENGTH_INDEFINITE);
        subCategoriesLoadingSnackbar = Snackbar.make(findViewById(R.id.activityAllDrills),
                "Loading sub-Categories...", Snackbar.LENGTH_INDEFINITE);

        setLoading(true);
        viewModel.getDrills().observe(this, this::setUpRecyclerView);
        viewModel.getAllCategories().observe(this, this::filterCategoriesPopup);
        viewModel.getAllSubCategories().observe(this, this::filterSubCategoriesPopup);
        viewModel.populateDrills();
    }

    public void filterByCategory(View view) {
        categoriesLoadingSnackbar.show();
        viewModel.loadAllCategories();
    }

    public void filterBySubCategory(View view) {
        subCategoriesLoadingSnackbar.show();
        viewModel.loadAllSubCategories();
    }

    public void resetFilters(View view) {
        categoryFilterIds = null;
        subCategoryFilterIds = null;
        viewModel.populateDrills();
    }

    public void setUpRecyclerView(List<Drill> drills) {
        setLoading(true);

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Once all the items are rendered: remove this listener, hide progress bar,
                // and display the random option
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setLoading(false);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DrillAdapter(drills, id -> {
            Intent intent = new Intent(this, DrillInfoActivity.class);
            intent.putExtra(Constants.INTENT_DRILL_ID, id);
            startActivity(intent);
        }));
    }

    public void setLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void filterCategoriesPopup(List<CategoryEntity> categories) {
        if (null == categoryFilterIds) {
            categoryFilterIds = categories.stream().map(CategoryEntity::getId)
                    .collect(Collectors.toSet());
        }

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
        categoriesLoadingSnackbar.dismiss();
    }

    private void filterSubCategoriesPopup(List<SubCategoryEntity> subCategories) {
        if (null == subCategoryFilterIds) {
            subCategoryFilterIds = subCategories.stream().map(SubCategoryEntity::getId)
                    .collect(Collectors.toSet());
        }
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
        subCategoriesLoadingSnackbar.dismiss();
    }
}