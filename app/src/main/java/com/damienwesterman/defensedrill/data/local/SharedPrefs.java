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
    private static final String KEY_SERVER_URL = "server_url";
    private static final String KEY_JWT = "jwt";

    private final SharedPreferences sharedPrefs;
    private final SharedPreferences encryptedSharedPrefs;

    /**
     * Private constructor.
     *
     * @param applicationContext Application Context
     */
    public SharedPrefs(Context applicationContext) {
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
        return encryptedSharedPrefs.getString(KEY_JWT, "");
    }

    /**
     * Save the JWT.
     *
     * @param jwt String JWT
     * @return true if saved successfully
     */
    public boolean setJwt(@NonNull String jwt) {
        SharedPreferences.Editor editor = encryptedSharedPrefs.edit();
        editor.putString(KEY_JWT, jwt);
        return editor.commit();
    }
}
