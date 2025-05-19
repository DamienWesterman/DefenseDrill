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

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
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

    @Inject
    public WebDrillApiViewModel(@NonNull Application application, DownloadDatabaseUseCase downloadDb) {
        super(application);

        this.downloadDb = downloadDb;
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
    public void downloadDb(@NonNull Consumer<List<DrillDTO>> successCallback,
                           @NonNull Consumer<String> failureCallback) {
        downloadDb.download(successCallback, failureCallback);
    }

    /**
     * Stop the download before it completes.
     */
    public void stopDownload() {
        downloadDb.cancel();
    }
}
