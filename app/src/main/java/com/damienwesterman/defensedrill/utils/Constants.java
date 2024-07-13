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

package com.damienwesterman.defensedrill.utils;

import com.damienwesterman.defensedrill.data.Drill;

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
    /** Value does not matter */
    public static final String INTENT_CREATE_CATEGORY = INTENT_PREFIX + "create_category";
    /** Value does not matter */
    public static final String INTENT_CREATE_SUB_CATEGORY = INTENT_PREFIX + "create_sub_category";

    // TODO doc comments
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

    // TODO doc comments
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
