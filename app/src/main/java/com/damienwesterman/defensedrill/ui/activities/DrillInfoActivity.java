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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.ui.view_models.DrillInfoViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * TODO doc comments
 * TODO Notate the required intents
 */
public class DrillInfoActivity extends AppCompatActivity {
    // TODO if they make changes and try to navigate away without saving, prompt user to save
    //      changes, maybe have a "changed" field
    private static final int LOW_CONFIDENCE_POSITION = 0;
    private static final int MEDIUM_CONFIDENCE_POSITION = 1;
    private static final int HIGH_CONFIDENCE_POSITION = 2;

    // TODO doc comments
    private enum ScreenType {
        GeneratedDrill,
        DisplayingDrill
    }

    private DrillInfoViewModel viewModel;
    private ScreenType screenType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drill_info);

        if (getIntent().hasExtra(Constants.INTENT_DRILL_ID)) {
            screenType = ScreenType.DisplayingDrill;
        } else {
            screenType = ScreenType.GeneratedDrill;
        }

        Spinner confidenceSpinner = findViewById(R.id.confidenceSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.confidence_levels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        confidenceSpinner.setAdapter(adapter);

        setUpViewModel();
    }

    public void regenerateDrill(View view) {
        changeUiToDrillLoading();
        viewModel.regenerateDrill();
    }

    public void resetSkippedDrills(View view) {
        viewModel.resetSkippedDrills();
        Toast.makeText(this, "Skipped drills have been reset", Toast.LENGTH_SHORT).show();
    }

    public void saveDrillInfo(View view) { // TODO change to be only the date or whatever
        Drill drill = collectDrillInfo();
        // TODO Finish this
//        viewModel.saveDrill(drill, this);
        // TODO toast saying success if successful
    }
    // TODO add the save button (with toast of success

    public void goHome(View view) {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }

    private void setUpViewModel() {
        viewModel = new ViewModelProvider(this).get(DrillInfoViewModel.class);

        viewModel.getDrill().observe(this, drill -> runOnUiThread(() -> {
            if (null == drill) {
                alertNoDrillFound();
                return;
            }

            fillDrillInfo(drill);
            changeUiToDrillInfoShown();

            // TODO: Eventually incorporate functionality to display the links for how to
            //       descriptions and videos
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
        // TODO Check what screenType we are in and change what we display, and offer to go home or reset skipped drills
    }

    private void changeUiToDrillLoading() {
        findViewById(R.id.drillProgressBar).setVisibility(View.VISIBLE);
        findViewById(R.id.drillName).setVisibility(View.GONE);
        findViewById(R.id.drillInfoDivider).setVisibility(View.GONE);
        findViewById(R.id.lastDrilledLabel).setVisibility(View.GONE);
        findViewById(R.id.lastDrilledDate).setVisibility(View.GONE);
        findViewById(R.id.notesLabel).setVisibility(View.GONE);
        findViewById(R.id.notes).setVisibility(View.GONE);
        findViewById(R.id.confidenceLabel).setVisibility(View.GONE);
        findViewById(R.id.confidenceSpinner).setVisibility(View.GONE);
        if (ScreenType.GeneratedDrill == screenType) {
            findViewById(R.id.regenerateButton).setVisibility(View.GONE);
            findViewById(R.id.resetSkippedDrillsButton).setVisibility(View.GONE);
        }
        findViewById(R.id.saveDrillInfoButton).setVisibility(View.GONE);
    }

    private void changeUiToDrillInfoShown() {
        findViewById(R.id.drillProgressBar).setVisibility(View.GONE);
        findViewById(R.id.drillName).setVisibility(View.VISIBLE);
        findViewById(R.id.drillInfoDivider).setVisibility(View.VISIBLE);
        findViewById(R.id.lastDrilledLabel).setVisibility(View.VISIBLE);
        findViewById(R.id.lastDrilledDate).setVisibility(View.VISIBLE);
        findViewById(R.id.notesLabel).setVisibility(View.VISIBLE);
        findViewById(R.id.notes).setVisibility(View.VISIBLE);
        findViewById(R.id.confidenceLabel).setVisibility(View.VISIBLE);
        findViewById(R.id.confidenceSpinner).setVisibility(View.VISIBLE);
        if (ScreenType.GeneratedDrill == screenType) {
            findViewById(R.id.regenerateButton).setVisibility(View.VISIBLE);
            findViewById(R.id.resetSkippedDrillsButton).setVisibility(View.VISIBLE);
        }
        findViewById(R.id.saveDrillInfoButton).setVisibility(View.VISIBLE);
    }

    private Drill collectDrillInfo() {
        return null;
    }

    private void fillDrillInfo(Drill drill) {TextView drillName = findViewById(R.id.drillName);
        drillName.setText(drill.getName());
        Spinner confidenceSpinner = findViewById(R.id.confidenceSpinner);
        int position;
        switch(drill.getConfidence()) {
            case Drill.HIGH_CONFIDENCE:
                position = HIGH_CONFIDENCE_POSITION;
                break;
            case Drill.MEDIUM_CONFIDENCE:
                position = MEDIUM_CONFIDENCE_POSITION;
                break;
            case Drill.LOW_CONFIDENCE:
            default:
                position = LOW_CONFIDENCE_POSITION;
                break;
        }
        confidenceSpinner.setSelection(position);
        TextView lastDrilled = findViewById(R.id.lastDrilledDate);
        Date drilledDate = new Date(drill.getLastDrilled());
        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        lastDrilled.setText(dateFormatter.format(drilledDate));
        TextView notes = findViewById(R.id.notes);
        notes.setText(drill.getNotes());
    }
}