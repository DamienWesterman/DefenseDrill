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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.damienwesterman.defensedrill.data.remote.ApiRepo;
import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
import com.damienwesterman.defensedrill.data.remote.dto.InstructionsDTO;
import com.damienwesterman.defensedrill.data.remote.dto.RelatedDrillDTO;
import com.damienwesterman.defensedrill.common.OperationCompleteCallback;
import com.damienwesterman.defensedrill.common.Constants;
import com.damienwesterman.defensedrill.domain.DrillGenerator;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.Getter;
import retrofit2.HttpException;

/**
 * View model for {@link Drill} objects geared towards displaying and modifying a single drill.
 */
@HiltViewModel
public class DrillInfoViewModel extends AndroidViewModel {
    private static final String TAG = DrillInfoViewModel.class.getSimpleName();

    @Getter
    private final MutableLiveData<Drill> uiCurrentDrill;
    @Nullable
    private DrillDTO drillDTO;
    @Getter
    private final MutableLiveData<List<InstructionsDTO>> uiInstructionsList;
    @Getter
    private final MutableLiveData<List<RelatedDrillDTO>> uiRelatedDrillsList;
    @Nullable
    private List<CategoryEntity> allCategories;
    @Nullable
    private List<SubCategoryEntity> allSubCategories;
    private final DrillRepository drillRepo;
    private final ApiRepo apiRepo;
    @Nullable
    private DrillGenerator drillGenerator;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public DrillInfoViewModel(Application application, DrillRepository drillRepo, ApiRepo apiRepo) {
        super(application);

        uiCurrentDrill = new MutableLiveData<>();
        uiInstructionsList = new MutableLiveData<>();
        uiRelatedDrillsList = new MutableLiveData<>();
        this.drillRepo = drillRepo;
        this.apiRepo = apiRepo;
    }

    /**
     * Populate the drill from the database by Drill's ID.
     *
     * @param drillId ID of the drill.
     */
    public void populateDrill(long drillId) {
        executor.execute(() -> uiCurrentDrill.postValue(drillRepo.getDrill(drillId).orElse(null)));
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
                drills = drillRepo.getAllDrills();
            } else if (Constants.USER_RANDOM_SELECTION == categoryId) {
                drills = drillRepo.getAllDrillsBySubCategoryId(subCategoryId);
            } else if (Constants.USER_RANDOM_SELECTION == subCategoryId) {
                drills = drillRepo.getAllDrillsByCategoryId(categoryId);
            } else {
                drills = drillRepo.getAllDrills(categoryId, subCategoryId);
            }
            drillGenerator = new DrillGenerator(drills, new Random());
            uiCurrentDrill.postValue(drillGenerator.generateDrill());
        });
    }

    /**
     * Regenerate and select a new random drill.
     * <br><br>
     * Only works if {@link #populateDrill(long, long)} was called previously.
     */
    public void regenerateDrill() {
        if (null != drillGenerator) {
            uiCurrentDrill.setValue(drillGenerator.regenerateDrill());
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
     * @param drill         Drill to attempt to add to the database.
     * @param reloadScreen  Should we post the results for force a screen re-load?
     * @param callback      Callback to call when the update is finished.
     */
    public void saveDrill(@NonNull Drill drill, boolean reloadScreen,
                          @Nullable OperationCompleteCallback callback) {
        executor.execute(() -> {
           try {
               if (!drillRepo.updateDrills(drill)) {
                   if (null != callback) {
                       callback.onFailure("Something went wrong");
                   }
               } else {
                   if (reloadScreen) {
                       uiCurrentDrill.postValue(drill);
                   }
                   if (null != callback) {
                       callback.onSuccess();
                   }
               }
           } catch (SQLiteConstraintException e) {
               if (null != callback) {
                   callback.onFailure("Issue saving Drill");
               }
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
            executor.execute(() -> allCategories = drillRepo.getAllCategories());
        }
    }

    /**
     * Load all sub-categories from the database. Should be called before
     * {@link #getAllSubCategories()}.
     */
    public void loadAllSubCategories() {
        if (null == allSubCategories) {
            executor.execute(() -> allSubCategories = drillRepo.getAllSubCategories());
        }
    }

    /**
     * Get the DrillDTO received from the backend.
     * <br><br>
     * {@link #loadNetworkLinks(Runnable, Consumer)} should have been called prior otherwise will
     * return null.
     * @return DrillDTO object.
     */
    @Nullable
    public DrillDTO getDrillDTO() {
        return this.drillDTO;
    }

    /**
     * Fetch and load instructions and related drills for the Drill. Drill has to have been
     * initialized otherwise nothing will happen.
     *
     * @param unauthorizedCallback  Callback for when 401 is returned.
     * @param failureCallback       Callback for when the network request fails.
     */
    public void loadNetworkLinks(Runnable unauthorizedCallback, Consumer<String> failureCallback) {
        if (null != uiCurrentDrill.getValue() && null != uiCurrentDrill.getValue().getServerDrillId()) {
            Disposable disposable = apiRepo.getDrill(uiCurrentDrill.getValue().getServerDrillId())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    drill -> {
                        this.drillDTO = drill;
                        uiInstructionsList.postValue(drill.getInstructions());
                        uiRelatedDrillsList.postValue(drill.getRelatedDrills());
                    },
                    throwable -> handleLoadNetworkLinksFailure(throwable,
                            unauthorizedCallback,
                            failureCallback)
                );
        }
    }

    /**
     * Handle failure to retrieve network links.
     *
     * @param throwable             Throwable given by Retrofit/RxJava.
     * @param unauthorizedCallback  Callback for when 401 is returned.
     * @param failureCallback       Callback for when the network request fails.
     */
    private void handleLoadNetworkLinksFailure(@NonNull Throwable throwable,
                                               @NonNull Runnable unauthorizedCallback,
                                               @NonNull Consumer<String> failureCallback) {
        if (throwable instanceof HttpException) {
            // getLocalizedMessage(): HTTP 401 Unauthorized
            HttpException httpException = (HttpException) throwable;

            switch (httpException.code()) {
                case HttpsURLConnection.HTTP_UNAUTHORIZED:
                    unauthorizedCallback.run();
                    break;
                case HttpsURLConnection.HTTP_NOT_FOUND:
                    // A little weird, but not exactly an error we can do anything about
                    this.drillDTO = null;
                    uiInstructionsList.postValue(null);
                    uiRelatedDrillsList.postValue(null);
                    break;
                default:
                    // Should not get here
                    Log.e(TAG, "Received unexpected HttpException: "
                            + httpException.getMessage());
                    failureCallback.accept("Server issue, please try again later");
                    break;
            }
        } else if (throwable instanceof IllegalArgumentException) {
            // Thrown by ApiRepo if the JWT from SharedPrefs is empty
            // This is actually acceptable, means user has not signed in, no error message necessary
            this.drillDTO = null;
            uiInstructionsList.postValue(null);
            uiRelatedDrillsList.postValue(null);
        } else if (throwable instanceof SocketTimeoutException) {
            // getLocalizedMessage(): failed to connect to your.server.org/1.1.1.1 (port 99999) from /2.2.2.2 (port 99999) after 10000ms
            failureCallback.accept("Issue connecting to the server, try again later");
        } else {
            failureCallback.accept("An unexpected error has occurred");
        }
    }

    /**
     * Find the local Drill ID by a Drill's server ID.
     *
     * @param serverId Server ID of the drill to find.
     * @param callback Callback that consumes the found Drill's ID, or -1 if not found.
     */
    public void findDrillIdByServerId(@NonNull Long serverId,
                                      @NonNull Consumer<Long> callback) {
        executor.execute(() -> {
            Optional<Drill> optDrill = drillRepo.getDrillByServerId(serverId);
            Long localDrillId;
            if (optDrill.isPresent()) {
                localDrillId = optDrill.get().getId();
            } else {
                localDrillId = null;
            }

            callback.accept(localDrillId);
        });
    }
}
