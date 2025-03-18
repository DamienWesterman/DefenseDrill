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

package com.damienwesterman.defensedrill.utils;

import com.damienwesterman.defensedrill.data.local.Drill;

public class Constants {
    public static final long USER_RANDOM_SELECTION = -1;

    /** Low Confidence index position of strings.xml's confidence_levels string-array */
    private static final int LOW_CONFIDENCE_POSITION = 0;
    /** Medium Confidence index position of strings.xml's confidence_levels string-array */
    private static final int MEDIUM_CONFIDENCE_POSITION = 1;
    /** High Confidence index position of strings.xml's confidence_levels string-array */
    private static final int HIGH_CONFIDENCE_POSITION = 2;

    public static final String INTENT_PREFIX = "com.damienwesterman.defensedrill.";
    /** Should be accompanied by a long of the Category ID chosen */
    public static final String INTENT_CATEGORY_CHOICE = INTENT_PREFIX + "category_choice";
    /** Should be accompanied by a long of the SubCategory ID chosen */
    public static final String INTENT_SUB_CATEGORY_CHOICE = INTENT_PREFIX + "sub_category_choice";
    /** Should be accompanied by a long of the Drill ID */
    public static final String INTENT_DRILL_ID = INTENT_PREFIX + "drill_id";
    /** Value does not matter */
    public static final String INTENT_VIEW_CATEGORIES = INTENT_PREFIX + "view_categories";
    /** Value does not matter */
    public static final String INTENT_VIEW_SUB_CATEGORIES = INTENT_PREFIX + "view_sub_categories";

    /**
     * No need to have instances of this class.
     */
    private Constants() { }

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
