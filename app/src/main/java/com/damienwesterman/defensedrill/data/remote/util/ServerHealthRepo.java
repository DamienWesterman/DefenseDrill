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

package com.damienwesterman.defensedrill.data.remote.util;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.data.remote.dto.HealthStatusDto;
import com.damienwesterman.defensedrill.ui.utils.OperationCompleteCallback;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Provides methods for checking the health of a spring server.
 */
public class ServerHealthRepo {
    private final static String TAG = ServerHealthRepo.class.getSimpleName();
    private final static String HEALTHY_RESPONSE = "UP";

    /**
     * Check if the server is returning a healthy state.
     *
     * @param serverUrl Server URL
     * @param callback Callback for operation completion, calls onSuccess() if the server returns
     *                 it is healthy, otherwise calls onFailure
     */
    public static void isServerHealthy(String serverUrl, OperationCompleteCallback callback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ServerHealthDao dao = retrofit.create(ServerHealthDao.class);

        Call<HealthStatusDto> serverRet = dao.getServerStatus();
        serverRet.enqueue(new Callback<HealthStatusDto>() {
            @Override
            public void onResponse(@NonNull Call<HealthStatusDto> call,
                                   @NonNull Response<HealthStatusDto> response) {
                if (HttpURLConnection.HTTP_OK == response.code()
                        && null != response.body()
                        && response.body().getStatus().equals(HEALTHY_RESPONSE)) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server Not Healthy");
                }
            }

            @Override
            public void onFailure(@NonNull Call<HealthStatusDto> call,
                                  @NonNull Throwable throwable) {
                callback.onFailure("Network Issue");
            }
        });
    }
}
