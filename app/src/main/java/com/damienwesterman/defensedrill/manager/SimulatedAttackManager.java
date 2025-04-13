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

package com.damienwesterman.defensedrill.manager;

import android.content.Context;
import android.content.Intent;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.utils.Constants;
import com.damienwesterman.defensedrill.utils.DrillGenerator;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * TODO: Doc comments
 */
public class SimulatedAttackManager {
    // TODO: START HERE FIXME: make sure that we can call all of the methods below and make sure that they work and are actually called
    // TODO: implement the following
    // TODO: https://developer.android.com/reference/android/app/AlarmManager#setAndAllowWhileIdle(int,%20long,%20android.app.PendingIntent)
    private static final String CATEGORY_NAME_SELF_DEFENSE = "Self Defense";

    private final Context context;
    private final DrillRepository drillRepo;
    private final DefenseDrillNotificationManager notificationManager;
    private final SharedPrefs sharedPrefs;

    @Inject
    public SimulatedAttackManager(@ApplicationContext Context context,
                                  DrillRepository drillRepo,
                                  DefenseDrillNotificationManager notificationManager,
                                  SharedPrefs sharedPrefs) {
        this.context = context;
        this.drillRepo = drillRepo;
        this.notificationManager = notificationManager;
        this.sharedPrefs = sharedPrefs;
    }

    // =============================================================================================
    // Static Manager Control Methods
    // =============================================================================================

    /**
     * TODO: Doc comments (can launch for first time or after settings have changed)
     * @param context
     */
    public static void start(Context context) {
        Intent intent = new Intent(Constants.INTENT_ACTION_START_SIMULATED_ATTACK_MANAGER);
        intent.setPackage(context.getPackageName());
        context.sendBroadcast(intent);
    }

    /**
     * TODO: Doc comments
     * @param context
     */
    public static void restart(Context context) {
        // TODO: Properly implement
    }

    // TODO: Doc comments
    public static void stop(Context context) {
        // TODO: Properly implement
    }

    // =============================================================================================
    // Public Methods
    // =============================================================================================
    // TODO: Doc comments
    public void scheduleSimulatedAttack() {
        // TODO: Implement timer using setAndAllowWhileIdle
    }

    public void stopSimulatedAttacks() {
        // TODO: Implement and cancel any existing alarms?
    }

    // TODO: Doc comments
    public void simulateAttack() {
        Optional<CategoryEntity> optSelfDefenseCategory =
                drillRepo.getCategory(CATEGORY_NAME_SELF_DEFENSE);
        if (!optSelfDefenseCategory.isPresent()) {
            // TODO: What do we want to do here, send an intent to someone, set a flag, set shared prefs and have someone report it? Or just leave it be? Check it when the user activates the notification system for simulated attacks?
            return;
        }

        List<Drill> drills = drillRepo.getAllDrillsByCategoryId(optSelfDefenseCategory.get().getId());
        DrillGenerator drillGenerator = new DrillGenerator(drills, new Random());
        Optional<Drill> optDrill = Optional.ofNullable(drillGenerator.generateDrill());
        optDrill.ifPresent(notificationManager::notifySimulatedAttack);

        scheduleSimulatedAttack();
    }
}