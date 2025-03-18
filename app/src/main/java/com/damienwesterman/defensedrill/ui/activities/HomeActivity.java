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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.TitleDescCard;
import com.damienwesterman.defensedrill.ui.utils.Utils;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.ArrayList;

/**
 * Home screen activity and entry point for the application. Displays the different general
 * functionalities of the app. CRUD operations in the database, Drill generation, and feedback.
 * <br><br>
 * INTENTS: None expected.
 */
public class HomeActivity extends AppCompatActivity {
    // TODO: FIXME: START HERE: Prompt the user for the server endpoint and store it
    private View rootView;

    // =============================================================================================
    // Activity Methods
    // =============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        rootView = findViewById(R.id.activityHome);

        TitleDescCard viewDrillsCard = findViewById(R.id.viewDrillsCard);
        viewDrillsCard.setOnLongClickListener(view -> {
            // Just for now, create some mock entries in the database
            mockDatabaseConfirmationPopup();
            return true;
        });
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
        } else if (R.id.viewDrillsCard == cardId) {
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
        } else if (R.id.feedbackCard == cardId) {
            // TODO implement feedback
            Utils.displayDismissibleSnackbar(rootView, "Feedback unimplemented");
        } else {
            Utils.displayDismissibleSnackbar(rootView, "Unknown option");
        }
    }


    // =============================================================================================
    // Popup / AlertDialog Methods
    // =============================================================================================
    private void mockDatabaseConfirmationPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Insert Mock Database?");
        builder.setIcon(R.drawable.warning_icon);
        builder.setMessage("This will delete ALL drills/ categories/ sub-categories " +
                "and replace them with some demo ones. Are you sure you want to do this?");
        builder.setPositiveButton("Do It.", (dialog, position) -> {
            new Thread(this::mockDatabaseEntries).start();
            Utils.displayDismissibleSnackbar(rootView, "Added mocked database entries");
        });
        builder.setNegativeButton("Never-mind", null);
        builder.create().show();
    }


    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    // TODO Take out once the server is ready
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private void mockDatabaseEntries() {
        DrillRepository repo = DrillRepository.getInstance(this.getApplicationContext());

        // Clear database
        repo.deleteDrills(repo.getAllDrills().toArray(new Drill[0]));
        repo.deleteCategories(repo.getAllCategories().toArray(new CategoryEntity[0]));
        repo.deleteSubCategories(repo.getAllSubCategories().toArray(new SubCategoryEntity[0]));

        // Set up categories
        CategoryEntity kickBoxingCategory = new CategoryEntity("Kickboxing", "Using fists and kicks.");
        CategoryEntity kravMagaCategory = new CategoryEntity("Krav Maga", "Real world escapes.");
        CategoryEntity jiuJitsuCategory = new CategoryEntity("Jiu-Jitsu", "Ground based grapples.");
        CategoryEntity weaponsDefenseCategory = new CategoryEntity("Weapons Defense", "How to deal with an attacker with a weapon.");
        repo.insertCategories(kickBoxingCategory, kravMagaCategory, jiuJitsuCategory, weaponsDefenseCategory);
        kickBoxingCategory = repo.getCategory(kickBoxingCategory.getName()).get();
        kravMagaCategory = repo.getCategory(kravMagaCategory.getName()).get();
        jiuJitsuCategory = repo.getCategory(jiuJitsuCategory.getName()).get();
        weaponsDefenseCategory = repo.getCategory(weaponsDefenseCategory.getName()).get();

        // Set up subCategories
        SubCategoryEntity strikesSubCategory = new SubCategoryEntity("Strikes", "Using your upper body to strike your opponent.");
        SubCategoryEntity kicksSubCategory = new SubCategoryEntity("Kicks", "Using your legs to strike your opponent.");
        SubCategoryEntity escapesSubCategory = new SubCategoryEntity("Escapes", "Escaping an opponent's hold on you.");
        SubCategoryEntity gunDefenseSubCategory = new SubCategoryEntity("Gun Defense", "Dealing with an attacker who has a gun.");
        SubCategoryEntity knifeDefenseSubCategory = new SubCategoryEntity("Knife Defense", "Dealing with an attacker who has a knife");
        repo.insertSubCategories(strikesSubCategory, kicksSubCategory, escapesSubCategory, gunDefenseSubCategory, knifeDefenseSubCategory);
        strikesSubCategory = repo.getSubCategory(strikesSubCategory.getName()).get();
        kicksSubCategory = repo.getSubCategory(kicksSubCategory.getName()).get();
        escapesSubCategory = repo.getSubCategory(escapesSubCategory.getName()).get();
        gunDefenseSubCategory = repo.getSubCategory(gunDefenseSubCategory.getName()).get();
        knifeDefenseSubCategory = repo.getSubCategory(knifeDefenseSubCategory.getName()).get();

        // Set up some drills
        Drill jab = new Drill("Jab", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        jab.addCategory(kickBoxingCategory);
        jab.addSubCategory(strikesSubCategory);
        Drill cross = new Drill("Cross", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        cross.addCategory(kickBoxingCategory);
        cross.addSubCategory(strikesSubCategory);
        Drill roundKick = new Drill("Round Kick", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        roundKick.addCategory(kickBoxingCategory);
        roundKick.addSubCategory(kicksSubCategory);
        Drill elbow = new Drill("Elbow", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        elbow.addCategory(kravMagaCategory);
        elbow.addSubCategory(strikesSubCategory);
        Drill knee = new Drill("Knee", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        knee.addCategory(kravMagaCategory);
        knee.addSubCategory(kicksSubCategory);
        Drill oneHandChokeEscape = new Drill("One Hand Standing Choke Escape", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        oneHandChokeEscape.addCategory(kravMagaCategory);
        oneHandChokeEscape.addSubCategory(escapesSubCategory);
        Drill shrimpEscape = new Drill("Shrimp Escape", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        shrimpEscape.addCategory(jiuJitsuCategory);
        shrimpEscape.addSubCategory(escapesSubCategory);
        Drill gunToHeadDrill = new Drill("Gun to the Front of the Head", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        gunToHeadDrill.addCategory(weaponsDefenseCategory);
        gunToHeadDrill.addSubCategory(gunDefenseSubCategory);
        Drill gunToHeadBack = new Drill("Gun to the Back", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        gunToHeadBack.addCategory(weaponsDefenseCategory);
        gunToHeadBack.addSubCategory(gunDefenseSubCategory);
        Drill slash = new Drill("Knife Slash", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        slash.addCategory(weaponsDefenseCategory);
        slash.addSubCategory(knifeDefenseSubCategory);
        Drill risingStab = new Drill("Rising Stab", System.currentTimeMillis(), false, Drill.LOW_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        risingStab.addCategory(weaponsDefenseCategory);
        risingStab.addSubCategory(knifeDefenseSubCategory);
        repo.insertDrills(jab, cross, roundKick, elbow, knee, oneHandChokeEscape, shrimpEscape, gunToHeadDrill, gunToHeadBack, slash, risingStab);
    }
}