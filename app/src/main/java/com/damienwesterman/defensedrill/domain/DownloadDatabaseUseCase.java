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
 * Copyright 2025 Damien Westerman
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

package com.damienwesterman.defensedrill.domain;

import android.util.Log;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.data.remote.ApiRepo;
import com.damienwesterman.defensedrill.data.remote.dto.CategoryDTO;
import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
import com.damienwesterman.defensedrill.data.remote.dto.SubCategoryDTO;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * TODO DOC COMMENTS
 */
@ViewModelScoped
public class DownloadDatabaseUseCase {
    // TODO: Make sure to handle all response codes somewhere - somehow (such as 500, 401)
    // TODO: Make sure to check first for existing names (maybe create a db method to get all by list of names)
    // TODO: Make sure to check first for existing server IDs (maybe create a db method to get all by list of server IDs)
    // TODO: Call it from the UI with proper callbacks and things (progress widget, feedback, etc.)
    // TODO: Test what happens when we fail (so network issue -unplug computer, no internet, bad jwt, database issue, or something)
    // TODO: Make sure to implement the disposable properly
    // TODO: Test the timing on everything, how long does each part of this save operation take and where can we cut time and optimize things
    private final ApiRepo apiRepo;
    private final DrillRepository drillRepo;
    private final SharedPrefs sharedPrefs;
    /** Map of CategoryEntities by their ServerId */
    private Map<Long, CategoryEntity> categoryMap;
    /** Map of SubCategoryEntities by their ServerId */
    private Map<Long, SubCategoryEntity> subCategoryMap;

    @Inject
    public DownloadDatabaseUseCase(ApiRepo apiRepo, DrillRepository drillRepo,
                                   SharedPrefs sharedPrefs) {
        this.apiRepo = apiRepo;
        this.drillRepo = drillRepo;
        this.sharedPrefs = sharedPrefs;
        this.categoryMap = Map.of();
        this.subCategoryMap = Map.of();
    }

    public void execute() {
        // TODO: Add a callback or something and call it
// TODO: Remove (..obviously)
drillRepo.deleteDrills(drillRepo.getAllDrills().toArray(new Drill[0]));
drillRepo.deleteSubCategories(drillRepo.getAllSubCategories().toArray(new SubCategoryEntity[0]));
drillRepo.deleteCategories(drillRepo.getAllCategories().toArray(new CategoryEntity[0]));
        Disposable disposable = loadCategoriesFromDatabase()
                .flatMap(categories -> loadSubCategoriesFromDatabase())
                .flatMap(subCategories -> loadDrillsFromDatabase())
                .subscribe(
                        drills ->  {
                            Log.i("DxTag", "Everything is loaded!");
                            sharedPrefs.setLastDrillUpdateTime(System.currentTimeMillis());
                        },
                        throwable -> Log.e("DxTag", "We have an issue: " + throwable.getLocalizedMessage())
                );
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    private Observable<List<DrillDTO>> loadDrillsFromDatabase() {
        // TODO: properly implement
        return apiRepo.getAllDrills()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnNext(
                    drills -> drillRepo.insertDrills(drills.stream()
                            .map(drill -> drill.toDrill(categoryMap, subCategoryMap))
                            .toArray(Drill[]::new))
            );
    }

    private Observable<List<CategoryDTO>> loadCategoriesFromDatabase() {
        // TODO: properly implement
        return apiRepo.getAllCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnNext(
                    categories -> {
                        // This will throw if there are any issues
                        drillRepo.insertCategories(categories.stream()
                                .map(CategoryDTO::toCategoryEntity)
                                .toArray(CategoryEntity[]::new));

                        List<CategoryEntity> savedCategories = drillRepo.getAllCategories();
                        categoryMap = savedCategories.stream()
                                .filter(category -> null != category.getServerId())
                                .collect(Collectors.toMap(CategoryEntity::getServerId, Function.identity()));
                    }
            );
    }

    private Observable<List<SubCategoryDTO>> loadSubCategoriesFromDatabase() {
        // TODO: properly implement
        return apiRepo.getAllSubCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnNext(
                    subCategories -> {
                        drillRepo.insertSubCategories(subCategories.stream()
                                .map(SubCategoryDTO::toSubCategoryEntity)
                                .toArray(SubCategoryEntity[]::new));

                        List<SubCategoryEntity> savedSubCategories = drillRepo.getAllSubCategories();
                        subCategoryMap = savedSubCategories.stream()
                                .filter(subCategory -> null != subCategory.getServerId())
                                .collect(Collectors.toMap(SubCategoryEntity::getServerId, Function.identity()));
                    }
            );
    }
}
