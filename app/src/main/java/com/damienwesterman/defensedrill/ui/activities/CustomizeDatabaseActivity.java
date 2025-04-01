package com.damienwesterman.defensedrill.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.ui.utils.UiUtils;
import com.damienwesterman.defensedrill.utils.Constants;

/**
 * Screen to offer the user options for altering the local database via CRUD operations.
 * <br><br>
 * INTENTS: None expected.
 */
public class CustomizeDatabaseActivity extends AppCompatActivity {
    private LinearLayout rootView;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customize_database);

        rootView = findViewById(R.id.activityCustomizeDatabase);
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.viewDrillsCard == cardId) {
            Intent intent = new Intent(this, ViewDrillsActivity.class);
            startActivity(intent);
        } else if (R.id.viewCategoriesCard == cardId) {
            Intent intent = new Intent(this, ViewAbstractCategoriesActivity.class);
            intent.putExtra(Constants.INTENT_VIEW_CATEGORIES, "");
            startActivity(intent);
        } else if (R.id.viewSubCategoriesCard == cardId) {
            Intent intent = new Intent(this, ViewAbstractCategoriesActivity.class);
            intent.putExtra(Constants.INTENT_VIEW_SUB_CATEGORIES, "");
            startActivity(intent);
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }
}