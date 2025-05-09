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

package com.damienwesterman.defensedrill.data.local;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.utils.Constants;

import java.util.List;

import lombok.RequiredArgsConstructor;

/**
 * Repository class to interact with the {@link WeeklyHourPolicyEntity} database.
 */
@RequiredArgsConstructor
public class SimulatedAttackRepo {
    private final WeeklyHourPolicyDao weeklyHourPolicyDao;

    @NonNull
    public synchronized List<WeeklyHourPolicyEntity> getAllWeeklyHourPolicies() {
        return this.weeklyHourPolicyDao.getAllWeeklyHourPolicies();
    }

    @NonNull
    public synchronized List<WeeklyHourPolicyEntity> getActivePolicies() {
        return this.weeklyHourPolicyDao.getActivePolicies();
    }

    /**
     * Create or update policies by their weekly hour.
     *
     * @param policies WeeklyHourPolicyEntity objects to create or update.
     * @return boolean if all inserts were successful.
     */
    // TODO: Do this for all insert operations (including nonnull annotation)
    public synchronized boolean insertPolicies(@NonNull WeeklyHourPolicyEntity... policies) {
        int numInserts = this.weeklyHourPolicyDao.insertWeeklyHourPolicy(policies).length;
        return policies.length == numInserts;
    }

    /**
     * Delete policies by their weekly hour. "Deleting" a policy just resets everything for that
     * weekly hour to defaults.
     *
     * @param weeklyHours WeeklyHourPolicyEntity objects to delete.
     * @return boolean if all deletes were successful.
     */
    public synchronized boolean deletePolicies(@NonNull Integer... weeklyHours) {
        WeeklyHourPolicyEntity[] policies = new WeeklyHourPolicyEntity[weeklyHours.length];
        int i = 0;
        for (int weeklyHour : weeklyHours) {
            policies[i++] = WeeklyHourPolicyEntity.builder()
                    .weeklyHour(weeklyHour)
                    .frequency(Constants.SimulatedAttackFrequency.NO_ATTACKS)
                    .active(false)
                    .policyName("")
                    .build();
        }

        return insertPolicies(policies);
    }

    public synchronized void populateDefaultPolicies() {
        final int numPoliciesUpperBound = 24 * 7;
        WeeklyHourPolicyEntity[] policies = new WeeklyHourPolicyEntity[numPoliciesUpperBound];
        for (int i = 0; i < numPoliciesUpperBound; i++) {
            policies[i] = WeeklyHourPolicyEntity.builder()
                    .weeklyHour(i)
                    .frequency(Constants.SimulatedAttackFrequency.NO_ATTACKS)
                    .active(false)
                    .policyName("")
                    .build();
        }

        insertPolicies(policies);
    }
}
