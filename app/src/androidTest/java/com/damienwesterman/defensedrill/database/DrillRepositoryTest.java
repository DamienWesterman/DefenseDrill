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

package com.damienwesterman.defensedrill.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DrillRepositoryTest {
    static Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    static DrillRepository repo = DrillRepository.getInstance(appContext);
    Drill drill1 = new Drill("drill one", 1, false, Drill.HIGH_CONFIDENCE, "notes one", 1,
            new ArrayList<>(), new ArrayList<>());
    Drill drill2 = new Drill("drill two", 2, false, Drill.MEDIUM_CONFIDENCE, "notes two", 2,
            new ArrayList<>(), new ArrayList<>());
    Drill drill3 = new Drill("drill three", 3, false, Drill.LOW_CONFIDENCE, "notes three", 3,
            new ArrayList<>(), new ArrayList<>());
    GroupEntity group1 = new GroupEntity("group one", "description one");
    GroupEntity group2 = new GroupEntity();

    @Before
    public void setUp() {
        // Assumed to work here, tested and working, but also has its own test cases
        repo.deleteDrills(repo.getAllDrills().toArray(new Drill[0]));
    }

    @Test
    public void test_getInstance() {
        assertEquals(repo, DrillRepository.getInstance(appContext));
    }

    @Test
    public void test_getAllDrills_AND_upsertDrills() {
        /*
            Can't just compare two lists, as the ID is auto generated at insert time. So instead,
            we have to insert, then getAll and check to make sure that all the unique names are
            present
         */

        repo.upsertDrills(drill1, drill2, drill3);

        List<Drill> returnedDrills = repo.getAllDrills();
        List<String> returnedDrillsNames = new ArrayList<>();

        assertEquals(3, returnedDrills.size());

        for (Drill drill : returnedDrills) {
            returnedDrillsNames.add(drill.getName());
        }

        assertTrue(returnedDrillsNames.contains(drill1.getName()));
        assertTrue(returnedDrillsNames.contains(drill2.getName()));
        assertTrue(returnedDrillsNames.contains(drill3.getName()));
    }

    @Test
    public void test_getAllDrills_emptyDB() {
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_getAllDrills_groupParameter_noMatchingDrills() {

    }
}
