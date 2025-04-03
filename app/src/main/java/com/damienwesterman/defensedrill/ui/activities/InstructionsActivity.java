package com.damienwesterman.defensedrill.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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

        // Modify Toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Instructions Details");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (R.id.homeButton == item.getItemId()) {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
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
}