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

package com.damienwesterman.defensedrill.data.local;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.damienwesterman.defensedrill.domain.BackupData;

import java.util.function.BiConsumer;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * View Model class for creating and importing database backups.
 */
@HiltViewModel
public class BackupImportViewModel extends AndroidViewModel {
    private final BackupData backupData;

    @Inject
    public BackupImportViewModel(@NonNull Application application,
                                 @NonNull BackupData backupData) {
        super(application);

        this.backupData = backupData;
    }

    /**
     * TODO: Doc comments
     * @param uri
     * @param callback
     */
    public void backupDatabase(@NonNull Uri uri, @NonNull BiConsumer<Boolean, String> callback) {
        new Thread(() -> {
            final boolean success = backupData.backupDatabase(uri);
            callback.accept(success, backupData.getResultMessage());
        }).start();
    }
}
