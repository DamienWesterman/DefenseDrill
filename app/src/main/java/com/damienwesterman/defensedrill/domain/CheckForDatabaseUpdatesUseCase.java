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

import javax.inject.Inject;

public class CheckForDatabaseUpdatesUseCase {
    // TODO: Maybe make this into a service
    // TODO: Make sure to run this entirely on a background thread(s) so that it persists the home screen (CHECK)
    // TODO: Check to make sure that we have a timestamp, internet connection, and jwt before trying this
    // TODO: If jwt is expired, then don't do anything I think?
    // TODO: Check drills, categories, and sub-categories endpoints, if any of them hit then we don't need to continue, just return
    // TODO: If there are any updates available, then send a notification that directs them to the update screen, maybe then highlight the card somehow?

    private final SharedPrefs sharedPrefs;
    private final CheckPhoneInternetConnection internetConnection;

    @Inject
    public CheckForDatabaseUpdatesUseCase(SharedPrefs sharedPrefs,
                                          CheckPhoneInternetConnection internetConnection) {
        this.sharedPrefs = sharedPrefs;
        this.internetConnection = internetConnection;
    }

    public void checkForUpdate() {
        Log.i("DxTag", "Checking for database update, timestamp: " + sharedPrefs.getLastDrillUpdateTime());
    }
}
