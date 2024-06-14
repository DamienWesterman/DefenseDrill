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
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.ui.adapters.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.ui.view_models.CategorySelectViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * TODO doc comments
 */
public class CategorySelectActivity extends AppCompatActivity {
    CategorySelectViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);

        viewModel = new ViewModelProvider(this).get(CategorySelectViewModel.class);

        setUpRecyclerView();
    }

    public void randomCategoryClick(View view) {
        Intent intent = new Intent(this, SubCategorySelectActivity.class);
        intent.putExtra(Constants.INTENT_CATEGORY_CHOICE, Constants.USER_RANDOM_SELECTION);
        startActivity(intent);
    }

    private void setUpRecyclerView() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<CategoryEntity> categories = viewModel.getCategories();

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
                recyclerView.setAdapter(new AbstractCategoryAdapter<>(categories, id -> {
                    Intent intent = new Intent(this, SubCategorySelectActivity.class);
                    intent.putExtra(Constants.INTENT_CATEGORY_CHOICE, id);
                    startActivity(intent);
                }));
            });
        });
    }
}