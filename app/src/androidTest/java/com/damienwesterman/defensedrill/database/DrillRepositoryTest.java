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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tests the {@link DrillRepository} database access class.
 */
@RunWith(AndroidJUnit4.class)
public class DrillRepositoryTest {
    static Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    static DrillRepository repo = DrillRepository.getInstance(appContext);
    Drill drill1;
    Drill drill2;
    Drill drill3;
    CategoryEntity category1;
    CategoryEntity category2;
    CategoryEntity category3;
    SubCategoryEntity subCategory1;
    SubCategoryEntity subCategory2;
    SubCategoryEntity subCategory3;

    @Before
    public void setUp() {
        drill1 = new Drill("drill one", 1, false, Drill.HIGH_CONFIDENCE, "notes one", 1,
                new ArrayList<>(), new ArrayList<>());
        drill2 = new Drill("drill two", 2, false, Drill.MEDIUM_CONFIDENCE, "notes two", 2,
                new ArrayList<>(), new ArrayList<>());
        drill3 = new Drill("drill three", 3, false, Drill.LOW_CONFIDENCE, "notes three", 3,
                new ArrayList<>(), new ArrayList<>());
        category1 = new CategoryEntity("category one", "description one");
        category2 = new CategoryEntity("category two", "description two");
        category3 = new CategoryEntity("category three", "description three");
        subCategory1 = new SubCategoryEntity("sub category one", "description one");
        subCategory2 = new SubCategoryEntity("sub category two", "description two");
        subCategory3 = new SubCategoryEntity("sub category three", "description three");

        // Assumed to work here, tested and working, but also has its own test cases
        repo.deleteDrills(repo.getAllDrills().toArray(new Drill[0]));
        repo.deleteCategories(repo.getAllCategories().toArray(new CategoryEntity[0]));
        repo.deleteSubCategories(repo.getAllSubCategories().toArray(new SubCategoryEntity[0]));
    }

    @AfterClass
    public static void tearDown() {
        repo.deleteDrills(repo.getAllDrills().toArray(new Drill[0]));
        repo.deleteCategories(repo.getAllCategories().toArray(new CategoryEntity[0]));
        repo.deleteSubCategories(repo.getAllSubCategories().toArray(new SubCategoryEntity[0]));
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

        assertTrue(repo.insertDrills(drill1, drill2, drill3));

        List<Drill> returnedDrills = repo.getAllDrills();
        assertEquals(3, returnedDrills.size());
        List<String> returnedDrillsNames = returnedDrills.stream()
                .map(Drill::getName)
                .collect(Collectors.toList());

        assertTrue(returnedDrillsNames.contains(drill1.getName()));
        assertTrue(returnedDrillsNames.contains(drill2.getName()));
        assertTrue(returnedDrillsNames.contains(drill3.getName()));
    }

    @Test
    public void test_getAllDrillsByCategoryId_noMatchingDrills() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        drill1.getCategories().add(category1);
        drill2.getCategories().add(category2);
        repo.insertDrills(drill1, drill2);

        assertEquals(0, repo.getAllDrillsByCategoryId(category3.getId()).size());
    }

    @Test
    public void test_getAllDrillsByCategoryId_oneMatchingDrills() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        drill1.getCategories().add(category1);
        drill2.getCategories().add(category2);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(1, repo.getAllDrillsByCategoryId(category1.getId()).size());
    }

    @Test
    public void test_getAllDrillsByCategoryId_multipleMatchingDrills() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        drill1.getCategories().add(category1);
        drill2.getCategories().add(category1);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(2, repo.getAllDrillsByCategoryId(category1.getId()).size());
    }

    @Test
    public void test_getAllDrillsByCategoryId_nonExistentCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        drill1.getCategories().add(category1);
        drill2.getCategories().add(category2);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrillsByCategoryId(category3.getId()).size());
    }

    @Test
    public void test_getAllDrillsByCategoryId_invalidCategoryId() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        drill1.getCategories().add(category1);
        drill2.getCategories().add(category2);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrillsByCategoryId(-1).size());
    }

    @Test
    public void test_getAllDrillsBySubCategoryId_noMatchingDrills() {
        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.getSubCategories().add(subCategory1);
        drill2.getSubCategories().add(subCategory2);
        repo.insertDrills(drill1, drill2);

        assertEquals(0, repo.getAllDrillsBySubCategoryId(subCategory3.getId()).size());
    }

    @Test
    public void test_getAllDrillsBySubCategoryId_oneMatchingDrills() {
        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.getSubCategories().add(subCategory1);
        drill2.getSubCategories().add(subCategory2);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(1, repo.getAllDrillsBySubCategoryId(subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrillsBySubCategoryId_multipleMatchingDrills() {
        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.getSubCategories().add(subCategory1);
        drill2.getSubCategories().add(subCategory1);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(2, repo.getAllDrillsBySubCategoryId(subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrillsBySubCategoryId_nonExistentSubCategory() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.getSubCategories().add(subCategory1);
        drill2.getSubCategories().add(subCategory2);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrillsBySubCategoryId(subCategory3.getId()).size());
    }

    @Test
    public void test_getAllDrillsBySubCategoryId_invalidCategoryId() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.getSubCategories().add(subCategory1);
        drill2.getSubCategories().add(subCategory2);

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrillsBySubCategoryId(-1).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_noMatchingDrillForCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addSubCategory(subCategory1);
        drill2.addSubCategory(subCategory2);
        drill3.addSubCategory(subCategory3);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_noMatchingDrillForSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill2.addCategory(category2);
        drill3.addCategory(category3);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_noMatchingDrillForCategoryORSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_oneMatchingDrillForCategoryOneDifferentDrillForSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill2.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_MultipleMatchingDrillsForCategoryOneDifferentDrillForSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill2.addSubCategory(subCategory1);
        drill3.addCategory(category1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_oneMatchingDrillForCategoryMultipleDifferentDrillsForSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill2.addSubCategory(subCategory1);
        drill3.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_multipleMatchingDrillsForCategoryMultipleDifferentDrillsForSubCategory() {
        Drill drill4 = new Drill("drill four", 4, false, Drill.HIGH_CONFIDENCE, "notes four", 4,
                new ArrayList<>(), new ArrayList<>());
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill2.addSubCategory(subCategory1);
        drill3.addCategory(category1);
        drill4.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_oneMatchingDrillForCategoryOneForSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category2);
        drill2.addSubCategory(subCategory2);
        drill3.addCategory(category3);
        drill3.addSubCategory(subCategory3);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(1, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_oneMatchingDrillForCategoryMultipleForSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category2);
        drill2.addSubCategory(subCategory1);
        drill3.addCategory(category3);
        drill3.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(1, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_MultipleMatchingDrillsForCategoryOneForSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category1);
        drill2.addSubCategory(subCategory2);
        drill3.addCategory(category1);
        drill3.addSubCategory(subCategory3);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(1, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_multipleMatchingDrillsForCategoryMultipleForSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category1);
        drill2.addSubCategory(subCategory1);
        drill3.addCategory(category1);
        drill3.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(3, repo.getAllDrills(category1.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_noDrillsForNonExistentCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category1);
        drill2.addSubCategory(subCategory1);
        drill3.addCategory(category1);
        drill3.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category3.getId(), subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_noDrillsForNonExistentSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category1);
        drill2.addSubCategory(subCategory1);
        drill3.addCategory(category1);
        drill3.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), subCategory3.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_noDrillsForNonExistentCategoryANDnonExistentSubCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category1);
        drill2.addSubCategory(subCategory1);
        drill3.addCategory(category1);
        drill3.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category3.getId(), subCategory3.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_invalidCategoryNoMatchingDrillForSubCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.addCategory(category2);
        drill1.addSubCategory(subCategory2);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(-1, subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_invalidCategoryYesMatchingDrillForSubCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(-1, subCategory1.getId()).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_NoMatchingDrillForCategoryInvalidSubCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.addCategory(category2);
        drill1.addSubCategory(subCategory2);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), -1).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_YesMatchingDrillForCategoryInvalidSubCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(category1.getId(), -1).size());
    }

    @Test
    public void test_getAllDrills_categoryANDsubCategoryParameters_invalidCategoryInvalidSubCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category1);
        drill2.addSubCategory(subCategory1);
        drill3.addCategory(category1);
        drill3.addSubCategory(subCategory1);
        repo.insertDrills(drill1, drill2, drill3);

        assertEquals(0, repo.getAllDrills(-1, -1).size());
    }

    @Test
    public void test_getDrill_idParameter_noExistingDrill() {
        repo.insertDrills(drill1);
        assertNull(repo.getDrill(repo.getDrill(drill1.getName()).getId() + 1));
    }

    @Test
    public void test_getDrill_idParameter_yesExistingDrill() {
        repo.insertDrills(drill1);
        Drill returnedDrill = repo.getAllDrills().get(0); // should not throw
        assertNotNull(repo.getDrill(returnedDrill.getId()));
    }

    @Test
    public void test_getDrill_stringParameter_noExistingDrill() {
        repo.insertDrills(drill1);
        assertNull(repo.getDrill(drill2.getName()));
    }

    @Test
    public void test_getDrill_stringParameter_yesExistingDrill() {
        repo.insertDrills(drill1);
        assertNotNull(repo.getDrill(drill1.getName()));
    }

    @Test
    public void test_getDrill_stringParameter_nullDrillArgument() {
        assertNull(repo.getDrill(null)); // Also making sure this does not throw
    }

    @Test
    public void test_insertDrills_insertOneDrill() {
        assertTrue(repo.insertDrills(drill1));

        List<Drill> returnedDrills = repo.getAllDrills();
        List<String> returnedDrillsNames = new ArrayList<>();

        assertEquals(1, returnedDrills.size());

        for (Drill drill : returnedDrills) {
            returnedDrillsNames.add(drill.getName());
        }

        assertTrue(returnedDrillsNames.contains(drill1.getName()));
    }

    @Test
    public void test_insertDrills_insertWithCategories() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        drill1.getCategories().add(category1);
        drill1.getCategories().add(category2);

        assertTrue(repo.insertDrills(drill1));

        assertEquals(2, repo.getDrill(drill1.getName()).getCategories().size());
    }

    @Test
    public void test_insertDrills_insertWithSubCategories() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.getSubCategories().add(subCategory1);
        drill1.getSubCategories().add(subCategory2);

        assertTrue(repo.insertDrills(drill1));

        assertEquals(2, repo.getDrill(drill1.getName()).getSubCategories().size());
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
    public void test_insertDrills_throwsWhenGivenNonExistentCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        drill1.getCategories().add(category1);
        drill1.getCategories().add(category2);
        drill1.getCategories().add(category3);

        assertThrows(SQLiteConstraintException.class, () -> repo.insertDrills(drill1));
    }

    @Test
    public void test_insertDrills_throwsWhenGivenNonExistentSubCategory() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.getSubCategories().add(subCategory1);
        drill1.getSubCategories().add(subCategory2);
        drill1.getSubCategories().add(subCategory3);

        assertThrows(SQLiteConstraintException.class, () -> repo.insertDrills(drill1));
    }

    @Test
    public void test_insertDrills_doesNothingWithNullArgument() {
        assertTrue(repo.insertDrills((Drill) null)); // Making sure this does not throw
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_insertDrills_doesNothingWithNullArgumentList() {
        assertTrue(repo.insertDrills((Drill[]) null)); // Making sure this does not throw
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_insertDrills_nullCategoryTreatedAsEmptyArray() {
        drill1.setCategories(null);
        assertTrue(repo.insertDrills(drill1)); // Making sure this does not throw
        assertEquals(1, repo.getAllDrills().size());
    }

    @Test
    public void test_insertDrills_nullSubCategoryTreatedAsEmptyArray() {
        drill1.setSubCategories(null);
        assertTrue(repo.insertDrills(drill1)); // Making sure this does not throw
        assertEquals(1, repo.getAllDrills().size());
    }

    @Test
    public void test_updateDrills_updateOneDrill() {
        repo.insertDrills(drill1);
        drill1 = repo.getDrill(drill1.getName());
        String newName = "new name";
        drill1.setName(newName);
        assertTrue(repo.updateDrills(drill1));

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
        assertTrue(repo.updateDrills(drill1, drill2));

        assertEquals(0, newName1.compareTo(repo.getDrill(drill1.getId()).getName()));
        assertEquals(0, newName2.compareTo(repo.getDrill(drill2.getId()).getName()));
    }

    @Test
    public void test_updateDrills_addCategories() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        drill1.getCategories().add(category1);

        repo.insertDrills(drill1);

        Drill insertedDrill = repo.getDrill(drill1.getName());

        assertEquals(1, insertedDrill.getCategories().size());

        insertedDrill.getCategories().add(category2);
        assertTrue(repo.updateDrills(insertedDrill));

        assertEquals(2, repo.getDrill(insertedDrill.getId()).getCategories().size());
    }

    @Test
    public void test_updateDrills_removeCategories() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        drill1.getCategories().add(category1);
        drill1.getCategories().add(category2);

        repo.insertDrills(drill1);

        Drill insertedDrill = repo.getDrill(drill1.getName());

        assertEquals(2, insertedDrill.getCategories().size());

        insertedDrill.removeCategory(category2);
        assertTrue(repo.updateDrills(insertedDrill));

        assertEquals(1, repo.getDrill(insertedDrill.getId()).getCategories().size());
    }

    @Test
    public void test_updateDrills_addSubCategories() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.getSubCategories().add(subCategory1);

        repo.insertDrills(drill1);
        Drill insertedDrill = repo.getDrill(drill1.getName());

        assertEquals(1, insertedDrill.getSubCategories().size());

        insertedDrill.getSubCategories().add(subCategory2);
        assertTrue(repo.updateDrills(insertedDrill));

        assertEquals(2, repo.getDrill(insertedDrill.getId()).getSubCategories().size());
    }

    @Test
    public void test_updateDrills_removeSubCategories() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.getSubCategories().add(subCategory1);
        drill1.getSubCategories().add(subCategory2);

        repo.insertDrills(drill1);
        Drill insertedDrill = repo.getDrill(drill1.getName());

        assertEquals(2, insertedDrill.getSubCategories().size());

        insertedDrill.removeSubCategory(subCategory2);
        assertTrue(repo.updateDrills(insertedDrill));

        assertEquals(1, repo.getDrill(insertedDrill.getId()).getSubCategories().size());
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
    public void test_updateDrills_throwsWhenGivenNonExistentCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        drill1.getCategories().add(category1);
        drill1.getCategories().add(category2);

        repo.insertDrills(drill1);

        Drill insertedDrill = repo.getDrill(drill1.getName());
        insertedDrill.getCategories().add(category3);

        assertThrows(SQLiteConstraintException.class, () -> repo.updateDrills(insertedDrill));
    }

    @Test
    public void test_updateDrills_throwsWhenGivenNonExistentSubCategory() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());

        drill1.getSubCategories().add(subCategory1);
        drill1.getSubCategories().add(subCategory2);

        repo.insertDrills(drill1);

        Drill insertedDrill = repo.getDrill(drill1.getName());
        insertedDrill.getSubCategories().add(subCategory3);

        assertThrows(SQLiteConstraintException.class, () -> repo.updateDrills(insertedDrill));
    }

    @Test
    public void test_updateDrills_updateNonExistentDrillDoesNothing() {
        assertFalse(repo.updateDrills(drill1));
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_updateDrills_doesNothingWithNullArgument() {
        assertTrue(repo.updateDrills((Drill) null)); // Making sure this does not throw
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_updateDrills_doesNothingWithNullArgumentList() {
        assertTrue(repo.updateDrills((Drill[]) null)); // Making sure this does not throw
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_updateDrills_nullCategoryTreatedAsEmptyArray() {
        repo.insertDrills(drill1);
        drill1 = repo.getDrill(drill1.getName());
        drill1.setCategories(null);
        assertTrue(repo.updateDrills(drill1)); // Making sure this does not throw
        assertEquals(1, repo.getAllDrills().size());
    }

    @Test
    public void test_updateDrills_nullSubCategoryTreatedAsEmptyArray() {
        repo.insertDrills(drill1);
        drill1 = repo.getDrill(drill1.getName());
        drill1.setSubCategories(null);
        assertTrue(repo.updateDrills(drill1)); // Making sure this does not throw
        assertEquals(1, repo.getAllDrills().size());

    }

    @Test
    public void test_deleteDrills_deleteExistingDrill() {
        repo.insertDrills(drill1, drill2);
        drill1 = repo.getDrill(drill1.getName());
        repo.deleteDrills(drill1);
        assertEquals(1, repo.getAllDrills().size());
    }

    @Test
    public void test_deleteDrills_nonExistingDrillDoesNothing() {
        repo.insertDrills(drill1, drill2);
        repo.deleteDrills(drill3);
        assertEquals(2, repo.getAllDrills().size());
    }

    @Test
    public void test_deleteDrills_doesNothingWithNullArgument() {
        repo.insertDrills(drill1);
        repo.deleteDrills((Drill) null); // Make sure this does not throw
        assertEquals(1, repo.getAllDrills().size());
    }

    @Test
    public void test_deleteDrills_doesNothingWithNullArgumentList() {
        repo.insertDrills(drill1);
        repo.deleteDrills((Drill[]) null); // Make sure this does not throw
        assertEquals(1, repo.getAllDrills().size());
    }

    @Test
    public void test_getAllCategories_emptyDB() {
        assertEquals(0, repo.getAllCategories().size());
    }

    @Test
    public void test_getAllCategories_AND_insertCategories_insertOneCategory() {
        assertTrue(repo.insertCategories(category1));

        List<CategoryEntity> returnedCategories = repo.getAllCategories();
        assertEquals(1, returnedCategories.size());

        assertEquals(0, category1.getName().compareTo(returnedCategories.get(0).getName()));
    }

    @Test
    public void test_getAllCategories_AND_insertCategories_insertMultipleCategories() {
        assertTrue(repo.insertCategories(category1, category2, category3));

        List<CategoryEntity> returnedCategories = repo.getAllCategories();
        assertEquals(3, returnedCategories.size());
        List<String> returnedCategoryNames = returnedCategories.stream()
                .map(CategoryEntity::getName)
                .collect(Collectors.toList());

        assertTrue(returnedCategoryNames.contains(category1.getName()));
        assertTrue(returnedCategoryNames.contains(category2.getName()));
        assertTrue(returnedCategoryNames.contains(category3.getName()));
    }

    @Test
    public void test_getCategory_idParameter_noExistingCategory() {
        repo.insertCategories(category1);
        assertNull(repo.getCategory(repo.getCategory(category1.getName()).getId() + 1));
    }

    @Test
    public void test_getCategory_idParameter_yesExistingCategory() {
        repo.insertCategories(category1);
        CategoryEntity returnedCategory = repo.getAllCategories().get(0); // should not throw
        assertNotNull(repo.getCategory(returnedCategory.getId()));
    }

    @Test
    public void test_getCategory_stringParameter_noExistingCategory() {
        repo.insertCategories(category1);
        assertNull(repo.getCategory(category2.getName()));
    }

    @Test
    public void test_getCategory_stringParameter_yesExistingCategory() {
        repo.insertCategories(category1);
        assertNotNull(repo.getCategory(category1.getName()));
    }

    @Test
    public void test_getCategory_stringParameter_nullArgument() {
        assertNull(repo.getCategory(null));
    }

    @Test
    public void test_insertCategory_throwsWhenViolateUniqueKey() {
        String sameName = "same name";
        category1.setName(sameName);
        category2.setName(sameName);
        assertThrows(SQLiteConstraintException.class, () -> repo.insertCategories(category1, category2));
    }

    @Test
    public void test_insertCategory_throwsWhenGivenNullName() {
        category1.setName(null);
        assertThrows(SQLiteConstraintException.class, () -> repo.insertCategories(category1));
    }

    @Test
    public void test_insertCategory_doesNothingWithNullArgument() {
        assertTrue(repo.insertCategories((CategoryEntity) null)); // Make sure this does not throw
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_insertCategory_doesNothingWithNullArgumentList() {
        assertTrue(repo.insertCategories((CategoryEntity[]) null)); // Make sure this does not throw
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_updateCategory_updateOneCategory() {
        repo.insertCategories(category1);
        category1 = repo.getCategory(category1.getName());
        String newName = "new name";
        category1.setName(newName);
        assertTrue(repo.updateCategories(category1));

        assertEquals(0, newName.compareTo(repo.getCategory(category1.getId()).getName()));
    }

    @Test
    public void test_updateCategory_updateMultipleCategories() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        String newName1 = "new name 1";
        String newName2 = "new name 2";
        category1.setName(newName1);
        category2.setName(newName2);
        assertTrue(repo.updateCategories(category1, category2));

        assertEquals(0, newName1.compareTo(repo.getCategory(category1.getId()).getName()));
        assertEquals(0, newName2.compareTo(repo.getCategory(category2.getId()).getName()));
    }

    @Test
    public void test_updateCategory_throwsWhenViolateUniqueKey() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        String sameName = "same name";
        category1.setName(sameName);
        category2.setName(sameName);
        assertThrows(SQLiteConstraintException.class, () -> repo.updateCategories(category1, category2));
    }

    @Test
    public void test_updateCategory_throwsWhenGivenNullName() {
        repo.insertCategories(category1);
        category1 = repo.getCategory(category1.getName());
        category1.setName(null);
        assertThrows(SQLiteConstraintException.class, () -> repo.updateCategories(category1));
    }

    @Test
    public void test_updateCategory_doesNothingWithNonExistentCategory() {
        assertFalse(repo.updateCategories(category1));
        assertEquals(0, repo.getAllCategories().size());
    }

    @Test
    public void test_updateCategory_doesNothingWithNullArgument() {
        assertTrue(repo.updateCategories((CategoryEntity) null));
        assertEquals(0, repo.getAllCategories().size());
    }

    @Test
    public void test_updateCategory_doesNothingWithNullArgumentList() {
        assertTrue(repo.updateCategories((CategoryEntity[]) null));
        assertEquals(0, repo.getAllCategories().size());
    }

    @Test
    public void test_deleteCategories_deleteExistingCategory() {
        repo.insertCategories(category1, category2);
        category1 = repo.getCategory(category1.getName());
        repo.deleteCategories(category1);
        assertEquals(1, repo.getAllCategories().size());
    }

    @Test
    public void test_deleteCategories_nonExistentCategoryDoesNothing() {
        repo.insertCategories(category1, category2);
        repo.deleteCategories(category3);
        assertEquals(2, repo.getAllCategories().size());
    }

    @Test
    public void test_deleteCategories_doesNothingWithNullArgument() {
        repo.insertCategories(category1);
        repo.deleteCategories((CategoryEntity) null); // Make sure this does not throw
        assertEquals(1, repo.getAllCategories().size());
    }

    @Test
    public void test_deleteCategories_doesNothingWithNullArgumentList() {
        repo.insertCategories(category1);
        repo.deleteCategories((CategoryEntity[]) null); // Make sure this does not throw
        assertEquals(1, repo.getAllCategories().size());
    }

    @Test
    public void test_getAllSubCategories_emptyDB() {
        assertEquals(0, repo.getAllSubCategories().size());
    }

    @Test
    public void test_getAllSubCategories_AND_insertSubCategories_insertOneSubCategory() {
        assertTrue(repo.insertSubCategories(subCategory1));

        List<SubCategoryEntity> returnedSubCategories = repo.getAllSubCategories();
        assertEquals(1, returnedSubCategories.size());

        assertEquals(0, subCategory1.getName().compareTo(returnedSubCategories.get(0).getName()));
    }

    @Test
    public void test_getAllSubCategories_AND_insertSubCategories_insertMultipleSubCategories() {
        assertTrue(repo.insertSubCategories(subCategory1, subCategory2, subCategory3));

        List<SubCategoryEntity> returnedSubCategories = repo.getAllSubCategories();
        assertEquals(3, returnedSubCategories.size());
        List<String> returnedSubCategoryNames = returnedSubCategories.stream()
                .map(SubCategoryEntity::getName)
                .collect(Collectors.toList());

        assertTrue(returnedSubCategoryNames.contains(subCategory1.getName()));
        assertTrue(returnedSubCategoryNames.contains(subCategory2.getName()));
        assertTrue(returnedSubCategoryNames.contains(subCategory3.getName()));
    }

    @Test
    public void test_getAllSubCategories_categoryParameter_noMatchingSubCategories() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category2);
        drill2.addSubCategory(subCategory2);

        repo.insertDrills(drill1, drill2);

        assertEquals(0, repo.getAllSubCategories(category3.getId()).size());
    }

    @Test
    public void test_getAllSubCategories_categoryParameter_oneMatchingSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category2);
        drill2.addSubCategory(subCategory2);

        repo.insertDrills(drill1, drill2);

        assertEquals(1, repo.getAllSubCategories(category2.getId()).size());
    }

    @Test
    public void test_getAllSubCategories_categoryParameter_multipleMatchingSubCategories() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category1);
        drill2.addSubCategory(subCategory2);

        repo.insertDrills(drill1, drill2);

        assertEquals(2, repo.getAllSubCategories(category1.getId()).size());
    }

    @Test
    public void test_getAllSubCategories_categoryParameter_multipleDrillsSameSubCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());
        category3 = repo.getCategory(category3.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category1);
        drill2.addSubCategory(subCategory1);

        repo.insertDrills(drill1, drill2);

        assertEquals(1, repo.getAllSubCategories(category1.getId()).size());
    }

    @Test
    public void test_getAllSubCategories_categoryParameter_nonExistentCategory() {
        repo.insertCategories(category1, category2, category3);
        category1 = repo.getCategory(category1.getName());
        category2 = repo.getCategory(category2.getName());

        repo.insertSubCategories(subCategory1, subCategory2, subCategory3);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        subCategory3 = repo.getSubCategory(subCategory3.getName());

        drill1.addCategory(category1);
        drill1.addSubCategory(subCategory1);
        drill2.addCategory(category2);
        drill2.addSubCategory(subCategory2);

        repo.insertDrills(drill1, drill2);

        assertEquals(0, repo.getAllSubCategories(category3.getId()).size());
    }

    @Test
    public void test_getAllSubCategories_categoryParameter_invalidArgument() {
        assertEquals(0, repo.getAllSubCategories(-1).size());
    }

    @Test
    public void test_getSubCategory_idParameter_noExistingSubCategory() {
        repo.insertSubCategories(subCategory1);
        assertNull(repo.getSubCategory(repo.getSubCategory(subCategory1.getName()).getId() + 1));
    }

    @Test
    public void test_getSubCategory_idParameter_yesExistingSubCategory() {
        repo.insertSubCategories(subCategory1);
        SubCategoryEntity returnedSubCategory = repo.getAllSubCategories().get(0); // should not throw
        assertNotNull(repo.getSubCategory(returnedSubCategory.getId()));
    }

    @Test
    public void test_getSubCategory_stringParameter_noExistingSubCategory() {
        repo.insertSubCategories(subCategory1);
        assertNull(repo.getSubCategory(subCategory2.getName()));
    }

    @Test
    public void test_getSubCategory_stringParameter_yesExistingSubCategory() {
        repo.insertSubCategories(subCategory1);
        assertNotNull(repo.getSubCategory(subCategory1.getName()));
    }

    @Test
    public void test_getSubCategory_stringParameter_nullArgument() {
        assertNull(repo.getSubCategory(null));
    }

    @Test
    public void test_insertSubCategory_throwsWhenViolateUniqueKey() {
        String sameName = "same name";
        subCategory1.setName(sameName);
        subCategory2.setName(sameName);
        assertThrows(SQLiteConstraintException.class, () -> repo.insertSubCategories(subCategory1, subCategory2));
    }

    @Test
    public void test_insertSubCategory_throwsWhenGivenNullName() {
        subCategory1.setName(null);
        assertThrows(SQLiteConstraintException.class, () -> repo.insertSubCategories(subCategory1));
    }

    @Test
    public void test_insertSubCategory_doesNothingWithNullArgument() {
        assertTrue(repo.insertSubCategories((SubCategoryEntity) null)); // Make sure this does not throw
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_insertSubCategory_doesNothingWithNullArgumentList() {
        assertTrue(repo.insertSubCategories((SubCategoryEntity[]) null)); // Make sure this does not throw
        assertEquals(0, repo.getAllDrills().size());
    }

    @Test
    public void test_updateSubCategory_updateOneSubCategory() {
        repo.insertSubCategories(subCategory1);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        String newName = "new name";
        subCategory1.setName(newName);
        assertTrue(repo.updateSubCategories(subCategory1));

        assertEquals(0, newName.compareTo(repo.getSubCategory(subCategory1.getId()).getName()));
    }

    @Test
    public void test_updateSubCategory_updateMultipleSubCategories() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        String newName1 = "new name 1";
        String newName2 = "new name 2";
        subCategory1.setName(newName1);
        subCategory2.setName(newName2);
        assertTrue(repo.updateSubCategories(subCategory1, subCategory2));

        assertEquals(0, newName1.compareTo(repo.getSubCategory(subCategory1.getId()).getName()));
        assertEquals(0, newName2.compareTo(repo.getSubCategory(subCategory2.getId()).getName()));
    }

    @Test
    public void test_updateSubCategory_throwsWhenViolateUniqueKey() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory2 = repo.getSubCategory(subCategory2.getName());
        String sameName = "same name";
        subCategory1.setName(sameName);
        subCategory2.setName(sameName);
        assertThrows(SQLiteConstraintException.class, () -> repo.updateSubCategories(subCategory1, subCategory2));
    }

    @Test
    public void test_updateSubCategory_throwsWhenGivenNullName() {
        repo.insertSubCategories(subCategory1);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        subCategory1.setName(null);
        assertThrows(SQLiteConstraintException.class, () -> repo.updateSubCategories(subCategory1));
    }

    @Test
    public void test_updateSubCategory_doesNothingWithNonExistentSubCategory() {
        assertFalse(repo.updateSubCategories(subCategory1));
        assertEquals(0, repo.getAllSubCategories().size());
    }

    @Test
    public void test_updateSubCategory_doesNothingWithNullArgument() {
        assertTrue(repo.updateSubCategories((SubCategoryEntity) null));
        assertEquals(0, repo.getAllSubCategories().size());
    }

    @Test
    public void test_updateSubCategory_doesNothingWithNullArgumentList() {
        assertTrue(repo.updateSubCategories((SubCategoryEntity[]) null));
        assertEquals(0, repo.getAllSubCategories().size());
    }

    @Test
    public void test_deleteSubCategories_deleteExistingSubCategory() {
        repo.insertSubCategories(subCategory1, subCategory2);
        subCategory1 = repo.getSubCategory(subCategory1.getName());
        repo.deleteSubCategories(subCategory1);
        assertEquals(1, repo.getAllSubCategories().size());
    }

    @Test
    public void test_deleteSubCategories_nonExistentSubCategoryDoesNothing() {
        repo.insertSubCategories(subCategory1, subCategory2);
        repo.deleteSubCategories(subCategory3);
        assertEquals(2, repo.getAllSubCategories().size());
    }

    @Test
    public void test_deleteSubCategories_doesNothingWithNullArgument() {
        repo.insertSubCategories(subCategory1);
        repo.deleteSubCategories((SubCategoryEntity) null); // Make sure this does not throw
        assertEquals(1, repo.getAllSubCategories().size());
    }

    @Test
    public void test_deleteSubCategories_doesNothingWithNullArgumentList() {
        repo.insertSubCategories(subCategory1);
        repo.deleteSubCategories((SubCategoryEntity[]) null); // Make sure this does not throw
        assertEquals(1, repo.getAllSubCategories().size());
    }
}
