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
import android.util.Log;

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.SimulatedAttackRepo;
import com.damienwesterman.defensedrill.data.local.WeeklyHourPolicyEntity;
import com.damienwesterman.defensedrill.utils.Constants;
import com.damienwesterman.defensedrill.utils.DrillGenerator;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * TODO: Doc comments
 */
public class SimulatedAttackManager {
    private static final String TAG = SimulatedAttackManager.class.getSimpleName();
    private static final long INVALID_ALARM_TIME = -1L;
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

    private final AlarmManager alarmManager;
    private final PendingIntent simulatedAttackPendingIntent;
    private final DrillRepository drillRepo;
    private final DefenseDrillNotificationManager notificationManager;
    private final SimulatedAttackRepo simulatedAttackRepo;
    private final SharedPrefs sharedPrefs;

    @Inject
    public SimulatedAttackManager(@ApplicationContext Context context,
                                  DrillRepository drillRepo,
                                  DefenseDrillNotificationManager notificationManager,
                                  SimulatedAttackRepo simulatedAttackRepo,
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
        this.simulatedAttackRepo = simulatedAttackRepo;
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
        simulatedCurrTime = getNextAlarmMillis();
        // TODO: FIXME: START HERE: Test the alarm generation by creating policies and inspecting the following log and make sure that we are falling within the policies hours and frequencies
// TODO: If there are no times, then kill the background service
        Log.i("DxTag", "Next trigger at: " + LocalDateTime.now().plusSeconds((simulatedCurrTime - System.currentTimeMillis()) / 1000));
// TODO: Only if the user has notifications enabled and has selected to receive simulated attacks AND getNextAlarmMillis() != INVALID_ALARM_TIME
        alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 3000, // getNextAlarmMillis(), TODO: PUT BACK IN
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
//        sendSimulateAttackNotification(); TODO: PUT BACK IN
        scheduleSimulatedAttack();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    // TODO: Doc comments
    private void sendSimulateAttackNotification() {
        Optional<CategoryEntity> optSelfDefenseCategory =
                drillRepo.getCategory(Constants.CATEGORY_NAME_SELF_DEFENSE);
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

// TODO: REMOVE TEST CODE
private static long simulatedCurrTime = System.currentTimeMillis();
    // TODO: Doc comments (in UTC)
    private long getNextAlarmMillis() {
        long currTime = simulatedCurrTime; // TODO: System.currentTimeMillis();
        int currWeeklyHour = getWeeklyHourFromMillis(currTime);

        // This should already be sorted in ascending order by weekly hour
        List<WeeklyHourPolicyEntity> policies = simulatedAttackRepo.getActivePolicies();
        if (policies.isEmpty()) {
            Log.w(TAG, "No active policies");
            return INVALID_ALARM_TIME;
        }

        /*
        Generate next alarm time randomly using the weeklyHour's policy
         */

        if (currWeeklyHour > policies.get(policies.size() - 1).getWeeklyHour()) {
            /*
            Means we reached the end of the week, wrap around and start again, pick from the first
            time window.
             */
            return generateAlarmFromBeginningOfTimeWindow(policies.get(0));
        }

        int nextPolicyIndex = -1;
        for (int i = 0; i < policies.size(); i++) {
            WeeklyHourPolicyEntity policy = policies.get(i);
            if (currWeeklyHour == policy.getWeeklyHour()) {
                nextPolicyIndex = i;
                break;
            } else if (currWeeklyHour < policy.getWeeklyHour()) {
                /*
                Current hour does not have a policy, so we can just choose a time from this next
                window without having to do further checks. This should not really happen as an hour
                without a policy should never be chosen, but just in case.
                 */
                return generateAlarmFromBeginningOfTimeWindow(policy);
            }
        }

        Constants.SimulatedAttackFrequency nextAlarmFrequency =
                policies.get(nextPolicyIndex).getFrequency();
        long nextAlarmTimeMillis =
                generateAlarmUsingFrequency(currTime, nextAlarmFrequency);
        int nextAlarmWeeklyHour = getWeeklyHourFromMillis(nextAlarmTimeMillis);

        /*
        Check subsequent policies if it spans multiple hours to make sure we do not violate any
        intermittent policies' frequencies
         */
        if (nextAlarmWeeklyHour != currWeeklyHour) {
            if (nextAlarmWeeklyHour > policies.get(policies.size() - 1).getWeeklyHour()) {
                /*
                Means we reached the end of the week, wrap around and start again, pick from the
                first time window.
                 */
                return generateAlarmFromBeginningOfTimeWindow(policies.get(0));
            }

            for (int i = nextPolicyIndex + 1; i < policies.size(); i++) {
                WeeklyHourPolicyEntity policy = policies.get(i);
                if (nextAlarmWeeklyHour < policy.getWeeklyHour()) {
                    // We are good and did not break any intermittent policies
                    break;
                }

                if (nextAlarmFrequency != policy.getFrequency()) {
                    // We have violated an intermittent policy, so just select from this next policy
                    return generateAlarmFromBeginningOfTimeWindow(policy);
                }
            }
        }

        return nextAlarmTimeMillis;
    }

    // TODO: DOC COMMENTS
    private long generateAlarmFromBeginningOfTimeWindow(WeeklyHourPolicyEntity policy) {
        long startingMillis = getNextOccurrenceWeeklyHourInMillis(policy.getWeeklyHour());

        /*
        Since we are starting from the beginning of an hour, we can select a time frame from the
        beginning of the hour until the frequency's upper bound.
         */
        long additionalMillis = ThreadLocalRandom.current().nextLong(
                policy.getFrequency().getNextAlarmMillisUpperBound()
        );

        return startingMillis + additionalMillis;
    }

    // TODO: Doc comments
    private long generateAlarmUsingFrequency(long startingMillis, Constants.SimulatedAttackFrequency frequency) {
        long additionalMillis = ThreadLocalRandom.current().nextLong(
                frequency.getNextAlarmMillisLowerBound(),
                frequency.getNextAlarmMillisUpperBound()
        );

        return startingMillis + additionalMillis;
    }

    // TODO: Doc comments (UTC epoch)
    private int getWeeklyHourFromMillis(long millisFromEpoch) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(millisFromEpoch)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        int dayOfWeek;
        switch (localDateTime.getDayOfWeek()) {
            case SUNDAY:
                dayOfWeek = 0;
                break;
            case MONDAY:
                dayOfWeek = 1;
                break;
            case TUESDAY:
                dayOfWeek = 2;
                break;
            case WEDNESDAY:
                dayOfWeek = 3;
                break;
            case THURSDAY:
                dayOfWeek = 4;
                break;
            case FRIDAY:
                dayOfWeek = 5;
                break;
            case SATURDAY:
                dayOfWeek = 6;
                break;
            default:
                // Definitely should not happen
                Log.e(TAG, "Invalid day of week: " + localDateTime.getDayOfWeek());
                throw new RuntimeException("Invalid Day of week: " + localDateTime.getDayOfWeek());
        }

        return (dayOfWeek * 24) + localDateTime.getHour();
    }

    // TODO: Doc comments UTC epoch
    private long getNextOccurrenceWeeklyHourInMillis(int weeklyHour) {
        DayOfWeek targetDay;
        switch (weeklyHour / 24) {
            case 0:
                targetDay = DayOfWeek.SUNDAY;
                break;
            case 1:
                targetDay = DayOfWeek.MONDAY;
                break;
            case 2:
                targetDay = DayOfWeek.TUESDAY;
                break;
            case 3:
                targetDay = DayOfWeek.WEDNESDAY;
                break;
            case 4:
                targetDay = DayOfWeek.THURSDAY;
                break;
            case 5:
                targetDay = DayOfWeek.FRIDAY;
                break;
            case 6:
                targetDay = DayOfWeek.SATURDAY;
                break;
            default:
                // Definitely should not happen
                Log.e(TAG, "Invalid weeklyHour: " + weeklyHour);
                throw new RuntimeException("Invalid weeklyHour: " + weeklyHour);
        }

        int targetHour = weeklyHour % 24;

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime next = now
                .withHour(targetHour)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .with(TemporalAdjusters.nextOrSame(targetDay));

        if (!next.isAfter(now)) {
            next = next.with(TemporalAdjusters.next(targetDay));
        }

        return next.toInstant().toEpochMilli();
    }
}