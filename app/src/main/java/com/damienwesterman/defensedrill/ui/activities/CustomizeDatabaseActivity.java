package com.damienwesterman.defensedrill.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.ui.utils.UiUtils;
import com.damienwesterman.defensedrill.utils.Constants;

/**
 * Screen to offer the user options for altering the local database via CRUD operations.
 * <br><br>
 * INTENTS: None expected.
 */
public class CustomizeDatabaseActivity extends AppCompatActivity {
    // TODO: Add an "unlock drills" activity
        // TODO: This should basically be a list of cards as before, however we want to gray out anything that is "not known"
        // TODO: On click should set in DB known or not known as well as change the card to "activate/deactivated" as denoted by gray or green
        // TODO: There should be two buttons at the top: "showing/hiding learned drills" and "showing/hiding unknown drills"

    private LinearLayout rootView;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_database);

        // Modify Toolbar
        Toolbar appToolbar = findViewById(R.id.appToolbar);
        appToolbar.setTitle("Customize Database");
        setSupportActionBar(appToolbar);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        rootView = findViewById(R.id.activityCustomizeDatabase);
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

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.viewDrillsCard == cardId) {
            Intent intent = new Intent(this, ViewDrillsActivity.class);
            startActivity(intent);
        } else if (R.id.unlockDrillsCard == cardId) {
            UnlockDrillsActivity.startActivity(this);
        } else if (R.id.viewCategoriesCard == cardId) {
            Intent intent = new Intent(this, ViewAbstractCategoriesActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_VIEW_CATEGORIES, "");
            startActivity(intent);
        } else if (R.id.viewSubCategoriesCard == cardId) {
            Intent intent = new Intent(this, ViewAbstractCategoriesActivity.class);
            intent.putExtra(Constants.INTENT_EXTRA_VIEW_SUB_CATEGORIES, "");
            startActivity(intent);
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }
}
