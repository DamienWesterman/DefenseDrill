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

package com.damienwesterman.defensedrill.ui.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;

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
    private final MutableLiveData<List<Drill>> drills;
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
    @Nullable
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
        drills = new MutableLiveData<>();
        sortOrder = SortOrder.SORT_NAME_ASCENDING;
    }

    /**
     * Get the LiveData object to observe for the list of Drill objects.
     *
     * @return LiveData object.
     */
    public LiveData<List<Drill>> getDrills() {
        return drills;
    }

    /**
     * Populate our list of Drills if it has not already been done.
     */
    public void populateDrills() {
        if (null == drills.getValue()) {
            rePopulateDrills();
        }
    }

    /**
     * Force re-load  the Drills from the database, even if they are already loaded.
     */
    public void rePopulateDrills() {
        executor.execute(() -> drills.postValue(repo.getAllDrills().stream()
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
        executor.execute(() -> drills.postValue(repo.getAllDrills(categoryIds, subCategoryIds).stream()
                .filter(Drill::isKnownDrill)
                .collect(Collectors.toList())));
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
            if (allCategories != null) {
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
            if (allSubCategories != null) {
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
        List<Drill> allDrills = drills.getValue();

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
            List<Drill> newDrills = drills.getValue();
            repo.deleteDrills(drill);
            if (newDrills != null) {
                newDrills.remove(drill);
                drills.postValue(newDrills);
            }
        });
    }

    /**
     * Sort the list of drills by the given {@link SortOrder}.
     * <br><br>
     * Will sort the list of drills using the given sort order, then will refresh the list and the
     * observable will be called again. Causes no effect if the new sort order is the same as the
     * old.
     *
     * @param newSortOrder New order to sort by.
     */
    public void setSortOrder(@NonNull SortOrder newSortOrder) {
        List<Drill> sortedDrills = drills.getValue();

        if (null != sortedDrills) {
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
            drills.postValue(sortedDrills);
            sortOrder = newSortOrder;
        }
    }
}
