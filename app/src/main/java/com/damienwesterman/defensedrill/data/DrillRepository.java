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

package com.damienwesterman.defensedrill.data;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * This class is used to interact with the SQLite database.
 * <br>
 * Call {@link DrillRepository#getInstance(Context)} to use the repo.
 * <br><br>
 * All methods are synchronized, and thus all calls are thread safe.
 */
public class DrillRepository {
    private static DrillRepository instance;

    private final DrillDatabase db;
    private final DrillDao drillDao;
    private final CategoryDao categoryDao;
    private final SubCategoryDao subCategoryDao;

    /**
     * Private constructor, access class with {@link #getInstance(Context context)}.
     *
     * @param context   Application context.
     */
    private DrillRepository(Context context) {
        this.db = DrillDatabase.getInstance(context);
        this.drillDao = this.db.getDrillDao();
        this.categoryDao = this.db.getCategoryDao();
        this.subCategoryDao = this.db.getSubCategoryDao();
    }

    /**
     * Get running DrillRepository instance.
     *
     * @param context Application context.
     * @return DrillRepository instance.
     */
    public synchronized static DrillRepository getInstance(Context context) {
        if ( null == instance) {
            instance = new DrillRepository(context.getApplicationContext());
        }

        return instance;
    }

    /**
     * Return a list of all Drills in the database.
     *
     * @return  List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills() {
        return this.drillDao.getAllDrills();
    }

    /**
     * Return a list of all Drills that belong to the specified category.
     *
     * @param categoryId   ID of the specific category of drills.
     * @return          List of Drill objects.
     */
    public synchronized List<Drill> getAllDrillsByCategoryId(long categoryId) {
        return this.drillDao.findAllDrillsByCategory(categoryId);
    }

    /**
     * Return a list of all Drills that belong to the specified sub category.
     *
     * @param subCategoryId    ID of the specific sub category of drills.
     * @return              List of Drill objects.
     */
    public synchronized List<Drill> getAllDrillsBySubCategoryId(long subCategoryId) {
        return this.drillDao.findAllDrillsBySubCategory(subCategoryId);
    }

    /**
     * Return a list of all Drills that belong to both the specified category and sub category.
     *
     * @param categoryId       ID of the specific category of drills.
     * @param subCategoryId    ID of the specific sub category of drills.
     * @return              List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills(long categoryId, long subCategoryId) {
        return this.drillDao.findAllDrillsByCategoryAndSubCategory(categoryId, subCategoryId);
    }

    /**
     * Return a list of all Drills that are part of both lists of category and subCategory IDs.
     * <br><br>
     * If either list is null, it will match to ANY the respective category/subCategory.
     *
     * @param categoryIds       List of IDs of the category of drills.
     * @param subCategoryIds    List of IDs of the sub category of drills.
     * @return                  List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills(List<Long> categoryIds, List<Long> subCategoryIds) {
        List<Drill> ret;

        if (null == categoryIds && null == subCategoryIds) {
            ret = getAllDrills();
        } else if (null == categoryIds) {
            ret = this.drillDao.findAllDrillsBySubCategory(subCategoryIds);
        } else if (null == subCategoryIds) {
            ret = this.drillDao.findAllDrillsByCategory(categoryIds);
        } else {
            ret = this.drillDao.findAllDrillsByCategoryAndSubCategory(categoryIds, subCategoryIds);
        }

        return ret;
    }

    /**
     * Return the drill that matches the given ID.
     *
     * @param id    ID of the drill to find.
     * @return      Drill object or null if the id does not exist in the database.
     */
    public synchronized Drill getDrill(long id) {
        return this.drillDao.findDrillById(id);
    }

    /**
     * Return the drill that matches the given name;
     *
     * @param name  Name of the drill to find.
     * @return      Drill object or null if the name does not exist in the database.
     */
    public synchronized Drill getDrill(String name) {
        if (null == name) {
            return null;
        }
        return this.drillDao.findDrillByName(name);
    }

    /**
     * Insert the given drill(s).
     *
     * @param drills    Drill(s) to insert.
     * @throws SQLiteConstraintException If name is not unique, name is null, or a category/subcategory does not exist.
     * @return boolean. True if <i>all</i> inserts succeeded. False if <i>ANY SINGLE</i> insert fails.
     */
    public synchronized boolean insertDrills(Drill... drills) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == drills) {
            return success.get();
        }

        db.runInTransaction(() -> {
            for (Drill drill : drills) {
                if (null == drill) {
                    continue;
                }

                if (null == drill.getCategories()) {
                    drill.setCategories(new ArrayList<>());
                }
                if (null == drill.getSubCategories()) {
                    drill.setSubCategories(new ArrayList<>());
                }

                if (1 != drillDao.insert(drill.getDrillEntity()).length) {
                    success.set(false);
                }

                // Need to extract the auto generated ID in order to update the join tables
                Drill insertedDrill = drillDao.findDrillByName(drill.getName());
                if (null != insertedDrill) {
                    long drillId = insertedDrill.getId();

                    for (CategoryEntity category : drill.getCategories()) {
                        if (null == category) {
                            success.set(false);
                            continue;
                        }
                        if (1 != drillDao.insert(new DrillCategoryJoinEntity(drillId, category.getId())).length) {
                            success.set(false);
                        }
                    }
                    for (SubCategoryEntity subCategory : drill.getSubCategories()) {
                        if (null == subCategory) {
                            success.set(false);
                            continue;
                        }
                        if (1 != drillDao.insert(new DrillSubCategoryJoinEntity(drillId, subCategory.getId())).length) {
                            success.set(false);
                        }
                    }
                }
            }
        });

        return success.get();
    }

    /**
     * Update the given drill(s).
     *
     * @param drills    Drill(s) to update.
     * @throws SQLiteConstraintException If name is not unique, name is null, or a category/subcategory does not exist.
     * @return boolean. True if <i>all</i> updates succeeded. False if <i>ANY SINGLE</i> updates fails.
     */
    public synchronized boolean updateDrills(Drill... drills) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == drills) {
            return success.get();
        }

        db.runInTransaction(() -> {
            for (Drill drill : drills) {
                if (null == drill) {
                    continue;
                }
                if (null == drill.getCategories()) {
                    drill.setCategories(new ArrayList<>());
                }
                if (null == drill.getSubCategories()) {
                    drill.setSubCategories(new ArrayList<>());
                }

                if (1 != drillDao.update(drill.getDrillEntity())) {
                    success.set(false);
                }

                long drillId = drill.getId();

                // Add/remove new/removed categories
                Set<Long> existingCategoryIds = drillDao.findAllCategoryJoinByDrillId(drillId).stream()
                        .filter(Objects::nonNull)
                        .map(DrillCategoryJoinEntity::getCategoryId)
                        .collect(Collectors.toSet());

                Set<Long> newCategoryIds = drill.getCategories().stream()
                        .filter(Objects::nonNull)
                        .map(CategoryEntity::getId)
                        .collect(Collectors.toSet());

                Set<Long> categoriesToRemove = new HashSet<>(existingCategoryIds);
                categoriesToRemove.removeAll(newCategoryIds);

                Set<Long> categoriesToAdd = new HashSet<>(newCategoryIds);
                categoriesToAdd.removeAll(existingCategoryIds);

                for (Long categoryId : categoriesToRemove) {
                    drillDao.delete(new DrillCategoryJoinEntity(drillId, categoryId));
                }

                for (Long categoryId : categoriesToAdd) {
                    if (1 != drillDao.insert(new DrillCategoryJoinEntity(drillId, categoryId)).length) {
                        success.set(false);
                    }
                }


                // Add/remove new/removed subCategories
                Set<Long> existingSubCategoryIds = drillDao.findAllSubCategoryJoinByDrillId(drillId).stream()
                        .filter(Objects::nonNull)
                        .map(DrillSubCategoryJoinEntity::getSubCategoryId)
                        .collect(Collectors.toSet());

                Set<Long> newSubCategoryIds = drill.getSubCategories().stream()
                        .filter(Objects::nonNull)
                        .map(SubCategoryEntity::getId)
                        .collect(Collectors.toSet());

                Set<Long> subCategoriesToRemove = new HashSet<>(existingSubCategoryIds);
                categoriesToRemove.removeAll(newSubCategoryIds);

                Set<Long> subCategoriesToAdd = new HashSet<>(newSubCategoryIds);
                categoriesToAdd.removeAll(existingSubCategoryIds);

                for (Long subCategoryId : subCategoriesToRemove) {
                    drillDao.delete(new DrillSubCategoryJoinEntity(drillId, subCategoryId));
                }

                for (Long subCategoryId : subCategoriesToAdd) {
                    if (1 != drillDao.insert(new DrillSubCategoryJoinEntity(drillId, subCategoryId)).length) {
                        success.set(false);
                    }
                }
            }
        });

        return success.get();
    }

    /**
     * Delete the given drill(s).
     *
     * @param drills    Drill(s) to delete.
     */
    public synchronized void deleteDrills(Drill... drills) {
        if (null == drills) {
            return;
        }
        this.db.runInTransaction(() -> {
            for (Drill drill : drills) {
                if (null == drill) {
                    continue;
                }
                this.drillDao.delete(drill.getDrillEntity());
            }
        });
    }

    /**
     * Get all categories in the database.
     *
     * @return  List of CategoryEntity objects.
     */
    public synchronized List<CategoryEntity> getAllCategories() {
        return this.categoryDao.getAll();
    }

    /**
     * Find a category based on the given id.
     *
     * @param id    Category ID.
     * @return      CategoryEntity object or null if the id does not exist in the database.
     */
    public synchronized CategoryEntity getCategory(long id) {
        return this.categoryDao.findById(id);
    }

    /**
     * Find a category based on the given name.
     *
     * @param name  Category name.
     * @return      CategoryEntity object or null if the name does not exist in the database.
     */
    public synchronized CategoryEntity getCategory(String name) {
        if (null == name) {
            return null;
        }
        return this.categoryDao.findByName(name);
    }

    /**
     * Insert the given category(s).
     *
     * @param categories    Category(s) to insert.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     * @return boolean. True if <i>all</i> inserts succeeded. False if <i>ANY SINGLE</i> insert fails.
     */
    public synchronized boolean insertCategories(CategoryEntity... categories) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == categories) {
            return success.get();
        }
        db.runInTransaction(() -> {
            for (CategoryEntity category : categories) {
                if (null == category) {
                    continue;
                }
                if (1 != this.categoryDao.insert(category).length) {
                    success.set(false);
                }
            }

        });

        return success.get();
    }

    /**
     * Update the given category(s).
     *
     * @param categories    Category(s) to update.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     * @return boolean. True if <i>all</i> updates succeeded. False if <i>ANY SINGLE</i> updates fails.
     */
    public synchronized boolean updateCategories(CategoryEntity... categories) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == categories) {
            return success.get();
        }
        db.runInTransaction(() -> {
            for (CategoryEntity category : categories) {
                if (null == category) {
                    continue;
                }
                if (1 != this.categoryDao.update(category)) {
                    success.set(false);
                }
            }

        });

        return success.get();
    }

    /**
     * Delete the given category(s).
     *
     * @param categories    Category(s) to delete.
     */
    public synchronized void deleteCategories(CategoryEntity... categories) {
        if (null == categories) {
            return;
        }
        db.runInTransaction(() -> {
            for (CategoryEntity category : categories) {
                if (null == category) {
                    continue;
                }
                this.categoryDao.delete(category);
            }

        });
    }

    /**
     * Get all subCategories in the database.
     *
     * @return  List of SubCategoryEntity objects.
     */
    public synchronized List<SubCategoryEntity> getAllSubCategories() {
        return this.subCategoryDao.getAll();
    }

    /**
     * Get all subCategories of a given category.
     *
     * @param categoryId   ID of the category to search for its subCategories.
     * @return          List of SubCategoryEntity objects.
     */
    public synchronized List<SubCategoryEntity> getAllSubCategories(long categoryId) {
        return this.subCategoryDao.findAllByCategory(categoryId);
    }

    /**
     * Find a subCategory based on the given id.
     *
     * @param id    SubCategory ID.
     * @return      SubCategoryEntity object or null if the id does not exist in the database.
     */
    public synchronized SubCategoryEntity getSubCategory(long id) {
        return this.subCategoryDao.findById(id);
    }

    /**
     * Find a subCategory based on the given name.
     *
     * @param name  SubCategory name.
     * @return      SubCategoryEntity object or null if the name does not exist in the database.
     */
    public synchronized SubCategoryEntity getSubCategory(String name) {
        if (null == name) {
            return null;
        }
        return this.subCategoryDao.findByName(name);
    }

    /**
     * Insert the given subCategory(s).
     *
     * @param subCategories SubCategory(s) to insert.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     * @return boolean. True if <i>all</i> inserts succeeded. False if <i>ANY SINGLE</i> insert fails.
     */
    public synchronized boolean insertSubCategories(SubCategoryEntity... subCategories) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == subCategories) {
            return success.get();
        }

        db.runInTransaction(() -> {
            for (SubCategoryEntity subCategory : subCategories) {
                if (null == subCategory) {
                    continue;
                }
                if (1 != this.subCategoryDao.insert(subCategory).length) {
                    success.set(false);
                }
            }

        });

        return success.get();
    }

    /**
     * Update the given subCategory(s).
     *
     * @param subCategories SubCategory(s) to update.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     * @return boolean. True if <i>all</i> updates succeeded. False if <i>ANY SINGLE</i> updates fails.
     */
    public synchronized boolean updateSubCategories(SubCategoryEntity... subCategories) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == subCategories) {
            return success.get();
        }
        db.runInTransaction(() -> {
            for (SubCategoryEntity subCategory : subCategories) {
                if (null == subCategory) {
                    continue;
                }
                if (1 != this.subCategoryDao.update(subCategory)) {
                    success.set(false);
                }
            }

        });

        return success.get();
    }

    /**
     * Delete the given subCategory(s).
     *
     * @param subCategories SubCategory(s) to delete.
     */
    public synchronized void deleteSubCategories(SubCategoryEntity... subCategories) {
        if (null == subCategories) {
            return;
        }
        db.runInTransaction(() -> {
            for (SubCategoryEntity subCategory : subCategories) {
                if (null == subCategory) {
                    continue;
                }
                this.subCategoryDao.delete(subCategory);
            }

        });
    }
}
