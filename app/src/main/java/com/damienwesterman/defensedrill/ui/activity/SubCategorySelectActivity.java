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
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.ui.adapter.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.ui.viewmodel.CategoryViewModel;
import com.damienwesterman.defensedrill.ui.viewmodel.SubCategoryViewModel;
import com.damienwesterman.defensedrill.util.Constants;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity during Drill Generation to select a sub-Category of drill, or random.
 * <br><br>
 * Proceeds {@link CategorySelectActivity}, then Launches {@link DrillInfoActivity} sending the
 * selected Category and SubCategory.
 */
@AndroidEntryPoint
public class SubCategorySelectActivity extends AppCompatActivity {
    private static final String RANDOM_CATEGORY_NAME = "Random";

    private SubCategoryViewModel subCategoryViewModel;
    long selectedCategoryId;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start the SubCategorySelectActivity.
     *
     * @param context           Context.
     * @param categoryChoice    Category ID the user chose, or {@link Constants#USER_RANDOM_SELECTION}.
     */
    public static void startActivity(@NonNull Context context, long categoryChoice) {
        Intent intent = new Intent(context, SubCategorySelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_CATEGORY_CHOICE, categoryChoice);
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category_select);

        // Modify Toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Generate Drill");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        selectedCategoryId = getIntent().getLongExtra(Constants.INTENT_EXTRA_CATEGORY_CHOICE,
                Constants.USER_RANDOM_SELECTION);


        subCategoryViewModel = new ViewModelProvider(this).get(SubCategoryViewModel.class);
        setUpRecyclerView();

        if (Constants.USER_RANDOM_SELECTION == selectedCategoryId) {
            subCategoryViewModel.populateAbstractCategories();
            setCategoryTitle(RANDOM_CATEGORY_NAME);
        } else {
            subCategoryViewModel.populateAbstractCategories(selectedCategoryId);

            CategoryViewModel categoryViewModel =
                    new ViewModelProvider(this).get(CategoryViewModel.class);
            categoryViewModel.getUiAbstractCategoriesList().observe(this, (categories) -> {
                CategoryEntity category = (CategoryEntity) categoryViewModel.findById(selectedCategoryId);
                setCategoryTitle(null != category ? category.getName() : RANDOM_CATEGORY_NAME);
            });
            categoryViewModel.populateAbstractCategories();
        }
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
    public void randomSubCategoryClick(View view) {
        DrillInfoActivity.startActivity(this, selectedCategoryId, Constants.USER_RANDOM_SELECTION);
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Private helper method to set up the recyclerView list of SubCategories and their callback.
     */
    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.subCategoryRecyclerView);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Once all the items are rendered: remove this listener, hide progress bar,
                // and display the random option
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                findViewById(R.id.subCategoryProgressBar).setVisibility(View.GONE);
                findViewById(R.id.randomSubCategoryCard).setVisibility(View.VISIBLE);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AbstractCategoryAdapter adapter = new AbstractCategoryAdapter(
                // Card click listener
                id -> DrillInfoActivity.startActivity(this, selectedCategoryId, id),
                // Long Card Click Listener
                null);
        recyclerView.setAdapter(adapter);
        subCategoryViewModel.getUiAbstractCategoriesList().observe(this, adapter::submitList);
    }

    private void setCategoryTitle(@NonNull String category) {
        TextView categorySelection = findViewById(R.id.categorySelection);
        runOnUiThread(() -> categorySelection.setText(getString(R.string.category_selected_prefix, category)));
    }
}