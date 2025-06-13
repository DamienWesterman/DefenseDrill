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

package com.damienwesterman.defensedrill.common;

import com.damienwesterman.defensedrill.BuildConfig;
import com.damienwesterman.defensedrill.data.local.Drill;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class Constants {
    /**
     * No need to have instances of this class.
     */
    private Constants() { }

    /**
     * Enum for the frequency of the simulated attack notifications.
     */
    @Getter
    @RequiredArgsConstructor
    public enum SimulatedAttackFrequency {
        NO_ATTACKS(-1, -1, -1),
        ONCE_PER_15_MINUTES(1,
                calculateLowerBoundFromMinutes(15),
                calculateUpperBoundFromMinutes(15)),
        ONCE_PER_30_MINUTES(1,
                calculateLowerBoundFromMinutes(30),
                calculateUpperBoundFromMinutes(30)),
        ONCE_PER_1_HOUR(1,
                calculateLowerBoundFromHours(1),
                calculateUpperBoundFromHours(1)),
        ONCE_PER_90_MINUTES(2,
                calculateLowerBoundFromMinutes(90),
                calculateUpperBoundFromMinutes(90)),
        ONCE_PER_2_HOURS(2,
                calculateLowerBoundFromHours(2),
                calculateUpperBoundFromHours(2)),
        ONCE_PER_3_HOURS(3,
                calculateLowerBoundFromHours(3),
                calculateUpperBoundFromHours(3)),
        ONCE_PER_4_HOURS(4,
                calculateLowerBoundFromHours(4),
                calculateUpperBoundFromHours(4)),
        ONCE_PER_6_HOURS(6,
                calculateLowerBoundFromHours(6),
                calculateUpperBoundFromHours(6)),
        ONCE_PER_12_HOURS(12,
                calculateLowerBoundFromHours(12),
                calculateUpperBoundFromHours(12));

        /**
         * Minimum number of hours needed for the selected frequency.
         */
        private final int minimumHoursNeeded;

        /**
         * Minimum number of milliseconds delay for the next alarm.
         */
        private final long nextAlarmMillisLowerBound;

        /**
         * Maximum number of milliseconds delay for the next alarm.
         */
        private final long nextAlarmMillisUpperBound;

        private static long calculateLowerBoundFromMinutes(int minutes) {
            // Let it truncate
            return (long) (TimeUnit.MINUTES.toMillis(minutes) * 0.8);
        }

        private static long calculateLowerBoundFromHours(int hours) {
            // Let it truncate
            return (long) (TimeUnit.HOURS.toMillis(hours) * 0.8);
        }

        private static long calculateUpperBoundFromMinutes(int minutes) {
            // Let it truncate
            return (long) (TimeUnit.MINUTES.toMillis(minutes) * 1.2);
        }

        private static long calculateUpperBoundFromHours(int hours) {
            // Let it truncate
            return (long) (TimeUnit.HOURS.toMillis(hours) * 1.2);
        }
    }

    public static final long USER_RANDOM_SELECTION = -1;
    /** Expected name for a self defense CategoryEntity. Should match with the server. */
    public static final String CATEGORY_NAME_SELF_DEFENSE = "Self Defense";

    public static final String SERVER_URL = BuildConfig.SERVER_URL;
    public static final String FEEDBACK_RECEIPT_EMAIL = BuildConfig.FEEDBACK_RECEIPT_EMAIL;

    /** Low Confidence index position of strings.xml's confidence_levels string-array. */
    private static final int LOW_CONFIDENCE_POSITION = 0;
    /** Medium Confidence index position of strings.xml's confidence_levels string-array. */
    private static final int MEDIUM_CONFIDENCE_POSITION = 1;
    /** High Confidence index position of strings.xml's confidence_levels string-array. */
    private static final int HIGH_CONFIDENCE_POSITION = 2;

    public static final String INTENT_PREFIX = "com.damienwesterman.defensedrill.";
    public static final String INTENT_ACTION_START_SIMULATED_ATTACK_MANAGER =
            INTENT_PREFIX + "start_simulated_attack_manager";
    public static final String INTENT_ACTION_STOP_SIMULATED_ATTACK_MANAGER =
            INTENT_PREFIX + "stop_simulated_attack_manager";
    public static final String INTENT_ACTION_SIMULATE_ATTACK =
            INTENT_PREFIX + "simulate_attack";

    /** Should be accompanied by a long of the Category ID chosen. */
    public static final String INTENT_EXTRA_CATEGORY_CHOICE = "category_choice";
    /** Should be accompanied by a long of the SubCategory ID chosen. */
    public static final String INTENT_EXTRA_SUB_CATEGORY_CHOICE = "sub_category_choice";
    /** Should be accompanied by a long of the Drill ID. */
    public static final String INTENT_EXTRA_DRILL_ID = "drill_id";
    /** Value does not matter. */
    public static final String INTENT_EXTRA_SIMULATED_ATTACK = "simulated_attack";
    /** Value does not matter. */
    public static final String INTENT_EXTRA_VIEW_CATEGORIES = "view_categories";
    /** Value does not matter. */
    public static final String INTENT_EXTRA_VIEW_SUB_CATEGORIES = "view_sub_categories";
    /** Value should be an object of type {@link com.damienwesterman.defensedrill.data.remote.dto.DrillDTO}. */
    public static final String INTENT_EXTRA_DRILL_DTO = "drill_dto";
    /** Should be accompanied by an int of the index position of the instruction to be displayed. */
    public static final String INTENT_EXTRA_INSTRUCTION_INDEX = "instruction_index";
    /** Should be accompanied by a string of an instruction's video id. */
    public static final String INTENT_EXTRA_VIDEO_ID = "video_id";
    /** Should be accompanied by a Class of the calling activity if relevant to onboarding order. */
    public static final String INTENT_EXTRA_START_ONBOARDING = "start_onboarding";

    /**
     * Converts a confidence weight into its respective position in the confidence_levels string
     * array in res/values/strings.xml. For use in a spinner with indexed positions.
     * <br><br>
     * Yeah yeah not a Constant but whatever, it goes here because I say it does.
     *
     * @param weight    int value of weight, see around {@link Drill#LOW_CONFIDENCE} for values.
     * @return          int value of position relevant to list in
     *                  {@link com.damienwesterman.defensedrill.R.array#confidence_levels}.
     */
    public static int confidenceWeightToPosition(int weight) {
        int position;
        switch(weight) {
            case Drill.HIGH_CONFIDENCE:
                position = HIGH_CONFIDENCE_POSITION;
                break;
            case Drill.MEDIUM_CONFIDENCE:
                position = MEDIUM_CONFIDENCE_POSITION;
                break;
            case Drill.LOW_CONFIDENCE:
            default:
                position = LOW_CONFIDENCE_POSITION;
                break;
        }

        return position;
    }

    /**
     * Converts a position index from the confidence_levels string array in res/values/strings.xm
     * into its respective confidence weight. For use in a spinner with indexed positions.
     * <br><br>
     * Yeah yeah not a Constant but whatever, it goes here because I say it does.
     *
     * @param position  int value of position relevant to list in
     *                  {@link com.damienwesterman.defensedrill.R.array#confidence_levels}.
     * @return          int value of weight, see around {@link Drill#LOW_CONFIDENCE} for values.
     */
    public static int confidencePositionToWeight(int position) {
        int confidence;
        switch(position) {
            case HIGH_CONFIDENCE_POSITION:
                confidence = Drill.HIGH_CONFIDENCE;
                break;
            case MEDIUM_CONFIDENCE_POSITION:
                confidence = Drill.MEDIUM_CONFIDENCE;
                break;
            case LOW_CONFIDENCE_POSITION:
            default:
                confidence = Drill.LOW_CONFIDENCE;
                break;
        }

        return confidence;
    }
}
