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

package com.damienwesterman.defensedrill.ui.view_models;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;
import com.damienwesterman.defensedrill.utils.Constants;
import com.damienwesterman.defensedrill.utils.DrillGenerator;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * View model for {@link Drill} objects geared towards displaying and modifying a single drill.
 */
@HiltViewModel
public class DrillInfoViewModel extends AndroidViewModel {
    private final MutableLiveData<Drill> currentDrill;
    private List<CategoryEntity> allCategories;
    private List<SubCategoryEntity> allSubCategories;
    private final DrillRepository repo;
    private DrillGenerator drillGenerator;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public DrillInfoViewModel(Application application, DrillRepository repo) {
        super(application);

        currentDrill = new MutableLiveData<>();
        this.repo = repo;
    }

    /**
     * Get the LiveData object to observe for the Drill object.
     *
     * @return LiveData object.
     */
    public LiveData<Drill> getDrill() {
        return currentDrill;
    }

    /**
     * Populate the drill from the database by Drill's ID.
     *
     * @param drillId ID of the drill.
     */
    public void populateDrill(long drillId) {
        executor.execute(() -> currentDrill.postValue(repo.getDrill(drillId).orElse(null)));
    }

    /**
     * Populate a random drill from the database given a category and sub-category ID.
     *
     * @param categoryId    Category ID to filter by.
     * @param subCategoryId Sub-category ID to filter by.
     */
    public void populateDrill(long categoryId, long subCategoryId) {
        executor.execute(() -> {
            List<Drill> drills;
            if (Constants.USER_RANDOM_SELECTION == categoryId &&
                    Constants.USER_RANDOM_SELECTION == subCategoryId) {
                drills = repo.getAllDrills();
            } else if (Constants.USER_RANDOM_SELECTION == categoryId) {
                drills = repo.getAllDrillsBySubCategoryId(subCategoryId);
            } else if (Constants.USER_RANDOM_SELECTION == subCategoryId) {
                drills = repo.getAllDrillsByCategoryId(categoryId);
            } else {
                drills = repo.getAllDrills(categoryId, subCategoryId);
            }
            drillGenerator = new DrillGenerator(drills, new Random());
            currentDrill.postValue(drillGenerator.generateDrill());
        });
    }

    /**
     * Regenerate and select a new random drill.
     * <br><br>
     * Only works if {@link #populateDrill(long, long)} was called previously.
     */
    public void regenerateDrill() {
        if (null != drillGenerator) {
            currentDrill.setValue(drillGenerator.regenerateDrill());
        }
    }

    /**
     * Reset any drills skipped using {@link #regenerateDrill()}.
     * <br><br>
     * Only works if {@link #populateDrill(long, long)} was called previously.
     */
    public void resetSkippedDrills() {
        if (null != drillGenerator) {
            drillGenerator.resetSkippedDrills();
        }
    }

    /**
     * Attempt to add a new drill to the database.
     *
     * @param drill     Drill to attempt to add to the database.
     * @param callback  Callback to call when the update is finished.
     */
    public void saveDrill(Drill drill, OperationCompleteCallback callback) {
        if (null == drill) {
            callback.onFailure("Issue saving Drill");
            return;
        }

        executor.execute(() -> {
           try {
               if (!repo.updateDrills(drill)) {
                   callback.onFailure("Something went wrong");
               } else {
                   currentDrill.postValue(drill);
                   callback.onSuccess();
               }
           } catch (SQLiteConstraintException e) {
               callback.onFailure("Issue saving Drill");
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
    public @Nullable List<CategoryEntity> getAllCategories() {
        return allCategories;
    }

    /**
     * Get the list of all sub-categories in the database.
     * <br><br>
     * {@link #loadAllSubCategories()} should have been called prior otherwise will return null.
     *
     * @return List of SubCategoryEntity objects.
     */
    public @Nullable List<SubCategoryEntity> getAllSubCategories() {
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
}
