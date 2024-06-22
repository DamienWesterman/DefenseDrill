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
import android.widget.ProgressBar;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.ui.adapters.DrillAdapter;
import com.damienwesterman.defensedrill.ui.view_models.DrillListViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.List;

/**
 * TODO Doc comments
 */
public class ViewDrillsActivity extends AppCompatActivity {
    private DrillListViewModel viewModel;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    // TODO Add buttons for filters

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_drills);

        viewModel = new ViewModelProvider(this).get(DrillListViewModel.class);
        progressBar = findViewById(R.id.allDrillsProgressBar);
        recyclerView = findViewById(R.id.allDrillsRecyclerView);

        viewModel.getDrills().observe(this, this::setUpRecyclerView);
        viewModel.populateDrills();
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
        recyclerView.setAdapter(new DrillAdapter(drills, id -> {
            Intent intent = new Intent(this, DrillInfoActivity.class);
            intent.putExtra(Constants.INTENT_DRILL_ID, id);
            startActivity(intent);
        }));
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


}