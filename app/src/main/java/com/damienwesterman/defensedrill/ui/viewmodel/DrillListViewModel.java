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
/*
 * Copyright 2024 Damien Westerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.damienwesterman.defensedrill.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;
import lombok.Setter;

/**
 * View model for {@link Drill} objects geared towards displaying a list of all drills, and allowing
 * deletion of drills.
 */
@HiltViewModel
public class DrillListViewModel extends AndroidViewModel {
    @Getter
    private final MutableLiveData<List<Drill>> uiDrillsList;
    @Nullable
    private List<CategoryEntity> allCategories;
    @Nullable
    private List<SubCategoryEntity> allSubCategories;
    @Nullable
    @Setter
    private Set<Long> categoryFilterIds;
    @Nullable
    @Setter
    private Set<Long> subCategoryFilterIds;
    private final DrillRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    @NonNull
    @Getter
    private SortOrder sortOrder;

    public enum SortOrder {
        SORT_NAME_ASCENDING,
        SORT_NAME_DESCENDING,
        SORT_DATE_ASCENDING,
        SORT_DATE_DESCENDING
    }

    @Inject
    public DrillListViewModel(@NonNull Application application, DrillRepository repo) {
        super(application);

        this.repo = repo;
        uiDrillsList = new MutableLiveData<>();
        sortOrder = SortOrder.SORT_NAME_ASCENDING;
    }

    /**
     * Populate our list of Drills and cause the UI to re-load.
     */
    public void populateDrills() {
        if (null == uiDrillsList.getValue()) {
            resetDrills();
        } else {
            // Display an updated list of drills with the current filters
            List<Long> categoryIds;
            if (null == this.categoryFilterIds) {
                categoryIds = null;
            } else {
                categoryIds = new ArrayList<>(this.categoryFilterIds);
            }

            List<Long> subCategoryIds;
            if (null == this.subCategoryFilterIds) {
                subCategoryIds = null;
            } else {
                subCategoryIds = new ArrayList<>(this.subCategoryFilterIds);
            }

            filterDrills(categoryIds, subCategoryIds);
        }
    }

    /**
     * Load the Drills from the database and reset all filters to their defaults.
     */
    public void resetDrills() {
        this.categoryFilterIds = null;
        this.subCategoryFilterIds = null;
        this.sortOrder = SortOrder.SORT_NAME_ASCENDING;
        executor.execute(() -> uiDrillsList.postValue(repo.getAllDrills().stream()
            .filter(Drill::isKnownDrill)
            .collect(Collectors.toList())));
    }

    /**
     * Filter the list of drills by category and sub-category IDs.
     * <br><br>
     * Can filter by multiple of either IDs. This is a whitelist filter, so it will show Drills that
     * match ANY categories AND sub-categories in the list. If either category or sub-category list
     * is null, then it will match all of that respective null list.
     *
     * @param categoryIds       List of category IDs to filter by.
     * @param subCategoryIds    List of sub-category IDs to filter by.
     */
    public void filterDrills(@Nullable List<Long> categoryIds,@Nullable List<Long> subCategoryIds) {
        executor.execute(() -> {
            List<Drill> newDrills = repo.getAllDrills(categoryIds, subCategoryIds).stream()
                    .filter(Drill::isKnownDrill)
                    .collect(Collectors.toList());
            if (SortOrder.SORT_DATE_ASCENDING != this.sortOrder) {
                sortDrills(newDrills, this.sortOrder);
            } else {
                uiDrillsList.postValue(newDrills);
            }
        });
    }

    /**
     * Get the list of all categories in the database.
     * <br><br>
     * {@link #loadAllCategories()} should have been called prior otherwise will return null.
     *
     * @return List of CategoryEntity objects.
     */
    @Nullable
    public List<CategoryEntity> getAllCategories() {
        return allCategories;
    }

    /**
     * Get the list of all sub-categories in the database.
     * <br><br>
     * {@link #loadAllSubCategories()} should have been called prior otherwise will return null.
     *
     * @return List of SubCategoryEntity objects.
     */
    @Nullable
    public List<SubCategoryEntity> getAllSubCategories() {
        return allSubCategories;
    }

    /**
     * Load all categories from the database. Should be called before {@link #getAllCategories()}.
     */
    public void loadAllCategories() {
        if (null == allCategories) {
            executor.execute(() -> allCategories = repo.getAllCategories());
        }
    }

    /**
     * Load all sub-categories from the database. Should be called before
     * {@link #getAllSubCategories()}.
     */
    public void loadAllSubCategories() {
        if (null == allSubCategories) {
            executor.execute(() -> allSubCategories = repo.getAllSubCategories());
        }
    }

    /**
     * Return a set of category IDs currently being filtered.
     * <br><br>
     * Saved here to persist destructive actions (i.e. screen rotations).
     *
     * @return  Set of category IDs.
     */
    @NonNull
    public Set<Long> getCategoryFilterIds() {
        if (null == categoryFilterIds) {
            if (null != allCategories) {
                categoryFilterIds = allCategories.stream().map(CategoryEntity::getId)
                        .collect(Collectors.toSet());
            } else {
                // Return an empty set while leaving categoryFilterIds null for future checks
                return Set.of();
            }
        }

        return categoryFilterIds;
    }

    /**
     * Return a set of sub-category IDs currently being filtered.
     * <br><br>
     * Saved here to persist destructive actions (i.e. screen rotations).
     *
     * @return  Set of sub-category IDs.
     */
    @NonNull
    public Set<Long> getSubCategoryFilterIds() {
        if (null == subCategoryFilterIds) {
            if (null != allSubCategories) {
                subCategoryFilterIds = allSubCategories.stream().map(SubCategoryEntity::getId)
                        .collect(Collectors.toSet());
            } else {
                // Return an empty set while leaving subCategoryFilterIds null for future checks
                return Set.of();
            }
        }

        return subCategoryFilterIds;
    }

    /**
     * Retrieve a single Drill by the associated database ID.
     *
     * @param id    ID of the desired drill.
     * @return      The Drill that maps to the given ID, or null if is doesn't exist.
     */
    @Nullable
    public Drill findDrillById(long id) {
        Drill ret = null;
        List<Drill> allDrills = uiDrillsList.getValue();

        if (null != allDrills) {
            for (Drill drill : allDrills) {
                if (drill.getId() == id) {
                    ret = drill;
                    break;
                }
            }
        }

        return ret;
    }

    public void deleteDrill(@NonNull Drill drill) {
        executor.execute(() -> {
            if (null != uiDrillsList.getValue()) {
                // Must be a new list to trigger submitList() logic
                List<Drill> newDrills = new ArrayList<>(uiDrillsList.getValue());
                repo.deleteDrills(drill);
                newDrills.remove(drill);
                uiDrillsList.postValue(newDrills);
            }
        });
    }

    /**
     * Sort the current ui list of drills by the given {@link SortOrder}.
     * <br><br>
     * Will sort the list of drills using the given sort order, then will refresh the list and the
     * observable will be called again.
     *
     * @param newSortOrder  New order to sort by.
     */
    public void sortDrills(@NonNull SortOrder newSortOrder) {
        if (null == uiDrillsList.getValue()) {
            return;
        }

        sortDrills(uiDrillsList.getValue(), newSortOrder);
    }

    /**
     * Sort the list of drills by the given {@link SortOrder}.
     * <br><br>
     * Will sort the list of drills using the given sort order, then will refresh the list and the
     * observable will be called again.
     *
     * @param listToSort    Drill list to sort then update the UI with.
     * @param newSortOrder  New order to sort by.
     */
    public void sortDrills(@NonNull List<Drill> listToSort, @NonNull SortOrder newSortOrder) {
        // Must be a new list to trigger submitList() logic
        List<Drill> sortedDrills = new ArrayList<>(listToSort);

        sortedDrills.sort((drill1, drill2) -> {
            switch(newSortOrder) {
                case SORT_DATE_ASCENDING:
                    return Long.compare(drill1.getLastDrilled(), drill2.getLastDrilled());
                case SORT_DATE_DESCENDING:
                    return Long.compare(drill2.getLastDrilled(), drill1.getLastDrilled());
                case SORT_NAME_DESCENDING:
                    return drill2.getName().compareTo(drill1.getName());
                case SORT_NAME_ASCENDING:
                    // Fallthrough intentional
                default:
                    return drill1.getName().compareTo(drill2.getName());

            }
        });
        uiDrillsList.postValue(sortedDrills);
        sortOrder = newSortOrder;
    }
}
