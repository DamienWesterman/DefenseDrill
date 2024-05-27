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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Tests the DrillGenerator service class. Must be an instrumented test as the database layer auto
 * generates unique Drill IDs. Due to the random nature of the selection algorithm, each test will
 * likely be tested multiple times to ensure consistently correct results.
 */
public class DrillGeneratorTest {
    private static final int NUM_TESTS = 10;

    static Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    static DrillRepository repo = DrillRepository.getInstance(appContext);

    @Before
    public void setup() {
        // Assumed to work here, tested and working, but also has its own test cases
        repo.deleteDrills(repo.getAllDrills().toArray(new Drill[0]));
    }

    @Test
    public void test_generateDrill_noParams_returnsNull_noDrillsInDB() {
        DrillGenerator generator = createDrillGenerator();
        Drill returnedDrill = generator.generateDrill();
        assertNull(returnedDrill);
    }

    @Test
    public void test_generateDrill_returnsAnyDrill_noNewDrills_oneDrillInList() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        DrillGenerator generator = createDrillGenerator(drill1);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill();
            assertNotNull(returnedDrill);
            assertEquals(0, returnedDrill.getName().compareTo(drill1.getName()));
        }
    }

    @Test
    public void test_generateDrill_returnsAnyDrill_noNewDrills_multipleDrillsInList() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        DrillGenerator generator = createDrillGenerator(drill1);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill();
            assertNotNull(returnedDrill);
        }
    }

    @Test
    public void test_generateDrill_returnsAnyNewDrill_MultipleNewDrills_multipleNotNewDrillsInList() {
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
        DrillGenerator generator = createDrillGenerator(drill1);

        for (int i = 0; i < NUM_TESTS; i++) {
            Drill returnedDrill = generator.generateDrill();
            assertNotNull(returnedDrill);
            assertTrue(0 == returnedDrill.getName().compareTo(drill1.getName()) ||
                    0 == returnedDrill.getName().compareTo(drill2.getName()));
            assertTrue(returnedDrill.isNewDrill());
        }
    }

    @Test
    public void test_generateDrill_performanceTestWithLargeList_executesInLessThan500Milliseconds() {
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
        DrillGenerator generator = createDrillGenerator(aLotOfDrills);

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
        DrillGenerator generator = createDrillGenerator(initialDrills);

        Set<String> generatedDrillNames = new HashSet<>();

        for (int i = 0; i < NUM_DRILLS * 10; i++) {
            Drill returnedDrill = generator.generateDrill();
            assertNotNull(returnedDrill);
            generatedDrillNames.add(returnedDrill.getName());
        }

        assertEquals(NUM_DRILLS, generatedDrillNames.size());
    }

    @Test
    public void test_regenerateDrill_noNewDrills_returnsDifferentDrillUntilNoDrillsLeft() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        drill1.setNewDrill(false);
        Drill drill2 = new Drill();
        drill2.setName("drill2");
        drill2.setNewDrill(false);
        Drill drill3 = new Drill();
        drill3.setName("drill3");
        drill3.setNewDrill(false);
        DrillGenerator generator = createDrillGenerator(drill1, drill2, drill3);

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
    public void test_regenerateDrill_yesNewDrills_returnsAllNewDrillsFirstThenNotNewDrills() {
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
        DrillGenerator generator = createDrillGenerator(drill1, drill2, drill3, drill4, drill5);

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
        DrillGenerator generator = createDrillGenerator(drill1, drill2, drill3);

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

    /**
     * Create a DrillGenerator object from a list of drills.
     *
     * @param drills    Drills to select randomly from.
     * @return          DrillGenerator object.
     */
    private DrillGenerator createDrillGenerator(Drill... drills) {
        repo.insertDrills(drills);
        return new DrillGenerator(repo.getAllDrills(), new Random());
    }

    /**
     * Create a DrillGenerator object from a list of drills.
     *
     * @param drills    Drills to select randomly from.
     * @return          DrillGenerator object.
     */
    private DrillGenerator createDrillGenerator(List<Drill> drills) {
        repo.insertDrills(drills.toArray(new Drill[0]));
        return new DrillGenerator(repo.getAllDrills(), new Random());
    }
}
