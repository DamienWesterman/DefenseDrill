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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

/**
 * TODO: DOC COMMENTS
 */
@RequiredArgsConstructor
public class SimulatedAttackRepo {
    private final WeeklyHourPolicyDao weeklyHourPolicyDao;

    @NonNull
    public synchronized List<WeeklyHourPolicyEntity> getAllWeeklyHourPolicies() {
        return this.weeklyHourPolicyDao.getAllWeeklyHourPolicies();
    }

    @NonNull
    Map<String, List<WeeklyHourPolicyEntity>> getAllPoliciesGroupedByName() {
        return this.weeklyHourPolicyDao.getAllPoliciesOrderedByPolicyName().stream()
                .collect(Collectors.groupingBy(WeeklyHourPolicyEntity::getPolicyName));
    }

    /**
     * TODO: Doc comments
     * @param policies
     * @return
     */
    // TODO: Do this for all insert operations (including nonnull annotation)
    public synchronized boolean insertPolicies(@NonNull WeeklyHourPolicyEntity... policies) {
        int numInserts = this.weeklyHourPolicyDao.insertWeeklyHourPolicy(policies).length;
        return policies.length == numInserts;
    }
}
