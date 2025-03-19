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
/*
 * Copyright 2024 Damien Westerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.damienwesterman.defensedrill.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.CommonPopups;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;
import com.damienwesterman.defensedrill.ui.utils.TitleDescCard;
import com.damienwesterman.defensedrill.ui.utils.UiUtils;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.ArrayList;

/**
 * Home screen activity and entry point for the application. Displays the different general
 * functionalities of the app. CRUD operations in the database, Drill generation, and feedback.
 * <br><br>
 * INTENTS: None expected.
 */
public class HomeActivity extends AppCompatActivity {
    private LinearLayout rootView;
    private Context context;
    private Activity activity;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rootView = findViewById(R.id.activityHome);
        context = this;
        activity = this;
    }

    // =============================================================================================
    // OnClickListener Methods
    // =============================================================================================
    public void onCardClick(View view) {
        // Add cards to delete things
        int cardId = view.getId();
        if (R.id.generateDrillCard == cardId) {
            Intent intent = new Intent(this, CategorySelectActivity.class);
            startActivity(intent);
        } else if (R.id.customizeDatabaseCard == cardId) {
            Intent intent = new Intent(this, CustomizeDatabaseActivity.class);
            startActivity(intent);
        } else if (R.id.networkSetupCard == cardId) {
            CommonPopups.displayServerSelectPopup(this, this,
                    new OperationCompleteCallback() {
                @Override
                public void onSuccess() {
                    // On Server Setup Success
                    UiUtils.displayDismissibleSnackbar(rootView, "Saved Server URL");
                    CommonPopups.displayLoginPopup(context, activity, new OperationCompleteCallback() {
                        @Override
                        public void onSuccess() {
                            // On Login Success
                            UiUtils.displayDismissibleSnackbar(rootView, "Login Successful!");
                        }

                        @Override
                        public void onFailure(String error) {
                            // On Login Failure
                            UiUtils.displayDismissibleSnackbar(rootView, error);
                        }
                    });
                }

                @Override
                public void onFailure(String error) {
                    // On Server Setup Failure
                    UiUtils.displayDismissibleSnackbar(rootView, error);
                }
            });
        } else if (R.id.feedbackCard == cardId) {
            // TODO implement feedback
            UiUtils.displayDismissibleSnackbar(rootView, "Feedback unimplemented");
        } else {
            UiUtils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }
}