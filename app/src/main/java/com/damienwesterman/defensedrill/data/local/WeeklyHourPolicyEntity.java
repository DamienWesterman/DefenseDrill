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

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.damienwesterman.defensedrill.utils.Constants;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * TODO: doc comments (entity represents a single weekly hour's simulated attack policy)
 */
@Entity(tableName = WeeklyHourPolicyEntity.TABLE_NAME)
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class WeeklyHourPolicyEntity {
    @Ignore
    public static final String TABLE_NAME = "weekly_hour_policy";

    /** Hour of the week this corresponds to, starting with 0 as Sunday at midnight */
    @PrimaryKey
    @ColumnInfo(name = "weekly_hour")
    private int weeklyHour;

    private Constants.SimulatedAttackFrequency frequency;

    private boolean active;

    /**
     * A policy name can be common among related WeeklyHourPolicyEntities, but should not be shared
     * among unrelated policies.
     */
    @ColumnInfo(name = "policy_name")
    private String policyName;

    // TODO: public methods to get/set using day of the week or something?
}
