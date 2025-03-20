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

package com.damienwesterman.defensedrill.data.remote.authentication;

import android.content.res.Resources;
import android.util.Log;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.R;
import com.damienwesterman.defensedrill.data.remote.dto.LoginDTO;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;

import java.net.HttpURLConnection;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * TODO: Doc comments
 */
public class AuthRepo {
    private static final String TAG = AuthRepo.class.getSimpleName();

    // TODO: Doc comments
    public static void attemptLogin(@NonNull String serverUrl,
                                    @NonNull OperationCompleteCallback callback,
                                    @NonNull Consumer<String> jwtConsumer,
                                    @NonNull Resources resources,
                                    @NonNull LoginDTO login) {
        if (!URLUtil.isValidUrl(serverUrl)) {
            callback.onFailure("Invalid server URL: '" + serverUrl + "'");
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AuthDao dao = retrofit.create(AuthDao.class);

        Call<String> serverRet = dao.login(login);
        serverRet.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call,
                                   @NonNull Response<String> response) {
                if (HttpURLConnection.HTTP_OK == response.code()) {
                    if (null != response.body()) {
                        // Success
                        jwtConsumer.accept(response.body());
                        callback.onSuccess();
                    } else {
                        // This should not happen
                        Log.e(TAG, "Login attempt returned HTTP_OK but response.body() is null");
                        callback.onFailure("Unexpected Error");
                    }
                } else if (HttpURLConnection.HTTP_UNAUTHORIZED == response.code()) {
                    callback.onFailure(resources.getString(R.string.login_failure_message));
                } else {
                    // This should not happen
                    Log.e(TAG, "Login attempt returned status code: " + response.code());
                    callback.onFailure("Unexpected Error");
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call,
                                  @NonNull Throwable throwable) {
                callback.onFailure(resources.getString(R.string.server_connection_issue));
            }
        });
    }
}
