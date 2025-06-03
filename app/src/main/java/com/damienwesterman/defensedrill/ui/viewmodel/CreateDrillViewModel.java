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
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.util.OperationCompleteCallback;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

/**
 * View model for {@link Drill} objects geared towards creation of a new Drill.
 */
@HiltViewModel
public class CreateDrillViewModel extends AndroidViewModel {
    private final DrillRepository repo;
    @Nullable
    private List<CategoryEntity> allCategories;
    @Nullable
    private List<SubCategoryEntity> allSubCategories;
    @NonNull
    @Getter
    private final Set<CategoryEntity> checkedCategoryEntities = new HashSet<>();
    @NonNull
    @Getter
    private final Set<SubCategoryEntity> checkedSubCategoryEntities = new HashSet<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Inject
    public CreateDrillViewModel(@NonNull Application application, DrillRepository repo) {
        super(application);

        this.repo = repo;
        loadAllCategories();
        loadAllSubCategories();
    }

    /**
     * Attempt to add a new drill to the database.
     *
     * @param drill     Drill to attempt to add to the database.
     * @param callback  Callback to call when the insert is finished.
     */
    public void saveDrill(@NonNull Drill drill, @NonNull OperationCompleteCallback callback) {
        executor.execute(() -> {
            try {
                if (!repo.insertDrills(drill)) {
                    callback.onFailure("Something went wrong");
                } else {
                    callback.onSuccess();
                }
            } catch (SQLiteConstraintException e) {
                callback.onFailure("ERROR: Name already exists");
            }
        });
    }

    /**
     * Get the list of all categories in the database.
     * <br><br>
     * May return null if it has not loaded from the database yet.
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
     * May return null if it has not loaded from the database yet.
     *
     * @return List of SubCategoryEntity objects.
     */
    @Nullable
    public List<SubCategoryEntity> getAllSubCategories() {
        return allSubCategories;
    }

    /**
     * Load all categories from the database.
     */
    private void loadAllCategories() {
        if (null == allCategories) {
            executor.execute(() -> allCategories = repo.getAllCategories());
        }
    }

    /**
     * Load all sub-categories from the database.
     */
    private void loadAllSubCategories() {
        if (null == allSubCategories) {
            executor.execute(() -> allSubCategories = repo.getAllSubCategories());
        }
    }
}
