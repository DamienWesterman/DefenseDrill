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

package com.damienwesterman.defensedrill.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.damienwesterman.defensedrill.database.Drill;
import com.damienwesterman.defensedrill.database.DrillRepository;
import com.damienwesterman.defensedrill.database.GroupEntity;
import com.damienwesterman.defensedrill.database.SubGroupEntity;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class DrillGeneratorTest {
    private static final int NUM_TESTS = 10;

    static Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    static DrillRepository repo = DrillRepository.getInstance(appContext);
    DrillGenerator generator;

    @Before
    public void setup() {
        generator = new DrillGenerator(repo);

        // Assumed to work here, tested and working, but also has its own test cases
        repo.deleteDrills(repo.getAllDrills().toArray(new Drill[0]));
        repo.deleteGroups(repo.getAllGroups().toArray(new GroupEntity[0]));
        repo.deleteSubGroups(repo.getAllSubGroups().toArray(new SubGroupEntity[0]));
    }

    // Due to the random nature of the selection algorithm, each test will likely be tested
    // multiple times to ensure consistently correct results

    @Test
    public void test_generateDrill_noParams_returnsNull_noDrillsInDB() {
        setDrills();
        Drill returnedDrill = generator.generateDrill();
        assertNull(returnedDrill);
    }

    @Test
    public void test_generateDrill_noParams_returnsAnyDrill_noNewDrills_oneDrillInDB() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        setDrills(drill1);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill();
            assertNotNull(returnedDrill);
            assertEquals(0, returnedDrill.getName().compareTo(drill1.getName()));
        }
    }

    @Test
    public void test_generateDrill_noParams_returnsAnyDrill_noNewDrills_multipleDrillsInDB() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill();
            assertNotNull(returnedDrill);
            assertNotNull(repo.getDrill(returnedDrill.getName())); // Check it returned a valid drill
        }
    }

    @Test
    public void test_generateDrill_noParams_returnsNewDrill_oneNewDrill_multipleOtherDrillsInDB() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(true);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill();
            assertNotNull(returnedDrill); // Check it returned a valid drill
            assertTrue(returnedDrill.isNewDrill());
        }
    }

    @Test
    public void test_generateDrill_noParams_returnsAnyNewDrill_MultipleNewDrills_multipleOtherDrillsInDB() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(true);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(true);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        Drill drill4 = new Drill();
        drill4.setName("drill4");
        drill4.setNewDrill(false);
        setDrills(drill1, drill2, drill3, drill4);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill();
            assertNotNull(returnedDrill);
            assertTrue(0 == returnedDrill.getName().compareTo(drill1.getName()) ||
                    0 == returnedDrill.getName().compareTo(drill2.getName()));
            assertTrue(returnedDrill.isNewDrill());
        }
    }

    @Test
    public void test_generateDrill_groupParam_returnsNull_noDrillsInDB() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        Drill returnedDrill = generator.generateDrill(groups.get(0));
        assertNull(returnedDrill);
    }

    @Test
    public void test_generateDrill_groupParam_returnsNull_noNewDrills_noMatchingDrillsInDB() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0));
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_groupParam_returnsAnyDrill_noNewDrills_oneMatchingDrillInDB_oneGroupInDB() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired group
            assertTrue(returnedDrill.getGroups().contains(groups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_groupParam_returnsAnyDrill_noNewDrills_multipleMatchingDrillsInDB_oneGroupInDB() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill1.addGroup(groups.get(0));
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired group
            assertTrue(returnedDrill.getGroups().contains(groups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_groupParam_returnsAnyDrill_noNewDrills_oneMatchingDrillInDB_multipleGroupsInDB() {
        List<GroupEntity> groups = setGroups(
                new GroupEntity("group1", "group1"),
                new GroupEntity("group2", "group2"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill1.addGroup(groups.get(1));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill1.addGroup(groups.get(1));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired group
            assertTrue(returnedDrill.getGroups().contains(groups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_groupParam_returnsAnyDrill_noNewDrills_multipleMatchingDrillsInDB_multipleGroupsInDB() {
        List<GroupEntity> groups = setGroups(
                new GroupEntity("group1", "group1"),
                new GroupEntity("group2", "group2"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill1.addGroup(groups.get(1));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired group
            assertTrue(returnedDrill.getGroups().contains(groups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_groupParam_returnsAnyNewDrill_yesNewDrills_yesMatchingDrillsInDB() {
        List<GroupEntity> groups = setGroups(
                new GroupEntity("group1", "group1"),
                new GroupEntity("group2", "group2"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(true);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(true);
        drill1.addGroup(groups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired group
            assertTrue(returnedDrill.getGroups().contains(groups.get(0)));
            assertTrue(returnedDrill.isNewDrill());
        }
    }

    @Test
    public void test_generateDrill_groupParam_returnsNull_nonExistentGroup() {

        List<GroupEntity> groups = setGroups(
                new GroupEntity("group1", "group1"),
                new GroupEntity("group2", "group2"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill1.addGroup(groups.get(1));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill1.addGroup(groups.get(1));
        setDrills(drill1, drill2, drill3);

        assertNull(generator.generateDrill(new GroupEntity("group3", "group3")));
    }

    @Test
    public void test_generateDrill_groupParam_returnsNull_nullArgument() {
        assertNull(generator.generateDrill((GroupEntity) null));
    }

    @Test
    public void test_generateDrill_subGroupParam_returnsNull_noDrillsInDB() {
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));
        Drill returnedDrill = generator.generateDrill(subGroups.get(0));
        assertNull(returnedDrill);
    }

    @Test
    public void test_generateDrill_subGroupParam_returnsNull_noNewDrills_noMatchingDrillsInDB() {
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(subGroups.get(0));
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_subGroupParam_returnsAnyDrill_noNewDrills_oneMatchingDrillInDB_oneSubGroupInDB() {
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(subGroups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired subGroup
            assertTrue(returnedDrill.getSubGroups().contains(subGroups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_subGroupParam_returnsAnyDrill_noNewDrills_multipleMatchingDrillsInDB_oneSubGroupInDB() {
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill1.addSubGroup(subGroups.get(0));
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(subGroups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired subGroup
            assertTrue(returnedDrill.getSubGroups().contains(subGroups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_subGroupParam_returnsAnyDrill_noNewDrills_oneMatchingDrillInDB_multipleSubGroupsInDB() {
        List<SubGroupEntity> subGroups = setSubGroups(
                new SubGroupEntity("subGroup1", "subGroup1"),
                new SubGroupEntity("subGroup2", "subGroup2"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(1));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(1));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(subGroups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired subGroup
            assertTrue(returnedDrill.getSubGroups().contains(subGroups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_subGroupParam_returnsAnyDrill_noNewDrills_multipleMatchingDrillsInDB_multipleSubGroupsInDB() {
        List<SubGroupEntity> subGroups = setSubGroups(
                new SubGroupEntity("subGroup1", "subGroup1"),
                new SubGroupEntity("subGroup2", "subGroup2"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(1));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(subGroups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired subGroup
            assertTrue(returnedDrill.getSubGroups().contains(subGroups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_subGroupParam_returnsAnyNewDrill_yesNewDrills_yesMatchingDrillsInDB() {
        List<SubGroupEntity> subGroups = setSubGroups(
                new SubGroupEntity("subGroup1", "subGroup1"),
                new SubGroupEntity("subGroup2", "subGroup2"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(true);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(true);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(subGroups.get(0));
            assertNotNull(returnedDrill);
            // Ensure this drill is part of the desired subGroup
            assertTrue(returnedDrill.getSubGroups().contains(subGroups.get(0)));
            assertTrue(returnedDrill.isNewDrill());
        }
    }

    @Test
    public void test_generateDrill_subGroupParam_returnsNull_nonExistentSubGroup() {

        List<SubGroupEntity> subGroups = setSubGroups(
                new SubGroupEntity("subGroup1", "subGroup1"),
                new SubGroupEntity("subGroup2", "subGroup2"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(1));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(1));
        setDrills(drill1, drill2, drill3);

        assertNull(generator.generateDrill(new SubGroupEntity("subGroup3", "subGroup3")));
    }

    @Test
    public void test_generateDrill_subGroupParam_returnsNull_nullArgument() {
        assertNull(generator.generateDrill((SubGroupEntity) null));
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_noDrillsInDB() {
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        Drill returnedDrill = generator.generateDrill(groups.get(0), subGroups.get(0));
        assertNull(returnedDrill);
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_noNewDrills_noMatchingDrillInDB() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0), subGroups.get(0));
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_noNewDrills_noMatchingDrillsForGroupInDB() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0), subGroups.get(0));
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_noNewDrills_noMatchingDrillsForSubGroupInDB() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill2.addGroup(groups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0), subGroups.get(0));
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_noNewDrills_oneMatchingDrill() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0), subGroups.get(0));
            assertNotNull(returnedDrill);
            assertTrue(returnedDrill.getGroups().contains(groups.get(0)));
            assertTrue(returnedDrill.getSubGroups().contains(subGroups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_noNewDrills_multipleMatchingDrills() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill3.addGroup(groups.get(0));
        drill3.addSubGroup(subGroups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0), subGroups.get(0));
            assertNotNull(returnedDrill);
            assertTrue(returnedDrill.getGroups().contains(groups.get(0)));
            assertTrue(returnedDrill.getSubGroups().contains(subGroups.get(0)));
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_yesNewDrills_yesMatchingDrills() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(true);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(true);
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill3.addGroup(groups.get(0));
        drill3.addSubGroup(subGroups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0), subGroups.get(0));
            assertNotNull(returnedDrill);
            assertTrue(returnedDrill.getGroups().contains(groups.get(0)));
            assertTrue(returnedDrill.getSubGroups().contains(subGroups.get(0)));
            assertTrue(returnedDrill.isNewDrill());
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_nonExistentGroup() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill3.addGroup(groups.get(0));
        drill3.addSubGroup(subGroups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(
                    new GroupEntity("group2", "group2"), subGroups.get(0));
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_nonExistentSubGroup() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill3.addGroup(groups.get(0));
        drill3.addSubGroup(subGroups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(
                    groups.get(0), new SubGroupEntity("subGroup2", "subGroup2"));
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_nonExistentGroupAndSubGroup() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill3.addGroup(groups.get(0));
        drill3.addSubGroup(subGroups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(
                    new GroupEntity("group2", "group2"),
                    new SubGroupEntity("subGroup2", "subGroup2"));
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_nullGroup() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill3.addGroup(groups.get(0));
        drill3.addSubGroup(subGroups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(null, subGroups.get(0));
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_nullSubGroup() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill3.addGroup(groups.get(0));
        drill3.addSubGroup(subGroups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(groups.get(0), null);
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_groupANDSubGroupParams_returnsNull_nullGroupAndSubGroup() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        drill3.addGroup(groups.get(0));
        drill3.addSubGroup(subGroups.get(0));
        setDrills(drill1, drill2, drill3);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill(null, null);
            assertNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_performanceTestWithLargeDatabase_executesInLessThan500Milliseconds() {
        List<Drill> aLotOfDrills = new ArrayList<>();

        for (int i = 0; i < 5000; i++) {
            Drill drill = new Drill();
            drill.setName("drill" + i);
            drill.setNewDrill(false);
            drill.setConfidence(new Random().nextInt(5));
            // Set last Drilled to 'i' days ago
            drill.setLastDrilled(System.currentTimeMillis() - (24L * 60 * 60 * 1000 * i));
            aLotOfDrills.add(drill);
        }
        setDrills(aLotOfDrills.toArray(new Drill[0]));

        long startTime = System.currentTimeMillis();
        Drill returnedDrill = generator.generateDrill();
        long elapsedTime = System.currentTimeMillis() - startTime;
        assertNotNull(returnedDrill); // What exactly is returns does not matter
        assertTrue("Method took too long: " + elapsedTime + " milliseconds", elapsedTime < 500);
    }

    @Test
    public void test_generateDrill_ensureEachDrillIsSelectedAtLeastOnce() {
        List<Drill> initialDrills = new ArrayList<>();
        final int NUM_DRILLS = 100;

        for (int i = 0; i < NUM_DRILLS; i++) {
            Drill drill = new Drill();
            drill.setName("drill" + i);
            drill.setNewDrill(false);
            drill.setConfidence(Drill.HIGH_CONFIDENCE);
            // Set last Drilled to 1 day ago for even possibilities
            drill.setLastDrilled(System.currentTimeMillis() - (24L * 60 * 60 * 1000));
            initialDrills.add(drill);
        }
        setDrills(initialDrills.toArray(new Drill[0]));

        Set<String> generatedDrillNames = new HashSet<>();

        for (int i = 0; i < NUM_DRILLS * 10; i++) {
            Drill returnedDrill = generator.generateDrill();
            assertNotNull(returnedDrill);
            generatedDrillNames.add(returnedDrill.getName());
        }

        assertEquals(NUM_DRILLS, generatedDrillNames.size());
    }

    @Test
    public void test_regenerateDrill_prevNoParams_noNewDrills_returnsDifferentDrillUntilNoDrillsLeft() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        int iterations = 0;
        Drill previousDrill = generator.generateDrill();
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            previousDrill = newDrill;
            iterations++;
        }

        assertEquals(3, iterations);
    }

    @Test
    public void test_regenerateDrill_prevGroupParam_noNewDrills_returnsDifferentDrillUntilNoDrillsLeft() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.addGroup(groups.get(0));
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        int iterations = 0;
        Drill previousDrill = generator.generateDrill(groups.get(0));
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertTrue(newDrill.getGroups().contains(groups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        }

        assertEquals(2, iterations);
    }

    @Test
    public void test_regenerateDrill_prevSubGroupParam_noNewDrills_returnsDifferentDrillUntilNoDrillsLeft() {
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.addSubGroup(subGroups.get(0));
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        int iterations = 0;
        Drill previousDrill = generator.generateDrill(subGroups.get(0));
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        }

        assertEquals(2, iterations);
    }

    @Test
    public void test_regenerateDrill_prevGroupAndSubGroupParam_noNewDrills_returnsDifferentDrillUntilNoDrillsLeft() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        int iterations = 0;
        Drill previousDrill = generator.generateDrill(subGroups.get(0));
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertTrue(newDrill.getGroups().contains(groups.get(0)));
                assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        }

        assertEquals(2, iterations);
    }

    @Test
    public void test_regenerateDrill_prevNoParams_yesNewDrills_returnsAllNewDrillsFirstThenNotNewDrills() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(true);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(true);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(true);
        Drill drill4 = new Drill();
        drill4.setName("drill4");
        drill4.setNewDrill(false);
        Drill drill5 = new Drill();
        drill5.setName("drill5");
        drill5.setNewDrill(false);
        setDrills(drill1, drill2, drill3, drill4, drill5);

        Drill previousDrill = generator.generateDrill();
        // Should return the remaining 2 new drills before returning the rest
        for (int i = 0; i < 2; i++ ) {
            Drill newDrill = generator.regenerateDrill();
            assertNotNull(newDrill);
            assertNotEquals(newDrill, previousDrill);
            assertTrue(newDrill.isNewDrill());
            previousDrill = newDrill;
        }

        // There should now be no remaining new drills
        int iterations = 0;
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertFalse(newDrill.isNewDrill());
            }
            previousDrill = newDrill;
            iterations++;
        }

        assertEquals(3, iterations);
    }

    @Test
    public void test_regenerateDrill_prevGroupParam_yesNewDrills_returnsAllNewDrillsFirstThenNotNewDrills() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(true);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(true);
        drill2.addGroup(groups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(true);
        drill3.addGroup(groups.get(0));
        Drill drill4 = new Drill();
        drill4.setName("drill4");
        drill4.setNewDrill(false);
        drill4.addGroup(groups.get(0));
        Drill drill5 = new Drill();
        drill5.setName("drill5");
        drill5.setNewDrill(false);
        drill5.addGroup(groups.get(0));
        Drill drill6 = new Drill();
        drill6.setName("drill6");
        drill6.setNewDrill(true);
        Drill drill7 = new Drill();
        drill7.setName("drill7");
        drill7.setNewDrill(false);
        setDrills(drill1, drill2, drill3, drill4, drill5, drill6, drill7);

        Drill previousDrill = generator.generateDrill(groups.get(0));
        // Should return the remaining 2 new drills before returning the rest
        for (int i = 0; i < 2; i++ ) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            assertNotNull(newDrill);
            assertTrue(newDrill.isNewDrill());
            assertTrue(newDrill.getGroups().contains(groups.get(0)));
            previousDrill = newDrill;
        }

        // There should now be no remaining new drills
        int iterations = 0;
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertFalse(newDrill.isNewDrill());
                assertTrue(newDrill.getGroups().contains(groups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        }

        assertEquals(3, iterations);
    }

    @Test
    public void test_regenerateDrill_prevSubGroupParam_yesNewDrills_returnsAllNewDrillsFirstThenNotNewDrills() {
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(true);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(true);
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(true);
        drill3.addSubGroup(subGroups.get(0));
        Drill drill4 = new Drill();
        drill4.setName("drill4");
        drill4.setNewDrill(false);
        drill4.addSubGroup(subGroups.get(0));
        Drill drill5 = new Drill();
        drill5.setName("drill5");
        drill5.setNewDrill(false);
        drill5.addSubGroup(subGroups.get(0));
        Drill drill6 = new Drill();
        drill6.setName("drill6");
        drill6.setNewDrill(true);
        Drill drill7 = new Drill();
        drill7.setName("drill7");
        drill7.setNewDrill(false);
        setDrills(drill1, drill2, drill3, drill4, drill5, drill6, drill7);

        Drill previousDrill = generator.generateDrill(subGroups.get(0));
        // Should return the remaining 2 new drills before returning the rest
        for (int i = 0; i < 2; i++ ) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            assertNotNull(newDrill);
            assertTrue(newDrill.isNewDrill());
            assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            previousDrill = newDrill;
        }

        // There should now be no remaining new drills
        int iterations = 0;
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertFalse(newDrill.isNewDrill());
                assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        }

        assertEquals(3, iterations);
    }

    @Test
    public void test_regenerateDrill_prevGroupAndSubGroupParam_yesNewDrills_returnsAllNewDrillsFirstThenNotNewDrills() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(true);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(true);
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(true);
        drill3.addGroup(groups.get(0));
        drill3.addSubGroup(subGroups.get(0));
        Drill drill4 = new Drill();
        drill4.setName("drill4");
        drill4.setNewDrill(false);
        drill4.addGroup(groups.get(0));
        drill4.addSubGroup(subGroups.get(0));
        Drill drill5 = new Drill();
        drill5.setName("drill5");
        drill5.setNewDrill(false);
        drill5.addGroup(groups.get(0));
        drill5.addSubGroup(subGroups.get(0));
        Drill drill6 = new Drill();
        drill6.setName("drill6");
        drill6.setNewDrill(true);
        Drill drill7 = new Drill();
        drill7.setName("drill7");
        drill7.setNewDrill(false);
        setDrills(drill1, drill2, drill3, drill4, drill5, drill6, drill7);

        Drill previousDrill = generator.generateDrill(subGroups.get(0));
        // Should return the remaining 2 new drills before returning the rest
        for (int i = 0; i < 2; i++ ) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            assertNotNull(newDrill);
            assertTrue(newDrill.isNewDrill());
            assertTrue(newDrill.getGroups().contains(groups.get(0)));
            assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            previousDrill = newDrill;
        }

        // There should now be no remaining new drills
        int iterations = 0;
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertFalse(newDrill.isNewDrill());
                assertTrue(newDrill.getGroups().contains(groups.get(0)));
                assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        }

        assertEquals(3, iterations);
    }

    @Test
    public void test_resetSkippedDrills_prevNoParams_noNewDrills_returnsDifferentDrillUntilNoDrillsLeft_thenResetOnce() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        int iterations = 0;
        Drill previousDrill = generator.generateDrill();
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            previousDrill = newDrill;
            iterations++;
        }

        generator.resetSkippedDrills();
        boolean firstPass = true;

        do {
            Drill newDrill = generator.regenerateDrill();
            if (firstPass) {
                // First time through we may regenerate the same drill, which is okay
                firstPass = false;
            } else {
                assertNotEquals(newDrill, previousDrill);
            }
            previousDrill = newDrill;
            iterations++;
        } while (null != previousDrill);

        assertEquals(7, iterations);
    }

    @Test
    public void test_resetSkippedDrills_prevGroupParam_noNewDrills_returnsDifferentDrillUntilNoDrillsLeft_thenResetOnce() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.addGroup(groups.get(0));
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        int iterations = 0;
        Drill previousDrill = generator.generateDrill(groups.get(0));
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertTrue(newDrill.getGroups().contains(groups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        }

        generator.resetSkippedDrills();
        boolean firstPass = true;

        do {
            Drill newDrill = generator.regenerateDrill();
            if (firstPass) {
                // First time through we may regenerate the same drill, which is okay
                firstPass = false;
            } else {
                assertNotEquals(newDrill, previousDrill);
            }
            if (null != newDrill) {
                assertTrue(newDrill.getGroups().contains(groups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        } while (null != previousDrill);

        assertEquals(5, iterations);
    }

    @Test
    public void test_resetSkippedDrills_prevSubGroupParam_noNewDrills_returnsDifferentDrillUntilNoDrillsLeft_thenResetOnce() {
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.addSubGroup(subGroups.get(0));
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        int iterations = 0;
        Drill previousDrill = generator.generateDrill(subGroups.get(0));
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        }

        generator.resetSkippedDrills();
        boolean firstPass = true;

        do {
            Drill newDrill = generator.regenerateDrill();
            if (firstPass) {
                // First time through we may regenerate the same drill, which is okay
                firstPass = false;
            } else {
                assertNotEquals(newDrill, previousDrill);
            }
            if (null != newDrill) {
                assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        } while (null != previousDrill);

        assertEquals(5, iterations);
    }

    @Test
    public void test_resetSkippedDrills_prevGroupAndSubGroupParam_noNewDrills_returnsDifferentDrillUntilNoDrillsLeft_thenResetOnce() {
        List<GroupEntity> groups = setGroups(new GroupEntity("group1", "group1"));
        List<SubGroupEntity> subGroups = setSubGroups(new SubGroupEntity("subGroup1", "subGroup1"));

        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        drill1.addGroup(groups.get(0));
        drill1.addSubGroup(subGroups.get(0));
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.addGroup(groups.get(0));
        drill2.addSubGroup(subGroups.get(0));
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        setDrills(drill1, drill2, drill3);

        int iterations = 0;
        Drill previousDrill = generator.generateDrill(subGroups.get(0));
        while (null != previousDrill) {
            Drill newDrill = generator.regenerateDrill();
            assertNotEquals(newDrill, previousDrill);
            if (null != newDrill) {
                assertTrue(newDrill.getGroups().contains(groups.get(0)));
                assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        }

        generator.resetSkippedDrills();
        boolean firstPass = true;

        do {
            Drill newDrill = generator.regenerateDrill();
            if (firstPass) {
                // First time through we may regenerate the same drill, which is okay
                firstPass = false;
            } else {
                assertNotEquals(newDrill, previousDrill);
            }
            if (null != newDrill) {
                assertTrue(newDrill.getGroups().contains(groups.get(0)));
                assertTrue(newDrill.getSubGroups().contains(subGroups.get(0)));
            }
            previousDrill = newDrill;
            iterations++;
        } while (null != previousDrill);

        assertEquals(5, iterations);
    }

    /**
     * Private helper function to insert and return generated Drills from DB.
     *
     * @param drills    Drills to insert into the DB.
     * @return          List of Drills inserted into the database.
     */
    private List<Drill> setDrills(Drill... drills) {
        repo.insertDrills(drills);
        return repo.getAllDrills();
    }

    /**
     * Private helper function to insert and return generated GroupEntities from DB.
     *
     * @param groups    Groups to insert into the DB.
     * @return          List of GroupEntities inserted into the database.
     */
    private List<GroupEntity> setGroups(GroupEntity... groups) {
        repo.insertGroups(groups);
        return repo.getAllGroups();
    }

    /**
     * Private helper function to insert and return generated SubGroupEntities from DB.
     *
     * @param subGroups SubGroups to insert into the DB.
     * @return          List of SubGroupEntities inserted into the database.
     */
    private List<SubGroupEntity> setSubGroups(SubGroupEntity... subGroups) {
        repo.insertSubGroups(subGroups);
        return repo.getAllSubGroups();
    }
}
