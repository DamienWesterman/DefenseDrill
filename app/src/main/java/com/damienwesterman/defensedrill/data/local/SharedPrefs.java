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

package com.damienwesterman.defensedrill.data.local;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

/**
 * Provides streamlined interaction with Shared Preferences.
 */
public class SharedPrefs {
    private static final String SHARED_PREFERENCES = "defense_drill_shared_preferences";
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_JWT = "jwt";

    private final SharedPreferences sharedPrefs;

    private static SharedPrefs instance;

    /**
     * Private constructor.
     *
     * @param applicationContext Application Context
     */
    private SharedPrefs(@NonNull Context applicationContext) {
        sharedPrefs = applicationContext
                .getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
    }

    /**
     * Get running SharedPrefs instance.
     *
     * @param applicationContext Application Context.
     * @return Running SharedPrefs instance.
     */
    @NonNull
    public static SharedPrefs getInstance(@NonNull Context applicationContext) {
        if (null == instance) {
            instance = new SharedPrefs(applicationContext.getApplicationContext());
        }

        return instance;
    }

    @NonNull
    public String getServerUrl() {
        return sharedPrefs.getString(KEY_SERVER_URL, "");
    }

    /**
     * Save server URL.
     *
     * @param serverUrl Server URL
     * @return true if saved successfully
     */
    public boolean setServerUrl(@NonNull String serverUrl) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_SERVER_URL, serverUrl);
        return editor.commit();
    }

    @NonNull
    public String getJwt() {
        return sharedPrefs.getString(KEY_JWT, "");
    }

    /**
     * Save the JWT.
     *
     * @param jwt String JWT
     * @return true if saved successfully
     */
    public boolean setJwt(@NonNull String jwt) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(KEY_JWT, jwt);
        return editor.commit();
    }
}
