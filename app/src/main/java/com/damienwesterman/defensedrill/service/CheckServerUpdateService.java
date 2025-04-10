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
import android.util.Log;

import androidx.annotation.Nullable;

import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.domain.CheckPhoneInternetConnection;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * TODO Doc comments
 */
@AndroidEntryPoint
public class CheckServerUpdateService extends Service {
    // TODO: Make sure to run this entirely on a background thread(s) so that it persists the home screen (CHECK)
    // TODO: Check to make sure that we have a timestamp, internet connection, and jwt before trying this
    // TODO: If jwt is expired, then don't do anything I think?
    // TODO: Check drills, categories, and sub-categories endpoints, if any of them hit then we don't need to continue, just return
    // TODO: If there are any updates available, then send a notification that directs them to the update screen, maybe then highlight the card somehow?

    @Inject
    SharedPrefs sharedPrefs;
    @Inject
    CheckPhoneInternetConnection internetConnection;

    // =============================================================================================
    // Service Creation Methods
    // =============================================================================================
    public static void startService(Context context) {
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

        new Thread(this::checkForUpdate).start();
    }

    // =============================================================================================
    // Private Helper Methods
    // =============================================================================================
    public void checkForUpdate() {
        while (true) {
            Log.i("DxTag", "Checking for database update, timestamp: " + sharedPrefs.getLastDrillUpdateTime());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
