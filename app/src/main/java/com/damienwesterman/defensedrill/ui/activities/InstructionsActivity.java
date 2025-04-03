package com.damienwesterman.defensedrill.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
import com.damienwesterman.defensedrill.utils.Constants;

import java.io.Serializable;
import java.util.List;

/**
 * Displays the a list of Instructions for a given drill Drill.
 * <br><br>
 * INTENTS: Expects to receive BOTH of the following-
 * <ul>
 *    <li> A {@link Constants#INTENT_DRILL_ID} intent.</li>
 *    <li> A {@link Constants#INTENT_DRILL_NAME} intent.</li>
 *    <li> A {@link Constants#INTENT_INSTRUCTIONS_DESCRIPTION} intent.</li>
 *    <li> A {@link Constants#INTENT_INSTRUCTION_INDEX} intent.</li>
 * </ul>
 */
public class InstructionsActivity extends AppCompatActivity {
    private long drillId;
    private String drillName;
    private String instructionsDescription;
    private int instructionsIndex;

    // Views
    private TextView drillNameView;
    private TextView instructionsDescriptionView;
    private View divider;
    private ProgressBar progressBar;
    private ListView listView;


    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        drillNameView = findViewById(R.id.drillName);
        instructionsDescriptionView = findViewById(R.id.instructionsDescription);
        progressBar = findViewById(R.id.instructionsProgressBar);
        divider = findViewById(R.id.instructionsDivider);
        listView = findViewById(R.id.instructionsList);

        setUiLoading(true);

        if (getIntent().hasExtra(Constants.INTENT_DRILL_ID)
                && getIntent().hasExtra(Constants.INTENT_DRILL_NAME)
                && getIntent().hasExtra(Constants.INTENT_INSTRUCTIONS_DESCRIPTION)
                && getIntent().hasExtra(Constants.INTENT_INSTRUCTION_INDEX)) {
            drillId = getIntent().getLongExtra(Constants.INTENT_DRILL_ID, -1);
            if (0 > drillId) {
                // Did not receive the proper intents. Toast so it persists screens
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
            }

            drillName = getIntent().getStringExtra(Constants.INTENT_DRILL_NAME);
            instructionsDescription = getIntent().getStringExtra(Constants.INTENT_INSTRUCTIONS_DESCRIPTION);
            drillNameView.setText(drillName);
            instructionsDescriptionView.setText(instructionsDescription);

            // Set instructions
            instructionsIndex = getIntent().getIntExtra(Constants.INTENT_INSTRUCTION_INDEX, -1);
            if (0 > instructionsIndex) {
                // Did not receive the proper intents. Toast so it persists screens
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
            }


        } else {
            // Did not receive the proper intents. Toast so it persists screens
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    private void setUiLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }
}