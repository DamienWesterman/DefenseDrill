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

import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Provides methods for checking the health of a spring server.
 */
public class ServerHealthRepo {
    private final static String TAG = ServerHealthRepo.class.getSimpleName();
    /**
     * Check if the server is returning a healthy state.
     *
     * @param serverUrl Server URL.
     * @return true if server reported it is healthy.
     */
    public static boolean isServerHealthy(String serverUrl) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .build();
        ServerHealthDao dao = retrofit.create(ServerHealthDao.class);

        Call<Void> serverRet = dao.getServerStatus();
        try {
            Response<Void> response = serverRet.execute();
            Log.i(TAG, "response.code=" + response.code());
            return HttpURLConnection.HTTP_OK == response.code();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }
}
