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

package com.damienwesterman.defensedrill.ui.activities;

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
import com.damienwesterman.defensedrill.data.local.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.adapters.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.ui.view_models.CategoryViewModel;
import com.damienwesterman.defensedrill.ui.view_models.SubCategoryViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity during Drill Generation to select a sub-Category of drill, or random.
 * <br><br>
 * Proceeds {@link CategorySelectActivity}, then Launches {@link DrillInfoActivity} sending the
 * selected Category and SubCategory.
 * <br><br>
 * INTENTS: Expects to receive a {@link Constants#INTENT_CATEGORY_CHOICE} intent.
 */
@AndroidEntryPoint
public class SubCategorySelectActivity extends AppCompatActivity {
    private static final String RANDOM_CATEGORY_NAME = "Random";

    SubCategoryViewModel subCategoryViewModel;
    CategoryViewModel categoryViewModel;
    long selectedCategoryId;

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

        subCategoryViewModel = new ViewModelProvider(this).get(SubCategoryViewModel.class);

        selectedCategoryId = getIntent().getLongExtra(Constants.INTENT_CATEGORY_CHOICE,
                Constants.USER_RANDOM_SELECTION);


        subCategoryViewModel = new ViewModelProvider(this).get(SubCategoryViewModel.class);
        subCategoryViewModel.getAbstractCategories().observe(this, this::setUpRecyclerView);

        if (Constants.USER_RANDOM_SELECTION == selectedCategoryId) {
            subCategoryViewModel.populateAbstractCategories();
            setCategoryTitle(RANDOM_CATEGORY_NAME);
        } else {
            subCategoryViewModel.populateAbstractCategories(selectedCategoryId);

            categoryViewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
            categoryViewModel.getAbstractCategories().observe(this, (categories) -> {
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
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
        Intent intent = new Intent(this, DrillInfoActivity.class);
        intent.putExtra(Constants.INTENT_CATEGORY_CHOICE, selectedCategoryId);
        intent.putExtra(Constants.INTENT_SUB_CATEGORY_CHOICE, Constants.USER_RANDOM_SELECTION);
        startActivity(intent);
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Private helper method to set up the recyclerView list of SubCategories and their callback.
     */
    private void setUpRecyclerView(List<AbstractCategoryEntity> abstractCategories) {
        List<SubCategoryEntity> subCategories = SubCategoryViewModel.getSubCategoryList(abstractCategories);

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

        runOnUiThread(() -> {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new AbstractCategoryAdapter(subCategories,
                    // Card click listener
                    id -> {
                Intent intent = new Intent(this, DrillInfoActivity.class);
                intent.putExtra(Constants.INTENT_CATEGORY_CHOICE, selectedCategoryId);
                intent.putExtra(Constants.INTENT_SUB_CATEGORY_CHOICE, id);
                startActivity(intent);
            }, null));
        });
    }

    private void setCategoryTitle(@NonNull String category) {
        TextView categorySelection = findViewById(R.id.categorySelection);
        runOnUiThread(() -> categorySelection.setText(getString(R.string.category_selected_prefix, category)));
    }
}