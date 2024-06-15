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

import android.content.Intent;
import android.os.Bundle;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.ui.view_models.DrillInfoViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

/**
 * TODO doc comments
 * TODO Notate the required intents
 */
public class DrillInfoActivity extends AppCompatActivity {
    private DrillInfoViewModel viewModel;
    // TODO make an enum or something so we know the origin of the intent and what views to display

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_info);
        // TODO: What to do when we do not return a drill

        setUpViewModel();
    }

    private void setUpViewModel() {
        viewModel = new ViewModelProvider(this).get(DrillInfoViewModel.class);

        viewModel.getDrill().observe(this, drill -> runOnUiThread(() -> {
            if (null == drill) {
                alertNoDrillFound();
                return;
            }
            // TODO Update UI components
        }));

        Intent intent = getIntent();

        if (intent.hasExtra(Constants.INTENT_DRILL_ID)) {
            long drillId = intent.getLongExtra(Constants.INTENT_DRILL_ID, -1);
            viewModel.populateDrill(drillId);
        } else {
            long categoryId = intent.getLongExtra(Constants.INTENT_CATEGORY_CHOICE, -1);
            long subCategoryId = intent.getLongExtra(Constants.INTENT_SUB_CATEGORY_CHOICE, -1);
            viewModel.populateDrill(categoryId, subCategoryId);
        }
    }

    private void alertNoDrillFound() {

    }
}