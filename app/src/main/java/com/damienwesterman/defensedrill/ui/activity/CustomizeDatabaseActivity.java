package com.damienwesterman.defensedrill.ui.activity;

import android.content.Context;
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
import com.damienwesterman.defensedrill.ui.common.UiUtils;

/**
 * Screen to offer the user options for altering the local database via CRUD operations.
 */
public class CustomizeDatabaseActivity extends AppCompatActivity {
    private LinearLayout rootView;

    // =============================================================================================
    // Activity Creation Methods
    // =============================================================================================
    /**
     * Start the CustomizeDatabaseActivity.
     *
     * @param context   Context.
     */
    public static void startActivity(@NonNull Context context) {
        Intent intent = new Intent(context, CustomizeDatabaseActivity.class);
        context.startActivity(intent);
    }

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
    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.viewDrillsCard == cardId) {
            ViewDrillsActivity.startActivity(this);
        } else if (R.id.unlockDrillsCard == cardId) {
            UnlockDrillsActivity.startActivity(this);
        } else if (R.id.viewCategoriesCard == cardId) {
            ViewAbstractCategoriesActivity.startActivity(this,
                    ViewAbstractCategoriesActivity.ActivityMode.MODE_CATEGORIES);
        } else if (R.id.viewSubCategoriesCard == cardId) {
            ViewAbstractCategoriesActivity.startActivity(this,
                    ViewAbstractCategoriesActivity.ActivityMode.MODE_SUB_CATEGORIES);
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }
}
