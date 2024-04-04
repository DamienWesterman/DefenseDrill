package com.damienwesterman.defensedrill.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import android.content.Context;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;

public class DrillRepositoryTest {
    /* Mock Classes */
    @Mock
    Context context = mock(Context.class);
    @Mock
    DrillDatabase drillDatabase = mock(DrillDatabase.class);


    DrillRepository drillRepository = DrillRepository.getInstance(context);

    @BeforeClass
    public static void setup() {
        System.out.println("\n\n======================================================");
        System.out.println("Beginning Test Suite...");
    }

    @AfterClass
    public static void tearDown() {
        System.out.println("Ending Test Suite...");
        System.out.println("======================================================\n\n");
    }

    @Test
    public void test_getInstance() {
        assertEquals(drillRepository, DrillRepository.getInstance(context));
    }

    @Test
    public void test_getAllEntities() {
        try (MockedStatic<DrillDatabase> mockedStatic = mockStatic(DrillDatabase.class)) {
            mockedStatic.when(() -> DrillDatabase.getInstance(any(Context.class)))
                    .thenReturn(drillDatabase);

            assertEquals(drillDatabase, DrillDatabase.getInstance(context));
        }
    }
}
