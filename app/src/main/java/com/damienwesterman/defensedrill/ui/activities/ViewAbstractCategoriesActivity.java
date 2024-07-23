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

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.AbstractCategoryEntity;
import com.damienwesterman.defensedrill.ui.adapters.AbstractCategoryAdapter;
import com.damienwesterman.defensedrill.ui.utils.CreateNewEntityCallback;
import com.damienwesterman.defensedrill.ui.utils.Utils;
import com.damienwesterman.defensedrill.ui.view_models.AbstractCategoryListViewModel;
import com.damienwesterman.defensedrill.ui.view_models.CategoryListViewModel;
import com.damienwesterman.defensedrill.ui.view_models.SubCategoryListViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.List;

/**
 * Activity to display, edit, and create categories or sub-categories (determined by the intent).
 * <br><br>
 * This is a generic activity that can be launched to display two different (and very similar)
 * screens. It can act as the screen for Customize Categories OR Customize Sub-Categories. Which one
 * is determined by the intent passed in. This screen allows a user to view all of an
 * AbstractCategory's entries, edit them (by click), delete them (by long click), or create a new
 * one.
 * <br><br>
 * INTENTS: Expects to receive <i>either</i> {@link Constants#INTENT_VIEW_CATEGORIES} or
 * {@link Constants#INTENT_VIEW_SUB_CATEGORIES} to determine what screen to show.
 */
public class ViewAbstractCategoriesActivity extends AppCompatActivity {
    // The following are abstract limits and do not represent any limits imposed by the database
    private static final int NAME_CHARACTER_LIMIT = 128;
    private static final int DESCRIPTION_CHARACTER_LIMIT = 512;

    private String TAG = "ViewAbstractCategoriesActivity";
    private AbstractCategoryListViewModel viewModel;
    private ActivityMode activityMode;

    // TODO go through all snackbars and make the R.id.OVERALL_VIEW a field
    private View rootView;
    private TextView title;
    private TextView instructions;
    private Button createButton;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    private enum ActivityMode {
        MODE_CATEGORIES,
        MODE_SUB_CATEGORIES
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_abstract_categories);

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.INTENT_VIEW_CATEGORIES)) {
            viewModel = new ViewModelProvider(this).get(CategoryListViewModel.class);
            activityMode = ActivityMode.MODE_CATEGORIES;
            TAG += "|Categories";
        } else if (intent.hasExtra(Constants.INTENT_VIEW_SUB_CATEGORIES)) {
            viewModel = new ViewModelProvider(this).get(SubCategoryListViewModel.class);
            activityMode = ActivityMode.MODE_SUB_CATEGORIES;
            TAG += "|SubCategories";
        } else {
            Log.e(TAG, "No category specified in the intent");
            // Toast so it persists screens
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            finish();
        }

        rootView = findViewById(R.id.activityViewAbstractCategories);
        title = findViewById(R.id.allAbstractCategoriesText);
        instructions = findViewById(R.id.allAbstractCategoriesInstruction);
        createButton = findViewById(R.id.createAbstractCategoryButton);
        progressBar = findViewById(R.id.allAbstractCategoriesProgressBar);
        recyclerView = findViewById(R.id.allAbstractCategoriesRecyclerView);

        setScreenTextByMode();
        setLoading(true);
        viewModel.getAbstractCategories().observe(this, this::setUpRecyclerView);
        viewModel.populateAbstractCategories();

        Log.i(TAG, "Started");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setLoading(true);
        viewModel.rePopulateAbstractCategories();
    }

    // TODO Doc comments
    public void createAbstractCategory(View view) {
        createAbstractCategoryPopup();
    }

    private void setUpRecyclerView(List<AbstractCategoryEntity> abstractCategoryEntities) {
        setLoading(true);

        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // Once all the items are rendered: remove this listener, hide progress bar
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setLoading(false);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new AbstractCategoryAdapter(abstractCategoryEntities,
                // Click listener
                id -> viewEditAbstractCategoryPopup(viewModel.findById(id)),
                // Long Click listener
                id -> deleteAbstractCategoryPopup(viewModel.findById(id))));
    }

    private void setScreenTextByMode() {
        if (ActivityMode.MODE_CATEGORIES == activityMode) {
            title.setText(R.string.categories);
            instructions.setText(R.string.all_categories_instructions);
            createButton.setText(R.string.create_new_category);
        } else {
            title.setText(R.string.sub_categories);
            instructions.setText(R.string.all_sub_categories_instructions);
            createButton.setText(R.string.create_new_sub_category);
        }
    }

    private void setLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            createButton.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            createButton.setEnabled(true);
        }
    }

    private void createAbstractCategoryPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_abstract_category_popup, null);

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);

        builder.setView(dialogView);
        builder.setTitle(ActivityMode.MODE_CATEGORIES == activityMode ?
                "Create Category" : "Create sub-Category");
        builder.setIcon(R.drawable.add_circle_icon);
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Back", (dialog, position) -> {
            // Do nothing
        });
        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            String name = nameEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            if (0 == name.length()) {
                Utils.displayDismissibleSnackbar(dialogView,
                        "Name can not be empty");
                return; // Do not dismiss
            } else if (NAME_CHARACTER_LIMIT <= name.length()) {
                Utils.displayDismissibleSnackbar(dialogView,
                        "Name must be less than " + NAME_CHARACTER_LIMIT +  " characters");
                return; // Do not dismiss
            }

            if (0 == description.length()) {
                Utils.displayDismissibleSnackbar(dialogView,
                        "Description can not be empty");
                return; // Do not dismiss
            } else if (DESCRIPTION_CHARACTER_LIMIT <= description.length()) {
                Utils.displayDismissibleSnackbar(dialogView,
                        "Description must be less than " + DESCRIPTION_CHARACTER_LIMIT +  " characters");
                return; // Do not dismiss
            }
            setLoading(true);
            viewModel.saveAbstractEntity(name, description, new CreateNewEntityCallback() {
                @Override
                public void onSuccess() {
                    alert.dismiss();
                    Utils.displayDismissibleSnackbar(rootView,
                            "Save successful!");
                    onRestart();
                }

                @Override
                public void onFailure(String msg) {
                    Utils.displayDismissibleSnackbar(dialogView,
                            "Name already exists");
                    runOnUiThread(() -> setLoading(false));
                    // Do not dismiss
                }
            });
        }));

        alert.show();
    }

    void viewEditAbstractCategoryPopup(AbstractCategoryEntity entity) {
        if (null == entity) {
            Utils.displayDismissibleSnackbar(rootView,
                    "Something went wrong");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.layout_abstract_category_popup, null);

        EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        EditText descriptionEditText = dialogView.findViewById(R.id.descriptionEditText);
        nameEditText.setText(entity.getName());
        descriptionEditText.setText(entity.getDescription());

        builder.setView(dialogView);
        builder.setTitle(ActivityMode.MODE_CATEGORIES == activityMode ?
                "Edit Category" : "Edit sub-Category");
        builder.setIcon(R.drawable.edit_icon);
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Back", (dialog, position) -> {
            // Do nothing
        });
        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
            String name = nameEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            if (0 == name.length()) {
                Utils.displayDismissibleSnackbar(dialogView,
                        "Name can not be empty");
                return; // Do not dismiss
            } else if (NAME_CHARACTER_LIMIT <= name.length()) {
                Utils.displayDismissibleSnackbar(dialogView,
                        "Name must be less than " + NAME_CHARACTER_LIMIT +  " characters");
                return; // Do not dismiss
            }

            if (0 == description.length()) {
                Utils.displayDismissibleSnackbar(dialogView,
                        "Description can not be empty");
                return; // Do not dismiss
            } else if (DESCRIPTION_CHARACTER_LIMIT <= description.length()) {
                Utils.displayDismissibleSnackbar(dialogView,
                        "Description must be less than " + DESCRIPTION_CHARACTER_LIMIT +  " characters");
                return; // Do not dismiss
            }
            setLoading(true);
            entity.setName(name);
            entity.setDescription(description);
            viewModel.updateAbstractEntity(entity, new CreateNewEntityCallback() {
                @Override
                public void onSuccess() {
                    alert.dismiss();
                    Utils.displayDismissibleSnackbar(rootView,
                            "Save successful!");
                    onRestart();
                }

                @Override
                public void onFailure(String msg) {
                    Utils.displayDismissibleSnackbar(dialogView,
                            "Name already exists");
                    runOnUiThread(() -> setLoading(false));
                    // Do not dismiss
                }
            });
        }));

        alert.show();
    }

    void deleteAbstractCategoryPopup(AbstractCategoryEntity entity) {
        if (null == entity) {
            Utils.displayDismissibleSnackbar(rootView,
                    "Something went wrong trying to delete");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Are you sure you want to delete:");
        builder.setIcon(R.drawable.warning_icon);
        builder.setCancelable(true);
        builder.setMessage(entity.getName());
        builder.setNegativeButton("Cancel", null);
        builder.setPositiveButton("Delete", (dialog, position) -> {
            setLoading(true);
            viewModel.deleteAbstractCategory(entity);
        });
        builder.create().show();
    }
}