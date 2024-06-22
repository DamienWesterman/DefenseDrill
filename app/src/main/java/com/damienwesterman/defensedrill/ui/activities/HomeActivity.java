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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.TitleDescCard;
import com.damienwesterman.defensedrill.utils.Constants;
import com.damienwesterman.defensedrill.utils.DrillGenerator;

import java.util.ArrayList;
import java.util.Random;

/**
 * TODO doc comments
 * TODO Make sure all the ui classes are clean
 * TODO Double check everywhere for null checks
 */
public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        TitleDescCard createDrillCard = findViewById(R.id.createDrillCard);
        createDrillCard.setOnLongClickListener(view -> {
            // Just for now, create some mock entries in the database
            new Thread(this::mockDatabaseEntries).start();
            Toast.makeText(this, "Added mocked database entries", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.generateDrillCard == cardId) {
            Intent intent = new Intent(this, CategorySelectActivity.class);
            startActivity(intent);
        } else if (R.id.createDrillCard == cardId) {
            Toast.makeText(this, "Unimplemented: Create Drill", Toast.LENGTH_SHORT).show();
        } else if (R.id.viewDrillsCard == cardId) {
            Intent intent = new Intent(this, ViewDrillsActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Unknown option", Toast.LENGTH_SHORT).show();
        }
    }

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
        kickBoxingCategory = repo.getCategory(kickBoxingCategory.getName());
        kravMagaCategory = repo.getCategory(kravMagaCategory.getName());
        jiuJitsuCategory = repo.getCategory(jiuJitsuCategory.getName());
        weaponsDefenseCategory = repo.getCategory(weaponsDefenseCategory.getName());

        // Set up subCategories
        SubCategoryEntity strikesSubCategory = new SubCategoryEntity("Strikes", "Using your upper body to strike your opponent.");
        SubCategoryEntity kicksSubCategory = new SubCategoryEntity("Kicks", "Using your legs to strike your opponent.");
        SubCategoryEntity escapesSubCategory = new SubCategoryEntity("Escapes", "Escaping an opponent's hold on you.");
        SubCategoryEntity gunDefenseSubCategory = new SubCategoryEntity("Gun Defense", "Dealing with an attacker who has a gun.");
        SubCategoryEntity knifeDefenseSubCategory = new SubCategoryEntity("Knife Defense", "Dealing with an attacker who has a knife");
        repo.insertSubCategories(strikesSubCategory, kicksSubCategory, escapesSubCategory, gunDefenseSubCategory, knifeDefenseSubCategory);
        strikesSubCategory = repo.getSubCategory(strikesSubCategory.getName());
        kicksSubCategory = repo.getSubCategory(kicksSubCategory.getName());
        escapesSubCategory = repo.getSubCategory(escapesSubCategory.getName());
        gunDefenseSubCategory = repo.getSubCategory(gunDefenseSubCategory.getName());
        knifeDefenseSubCategory = repo.getSubCategory(knifeDefenseSubCategory.getName());

        // Set up some drills
        Drill jab = new Drill("Jab", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        jab.addCategory(kickBoxingCategory);
        jab.addSubCategory(strikesSubCategory);
        Drill cross = new Drill("Cross", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        cross.addCategory(kickBoxingCategory);
        cross.addSubCategory(strikesSubCategory);
        Drill roundKick = new Drill("Round Kick", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        roundKick.addCategory(kickBoxingCategory);
        roundKick.addSubCategory(kicksSubCategory);
        Drill elbow = new Drill("Elbow", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        elbow.addCategory(kravMagaCategory);
        elbow.addSubCategory(strikesSubCategory);
        Drill knee = new Drill("Knee", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        knee.addCategory(kravMagaCategory);
        knee.addSubCategory(kicksSubCategory);
        Drill oneHandChokeEscape = new Drill("One Hand Standing Choke Escape", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        oneHandChokeEscape.addCategory(kravMagaCategory);
        oneHandChokeEscape.addSubCategory(escapesSubCategory);
        Drill shrimpEscape = new Drill("Shrimp Escape", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        shrimpEscape.addCategory(jiuJitsuCategory);
        shrimpEscape.addSubCategory(escapesSubCategory);
        Drill gunToHeadDrill = new Drill("Gun to the Front of the Head", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        gunToHeadDrill.addCategory(weaponsDefenseCategory);
        gunToHeadDrill.addSubCategory(gunDefenseSubCategory);
        Drill gunToHeadBack = new Drill("Gun to the Back", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        gunToHeadBack.addCategory(weaponsDefenseCategory);
        gunToHeadBack.addSubCategory(gunDefenseSubCategory);
        Drill slash = new Drill("Knife Slash", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        slash.addCategory(weaponsDefenseCategory);
        slash.addSubCategory(knifeDefenseSubCategory);
        Drill risingStab = new Drill("Rising Stab", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        risingStab.addCategory(weaponsDefenseCategory);
        risingStab.addSubCategory(knifeDefenseSubCategory);
        repo.insertDrills(jab, cross, roundKick, elbow, knee, oneHandChokeEscape, shrimpEscape, gunToHeadDrill, gunToHeadBack, slash, risingStab);
    }
}