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
import android.util.Log;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.data.local.SimulatedAttackRepo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

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

    private final DrillRepository drillRepo;
    private final SimulatedAttackRepo simulatedAttackRepo;
    private final ContentResolver contentResolver;

    @Inject
    public BackupData(@ApplicationContext Context context,
                      @NonNull DrillRepository drillRepo,
                      @NonNull SimulatedAttackRepo simulatedAttackRepo) {
        this.drillRepo = drillRepo;
        this.simulatedAttackRepo = simulatedAttackRepo;
        this.contentResolver = context.getContentResolver();
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
        // TODO: Finish me
        return "Test String";
    }
}
