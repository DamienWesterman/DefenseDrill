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

package com.damienwesterman.defensedrill.data.remote.api;

import android.util.Log;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;

import java.net.HttpURLConnection;
import java.util.List;
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
public class ApiRepo {
    private final static String TAG = ApiRepo.class.getSimpleName();

    // TODO: Doc comments
    public static void getAllDrills(@NonNull String serverUrl,
                                    @NonNull String jwt,
                                    @NonNull OperationCompleteCallback callback,
                                    @NonNull Consumer<List<DrillDTO>> drillsConsumer) {
        if (!URLUtil.isValidUrl(serverUrl)) {
            callback.onFailure("Invalid server URL: '" + serverUrl + "'");
            return;
        }

        String jwtHeader = "jwt=" + jwt;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiDao dao = retrofit.create(ApiDao.class);

        Call<List<DrillDTO>> serverCall = dao.getAllDrills(jwtHeader);
        serverCall.enqueue(new Callback<List<DrillDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<DrillDTO>> call,
                                   @NonNull Response<List<DrillDTO>> response) {
                // TODO: properly implement
                int statusCode = response.code();
                if (HttpURLConnection.HTTP_NO_CONTENT == statusCode) {
                    Log.i(TAG, "No Content");
                } else if (HttpURLConnection.HTTP_UNAUTHORIZED == statusCode) {
                    Log.i(TAG, "Unauthorized");
                } else if (HttpURLConnection.HTTP_OK == statusCode) {
                    if (null != response.body()) {
                        // Success
                        drillsConsumer.accept(response.body());
                    } else {
                        // This should not happen
                        Log.e(TAG, "getAllDRills() returned HTTP_OK but response.body() is null");
                    }
                } else {
                    Log.e(TAG, "Unexpected http return code from getAllDrills(): " + statusCode);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DrillDTO>> call,
                                  @NonNull Throwable throwable) {
                // TODO: properly implement
            }
        });
    }
}
