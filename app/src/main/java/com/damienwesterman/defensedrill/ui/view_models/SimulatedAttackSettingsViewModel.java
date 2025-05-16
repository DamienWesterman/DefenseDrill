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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SimulatedAttackRepo;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;
import com.damienwesterman.defensedrill.utils.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

/**
 * View model for {@link WeeklyHourPolicyEntity} objects, allowing for modification in the database.
 */
@HiltViewModel
public class SimulatedAttackSettingsViewModel extends AndroidViewModel {
    private static final String TAG = SimulatedAttackSettingsViewModel.class.getSimpleName();

    private final SimulatedAttackRepo simulatedAttackRepo;
    private final DrillRepository drillRepo;
    private final MutableLiveData<List<WeeklyHourPolicyEntity>> policies;
    @Getter
    private Map<String, List<WeeklyHourPolicyEntity>> policiesByName;

    @Inject
    public SimulatedAttackSettingsViewModel(@NonNull Application application,
                                            @NonNull DrillRepository drillRepo,
                                            @NonNull SimulatedAttackRepo simulatedAttackRepo) {
        super(application);

        this.simulatedAttackRepo = simulatedAttackRepo;
        this.drillRepo = drillRepo;
        this.policies = new MutableLiveData<>();
        this.policiesByName = new HashMap<>();
    }

    public LiveData<List<WeeklyHourPolicyEntity>> getPolicies() {
        return this.policies;
    }

    public void loadPolicies() {
        if (!policies.isInitialized()) {
            new Thread(this::loadAllPoliciesFromDb).start();
        }
    }

    public void populateDefaultPolicies() {
        new Thread(() -> {
            simulatedAttackRepo.populateEmptyDatabase();
            loadAllPoliciesFromDb();
        }).start();
    }

    public void savePolicies(@NonNull List<WeeklyHourPolicyEntity> policies,
                             boolean updateOperation,
                             boolean reloadUi,
                             @NonNull OperationCompleteCallback callback) {
        if (!policies.isEmpty()) {
            new Thread(() -> {
                try {
                    if (updateOperation) {
                        // Check to see if any weekly policies have been removed
                        List<WeeklyHourPolicyEntity> existingPolicies = policiesByName
                            .get(policies.get(0).getPolicyName());
                        if (null == existingPolicies) {
                            callback.onFailure("Something went wrong");
                            return;
                        }
                        Set<Integer> newPoliciesWeeklyHourSet = policies.stream()
                            .map(WeeklyHourPolicyEntity::getWeeklyHour)
                            .collect(Collectors.toSet());
                        existingPolicies.removeIf(policy ->
                                newPoliciesWeeklyHourSet.contains(policy.getWeeklyHour()));

                        if (!existingPolicies.isEmpty()) {
                            // This mean that some of the previous alarms have been removed
                            boolean success = simulatedAttackRepo.deletePolicies(existingPolicies.stream()
                                    .map(WeeklyHourPolicyEntity::getWeeklyHour)
                                    .toArray(Integer[]::new));

                            if (!success) {
                                callback.onFailure("An error has occurred trying to save alarm.");
                                return;
                            }
                        }
                    }

                    if (simulatedAttackRepo.insertPolicies(policies.toArray(new WeeklyHourPolicyEntity[0]))) {
                        callback.onSuccess();

                        if (reloadUi) {
                            loadAllPoliciesFromDb();
                        }
                    } else {
                        // This shouldn't really happen
                        callback.onFailure("An error has occurred trying to save alarm");
                        Log.e(TAG, "repo.insertPolicies() failed");
                    }
                } catch (SQLiteConstraintException e) {
                    // Not sure how this would happen either
                    callback.onFailure("An error has occurred trying to save alarm");
                    Log.e(TAG, "SQLite exception during repo.insertPolicies()", e);
                }
            }).start();
        }
    }

    public void removePolicies(@NonNull List<Integer> weeklyHours,
                               @NonNull OperationCompleteCallback callback) {
        new Thread(() -> {
            try {
                if (simulatedAttackRepo.deletePolicies(weeklyHours.toArray(new Integer[0]))) {
                    callback.onSuccess();

                    // Re-load policies to update UI
                    loadAllPoliciesFromDb();
                } else {
                    // This shouldn't really happen
                    callback.onFailure("An error has occurred trying to save new alarm");
                    Log.e(TAG, "repo.insertPolicies() failed");
                }
            } catch (SQLiteConstraintException e) {
                // Not sure how this would happen either
                callback.onFailure("An error has occurred trying to save new alarm");
                Log.e(TAG, "SQLite exception during repo.insertPolicies()", e);
            }
        }).start();
    }

    /**
     * Loads all policies from the database and posts the results so the UI callback is called.
     */
    private void loadAllPoliciesFromDb() {
        List<WeeklyHourPolicyEntity> policyEntities = simulatedAttackRepo.getAllPolicies();
        this.policiesByName = policyEntities.stream()
                .filter(policy -> !policy.getPolicyName().isEmpty())
                .collect(Collectors.groupingBy(WeeklyHourPolicyEntity::getPolicyName));
        policies.postValue(policyEntities);
    }

    /**
     * Check the database to see if any self defense drills exist, denoted by a Category named with
     * {@link Constants#CATEGORY_NAME_SELF_DEFENSE}. Report back using the callback.
     *
     * @param callback Callback that accepts a boolean of whether or not there are self defense
     *                 Drills in the database.
     */
    public void checkForSelfDefenseDrills(Consumer<Boolean> callback) {
        new Thread(() -> {
            Optional<CategoryEntity> optSelfDefenseCategory =
                    drillRepo.getCategory(Constants.CATEGORY_NAME_SELF_DEFENSE);
            if (!optSelfDefenseCategory.isPresent()) {
                // No category
                callback.accept(false);
                return;
            }

            List<Drill> selfDefenseDrills =
                    drillRepo.getAllDrillsByCategoryId(optSelfDefenseCategory.get().getId());

            callback.accept(!selfDefenseDrills.isEmpty());
        }).start();
    }

    /**
     * Create a default Self Defense Category.
     *
     * @param callback OperationCompleteCallback.
     */
    public void createDefaultSelfDefenseCategory(OperationCompleteCallback callback) {
        new Thread(() -> {
            if (drillRepo.getCategory(Constants.CATEGORY_NAME_SELF_DEFENSE).isPresent()) {
                // Category already exists
                callback.onSuccess();
                return;
            }

            boolean success = drillRepo.insertCategories(CategoryEntity.builder()
                    .name(Constants.CATEGORY_NAME_SELF_DEFENSE)
                    .description("Drills used for Self Defense")
                    .build());
            if (success) {
                callback.onSuccess();
            } else {
                callback.onFailure("Failed to create Self Defense Category");
            }
        }).start();
    }
}
