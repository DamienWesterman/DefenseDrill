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

import java.util.ArrayList;

/**
 * TOD doc comments
 */
public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }
    // TODO: Use LinearLayout for this one as it is simpler, then for the Category and SubCategory lists use
    //       RecyclerView. Maybe have a service or something in order to report over all the activities
    //       what Category and SubCategory was picked, that way it could also do the database interactions
    //       off the main thread? Or maybe just pass each to the next activity as it is called and
    //       have the last one responsible for it? That's probably better so we can show some sort
    //       of loading screen then hide it and all that, especially during regeneration

    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.generateDrillCard == cardId) {
            Intent intent = new Intent(this, CategorySelectActivity.class);
            startActivity(intent);
        } else if (R.id.createDrillCard == cardId) {
            Toast.makeText(this, "Unimplemented: Create Drill", Toast.LENGTH_SHORT).show();
            // Just for now, create some mock entries in the database
            new Thread(this::mockDatabaseEntries).start();
        } else if (R.id.viewDrillsCard == cardId) {
            Toast.makeText(this, "Unimplemented: View Drills", Toast.LENGTH_SHORT).show();
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
        repo.insertCategories(kickBoxingCategory, kravMagaCategory, jiuJitsuCategory);
        kickBoxingCategory = repo.getCategory(kickBoxingCategory.getName());
        kravMagaCategory = repo.getCategory(kravMagaCategory.getName());
        jiuJitsuCategory = repo.getCategory(jiuJitsuCategory.getName());

        // Set up subCategories
        SubCategoryEntity strikesSubCategory = new SubCategoryEntity("Strikes", "Using your upper body to strike your opponent.");
        SubCategoryEntity kicksSubCategory = new SubCategoryEntity("Kicks", "Using your legs to strike your opponent.");
        SubCategoryEntity escapesSubCategory = new SubCategoryEntity("Escapes", "Escaping an opponent's hold on you.");
        repo.insertSubCategories(strikesSubCategory, kicksSubCategory, escapesSubCategory);
        strikesSubCategory = repo.getSubCategory(strikesSubCategory.getName());
        kicksSubCategory = repo.getSubCategory(kickBoxingCategory.getName());
        escapesSubCategory = repo.getSubCategory(escapesSubCategory.getName());

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
        repo.insertDrills(jab, cross, roundKick, elbow, knee, oneHandChokeEscape, shrimpEscape);
    }
}