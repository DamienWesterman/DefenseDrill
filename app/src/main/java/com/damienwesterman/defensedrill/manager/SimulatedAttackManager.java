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
 * Copyright 2025 Damien Westerman
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

import androidx.annotation.NonNull;

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
 * Manager for scheduling and sending Simulated Attack notifications to the user.
 */
public class SimulatedAttackManager {
    private static final String TAG = SimulatedAttackManager.class.getSimpleName();
    private static final long INVALID_ALARM_TIME = -1L;

    private final AlarmManager alarmManager;
    /** Pending intent for our notification. This way we can cancel it specifically if need be. */
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
     * Start the SimulatedAttackManager.
     *
     * @param context   Context.
     */
    public static void start(@NonNull Context context) {
        Intent intent = new Intent(Constants.INTENT_ACTION_START_SIMULATED_ATTACK_MANAGER);
        intent.setPackage(context.getPackageName());
        intent.setComponent(new ComponentName(context, BroadcastReceiverManager.class));
        context.sendBroadcast(intent);

        // Ultimately calls scheduleSimulatedAttack()
    }

    /**
     * Restart the SimulatedAttackManager.
     *
     * @param context   Context.
     */
    public static void restart(@NonNull Context context) {
        // The start process already cancels previous alarms
        start(context);
    }

    /**
     * Stop the SimulatedAttackManager.
     *
     * @param context   Context.
     */
    public static void stop(@NonNull Context context) {
        Intent intent = new Intent(Constants.INTENT_ACTION_STOP_SIMULATED_ATTACK_MANAGER);
        intent.setPackage(context.getPackageName());
        intent.setComponent(new ComponentName(context, BroadcastReceiverManager.class));
        context.sendBroadcast(intent);

        // Ultimately calls stopSimulatedAttacks()
    }

    // =============================================================================================
    // "Public" Methods
    // =============================================================================================
    /**
     * Randomly select the next time for a simulated attack notification, abiding by the user's
     * custom notification policies.
     */
    /* package-private */ void scheduleSimulatedAttack() {
        stopSimulatedAttacks();

        long nextAlarmMillis = getNextAlarmMillis();
        if (INVALID_ALARM_TIME == nextAlarmMillis) {
            // Something went wrong, don't schedule another notification
            return;
        }

        if (sharedPrefs.areSimulatedAttacksEnabled()
                && notificationManager.areNotificationsEnabled()) {
            // Schedule the next alarm, which will then trigger simulateAttack() at nextAlarmMillis
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmMillis,
                    simulatedAttackPendingIntent
            );
        }
    }

    /**
     * Stop any scheduled simulated attacks.
     */
    /* package-private */ void stopSimulatedAttacks() {
        alarmManager.cancel(simulatedAttackPendingIntent);
    }

    /**
     * Simulate an attack by selecting a random drill, then scheduling the next simulated attack.
     */
    /* package-private */ void simulateAttack() {
        if (sendSimulatedAttackNotification()) {
            // Only schedule the next notification if we successfully generated this notification
            scheduleSimulatedAttack();
        }
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * Randomly select a Drill from a "Self Defense" category and push a notification to the user.
     *
     * @return  true if we successfully sent a notification, otherwise false.
     */
    private boolean sendSimulatedAttackNotification() {
        Optional<CategoryEntity> optSelfDefenseCategory =
                drillRepo.getCategory(Constants.CATEGORY_NAME_SELF_DEFENSE);
        if (!optSelfDefenseCategory.isPresent()) {
            Log.w(TAG, "No Self Defense Category");
            return false;
        }

        List<Drill> drills = drillRepo.getAllDrillsByCategoryId(optSelfDefenseCategory.get().getId());
        if (drills.isEmpty()) {
            Log.w(TAG, "No Self Defense Drills");
            return false;
        }

        DrillGenerator drillGenerator = new DrillGenerator(drills, new Random());
        Optional<Drill> optDrill = Optional.ofNullable(drillGenerator.generateDrill());
        optDrill.ifPresent(notificationManager::notifySimulatedAttack);

        return optDrill.isPresent();
    }

    /**
     * Generate the next alarm time in epoch millis based on the user's custom notification
     * policies.
     *
     * @return Milliseconds after epoch of the next alarm, or {@link #INVALID_ALARM_TIME} on error.
     */
    private long getNextAlarmMillis() {
        long currTime = System.currentTimeMillis();
        int currWeeklyHour = getWeeklyHourFromMillis(currTime);

        // This will already be sorted in ascending order by weekly hour
        List<WeeklyHourPolicyEntity> policies = simulatedAttackRepo.getActivePolicies();
        policies.removeIf(policy ->
            policy.getPolicyName().isEmpty() ||
            Constants.SimulatedAttackFrequency.NO_ATTACKS ==  policy.getFrequency());
        if (policies.isEmpty()) {
            Log.w(TAG, "No active policies");
            return INVALID_ALARM_TIME;
        }

        if (currWeeklyHour > policies.get(policies.size() - 1).getWeeklyHour()) {
            /*
            Means we reached the end of the week, wrap around and start again, pick from the first
            time window. This should not really happen as an hour without a policy should never be
            chosen, but just in case.
             */
            return generateAlarmFromBeginningOfTimeWindow(policies.get(0));
        }

        // Generate next alarm time randomly using the weeklyHour's policy
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

        if (nextAlarmWeeklyHour != currWeeklyHour) {
            if (nextAlarmWeeklyHour > policies.get(policies.size() - 1).getWeeklyHour()) {
                /*
                Means we reached the end of the week, wrap around and start again, pick from the
                first time window.
                 */
                return generateAlarmFromBeginningOfTimeWindow(policies.get(0));
            }

            /*
            Check subsequent policies to make sure we do not violate any intermittent policies'
            frequencies.
             */
            for (int i = nextPolicyIndex; i < policies.size(); i++) {
                WeeklyHourPolicyEntity policy = policies.get(i);
                if (nextAlarmWeeklyHour < policy.getWeeklyHour()) {
                    /*
                     We went past all other policies before nextAlarmWeeklyHour, meaning there is no
                     policy for this hour, so we need to pick from this subsequent policy.
                     */
                    return generateAlarmFromBeginningOfTimeWindow(policy);
                }

                if (nextAlarmFrequency != policy.getFrequency()) {
                    // We have violated an intermittent policy, so just select from this next policy
                    return generateAlarmFromBeginningOfTimeWindow(policy);
                }

                if (nextAlarmWeeklyHour == policy.getWeeklyHour()) {
                    // We are good and did not break any intermittent policies
                    break;
                }
            }
        }

        return nextAlarmTimeMillis;
    }

    /**
     * Generate the next alarm time in epoch millis using the given policy.
     *
     * @param policy    Policy to use to generate the next alarm time.
     * @return          Milliseconds after epoch of the next alarm.
     */
    private long generateAlarmFromBeginningOfTimeWindow(@NonNull WeeklyHourPolicyEntity policy) {
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

    /**
     * Generate the next alarm time in epoch millis using a starting time and a frequency.
     *
     * @param startingMillis    Milliseconds after epoch of the starting time.
     * @param frequency         Frequency of the alarm to be generated.
     * @return                  Milliseconds after epoch of the next alarm.
     */
    private long generateAlarmUsingFrequency(long startingMillis,
                                             @NonNull Constants.SimulatedAttackFrequency frequency) {
        long additionalMillis = ThreadLocalRandom.current().nextLong(
                frequency.getNextAlarmMillisLowerBound(),
                frequency.getNextAlarmMillisUpperBound()
        );

        return startingMillis + additionalMillis;
    }

    /**
     * Convert milliseconds after epoch into a weeklyHour values (0 = Sunday at midnight).
     *
     * @param millisFromEpoch   Milliseconds after epoch.
     * @return                  Weekly Hour (0 = Sunday at midnight).
     */
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

    /**
     * Get the next local time occurrence of a weeklyHour (0 = Sunday at midnight) in milliseconds
     * after epoch.
     *
     * @param weeklyHour    Weekly Hour (0 = Sunday at midnight).
     * @return              The next occurrence of the weeklyHour in milliseconds after epoch.
     */
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
