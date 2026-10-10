package com.damienwesterman.defensedrill.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.BackupImportViewModel;
import com.damienwesterman.defensedrill.ui.common.UiUtils;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Screen to offer the user options for altering the local database via CRUD operations.
 */
@AndroidEntryPoint
public class CustomizeDatabaseActivity extends AppCompatActivity {
    private static final String TAG = CustomizeDatabaseActivity.class.getSimpleName();

    private BackupImportViewModel backupImportViewModel;
    private ActivityResultLauncher<String> createDocumentLauncher;

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
        backupImportViewModel = new ViewModelProvider(this).get(BackupImportViewModel.class);

        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("application/json"),
                uri -> backupImportViewModel.backupDatabase(uri, (success, message) -> {
                    if (!success) {
                        Log.e(TAG, "backupDatabase Failed: " + message);
                    }
                    // TODO: Replace with a popup or something
                    runOnUiThread(() -> UiUtils.displayDismissibleSnackbar(rootView, message));
                }));
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
        } else if (R.id.backupDatabaseCard == cardId) {
            // TODO: implement properly
            createDocumentLauncher.launch("defense_drill_backup.json");
        } else if (R.id.importDatabaseCard == cardId) {
            UiUtils.displayDismissibleSnackbar(rootView, "IMPORTING DATA");
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }
}
