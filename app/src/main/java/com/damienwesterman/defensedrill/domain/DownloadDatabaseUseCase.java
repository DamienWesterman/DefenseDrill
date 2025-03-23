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

import android.content.Context;
import android.util.Log;

import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.api.ApiRepo;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

/**
 * TODO DOC COMMENTS
 */
public class DownloadDatabaseUseCase {
    // TODO: FIXME: START HERE
    // TODO: Learn Hilt/Dagger
    // TODO: Make this a HiltDependency class or something (maybe need to change SharedPrefs to be as well
    // TODO: Create other API elements (get all drills and get all subcategories)
    // TODO: Put it all together here (keep some hashsets/maps of the categories/subcategories based on serverId)
    // TODO: Call it from the UI with proper callbacks and things (progress widget, feedback, etc.)
    // TODO: Test what happens when we fail (so network issue -unplug computer, no internet, bad jwt, database issue, or something)

    public void execute(Context context) {
        loadDrillsFromDatabase(context);
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    private void loadDrillsFromDatabase(Context context) {
        Disposable disposable = ApiRepo.getAllDrills(
                        SharedPrefs.getInstance(context).getServerUrl(),
                        SharedPrefs.getInstance(context).getJwt())
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
