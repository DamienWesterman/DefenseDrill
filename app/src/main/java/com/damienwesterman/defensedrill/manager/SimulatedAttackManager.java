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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.SimulatedAttackRepo;
import com.damienwesterman.defensedrill.utils.Constants;
import com.damienwesterman.defensedrill.utils.DrillGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * TODO: Doc comments
 */
public class SimulatedAttackManager {
    // TODO: Implement the actual algorithm for selecting the next alarm
    /*
        Algorithm/Data structure ideas:
            - During a time frame, if it is the first alarm in the time frame, it can be triggered at ANY time
                - So we don't have to wait like 15 min if it's the first one in a 1pm-2pm time slot
                - So it can be triggered anywhere from 1pm-1:18pm
            - I'm thinking get the next trigger in this time frame, THEN if it is out of this time frame move on to deciding the next (which will be fit the logic above)
            - So that seems pretty simple, get a random int using the current time zones frequency, if it is out then go to the next (which should be guaranteed to have a proper time)
            - So the time zones can be saved in a simple list, ranging from 0 - 167 for each hour of the week (sun - sat, 0 = sun at midnight, 1 = sun at 1 AM, etc.)
            - Each position simply has a value (maybe an enum) that says the frequency on it, and if it is activated (for UI purposes), and what alarm index number it belongs to
                - Then this can have two methods of retrieval, regular (range 0 - 167) and also grouped by alarm index
                - The ViewModel for the UI can have the entire list saved and if a user adds/modifies a time slot that already belongs to another group, report that it is conflicting and cannot be saved
     */
    // TODO: Make sure to document somewhere that there should always be exactly 168 entries in the database because that's how many hours are in the week. A "deleted" policy should be empty and have all defaults an inactive
    private static final String CATEGORY_NAME_SELF_DEFENSE = "Self Defense";

    private final AlarmManager alarmManager;
    private final PendingIntent simulatedAttackPendingIntent;
    private final DrillRepository drillRepo;
    private final DefenseDrillNotificationManager notificationManager;
    private final SimulatedAttackRepo repo;
    private final SharedPrefs sharedPrefs;

    @Inject
    public SimulatedAttackManager(@ApplicationContext Context context,
                                  DrillRepository drillRepo,
                                  DefenseDrillNotificationManager notificationManager,
                                  SimulatedAttackRepo repo,
                                  SharedPrefs sharedPrefs) {
        this.alarmManager = context.getSystemService(AlarmManager.class);
        Intent intent = new Intent(Constants.INTENT_ACTION_SIMULATE_ATTACK);
        intent.setPackage(context.getPackageName());
        intent.setComponent(new ComponentName(context, BroadcastReceiverManager.class));
        this.simulatedAttackPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );
        this.drillRepo = drillRepo;
        this.notificationManager = notificationManager;
        this.repo = repo;
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
        intent.setComponent(new ComponentName(context, BroadcastReceiverManager.class));
        context.sendBroadcast(intent);

        // Ultimately calls scheduleSimulatedAttack()
    }

    // TODO: doc comments
    public static void restart(Context context) {
        // The start process already cancels previous alarms
        start(context);
    }

    // TODO: Doc comments
    public static void stop(Context context) {
        Intent intent = new Intent(Constants.INTENT_ACTION_STOP_SIMULATED_ATTACK_MANAGER);
        intent.setPackage(context.getPackageName());
        intent.setComponent(new ComponentName(context, BroadcastReceiverManager.class));
        context.sendBroadcast(intent);

        // Ultimately calls stopSimulatedAttacks()
    }

    // =============================================================================================
    // "Public" Methods
    // =============================================================================================
    // TODO: Doc comments
    /* package-private */ void scheduleSimulatedAttack() {
        stopSimulatedAttacks();
// TODO: Remove test code
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
Log.i("DxTag", "Next trigger at: " + LocalDateTime.now().plusSeconds((getNextAlarmMillis() - System.currentTimeMillis()) / 1000));
}
        // TODO: Only if the user has notifications enabled and has selected to receive simulated attacks
        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                getNextAlarmMillis(),
                simulatedAttackPendingIntent
        );
    }

    /**
     * TODO: Doc comments
     */
    /* package-private */ void stopSimulatedAttacks() {
        alarmManager.cancel(simulatedAttackPendingIntent);
    }

    // TODO: Doc comments
    /* package-private */ void simulateAttack() {
        sendSimulateAttackNotification();
        scheduleSimulatedAttack();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    // TODO: Doc comments
    private void sendSimulateAttackNotification() {
        Optional<CategoryEntity> optSelfDefenseCategory =
                drillRepo.getCategory(CATEGORY_NAME_SELF_DEFENSE);
        if (!optSelfDefenseCategory.isPresent()) {
            // TODO: What do we want to do here, send an intent to someone, set a flag, set shared prefs and have someone report it? Or just leave it be? Check it when the user activates the notification system for simulated attacks?
            return;
        }

        List<Drill> drills = drillRepo.getAllDrillsByCategoryId(optSelfDefenseCategory.get().getId());
        DrillGenerator drillGenerator = new DrillGenerator(drills, new Random());
        // TODO: What to do if there are none of this category??
        Optional<Drill> optDrill = Optional.ofNullable(drillGenerator.generateDrill());
        optDrill.ifPresent(notificationManager::notifySimulatedAttack);
    }

    // TODO: Doc comments (in UTC)
    private long getNextAlarmMillis() {
        // TODO: FIXME finish
        // TODO: If there are no times, then kill the background service

        // 1. Get Current Time
        // 2. Convert to weeklyHour
        // 3. Generate next alarm time randomly using the weeklyHour's policy
            // If the current weeklyHour does not have a policy, randomly pick one from the next weeklyHour that has a policy and return immediately
        // If this happens, you can choose randomly from the start of that policy time's window
        // 4. Convert next alarm time to weeklyHour
        // 5. Check the intermediate policies if it spans more than one and see if they differ from the current
        // 6. If they do, then abandon this next alarm time and generate using the first policy that differs
            // If this happens, you can choose randomly from the start of that policy time's window

        return System.currentTimeMillis() + 5000;
    }
}