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

/**
 * TODO Doc comments
 */
public class Constants {
    public static final long RANDOM_CHOICE = -1;

    // Intents
    public static final String INTENT_PREFIX = "com.damienwesterman.defensedrill.";
    /** Should be accompanied by a long of the Category ID chosen */
    public static final String INTENT_CATEGORY_CHOICE = INTENT_PREFIX + "category_choice";
    /** Should be accompanied by a long of the SubCategory ID chosen */
    public static final String INTENT_SUB_CATEGORY_CHOICE = INTENT_PREFIX + "sub_category_choice";
}
