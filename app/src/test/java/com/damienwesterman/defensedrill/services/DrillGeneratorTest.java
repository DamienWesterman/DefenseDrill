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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;

import com.damienwesterman.defensedrill.database.Drill;
import com.damienwesterman.defensedrill.database.DrillRepository;
import com.damienwesterman.defensedrill.database.GroupEntity;
import com.damienwesterman.defensedrill.database.SubGroupEntity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

@RunWith(MockitoJUnitRunner.class)
public class DrillGeneratorTest {
    DrillGenerator generator;
    @Mock
    DrillRepository repo;

    @Before
    public void setup() {
        generator = new DrillGenerator(repo);
    }

    @Test
    public void test_generateDrill_noParams_returnsAnyDrill_noNewDrills_noDrillsInDB() {
        setGetAllDrills();
        Drill returnedDrill = generator.generateDrill();
        assertNull(returnedDrill);
    }

    @Test
    public void test_generateDrill_noParams_returnsAnyDrill_noNewDrills_oneDrillInDB() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        setGetAllDrills(drill1);
        Drill returnedDrill = generator.generateDrill();
        assertEquals(returnedDrill, drill1);
    }

    @Test
    public void test_generateDrill_noParams_returnsAnyDrill_noNewDrills_multipleDrillsInDB() {
        Drill drill1 = new Drill();
        drill1.setName("drill1");
        Drill drill2 = new Drill();
        drill1.setName("drill2");
        Drill drill3 = new Drill();
        drill1.setName("drill3");
        setGetAllDrills(drill1, drill2, drill3);
        Drill returnedDrill = generator.generateDrill();
        assertTrue(repo.getAllDrills().contains(returnedDrill));
    }

    // When testing the random generation, just make sure that it fits the criteria is all
    // Then when re-generating drills, make sure that it isn't the same as the previously suggested one

    /**
     * Private helper function to mock the return of repo.getAllDrills().
     *
     * @param drills    Drills to return from repo.getAllDrills().
     */
    private void setGetAllDrills(Drill... drills) {
        when(repo.getAllDrills()).thenReturn(Arrays.asList(drills));
    }
}
