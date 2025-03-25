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

package com.damienwesterman.defensedrill.domain;

import android.util.Log;

import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.api.ApiRepo;

import javax.inject.Inject;

import dagger.hilt.android.scopes.ViewModelScoped;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * TODO DOC COMMENTS
 */
@ViewModelScoped
public class DownloadDatabaseUseCase {
    // TODO: Create other API elements (get all drills and get all subcategories)
    // TODO: Put it all together here (keep some hashsets/maps of the categories/subcategories based on serverId)
    // TODO: Call it from the UI with proper callbacks and things (progress widget, feedback, etc.)
    // TODO: Test what happens when we fail (so network issue -unplug computer, no internet, bad jwt, database issue, or something)
    private final SharedPrefs sharedPrefs;

    @Inject
    public DownloadDatabaseUseCase(SharedPrefs sharedPrefs) {
        this.sharedPrefs = sharedPrefs;
    }

    public void execute() {
        loadDrillsFromDatabase();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    private void loadDrillsFromDatabase() {
        // TODO: properly implement
        Disposable disposable = ApiRepo.getAllDrills(
                        sharedPrefs.getServerUrl(),
                        sharedPrefs.getJwt())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        drills -> {
                            drills.forEach( drill -> Log.i("DxTag", drill.getName()));
                        },
                        throwable -> Log.e("DxTag", "We have an issue: " + throwable.getLocalizedMessage())
                );

//            DrillRepository.getInstance(context).insertDrills(drillDTOs.stream()
//                    .map(DrillDTO::toDrill).toArray(Drill[]::new))
    }
}
