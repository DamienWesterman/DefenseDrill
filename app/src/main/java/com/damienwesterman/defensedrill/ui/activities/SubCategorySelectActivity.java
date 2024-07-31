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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.adapters.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.ui.view_models.CategoryViewModel;
import com.damienwesterman.defensedrill.ui.view_models.SubCategoryViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.List;

/**
 * Activity during Drill Generation to select a sub-Category of drill, or random.
 * <br><br>
 * Proceeds {@link CategorySelectActivity}, then Launches {@link DrillInfoActivity} sending the
 * selected Category and SubCategory.
 * <br><br>
 * INTENTS: Expects to receive a {@link Constants#INTENT_CATEGORY_CHOICE} intent.
 */
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