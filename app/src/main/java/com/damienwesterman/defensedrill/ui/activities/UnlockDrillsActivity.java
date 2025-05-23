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
 * Copyright 2025 Damien Westerman
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.ui.adapters.UnlockDrillAdapter;
import com.damienwesterman.defensedrill.ui.view_models.UnlockDrillsViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Activity to mark drills as known or unknown.
 */
@AndroidEntryPoint
public class UnlockDrillsActivity extends AppCompatActivity {
    private UnlockDrillsViewModel viewModel;

    private Button toggleKnownDrillsButton;
    private Button toggleUnknownDrillsButton;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start UnlockDrillsActivity.
     *
     * @param context       Context.
     */
    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, UnlockDrillsActivity.class);
        context.startActivity(intent);
    }

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unlock_drills);

        // Modify Toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Unlock Drills");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(UnlockDrillsViewModel.class);

        toggleKnownDrillsButton = findViewById(R.id.toggleKnownDrillsButton);
        toggleUnknownDrillsButton = findViewById(R.id.toggleUnknownDrillsButton);
        progressBar = findViewById(R.id.unlockDrillsProgressBar);
        recyclerView = findViewById(R.id.unlockDrillsRecyclerView);

        viewModel.getDisplayedDrills().observe(this, this::setUpRecyclerView);
        viewModel.populateDrills();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        viewModel.displayFilteredList();
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
    public void toggleKnownDrills(View view) {
        if (viewModel.isShowKnownDrills()) {
            toggleKnownDrillsButton.setText(R.string.hiding_known_drills);
            viewModel.setShowKnownDrills(false);
        } else {
            toggleKnownDrillsButton.setText(R.string.showing_known_drills);
            viewModel.setShowKnownDrills(true);
        }
    }

    public void toggleUnknownDrills(View view) {
        if (viewModel.isShowUnknownDrills()) {
            toggleUnknownDrillsButton.setText(R.string.hiding_unknown_drills);
            viewModel.setShowUnknownDrills(false);
        } else {
            toggleUnknownDrillsButton.setText(R.string.showing_unknown_drills);
            viewModel.setShowUnknownDrills(true);
        }
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Callback method for when the drills list has been loaded from the database or updated via
     * filter. Sets the UI and attaches checked listener.
     *
     * @param displayedDrills   List of Drill objects.
     */
    private void setUpRecyclerView(@NonNull List<Drill> displayedDrills) {
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Once all the items are rendered: remove this listener, hide progress bar
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                progressBar.setVisibility(View.GONE);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager((this)));
        recyclerView.setAdapter(new UnlockDrillAdapter(displayedDrills, viewModel::setDrillKnown));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }
}
