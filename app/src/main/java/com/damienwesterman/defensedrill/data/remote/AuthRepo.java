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

package com.damienwesterman.defensedrill.data.remote;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.dto.LoginDTO;
import com.damienwesterman.defensedrill.ui.util.OperationCompleteCallback;

import java.net.HttpURLConnection;

import javax.inject.Inject;

import dagger.hilt.android.qualifiers.ApplicationContext;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repo for interacting with the security portion of the API server backend.
 */
public class AuthRepo {
    private static final String TAG = AuthRepo.class.getSimpleName();
    private final Context applicationContext;
    private final SharedPrefs sharedPrefs;
    private final AuthDao authDao;

    @Inject
    /* package-private */ AuthRepo(@ApplicationContext Context applicationContext,
                                   SharedPrefs sharedPrefs, AuthDao authDao) {
        this.applicationContext = applicationContext;
        this.sharedPrefs = sharedPrefs;
        this.authDao = authDao;
    }

    /**
     * Attempt to log in with the provided credentials.
     *
     * @param login     Login credentials.
     * @param callback  Callback.
     */
    public void attemptLogin(@NonNull LoginDTO login,
                             @NonNull OperationCompleteCallback callback) {
        Call<String> serverCall = authDao.login(login);
        serverCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,
                                   @NonNull Response<String> response) {
                switch (response.code()) {
                    case HttpURLConnection.HTTP_OK:
                        if (null != response.body()) {
                            // Success
                            sharedPrefs.setJwt(response.body());
                            callback.onSuccess();
                        } else {
                            // This should not happen
                            Log.e(TAG, "Login attempt returned HTTP_OK but response.body() is null");
                            callback.onFailure("Unexpected Error");
                        }

                        break;
                    case HttpURLConnection.HTTP_UNAUTHORIZED:
                        callback.onFailure(applicationContext.getResources()
                                .getString(R.string.login_failure_message));
                        break;
                    default:
                        // This should not happen
                        Log.e(TAG, "Login attempt returned status code: " + response.code());
                        callback.onFailure("Unexpected Error");
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,
                                  @NonNull Throwable throwable) {
                Log.e(TAG, "Failed to log in", throwable);
                callback.onFailure(applicationContext.getResources()
                                        .getString(R.string.server_connection_issue));
            }
        });
    }
}
