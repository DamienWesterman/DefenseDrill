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

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.data.remote.ApiRepo;
import com.damienwesterman.defensedrill.data.remote.dto.CategoryDTO;
import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
import com.damienwesterman.defensedrill.data.remote.dto.SubCategoryDTO;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * Use Case to Download all Drills, Categories, and SubCategories from the server and save them to
 * local storage.
 */
public class DownloadDatabaseUseCase {
    private static final String TAG = DownloadDatabaseUseCase.class.getSimpleName();

    private final ApiRepo apiRepo;
    private final DrillRepository drillRepo;
    private final SharedPrefs sharedPrefs;
    /** Map of CategoryEntities by their ServerId */
    private Map<Long, CategoryEntity> categoryMap;
    /** Map of SubCategoryEntities by their ServerId */
    private Map<Long, SubCategoryEntity> subCategoryMap;
    private Disposable disposable;

    @Inject
    public DownloadDatabaseUseCase(ApiRepo apiRepo, DrillRepository drillRepo,
                                   SharedPrefs sharedPrefs) {
        this.apiRepo = apiRepo;
        this.drillRepo = drillRepo;
        this.sharedPrefs = sharedPrefs;
        this.categoryMap = Map.of();
        this.subCategoryMap = Map.of();
        disposable = null;
    }

    /**
     * Download and save all Drills, Categories, and SubCategories from the server.
     *
     * @param callback Callback
     */
    public void download(OperationCompleteCallback callback) {
        disposable = loadCategoriesFromServer()
            .flatMap(categories -> loadSubCategoriesFromServer())
            .flatMap(subCategories -> loadDrillsFromServer())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                drills ->  {
                    sharedPrefs.setLastDrillUpdateTime(System.currentTimeMillis());
                    callback.onSuccess();
                    disposable = null;
                },
                throwable -> {
                    callback.onFailure(extractErrorMessage(throwable));
                    disposable = null;
                }
            );
    }

    /**
     * Cancel and stop the download/save operation.
     * <br><br>
     * NOTE: This does not reverse any of the operation that has already been performed.
     */
    public void cancel() {
        if (null != disposable && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Return an observable that loads all drills from the server and returns them locally.
     *
     * @return Observable for a List of DrillDTO objects.
     */
    private Observable<List<DrillDTO>> loadDrillsFromServer() {
        return apiRepo.getAllDrills()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnNext(
                drills -> {
                    List<Drill> existingDrills = drillRepo.getAllDrills();
                    Map<String, Drill> existingNamesMap = existingDrills.stream()
                                    .collect(Collectors.toMap(Drill::getName, Function.identity()));
                    Map<Long, Drill> drillServerIdMap = existingDrills.stream()
                            .filter(drill -> null != drill.getServerDrillId())
                            .collect(Collectors.toMap(Drill::getServerDrillId, Function.identity()));
                    List<Drill> drillsToUpdate = new ArrayList<>();

                    /*
                    We want to filter this list so that certain categories that may already
                    be in the database are not persisted again, causing issue. We are filtering
                    in place as the list ends in this method and is not used again.
                     */
                    drills.removeIf(drill -> {
                        if (drillServerIdMap.containsKey(drill.getId())) {
                            /*
                            We have already downloaded this from the API, should be updated via
                            the update from API functionality
                             */
                            return true;
                        }

                        if (existingNamesMap.containsKey(drill.getName())) {
                            Drill duplicateDrill = existingNamesMap.get(drill.getName());
                            if (null != duplicateDrill
                                    && null != duplicateDrill.getServerDrillId()) {
                                // If the name exists and is not assigned a server ID, update it
                                duplicateDrill.setServerDrillId(drill.getId());
                                drillsToUpdate.add(duplicateDrill);
                            }

                            return true;
                        }

                        return false;
                    });

                    // These will throw if there are any issues
                    if (!drills.isEmpty()) {
                        drillRepo.insertDrills(drills.stream()
                                .map(drill -> drill.toDrill(categoryMap, subCategoryMap))
                                .toArray(Drill[]::new));
                    }
                    if (!drillsToUpdate.isEmpty()) {
                        drillRepo.updateDrills(
                                drillsToUpdate.toArray(new Drill[0]));
                    }
                }
            );
    }

    /**
     * Return an observable that loads all categories from the server and returns them locally.
     *
     * @return Observable for a List of CategoryDTO objects.
     */
    private Observable<List<CategoryDTO>> loadCategoriesFromServer() {
        return apiRepo.getAllCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnNext(
                categories -> {
                    List<CategoryEntity> existingCategories = drillRepo.getAllCategories();
                    Map<String, CategoryEntity> existingNamesMap = existingCategories.stream()
                            .collect(Collectors.toMap(CategoryEntity::getName, Function.identity()));
                    // Map the categories by the server ID for efficient lookup
                    categoryMap = existingCategories.stream()
                            .filter(category -> null != category.getServerId())
                            .collect(Collectors.toMap(CategoryEntity::getServerId, Function.identity()));
                    List<CategoryEntity> categoriesToUpdate = new ArrayList<>();

                    /*
                    We want to filter this list so that certain categories that may already
                    be in the database are not persisted again, causing issue. We are filtering
                    in place as the list ends in this method and is not used again.
                     */
                    categories.removeIf(category -> {
                        if (categoryMap.containsKey(category.getId())) {
                            /*
                            We have already downloaded this from the API, should be updated via
                            the update from API functionality
                             */
                            return true;
                        }

                        if (existingNamesMap.containsKey(category.getName())) {
                            CategoryEntity duplicateCategory = existingNamesMap.get(category.getName());
                            if (null != duplicateCategory
                                    && null != duplicateCategory.getServerId()) {
                                // If the name exists and is not assigned a server ID, update it
                                duplicateCategory.setServerId(category.getId());
                                categoriesToUpdate.add(duplicateCategory);
                            }
                            return true;
                        }

                        return false;
                    });

                    // These will throw if there are any issues
                    if (!categories.isEmpty()) {
                        drillRepo.insertCategories(categories.stream()
                                .map(CategoryDTO::toCategoryEntity)
                                .toArray(CategoryEntity[]::new));
                    }
                    if (!categoriesToUpdate.isEmpty()) {
                        drillRepo.updateCategories(
                                categoriesToUpdate.toArray(new CategoryEntity[0]));
                    }

                    if (!categories.isEmpty() || !categoriesToUpdate.isEmpty()) {
                        // Only need to update categoryMap if we added to the database
                        categoryMap = drillRepo.getAllCategories().stream()
                                .filter(category -> null != category.getServerId())
                                .collect(Collectors.toMap(CategoryEntity::getServerId, Function.identity()));
                    }
                }
            );
    }

    /**
     * Return an observable that loads all sub-categories from the server and returns them locally.
     *
     * @return Observable for a List of SubCategoryDTO objects.
     */
    private Observable<List<SubCategoryDTO>> loadSubCategoriesFromServer() {
        return apiRepo.getAllSubCategories()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .doOnNext(
                subCategories -> {
                    List<SubCategoryEntity> existingSubCategories = drillRepo.getAllSubCategories();
                    Map<String, SubCategoryEntity> existingNamesMap = existingSubCategories.stream()
                            .collect(Collectors.toMap(SubCategoryEntity::getName, Function.identity()));
                    // Map the subCategories by the server ID for efficient lookup
                    subCategoryMap = existingSubCategories.stream()
                            .filter(subCategory -> null != subCategory.getServerId())
                            .collect(Collectors.toMap(SubCategoryEntity::getServerId, Function.identity()));
                    List<SubCategoryEntity> subCategoriesToUpdate = new ArrayList<>();

                    /*
                    We want to filter this list so that certain sub-categories that may already
                    be in the database are not persisted again, causing issue. We are filtering
                    in place as the list ends in this method and is not used again.
                     */
                    subCategories.removeIf((subCategory -> {
                        if (subCategoryMap.containsKey(subCategory.getId())) {
                            /*
                            We have already downloaded this from the API, should be updated via
                            the update from API functionality
                             */
                            return true;
                        }

                        if (existingNamesMap.containsKey(subCategory.getName())) {
                            SubCategoryEntity duplicateSubCategory = existingNamesMap
                                    .get(subCategory.getName());
                            if (null != duplicateSubCategory
                                    && null != duplicateSubCategory.getServerId()) {
                                //If the name exists and is not assigned a server ID, update it
                                duplicateSubCategory.setServerId(subCategory.getId());
                                subCategoriesToUpdate.add(duplicateSubCategory);
                            }

                            return true;
                        }

                        return false;
                    }));

                    // These will throw if there are any issues
                    if (!subCategories.isEmpty()) {
                        drillRepo.insertSubCategories(subCategories.stream()
                                .map(SubCategoryDTO::toSubCategoryEntity)
                                .toArray(SubCategoryEntity[]::new));
                    }
                    if (!subCategoriesToUpdate.isEmpty()) {
                        drillRepo.updateSubCategories(
                                subCategoriesToUpdate.toArray(new SubCategoryEntity[0]));
                    }

                    if (!subCategories.isEmpty() || !subCategoriesToUpdate.isEmpty()) {
                        // Only need to update subCategoryMap if we added to the database
                        subCategoryMap = drillRepo.getAllSubCategories().stream()
                                .filter(subCategory -> null != subCategory.getServerId())
                                .collect(Collectors.toMap(SubCategoryEntity::getServerId, Function.identity()));
                    }
                }
            );
    }

    /**
     * Extract an error message from the throwable.
     *
     * @param throwable Throwable
     * @return String error message
     */
    @NonNull
    private String extractErrorMessage(@NonNull Throwable throwable) {
        String errorMessage;

        // TODO: Subsequent PR - Double check what the return type looks like in a 201, can be seen when we implement the update functionality
        if (throwable instanceof HttpException) {
            // getLocalizedMessage(): HTTP 401 Unauthorized
            HttpException httpException = (HttpException) throwable;

            if (httpException.code() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
                errorMessage = "Unauthorized, please log in again";
            } else {
                // Should not get here
                Log.e(TAG, "Received unexpected HttpException: "
                        + httpException.getMessage());
                errorMessage = "Server issue, please try again later";
            }
        } else if (throwable instanceof IllegalArgumentException) {
            // Thrown by ApiRepo if the JWT from SharedPrefs is empty
            errorMessage = "Unauthorized, please log in again";
        } else if (throwable instanceof SocketTimeoutException) {
            // getLocalizedMessage(): failed to connect to your.server.org/1.1.1.1 (port 99999) from /2.2.2.2 (port 99999) after 10000ms
            errorMessage = "Issue connecting to the server, try again later";
        } else if (throwable instanceof SQLiteConstraintException) {
            // getLocalizedMessage(): UNIQUE constraint failed: drill.name (code 2067 SQLITE_CONSTRAINT_UNIQUE[2067])
            // This shouldn't happen, if it does it represents a logic error
            Log.e(TAG, "Sqlite issue: " + throwable.getLocalizedMessage());
            errorMessage = "Issue saving drills to your phone";
        } else {
            errorMessage = "An unexpected error has occurred";
        }

        return errorMessage;
    }
}
