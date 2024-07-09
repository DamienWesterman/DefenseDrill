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
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.ui.utils.CreateNewDrillCallback;
import com.damienwesterman.defensedrill.ui.view_models.CreateDrillViewModel;

public class CreateDrillActivity extends AppCompatActivity {
    private CreateDrillViewModel viewModel;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_drill);

        viewModel = new ViewModelProvider(this).get(CreateDrillViewModel.class);
        context = this;
    }

    public void editCategories(View view) {
    }

    public void editSubCategories(View view) {
    }

    public void saveDrill(View view) {
        // TODO check to make sure user has selected at least one category/sub-category, popup to confirm if 0
        // TODO popup to have the user double check the spelling of the name, cannot change
        // TODO have a saving progress wheel
         viewModel.saveDrill((Drill) null, new CreateNewDrillCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> Toast.makeText(context, "Successfully saved", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onFailure(String msg) {
                runOnUiThread(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // TODO make sure we do input sanitation checking
    // TODO Last drilled today BUT set it as a new drill
    // TODO set the confidence spinner
    // TODO set the list items for the categories and subCategories
}