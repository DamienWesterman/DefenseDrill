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

package com.damienwesterman.defensedrill.ui.view_models;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;
import com.damienwesterman.defensedrill.domain.DownloadDatabaseUseCase;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;

import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for interacting with the DefenseDrill API Backend.
 */
@HiltViewModel
public class WebDrillApiViewModel extends AndroidViewModel {
    private final DownloadDatabaseUseCase downloadDb;
    private final DrillRepository drillRepo;

    @Inject
    public WebDrillApiViewModel(@NonNull Application application,
                                DownloadDatabaseUseCase downloadDb,
                                DrillRepository drillRepo) {
        super(application);

        this.downloadDb = downloadDb;
        this.drillRepo = drillRepo;
    }

    /**
     * Begin the operation to download and save all Drills, Categories, and SubCategories from the
     * server.
     *
     * @param successCallback   Callback for successful operation. Takes in a list of newly added
     *                          Drills. List may be empty.
     * @param failureCallback   Callback for failure operation. Takes in a string containing the
     *                          error message.
     */
    public void downloadDb(@NonNull Consumer<List<Drill>> successCallback,
                           @NonNull Consumer<String> failureCallback) {
        // TODO: REMOVE TEST CODE
        new Thread(() -> downloadDb.download(successCallback, failureCallback)).start();
    }

    /**
     * Stop the download before it completes.
     */
    public void stopDownload() {
        downloadDb.cancel();
    }

    /**
     * Mark the provided drills as "known" in the database.
     *
     * @param knownDrills   List of drills the user has indicated they know.
     * @param callback      Callback.
     */
    public void markDrillsAsKnown(@NonNull List<Drill> knownDrills,
                                  @NonNull OperationCompleteCallback callback) {
        knownDrills.forEach(drill -> drill.setIsKnownDrill(true));
        new Thread(() -> {
            try {
                if (drillRepo.updateDrills(knownDrills.toArray(new Drill[0]))) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Something went wrong");
                }
            } catch (SQLiteConstraintException e) {
                callback.onFailure("Something went wrong");
            }
        }).start();
    }
}
