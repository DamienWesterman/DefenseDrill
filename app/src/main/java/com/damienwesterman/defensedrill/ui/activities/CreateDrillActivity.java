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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.CreateNewDrillCallback;
import com.damienwesterman.defensedrill.ui.view_models.CreateDrillViewModel;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// TODO change some toasts to snackbars (across the whole app) so user can dismiss then (check ChatGPT)
// TODO doc comments
public class CreateDrillActivity extends AppCompatActivity {
    private CreateDrillViewModel viewModel;
    private Context context;

    private EditText enteredName;
    private Spinner confidenceSpinner;
    private Button categoriesButton;
    private Button subCategoriesButton;
    private EditText enteredNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_drill);

        viewModel = new ViewModelProvider(this).get(CreateDrillViewModel.class);
        context = this;

        viewModel.loadAllCategories();
        viewModel.loadAllSubCategories();

        enteredName = findViewById(R.id.nameEditText);
        confidenceSpinner = findViewById(R.id.confidenceSpinner);
        categoriesButton = findViewById(R.id.addCategoriesButton);
        subCategoriesButton = findViewById(R.id.addSubCategoriesButton);
        enteredNotes = findViewById(R.id.notes);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.confidence_levels,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        confidenceSpinner.setAdapter(adapter);
    }

    public void addCategories(View view) {
        addCategoriesPopup(viewModel.getAllCategories());
    }

    public void addSubCategories(View view) {
        addSubCategoriesPopup(viewModel.getAllSubCategories());
    }

    public void saveDrill(View view) {
        Drill drill = generateDrillFromUserInput();
        setUserEditable(false);
        if (null != drill) {
            // TODO check to make sure user has selected at least one category/sub-category, popup to confirm if 0
            // TODO popup to have the user double check the spelling of the name, cannot change
            viewModel.saveDrill(drill, new CreateNewDrillCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> Toast.makeText(context, "Successfully saved", Toast.LENGTH_SHORT).show());
                    // TODO popup option confirming saved, allow to enter another or to go back
                    // TODO when returning to last screen, make sure it re loads with the new drills
                }

                @Override
                public void onFailure(String msg) {
                    runOnUiThread(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            setUserEditable(true);
        }
        // TODO: MAKE SURE TO setUserEditable(true) IN _EVER_ FLOW PATH
    }

    private void addCategoriesPopup(List<CategoryEntity> categories) {
        if (null == categories) {
            Toast.makeText(this, "Issue retrieving categories", Toast.LENGTH_SHORT).show();
            return;
        } else if (0 == categories.size()) {
            Toast.makeText(this, "No Categories in database", Toast.LENGTH_SHORT).show();
            return;
        }

        List<CategoryEntity> checkedCategoryEntities = viewModel.getCheckedCategoryEntities();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] categoryNames = categories
                .stream().map(CategoryEntity::getName).toArray(String[]::new);
        final boolean[] checkedCategories = new boolean[categoryNames.length];

        for (int i = 0; i < checkedCategories.length; i++) {
            if (checkedCategoryEntities.contains(categories.get(i))) {
                checkedCategories[i] = true;
            }
        }

        builder.setTitle("Select Categories");
        builder.setIcon(R.drawable.add_circle_icon);
        builder.setMultiChoiceItems(categoryNames, checkedCategories,
                (dialog, position, isChecked) -> checkedCategories[position] = isChecked);
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Back", null);
        builder.setNeutralButton("Clear All", null);
        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // "Save"
                for (int i = 0; i < checkedCategories.length; i++) {
                    if (checkedCategories[i] && !checkedCategoryEntities.contains(categories.get(i))) {
                        // Checked but not already in list - add
                        checkedCategoryEntities.add(categories.get(i));
                    } else if (!checkedCategories[i]) {
                        // Not checked but in list - remove
                        checkedCategoryEntities.remove(categories.get(i));
                    }
                }
                alert.dismiss();
            });
            alert.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view -> {
                // "Back"
                alert.dismiss();
            });
            alert.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                // "Clear All"
                Arrays.fill(checkedCategories, false);

                ListView listView = alert.getListView();
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, false);
                }
                // Do not dismiss
            });
        });

        alert.show();
    }

    private void addSubCategoriesPopup(List<SubCategoryEntity> subCategories) {
        if (null == subCategories) {
            Toast.makeText(this, "Issue retrieving sub-categories", Toast.LENGTH_SHORT).show();
            return;
        } else if (0 == subCategories.size()) {
            Toast.makeText(this, "No sub-Categories in database", Toast.LENGTH_SHORT).show();
            return;
        }

        List<SubCategoryEntity> checkedSubCategoryEntities = viewModel.getCheckedSubCategoryEntities();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final String[] subCategoryNames = subCategories
                .stream().map(SubCategoryEntity::getName).toArray(String[]::new);
        final boolean[] checkedSubCategories = new boolean[subCategoryNames.length];

        for (int i = 0; i < checkedSubCategories.length; i++) {
            if (checkedSubCategoryEntities.contains(subCategories.get(i))) {
                checkedSubCategories[i] = true;
            }
        }

        builder.setTitle("Select sub-Categories");
        builder.setIcon(R.drawable.add_circle_icon);
        builder.setMultiChoiceItems(subCategoryNames, checkedSubCategories,
                (dialog, position, isChecked) -> checkedSubCategories[position] = isChecked);
        builder.setCancelable(true);
        builder.setPositiveButton("Save", null);
        builder.setNegativeButton("Back", null);
        builder.setNeutralButton("Clear All", null);
        AlertDialog alert = builder.create();
        alert.setOnShowListener(dialogInterface -> {
            alert.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(view -> {
                // "Save"
                for (int i = 0; i < checkedSubCategories.length; i++) {
                    if (checkedSubCategories[i] && !checkedSubCategoryEntities.contains(subCategories.get(i))) {
                        // Checked but not already in list - add
                        checkedSubCategoryEntities.add(subCategories.get(i));
                    } else if (!checkedSubCategories[i]) {
                        // Not checked but in list - remove
                        checkedSubCategoryEntities.remove(subCategories.get(i));
                    }
                }
                alert.dismiss();
            });
            alert.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(view -> {
                // "Back"
                alert.dismiss();
            });
            alert.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(view -> {
                // "Clear All"
                Arrays.fill(checkedSubCategories, false);

                ListView listView = alert.getListView();
                for (int i = 0; i < listView.getCount(); i++) {
                    listView.setItemChecked(i, false);
                }
                // Do not dismiss
            });
        });

        alert.show();
    }

    private void setUserEditable(boolean editable) {
        enteredName.setEnabled(editable);
        confidenceSpinner.setEnabled(editable);
        categoriesButton.setEnabled(editable);
        subCategoriesButton.setEnabled(editable);
        enteredNotes.setEnabled(editable);
    }

    // TODO Doc comments, also does input sanitation, nullable if issue
    private Drill generateDrillFromUserInput() {
        // Both of these are limits just for display purposes on other screen. They are a little
        // arbitrary and do not represent actual limits in the database layer.
        final int NAME_CHARACTER_LIMIT = 256;
        final int NOTES_CHARACTER_LIMIT = 2048;

        Drill drill;
        String name;
        String notes;

        name = enteredName.getText().toString();
        if (0 == name.length()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return null;
        } else if (NAME_CHARACTER_LIMIT <= name.length()) {
            Toast.makeText(this, "Name must be less than " + NAME_CHARACTER_LIMIT +
                    " characters", Toast.LENGTH_SHORT).show();
            return null;
        }

        notes = enteredNotes.getText().toString();
        if (NOTES_CHARACTER_LIMIT <= notes.length()) {
            Toast.makeText(this, "Notes must be less than " + NOTES_CHARACTER_LIMIT +
                    " characters", Toast.LENGTH_SHORT).show();
            return null;
        }

        drill = new Drill(
                name,
                System.currentTimeMillis(),
                true,
                Constants.confidencePositionToWeight(confidenceSpinner.getSelectedItemPosition()),
                notes,
                0,
                viewModel.getCheckedCategoryEntities(),
                viewModel.getCheckedSubCategoryEntities()
        );

        return drill;
    }

    // TODO make sure we do input sanitation checking
    // TODO Last drilled today BUT set it as a new drill
}