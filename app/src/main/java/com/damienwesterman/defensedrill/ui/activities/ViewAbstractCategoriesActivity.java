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
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.adapters.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.ui.view_models.AbstractCategoryListViewModel;
import com.damienwesterman.defensedrill.ui.view_models.CategoryListViewModel;
import com.damienwesterman.defensedrill.ui.view_models.SubCategoryListViewModel;
import com.damienwesterman.defensedrill.utils.Constants;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

// TODO doc comments (necessary intents)
public class ViewAbstractCategoriesActivity extends AppCompatActivity {
    private String TAG = "ViewAbstractCategoriesActivity";
    private AbstractCategoryListViewModel viewModel;
    private ActivityMode activityMode;

    // TODO go through all snackbars and make the R.id.OVERALL_VIEW a field, maybe abstract out the call itself
    private View rootView;
    private TextView title;
    private TextView instructions;
    private Button createButton;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private enum ActivityMode {
        MODE_CATEGORIES,
        MODE_SUB_CATEGORIES
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_abstract_categories);

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.INTENT_VIEW_CATEGORIES)) {
            viewModel = new ViewModelProvider(this).get(CategoryListViewModel.class);
            activityMode = ActivityMode.MODE_CATEGORIES;
            TAG += "|Categories";
        } else if (intent.hasExtra(Constants.INTENT_VIEW_SUB_CATEGORIES)) {
            viewModel = new ViewModelProvider(this).get(SubCategoryListViewModel.class);
            activityMode = ActivityMode.MODE_SUB_CATEGORIES;
            TAG += "|SubCategories";
        } else {
            Log.e(TAG, "No category specified in the intent");
            // Toast so it persists screens
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

        rootView = findViewById(R.id.activityViewAbstractCategories);
        title = findViewById(R.id.allAbstractCategoriesText);
        instructions = findViewById(R.id.allAbstractCategoriesInstruction);
        createButton = findViewById(R.id.createAbstractCategoryButton);
        progressBar = findViewById(R.id.allAbstractCategoriesProgressBar);
        recyclerView = findViewById(R.id.allAbstractCategoriesRecyclerView);

        setScreenTextByMode();
        setLoading(true);
        viewModel.getAbstractCategories().observe(this, this::setUpRecyclerView);
        viewModel.populateAbstractCategories();

        Log.i(TAG, "Started");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setLoading(true);
        viewModel.rePopulateAbstractCategories();
    }

    public void createAbstractCategory(View view) {
    }

    private void setUpRecyclerView(List<AbstractCategoryEntity> abstractCategoryEntities) {
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
        recyclerView.setAdapter(new AbstractCategoryAdapter(abstractCategoryEntities,
                // Click listener
                id -> viewEditAbstractCategoryPopup(viewModel.findById(id)),
                // Long Click listener
                id -> deleteAbstractCategoryPopup(viewModel.findById(id))));
    }

    private void setScreenTextByMode() {
        if (ActivityMode.MODE_CATEGORIES == activityMode) {
            title.setText(R.string.categories);
            instructions.setText(R.string.all_categories_instructions);
            createButton.setText(R.string.create_new_category);
        } else if (ActivityMode.MODE_SUB_CATEGORIES == activityMode) {
            title.setText(R.string.sub_categories);
            instructions.setText(R.string.all_sub_categories_instructions);
            createButton.setText(R.string.create_new_sub_category);
        } else {
            Snackbar snackbar = Snackbar.make(rootView, "Something went wrong",
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("OK", (view) -> snackbar.dismiss());
            snackbar.show();
        }
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    void viewEditAbstractCategoryPopup(AbstractCategoryEntity entity) {

    }

    void deleteAbstractCategoryPopup(AbstractCategoryEntity entity) {

    }
}