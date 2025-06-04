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

package com.damienwesterman.defensedrill.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.ApiRepo;
import com.damienwesterman.defensedrill.domain.CheckPhoneInternetConnection;
import com.damienwesterman.defensedrill.manager.DefenseDrillNotificationManager;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;

import dagger.hilt.android.AndroidEntryPoint;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * Background service to check the server for updates.
 */
@AndroidEntryPoint
public class CheckServerUpdateService extends Service {
    @Inject
    SharedPrefs sharedPrefs;
    @Inject
    CheckPhoneInternetConnection internetConnection;
    @Inject
    ApiRepo apiRepo;
    @Inject
    DefenseDrillNotificationManager notificationManager;

    private Disposable disposable = null;

    // =============================================================================================
    // Service Creation Methods
    // =============================================================================================
    public static void startService(@NonNull Context context) {
        Intent intent = new Intent(context, CheckServerUpdateService.class);
        context.startService(intent);
    }

    // =============================================================================================
    // Android Service Methods
    // =============================================================================================
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        checkForDatabaseUpdate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (null != disposable
                && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
        }
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================

    /**
     * Check the backend if there has been any updates since our last download. If so, send the user
     * a notification that they should download the drills from the backend.
     */
    private void checkForDatabaseUpdate() {
        long lastUpdate = sharedPrefs.getLastDrillUpdateTime();
        if (internetConnection.isNetworkConnected()
                && 0 < lastUpdate
                && !sharedPrefs.getJwt().isEmpty()) {
            disposable = apiRepo.getAllDrillsUpdatedAfterTimestamp(lastUpdate)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(response -> {
                    if (HttpsURLConnection.HTTP_OK == response.code()) {
                        // There are updates! Alert the user and stop the chain
                        notificationManager.notifyDatabaseUpdateAvailable();
                        stopSelf();
                        return Observable.empty();
                    }

                    return apiRepo.getAllCategoriesUpdatedAfterTimestamp(lastUpdate);
                })
                .flatMap(response -> {
                    if (HttpsURLConnection.HTTP_OK == response.code()) {
                        // There are updates! Alert the user and stop the chain
                        notificationManager.notifyDatabaseUpdateAvailable();
                        stopSelf();
                        return Observable.empty();
                    }

                    return apiRepo.getAllSubCategoriesUpdatedAfterTimestamp(lastUpdate);
                })
                .subscribe(
                    response -> {
                        if (HttpsURLConnection.HTTP_OK == response.code()) {
                            // There are updates! Alert the user and stop the chain
                            notificationManager.notifyDatabaseUpdateAvailable();
                        }
                        stopSelf();
                    },
                    throwable -> {
                        // No need to do anything
                        stopSelf();
                    }
                );
        }
    }
}
