package com.damienwesterman.defensedrill.ui.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
import com.damienwesterman.defensedrill.data.remote.dto.InstructionsDTO;
import com.damienwesterman.defensedrill.utils.Constants;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the a list of Instructions for a given drill Drill.
 * <br><br>
 * INTENTS: Expects to receive BOTH of the following-
 * <ul>
 *    <li> A {@link Constants#INTENT_DRILL_DTO} intent.</li>
 *    <li> A {@link Constants#INTENT_INSTRUCTION_INDEX} intent.</li>
 * </ul>
 */
public class InstructionsActivity extends AppCompatActivity {
    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        TextView drillNameView = findViewById(R.id.drillName);
        TextView instructionsDescriptionView = findViewById(R.id.instructionsDescription);
        ListView listView = findViewById(R.id.instructionsList);

        // Get Drill
        if (getIntent().hasExtra(Constants.INTENT_DRILL_DTO)
                && getIntent().hasExtra(Constants.INTENT_INSTRUCTION_INDEX)) {
            Serializable serializedDrill = getIntent().getSerializableExtra(Constants.INTENT_DRILL_DTO);
            DrillDTO drill;
            String drillName;
            if (serializedDrill instanceof DrillDTO) {
                drill = (DrillDTO) serializedDrill;
                drillName = drill.getName();
            } else {
                // Did not receive the proper intents. Toast so it persists screens
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Get instructions
            int instructionsIndex = getIntent().getIntExtra(Constants.INTENT_INSTRUCTION_INDEX, -1);
            if (0 > instructionsIndex) {
                // Did not receive the proper intents. Toast so it persists screens
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            InstructionsDTO instructions = drill.getInstructions().get(instructionsIndex);
            String instructionsDescription = instructions.getDescription();
            List<String> steps = instructions.getSteps();

            // Setup UI
            drillNameView.setText(drillName);
            instructionsDescriptionView.setText(instructionsDescription);
            List<String> formattedSteps = new ArrayList<>(steps.size());
            for (int i = 0; i < steps.size(); i++) {
                String sb = (i + 1) +
                        ". " +
                        steps.get(i);
                formattedSteps.add(i, sb);
            }
            ArrayAdapter<String> arr = new ArrayAdapter<>(
                    this,
                    R.layout.layout_list_item,
                    formattedSteps);
            listView.setAdapter(arr);
        } else {
            // Did not receive the proper intents. Toast so it persists screens
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}