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
 * Copyright 2026 Damien Westerman
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

package com.damienwesterman.defensedrill.domain;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.local.SimulatedAttackRepo;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * This class will be used to create a backup JSON file of the database and shared preferences so
 * they can be imported into another device.
 */
public class BackupData {
    private static final String TAG = BackupData.class.getSimpleName();

    @NonNull
    private String resultsMessage = "Something went wrong";

    private final ContentResolver contentResolver;
    private final DrillRepository drillRepo;
    private final SimulatedAttackRepo simulatedAttackRepo;
    private final SharedPrefs sharedPrefs;

    private final Gson gson = new Gson();

    @Inject
    public BackupData(@ApplicationContext Context context,
                      @NonNull DrillRepository drillRepo,
                      @NonNull SimulatedAttackRepo simulatedAttackRepo,
                      @NonNull SharedPrefs sharedPrefs) {
        this.contentResolver = context.getContentResolver();
        this.drillRepo = drillRepo;
        this.simulatedAttackRepo = simulatedAttackRepo;
        this.sharedPrefs = sharedPrefs;
    }

    /**
     * Create the backup file and save it in the indicated location. Call
     * {@link #getResultMessage()} for a user-friendly result message.
     *
     * @param uri Desired path for the backup file.
     * @return true if the backup file was created successfully, false otherwise.
     */
    public boolean backupDatabase(@NonNull Uri uri) {
        try (OutputStream os = contentResolver.openOutputStream(uri, "w")) {
            if (null == os) {
                // TODO: Something properly
                throw new IOException("Unable to open backup file");
            }

            // TODO: Actually get the things
            os.write(createJsonString().getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            //TODO: Handle properly
            throw new RuntimeException(e);
        } catch (IOException e) {
            //TODO: Handle properly
            throw new RuntimeException(e);
        }

        resultsMessage = "Backup file successfully created!";
        return true;
    }

    @NonNull
    public String getResultMessage() {
        return this.resultsMessage;
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    /**
     * TODO: Doc comments
     * @return
     */
    @NonNull
    private String createJsonString() {
        Map<String, String> jsonMap = new HashMap<>();

        // TODO: extract the string keys
        jsonMap.put("database", createDatabaseJson());
        jsonMap.put("simulateAttacks", createSimulatedAttacksJson());
        jsonMap.put("sharedPrefs", createSharedPrefsJson());

        return gson.toJson(jsonMap);
    }

    @NonNull
    private String createDatabaseJson() {
        Map<String, String> jsonMap = new HashMap<>();

        // TODO: extract the string keys
        jsonMap.put("categories", gson.toJson(drillRepo.getAllCategories()));
        jsonMap.put("subCategories", gson.toJson(drillRepo.getAllSubCategories()));
        jsonMap.put("drills", gson.toJson(drillRepo.getAllDrills()));

        return gson.toJson(jsonMap);
    }

    @Nonnull
    private String createSimulatedAttacksJson() {
        Map<String, String> jsonMap = new HashMap<>();

        // TODO: extract the string keys
        jsonMap.put("policies", gson.toJson(simulatedAttackRepo.getAllPolicies()));

        return gson.toJson(jsonMap);
    }

    @NonNull
    private String createSharedPrefsJson() {
        Map<String, String> jsonMap = new HashMap<>();

        // TODO: extract the string keys
        jsonMap.put("lastUpdate", gson.toJson(sharedPrefs.getLastDrillUpdateTime()));
        jsonMap.put("attacksEnabled", gson.toJson(sharedPrefs.areSimulatedAttacksEnabled()));
        jsonMap.put("popupEnabled", gson.toJson(sharedPrefs.isSimulatedAttackPopupDefault()));
        // Don't need onboarding, that will have to have been finished before importing anyway

        return gson.toJson(jsonMap);
    }
}
