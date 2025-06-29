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
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.ui.adapter.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.ui.common.OnboardingUtils;
import com.damienwesterman.defensedrill.ui.viewmodel.CategoryViewModel;
import com.damienwesterman.defensedrill.common.Constants;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity during Drill Generation to select a Category of drill, or random.
 * <br><br>
 * Then Launches {@link SubCategorySelectActivity} sending the selected Category.
 */
@AndroidEntryPoint
public class CategorySelectActivity extends AppCompatActivity {
    @Inject
    SharedPrefs sharedPrefs;

    private Context context;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start the CategorySelectActivity.
     *
     * @param context   Context.
     */
    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, CategorySelectActivity.class);
        context.startActivity(intent);
    }

    /**
     * Start the CategorySelectActivity. Clears the activity stack so CategorySelect is now the top.
     *
     * @param context   Context.
     */
    public static void startActivityClearTop(@NonNull Context context) {
        Intent intent = new Intent(context, CategorySelectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Start the CategorySelectActivity in the Onboarding state.
     *
     * @param context   Context.
     */
    public static void startOnboardingActivity(@NonNull Context context) {
        Intent intent = new Intent(context, CategorySelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_START_ONBOARDING, "");
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_select);
        Toolbar appToolbar = findViewById(R.id.appToolbar);

        context = this;

        // Modify Toolbar
        appToolbar.setTitle("Generate Drill");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setUpRecyclerView();

        if (getIntent().hasExtra(Constants.INTENT_EXTRA_START_ONBOARDING)) {
            startOnboarding();
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
    public void randomCategoryClick(View view) {
        SubCategorySelectActivity.startActivity(this, Constants.USER_RANDOM_SELECTION);
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Private helper method to set up the recyclerView list of Categories and their callback.
     */
    private void setUpRecyclerView() {
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

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        AbstractCategoryAdapter adapter = new AbstractCategoryAdapter(
                // Card Click Listener
                id -> SubCategorySelectActivity.startActivity(this, id),
                // Long Card Click Listener
                null);
        recyclerView.setAdapter(adapter);


        CategoryViewModel viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        viewModel.getUiAbstractCategoriesList().observe(this, adapter::submitList);
        viewModel.populateAbstractCategories();
    }

    // =============================================================================================
    // Onboarding Methods
    // =============================================================================================
    /**
     * Start the onboarding process for this activity, walking the user through the screen and
     * explaining how it works. Preceded by
     * {@link HomeActivity#continueOnboardingActivity(Context, Class)}, proceeded by
     * {@link SubCategorySelectActivity#startOnboardingActivity(Context)}.
     */
    private void startOnboarding() {
        boolean cancelable = sharedPrefs.isOnboardingComplete();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        TapTarget randomTapTarget = OnboardingUtils.createTapTarget(
                findViewById(R.id.randomCategoryCard),
                "Select a Category",
                getString(R.string.onboarding_random_category_description),
                cancelable);

        Runnable startTapTarget = () -> TapTargetView.showFor(this, randomTapTarget,
                new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            super.onTargetClick(view);
                            SubCategorySelectActivity.startOnboardingActivity(context);
                        }
        });

        activityUsePopup(cancelable, startTapTarget);
    }

    /**
     * Explain the purpose of this activity.
     *
     * @param cancelable        true if this popup can be canceled by the user.
     * @param onDialogFinish    Runnable once the user has finished the popup.
     */
    private void activityUsePopup(boolean cancelable,
                                  @Nullable Runnable onDialogFinish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Category");
        builder.setIcon(R.drawable.ic_launcher_foreground);
        builder.setCancelable(cancelable);
        builder.setMessage(R.string.onboarding_category_select_activity_description);
        builder.setPositiveButton("Continue", (dialogInterface, i) -> {
            if (null != onDialogFinish) {
                onDialogFinish.run();
            }
        });
        if (cancelable) {
            builder.setNeutralButton("Exit", null);
        }

        builder.create().show();
    }
}
