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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drills);

        viewModel = new ViewModelProvider(this).get(DrillListViewModel.class);
        progressBar = findViewById(R.id.allDrillsProgressBar);
        recyclerView = findViewById(R.id.allDrillsRecyclerView);

        setLoading(true);
        viewModel.getDrills().observe(this, this::setUpRecyclerView);
        viewModel.loadAllCategories();
        viewModel.loadAllSubCategories();
        viewModel.populateDrills();
    }

    public void filterByCategory(View view) {
        filterCategoriesPopup(viewModel.getAllCategories());
    }

    public void filterBySubCategory(View view) {
        filterSubCategoriesPopup(viewModel.getAllSubCategories());
    }

    public void resetFilters(View view) {
        viewModel.setCategoryFilterIds(null);
        viewModel.setSubCategoryFilterIds(null);
        viewModel.rePopulateDrills();
    }

    public void createDrill(View view) {
        Intent intent = new Intent(this, CreateDrillActivity.class);
        startActivity(intent);
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

    private void deleteDrillPopup(Drill drill) {
        if (null == drill) {
            Toast.makeText(this, "Something went wrong trying to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to delete the following Drill:");
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
}