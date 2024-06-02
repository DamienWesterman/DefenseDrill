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

package com.damienwesterman.defensedrill.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.database.Drill;
import com.damienwesterman.defensedrill.database.DrillRepository;
import com.damienwesterman.defensedrill.database.GroupEntity;
import com.damienwesterman.defensedrill.database.SubGroupEntity;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
    }
    // TODO: Use LinearLayout for this one as it is simpler, then for the Group and SubGroup lists use
    //       RecyclerView. Maybe have a service or something in order to report over all the activities
    //       what Group and SubGroup was picked, that way it could also do the database interactions
    //       off the main thread? Or maybe just pass each to the next activity as it is called and
    //       have the last one responsible for it? That's probably better so we can show some sort
    //       of loading screen then hide it and all that, especially during regeneration

    public void onCardClick(View view) {
        int cardId = view.getId();
        if (R.id.generateDrillCard == cardId) {
            Toast.makeText(this, "Unimplemented: Generate Drill", Toast.LENGTH_SHORT).show();
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
        repo.deleteGroups(repo.getAllGroups().toArray(new GroupEntity[0]));
        repo.deleteSubGroups(repo.getAllSubGroups().toArray(new SubGroupEntity[0]));

        // Set up groups
        GroupEntity kickBoxingGroup = new GroupEntity("Kickboxing", "Using fists and kicks.");
        GroupEntity kravMagaGroup = new GroupEntity("Krav Maga", "Real world escapes.");
        GroupEntity jiuJitsuGroup = new GroupEntity("Jiu-Jitsu", "Ground based grapples.");
        repo.insertGroups(kickBoxingGroup, kravMagaGroup, jiuJitsuGroup);
        kickBoxingGroup = repo.getGroup(kickBoxingGroup.getName());
        kravMagaGroup = repo.getGroup(kravMagaGroup.getName());
        jiuJitsuGroup = repo.getGroup(jiuJitsuGroup.getName());

        // Set up subGroups
        SubGroupEntity strikesSubGroup = new SubGroupEntity("Strikes", "Using your upper body to strike your opponent.");
        SubGroupEntity kicksSubGroup = new SubGroupEntity("Kicks", "Using your legs to strike your opponent.");
        SubGroupEntity escapesSubGroup = new SubGroupEntity("Escapes", "Escaping an opponent's hold on you.");
        repo.insertSubGroups(strikesSubGroup, kicksSubGroup, escapesSubGroup);
        strikesSubGroup = repo.getSubGroup(strikesSubGroup.getName());
        kicksSubGroup = repo.getSubGroup(kickBoxingGroup.getName());
        escapesSubGroup = repo.getSubGroup(escapesSubGroup.getName());

        // Set up some drills
        Drill jab = new Drill("Jab", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        jab.addGroup(kickBoxingGroup);
        jab.addSubGroup(strikesSubGroup);
        Drill cross = new Drill("Cross", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        cross.addGroup(kickBoxingGroup);
        cross.addSubGroup(strikesSubGroup);
        Drill roundKick = new Drill("Round Kick", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        roundKick.addGroup(kickBoxingGroup);
        roundKick.addSubGroup(kicksSubGroup);
        Drill elbow = new Drill("Elbow", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        elbow.addGroup(kravMagaGroup);
        elbow.addSubGroup(strikesSubGroup);
        Drill knee = new Drill("Knee", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        knee.addGroup(kravMagaGroup);
        knee.addSubGroup(kicksSubGroup);
        Drill oneHandChokeEscape = new Drill("One Hand Standing Choke Escape", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        oneHandChokeEscape.addGroup(kravMagaGroup);
        oneHandChokeEscape.addSubGroup(escapesSubGroup);
        Drill shrimpEscape = new Drill("Shrimp Escape", System.currentTimeMillis(), true, Drill.HIGH_CONFIDENCE, "",
                -1, new ArrayList<>(), new ArrayList<>());
        shrimpEscape.addGroup(jiuJitsuGroup);
        shrimpEscape.addSubGroup(escapesSubGroup);
        repo.insertDrills(jab, cross, roundKick, elbow, knee, oneHandChokeEscape, shrimpEscape);
    }
}