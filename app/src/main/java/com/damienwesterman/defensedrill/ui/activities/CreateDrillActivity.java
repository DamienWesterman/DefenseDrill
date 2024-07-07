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

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.damienwesterman.defensedrill.R;

public class CreateDrillActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_drill);
    }

    public void editCategories(View view) {
    }

    public void editSubCategories(View view) {
    }

    // TODO make sure we do input sanitation checking
    // TODO popup to have the user double check the spelling, cannot change
    // TODO Last drilled today BUT set it as a new drill
    // TODO set the confidence spinner
    // TODO set the list items for the categories and subCategories
}