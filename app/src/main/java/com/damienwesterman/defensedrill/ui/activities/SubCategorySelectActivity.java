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

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.adapters.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.ui.view_models.SubCategorySelectViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * TODO Doc comments
 * TODO note the required intents
 */
public class SubCategorySelectActivity extends AppCompatActivity {
    SubCategorySelectViewModel viewModel;
    long userCategoryChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category_select);

        viewModel = new ViewModelProvider(this).get(SubCategorySelectViewModel.class);

        userCategoryChoice = getIntent().getLongExtra(Constants.INTENT_CATEGORY_CHOICE,
                Constants.USER_RANDOM_SELECTION);

        setUpRecyclerView();
    }

    public void randomSubCategoryClick(View view) {
        Intent intent = new Intent(this, DrillInfoActivity.class);
        intent.putExtra(Constants.INTENT_CATEGORY_CHOICE, Constants.USER_RANDOM_SELECTION);
        intent.putExtra(Constants.INTENT_SUB_CATEGORY_CHOICE, Constants.USER_RANDOM_SELECTION);
        startActivity(intent);
    }

    private void setUpRecyclerView() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<SubCategoryEntity> subCategories = viewModel.getCategories(userCategoryChoice);

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
                recyclerView.setAdapter(new AbstractCategoryAdapter<>(subCategories, id -> {
                    Intent intent = new Intent(this, DrillInfoActivity.class);
                    intent.putExtra(Constants.INTENT_CATEGORY_CHOICE, userCategoryChoice);
                    intent.putExtra(Constants.INTENT_SUB_CATEGORY_CHOICE, id);
                    startActivity(intent);
                }));
            });
        });
    }
}