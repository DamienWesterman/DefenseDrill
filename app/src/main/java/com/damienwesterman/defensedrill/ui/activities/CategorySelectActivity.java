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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.ui.adapters.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.ui.view_models.CategoryViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.List;

/**
 * Activity during Drill Generation to select a Category of drill, or random.
 * <br><br>
 * Then Launches {@link SubCategorySelectActivity} sending the selected Category.
 * <br><br>
 * INTENTS: None required.
 */
public class CategorySelectActivity extends AppCompatActivity {
    CategoryViewModel viewModel;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);

        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        viewModel.getAbstractCategories().observe(this, this::setUpRecyclerView);
        viewModel.populateAbstractCategories();
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void randomCategoryClick(View view) {
        Intent intent = new Intent(this, SubCategorySelectActivity.class);
        intent.putExtra(Constants.INTENT_CATEGORY_CHOICE, Constants.USER_RANDOM_SELECTION);
        startActivity(intent);
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Private helper method to set up the recyclerView list of Categories and their callback.
     */
    private void setUpRecyclerView(List<AbstractCategoryEntity> abstractCategories) {
        List<CategoryEntity> categories = CategoryViewModel.getCategoryList(abstractCategories);

        RecyclerView recyclerView = findViewById(R.id.categoryRecyclerView);
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Once all the items are rendered: remove this listener, hide progress bar,
                // and display the random option
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                findViewById(R.id.categoryProgressBar).setVisibility(View.GONE);
                findViewById(R.id.randomCategoryCard).setVisibility(View.VISIBLE);
            }
        });

        runOnUiThread(() -> {
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new AbstractCategoryAdapter(categories,
                    // Card click listener
                    id -> {
                Intent intent = new Intent(this, SubCategorySelectActivity.class);
                intent.putExtra(Constants.INTENT_CATEGORY_CHOICE, id);
                startActivity(intent);
            }, null));
        });
    }
}