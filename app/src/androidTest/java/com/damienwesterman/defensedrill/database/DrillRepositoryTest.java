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
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class DrillRepositoryTest {
    Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    DrillRepository repo = DrillRepository.getInstance(appContext);
    Drill drill1;
    Drill drill2;
    Drill drill3;
    GroupEntity group1;
    GroupEntity group2;
    GroupEntity group3;
    SubGroupEntity subGroup1;
    SubGroupEntity subGroup2;
    SubGroupEntity subGroup3;

    @Before
    public void setUp() {
        drill1 = new Drill("drill one", 1, false, Drill.HIGH_CONFIDENCE, "notes one", 1,
                new ArrayList<>(), new ArrayList<>());
        drill2 = new Drill("drill two", 2, false, Drill.MEDIUM_CONFIDENCE, "notes two", 2,
                new ArrayList<>(), new ArrayList<>());
        drill3 = new Drill("drill three", 3, false, Drill.LOW_CONFIDENCE, "notes three", 3,
                new ArrayList<>(), new ArrayList<>());
        group1 = new GroupEntity("group one", "description one");
        group2 = new GroupEntity("group two", "description two");
        group3 = new GroupEntity("group three", "description three");
        subGroup1 = new SubGroupEntity("sub group one", "description one");
        subGroup2 = new SubGroupEntity("sub group two", "description two");
        subGroup3 = new SubGroupEntity("sub group three", "description three");

        // Assumed to work here, tested and working, but also has its own test cases
        repo.deleteDrills(repo.getAllDrills().toArray(new Drill[0]));
        repo.deleteGroups(repo.getAllGroups().toArray(new GroupEntity[0]));
        repo.deleteSubGroups(repo.getAllSubGroups().toArray(new SubGroupEntity[0]));
    }

    @Test
    public void test_getInstance() {
        assertEquals(repo, DrillRepository.getInstance(appContext));
    }

    @Test
    public void test_getAllDrills_emptyDB() {
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_getAllDrills_AND_insertDrills_insertMultipleDrills() {
        /*
            Can't just compare two lists, as the ID is auto generated at insert time. So instead,
            we have to insert, then getAll and check to make sure that all the unique names are
            present
         */

        repo.insertDrills(drill1, drill2, drill3);

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
    public void test_getAllDrills_groupParameter_noMatchingDrills() {
        repo.insertGroups(group1, group2, group3);
        group1 = repo.getOneGroup(group1.getName());
        group2 = repo.getOneGroup(group2.getName());
        group3 = repo.getOneGroup(group3.getName());

        drill1.getGroups().add(group1);
        drill2.getGroups().add(group2);
        repo.insertDrills(drill1, drill2);

        assertEquals(0, repo.getAllDrills(group3).size());
    }

    @Test
    public void test_getAllDrills_groupParameter_oneMatchingDrills() {
        repo.insertGroups(group1, group2, group3);
        group1 = repo.getOneGroup(group1.getName());
        group2 = repo.getOneGroup(group2.getName());
        group3 = repo.getOneGroup(group3.getName());

        drill1.getGroups().add(group1);
        drill2.getGroups().add(group2);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(1, repo.getAllDrills(group1).size());
    }

    @Test
    public void test_getAllDrills_groupParameter_multipleMatchingDrills() {
        repo.insertGroups(group1, group2, group3);
        group1 = repo.getOneGroup(group1.getName());
        group2 = repo.getOneGroup(group2.getName());
        group3 = repo.getOneGroup(group3.getName());

        drill1.getGroups().add(group1);
        drill2.getGroups().add(group1);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(2, repo.getAllDrills(group1).size());
    }

    @Test
    public void test_getAllDrills_groupParameter_nonExistentGroup() {
        repo.insertGroups(group1, group2);
        group1 = repo.getOneGroup(group1.getName());
        group2 = repo.getOneGroup(group2.getName());

        drill1.getGroups().add(group1);
        drill2.getGroups().add(group2);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(group3).size());
    }

    // TODO SubGroupParameter
    // TODO GroupANDSubGroupParameters

    @Test
    public void test_insertDrills_insertOneDrill() {
        repo.insertDrills(drill1);

        List<Drill> returnedDrills = repo.getAllDrills();
        List<String> returnedDrillsNames = new ArrayList<>();

        assertEquals(1, returnedDrills.size());

        for (Drill drill : returnedDrills) {
            returnedDrillsNames.add(drill.getName());
        }

        assertTrue(returnedDrillsNames.contains(drill1.getName()));
    }

    @Test
    public void test_insertDrills_insertWithGroups() {
        repo.insertGroups(group1, group2);
        group1 = repo.getOneGroup(group1.getName());
        group2 = repo.getOneGroup(group2.getName());

        drill1.getGroups().add(group1);
        drill1.getGroups().add(group2);

        repo.insertDrills(drill1);

        assertEquals(2, repo.getDrill(drill1.getName()).getGroups().size());
    }

    @Test
    public void test_insertDrills_insertWithSubGroups() {
        repo.insertSubGroups(subGroup1, subGroup2);
        subGroup1 = repo.getOneSubGroup(subGroup1.getName());
        subGroup2 = repo.getOneSubGroup(subGroup2.getName());

        drill1.getSubGroups().add(subGroup1);
        drill1.getSubGroups().add(subGroup2);

        repo.insertDrills(drill1);

        assertEquals(2, repo.getDrill(drill1.getName()).getSubGroups().size());
    }

    @Test
    public void test_insertDrills_throwsWhenViolateUniqueKey() {
        String sameName = "same name";
        drill1.setName(sameName);
        drill2.setName(sameName);
        assertThrows(SQLiteConstraintException.class, () -> repo.insertDrills(drill1, drill2));
    }

    @Test
    public void test_insertDrills_throwsWhenGivenNullName() {
        drill1.setName(null);
        assertThrows(SQLiteConstraintException.class, () -> repo.insertDrills(drill1));
    }

    @Test
    public void test_insertDrills_throwsWhenGivenNonExistentGroup() {
        repo.insertGroups(group1, group2);
        group1 = repo.getOneGroup(group1.getName());
        group2 = repo.getOneGroup(group2.getName());

        drill1.getGroups().add(group1);
        drill1.getGroups().add(group2);
        drill1.getGroups().add(group3);

        assertThrows(SQLiteConstraintException.class, () -> repo.insertDrills(drill1));
    }

    @Test
    public void test_insertDrills_throwsWhenGivenNonExistentSubGroup() {
        repo.insertSubGroups(subGroup1, subGroup2);
        subGroup1 = repo.getOneSubGroup(subGroup1.getName());
        subGroup2 = repo.getOneSubGroup(subGroup2.getName());

        drill1.getSubGroups().add(subGroup1);
        drill1.getSubGroups().add(subGroup2);
        drill1.getSubGroups().add(subGroup3);

        assertThrows(SQLiteConstraintException.class, () -> repo.insertDrills(drill1));
    }

    @Test
    public void test_updateDrills_updateOneDrill() {
        repo.insertDrills(drill1);
        drill1 = repo.getDrill(drill1.getName());
        String newName = "new name";
        drill1.setName(newName);
        repo.updateDrills(drill1);

        assertEquals(0, newName.compareTo(repo.getDrill(drill1.getId()).getName()));
    }

    @Test
    public void test_updateDrills_updateMultipleDrills() {
        repo.insertDrills(drill1, drill2);
        drill1 = repo.getDrill(drill1.getName());
        drill2 = repo.getDrill(drill2.getName());
        String newName1 = "new name";
        String newName2 = "new name two";
        drill1.setName(newName1);
        drill2.setName(newName2);
        repo.updateDrills(drill1, drill2);

        assertEquals(0, newName1.compareTo(repo.getDrill(drill1.getId()).getName()));
        assertEquals(0, newName2.compareTo(repo.getDrill(drill2.getId()).getName()));
    }

    @Test
    public void test_updateDrills_addGroups() {
        repo.insertGroups(group1, group2);
        group1 = repo.getOneGroup(group1.getName());
        group2 = repo.getOneGroup(group2.getName());

        drill1.getGroups().add(group1);

        repo.insertDrills(drill1);

        Drill insertedDrill = repo.getDrill(drill1.getName());

        assertEquals(1, insertedDrill.getGroups().size());

        insertedDrill.getGroups().add(group2);
        repo.updateDrills(insertedDrill);

        assertEquals(2, repo.getDrill(insertedDrill.getId()).getGroups().size());
    }

    @Test
    public void test_updateDrills_removeGroups() {
        repo.insertGroups(group1, group2);
        group1 = repo.getOneGroup(group1.getName());
        group2 = repo.getOneGroup(group2.getName());

        drill1.getGroups().add(group1);
        drill1.getGroups().add(group2);

        repo.insertDrills(drill1);

        Drill insertedDrill = repo.getDrill(drill1.getName());

        assertEquals(2, insertedDrill.getGroups().size());

        insertedDrill.removeGroup(group2);
        repo.updateDrills(insertedDrill);

        assertEquals(1, repo.getDrill(insertedDrill.getId()).getGroups().size());
    }

    @Test
    public void test_updateDrills_addSubGroups() {
        repo.insertSubGroups(subGroup1, subGroup2);
        subGroup1 = repo.getOneSubGroup(subGroup1.getName());
        subGroup2 = repo.getOneSubGroup(subGroup2.getName());

        drill1.getSubGroups().add(subGroup1);

        repo.insertDrills(drill1);
        Drill insertedDrill = repo.getDrill(drill1.getName());

        assertEquals(1, insertedDrill.getSubGroups().size());

        insertedDrill.getSubGroups().add(subGroup2);
        repo.updateDrills(insertedDrill);

        assertEquals(2, repo.getDrill(insertedDrill.getId()).getSubGroups().size());
    }

    @Test
    public void test_updateDrills_removeSubGroups() {
        repo.insertSubGroups(subGroup1, subGroup2);
        subGroup1 = repo.getOneSubGroup(subGroup1.getName());
        subGroup2 = repo.getOneSubGroup(subGroup2.getName());

        drill1.getSubGroups().add(subGroup1);
        drill1.getSubGroups().add(subGroup2);

        repo.insertDrills(drill1);
        Drill insertedDrill = repo.getDrill(drill1.getName());

        assertEquals(2, insertedDrill.getSubGroups().size());

        insertedDrill.removeSubGroup(subGroup2);
        repo.updateDrills(insertedDrill);

        assertEquals(1, repo.getDrill(insertedDrill.getId()).getSubGroups().size());
    }

    @Test
    public void test_updateDrills_throwsWhenViolateUniqueKey() {
        repo.insertDrills(drill1, drill2);
        drill1 = repo.getDrill(drill1.getName());
        drill2 = repo.getDrill(drill2.getName());
        String sameName = "same name";
        drill1.setName(sameName);
        drill2.setName(sameName);
        assertThrows(SQLiteConstraintException.class, () -> repo.updateDrills(drill1, drill2));
    }

    @Test
    public void test_updateDrills_throwsWhenGivenNullName() {
        repo.insertDrills(drill1);
        drill1 = repo.getDrill(drill1.getName());
        drill1.setName(null);
        assertThrows(SQLiteConstraintException.class, () -> repo.updateDrills(drill1));
    }

    @Test
    public void test_updateDrills_throwsWhenGivenNonExistentGroup() {
        repo.insertGroups(group1, group2);
        group1 = repo.getOneGroup(group1.getName());
        group2 = repo.getOneGroup(group2.getName());

        drill1.getGroups().add(group1);
        drill1.getGroups().add(group2);

        repo.insertDrills(drill1);

        Drill insertedDrill = repo.getDrill(drill1.getName());
        insertedDrill.getGroups().add(group3);

        assertThrows(SQLiteConstraintException.class, () -> repo.updateDrills(insertedDrill));
    }

    @Test
    public void test_updateDrills_throwsWhenGivenNonExistentSubGroup() {
        repo.insertSubGroups(subGroup1, subGroup2);
        subGroup1 = repo.getOneSubGroup(subGroup1.getName());
        subGroup2 = repo.getOneSubGroup(subGroup2.getName());

        drill1.getSubGroups().add(subGroup1);
        drill1.getSubGroups().add(subGroup2);

        repo.insertDrills(drill1);

        Drill insertedDrill = repo.getDrill(drill1.getName());
        insertedDrill.getSubGroups().add(subGroup3);

        assertThrows(SQLiteConstraintException.class, () -> repo.updateDrills(insertedDrill));
    }

    @Test
    public void test_updateDrills_updateNonExistentDrillDoesNothing() {
        repo.updateDrills(drill1);
        assertEquals(0, repo.getAllDrills().size());
    }

    // TODO: IMPLEMENT GROUP TESTS
        // Get name - yes name exists - no name
        // upsert
}
