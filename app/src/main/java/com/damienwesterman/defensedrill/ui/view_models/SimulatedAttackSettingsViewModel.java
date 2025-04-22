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

import com.damienwesterman.defensedrill.data.local.SimulatedAttackRepo;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * TODO: Doc comments
 */
@HiltViewModel
public class SimulatedAttackSettingsViewModel extends AndroidViewModel {
    private static final String TAG = SimulatedAttackSettingsViewModel.class.getSimpleName();

    private final SimulatedAttackRepo repo;
    private final MutableLiveData<List<WeeklyHourPolicyEntity>> policies;
    // TODO: Also make sure to have a mutable live data of a map of list of policies grouped by policy name for when needed (excluding blank)
    // TODO: Maybe use ^^ for checking when groups etc exist already, could be easier

    @Inject
    public SimulatedAttackSettingsViewModel(@NonNull Application application,
                                            SimulatedAttackRepo repo) {
        super(application);

        this.repo = repo;
        this.policies = new MutableLiveData<>();
    }

    public LiveData<List<WeeklyHourPolicyEntity>> getPolicies() {
        return this.policies;
    }

    public void loadPolicies() {
        if (!policies.isInitialized()) {
            new Thread(() -> policies.postValue(repo.getAllWeeklyHourPolicies())).start();
        }
    }

    public void savePolicies(@NonNull List<WeeklyHourPolicyEntity> policies,
                             @NonNull OperationCompleteCallback callback) {
        if (!policies.isEmpty()) {
            new Thread(() -> {
                try {
                    if (repo.insertPolicies(policies.toArray(new WeeklyHourPolicyEntity[0]))) {
                        callback.onSuccess(); // TODO: in the UI's callback here they should hide the recyclerView until it is loaded again

                        // Re-load policies to update UI
                        this.policies.postValue(repo.getAllWeeklyHourPolicies());
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
    }

    public void removePolicies(@NonNull List<Integer> weeklyHours,
                               @NonNull OperationCompleteCallback callback) {
        new Thread(() -> {
            try {
            if (repo.deletePolicies(weeklyHours.toArray(new Integer[0]))) {
                callback.onSuccess();

                // Re-load policies to update UI
                policies.postValue(repo.getAllWeeklyHourPolicies());
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
}
