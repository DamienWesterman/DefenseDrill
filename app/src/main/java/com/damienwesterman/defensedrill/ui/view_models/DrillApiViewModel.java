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

import com.damienwesterman.defensedrill.domain.DownloadDatabaseUseCase;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.disposables.Disposable;

/**
 * ViewModel for interacting with the DefenseDrill API Backend.
 */
@HiltViewModel
public class DrillApiViewModel extends AndroidViewModel {
    private final DownloadDatabaseUseCase downloadDb;
    private Disposable disposable;

    @Inject
    public DrillApiViewModel(@NonNull Application application, DownloadDatabaseUseCase downloadDb) {
        super(application);

        this.downloadDb = downloadDb;
    }

    public void downloadDb(OperationCompleteCallback callback) {
        disposable = downloadDb.execute(new OperationCompleteCallback() {
            @Override
            public void onSuccess() {
                callback.onSuccess();
                disposable = null;
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
                disposable = null;
            }
        });
    }

    public void stopDownload() {
        if (null != disposable) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
                disposable = null;
            }
        }
    }
}
