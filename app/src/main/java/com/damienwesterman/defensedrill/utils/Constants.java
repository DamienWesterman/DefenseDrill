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
    /** Should be accompanied by a long of the Group ID chosen */
    public static final String INTENT_GROUP_CHOICE = INTENT_PREFIX + "group_choice";
    /** Should be accompanied by a long of the SubGroup ID chosen */
    public static final String INTENT_SUB_GROUP_CHOICE = INTENT_PREFIX + "sub_group_choice";
}
