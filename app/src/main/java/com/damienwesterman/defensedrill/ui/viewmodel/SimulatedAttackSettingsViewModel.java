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
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SimulatedAttackRepo;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.common.OperationCompleteCallback;
import com.damienwesterman.defensedrill.common.Constants;

import java.util.ArrayList;
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
    /**
     * This a substitute for the Map policiesByName to be used by the ListAdapter. This should be
     * a list in the form of {@code <stringPolicyName, policyEntitiesList>} in ascending order by
     * the policy name for consistency.
     */
    @Getter
    private final MutableLiveData<List<Pair<String, List<WeeklyHourPolicyEntity>>>> uiPoliciesList;
    @Getter
    private List<WeeklyHourPolicyEntity> policies;
    @Getter
    private Map<String, List<WeeklyHourPolicyEntity>> policiesByName;

    @Inject
    public SimulatedAttackSettingsViewModel(@NonNull Application application,
                                            @NonNull DrillRepository drillRepo,
                                            @NonNull SimulatedAttackRepo simulatedAttackRepo) {
        super(application);

        this.simulatedAttackRepo = simulatedAttackRepo;
        this.drillRepo = drillRepo;
        this.uiPoliciesList = new MutableLiveData<>();
        this.policies = new ArrayList<>();
        this.policiesByName = new HashMap<>();
    }

    public void loadPolicies() {
        if (!uiPoliciesList.isInitialized()) {
            new Thread(this::loadAllPoliciesFromDb).start();
        }
    }

    public void populateDefaultPolicies() {
        new Thread(() -> {
            simulatedAttackRepo.populateEmptyDatabase();
            loadAllPoliciesFromDb();
        }).start();
    }

    /**
     * Save policies to the database. This may be an insert or update operation. If it is an update
     * operation, provide a value in policyToUpdate. If it is an insert operation, leave null.
     *
     * @param policies          List of related policies to save.
     * @param policyToUpdate    If this is an update operation, this is the name of the policy to
     *                          update. Necessary in the event the name itself is updated.
     * @param reloadUi          Should we reload the UI by posting the new values in the
     *                          MutableLiveData.
     * @param callback          Operation Complete Callback.
     */
    public void savePolicies(@NonNull List<WeeklyHourPolicyEntity> policies,
                             @Nullable String policyToUpdate,
                             boolean reloadUi,
                             @NonNull OperationCompleteCallback callback) {
        if (!policies.isEmpty()) {
            new Thread(() -> {
                try {
                    if (null != policyToUpdate) {
                        // Check to see if any weekly policies have been removed
                        List<WeeklyHourPolicyEntity> existingPolicies = policiesByName
                            .get(policyToUpdate);
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

    /**
     * Remove the provided weekly hours from the database.
     *
     * @param weeklyHours   List of policies denoted by their weekly hour to remove from the
     *                      database.
     * @param callback      Callback.
     */
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
        this.policies = simulatedAttackRepo.getAllPolicies();
        this.policiesByName = policies.stream()
                .filter(policy -> !policy.getPolicyName().isEmpty())
                .collect(Collectors.groupingBy(WeeklyHourPolicyEntity::getPolicyName));

        List<String> policyNames = new ArrayList<>(policiesByName.keySet());
        policyNames.sort(null);
        uiPoliciesList.postValue(policyNames.stream()
                .map(policyName ->
                        new Pair<>(policyName, policiesByName.get(policyName)))
                .collect(Collectors.toList()));

    }

    /**
     * Check the database to see if any self defense drills exist, denoted by a Category named with
     * {@link Constants#CATEGORY_NAME_SELF_DEFENSE}. Report back using the callback.
     *
     * @param callback Callback that accepts a boolean of whether or not there are self defense
     *                 Drills in the database.
     */
    public void checkForSelfDefenseDrills(@NonNull Consumer<Boolean> callback) {
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
    public void createDefaultSelfDefenseCategory(@Nullable OperationCompleteCallback callback) {
        new Thread(() -> {
            if (drillRepo.getCategory(Constants.CATEGORY_NAME_SELF_DEFENSE).isPresent()) {
                // Category already exists
                if (null != callback) {
                    callback.onSuccess();
                }
                return;
            }

            boolean success = drillRepo.insertCategories(CategoryEntity.builder()
                    .name(Constants.CATEGORY_NAME_SELF_DEFENSE)
                    .description("Drills used for Self Defense")
                    .build());
            if (success) {
                if (null != callback) {
                    callback.onSuccess();
                }
            } else {
                if (null != callback) {
                    callback.onFailure("Failed to create Self Defense Category");
                }
            }
        }).start();
    }
}
