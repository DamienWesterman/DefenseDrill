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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Provides streamlined interaction with Shared Preferences.
 */
public class SharedPrefs {
    private static final String TAG = SharedPrefs.class.getSimpleName();
    private static final String SHARED_PREFERENCES = "defense_drill_shared_preferences";
    private static final String ENCRYPTED_SHARED_PREFERENCES
            = "defense_drill_encrypted_shared_preferences";
    private static final String KEY_JWT = "jwt";
    private static final String KEY_LAST_DRILL_UPDATE_TIME = "last_drill_update_time";
    private static final String KEY_SIMULATED_ATTACKS_ENABLED = "simulated_attacks_enabled";
    /** Denotes if the user wants the simulated attacks instructional popup to launch by default */
    private static final String KEY_SIMULATED_ATTACKS_POPUP_BY_DEFAULT
            = "simulated_attacks_popup_by_default";
    private static final String KEY_ONBOARDING_COMPLETE = "onboarding_complete";

    private final SharedPreferences sharedPrefs;
    private final SharedPreferences encryptedSharedPrefs;

    /**
     * Private constructor.
     *
     * @param applicationContext Application Context.
     */
    public SharedPrefs(@NonNull Context applicationContext) {
        sharedPrefs = applicationContext
                .getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);

        try {
            MasterKey key = new MasterKey.Builder(applicationContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            encryptedSharedPrefs = EncryptedSharedPreferences.create(
                    applicationContext,
                    ENCRYPTED_SHARED_PREFERENCES,
                    key,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            // This should be a big issue as it should not happen
            Log.e(TAG, "GeneralSecurityException during encryptedSharedPrefs generation", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            // This should be a big issue as it should not happen
            Log.e(TAG, "IOException during encryptedSharedPrefs generation", e);
            throw new RuntimeException(e);
        }
    }

    @NonNull
    public String getJwt() {
        return encryptedSharedPrefs.getString(KEY_JWT, "");
    }

    public boolean setJwt(@NonNull String jwt) {
        SharedPreferences.Editor editor = encryptedSharedPrefs.edit();
        editor.putString(KEY_JWT, jwt);
        return editor.commit();
    }

    public long getLastDrillUpdateTime() {
        return sharedPrefs.getLong(KEY_LAST_DRILL_UPDATE_TIME, 0);
    }

    public boolean setLastDrillUpdateTime(long lastDrillUpdateTime) {
        if (0 > lastDrillUpdateTime) {
            return false;
        }

        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong(KEY_LAST_DRILL_UPDATE_TIME, lastDrillUpdateTime);
        return editor.commit();
    }

    public boolean areSimulatedAttacksEnabled() {
        return sharedPrefs.getBoolean(KEY_SIMULATED_ATTACKS_ENABLED, false);
    }

    public boolean setSimulatedAttacksEnabled(boolean enabled) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_SIMULATED_ATTACKS_ENABLED, enabled);
        return editor.commit();
    }

    public boolean isSimulatedAttackPopupDefault() {
        return sharedPrefs.getBoolean(KEY_SIMULATED_ATTACKS_POPUP_BY_DEFAULT, true);
    }

    public boolean setSimulatedAttackPopupDefault(boolean isDefault) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_SIMULATED_ATTACKS_POPUP_BY_DEFAULT, isDefault);
        return editor.commit();
    }

    public boolean isOnboardingComplete() {
        return sharedPrefs.getBoolean(KEY_ONBOARDING_COMPLETE, false);
    }

    public boolean setOnboardingComplete(boolean isComplete) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(KEY_ONBOARDING_COMPLETE, isComplete);
        return editor.commit();
    }
}
