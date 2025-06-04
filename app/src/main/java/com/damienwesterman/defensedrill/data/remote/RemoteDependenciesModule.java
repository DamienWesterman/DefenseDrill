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

import android.util.Log;
import android.webkit.URLUtil;

import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.common.Constants;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Dagger module for Dependency Injection for the remote data layer.
 */
@Module
@InstallIn(SingletonComponent.class)
public class RemoteDependenciesModule {
    private static final String TAG = RemoteDependenciesModule.class.getSimpleName();

    @Provides
    // NOT Singleton, under normal use case this should be used about once per month
    /* package-private */ static AuthDao getAuthDao() {
        return new Retrofit.Builder()
                .baseUrl(getServerUrl())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthDao.class);
    }

    @Provides
    @Singleton
    public static ApiRepo getApiRepo(SharedPrefs sharedPrefs) {
        return new ApiRepo(sharedPrefs, createApiDao());
    }

    private static String getServerUrl() {
        String serverUrl = Constants.SERVER_URL;
        if (!URLUtil.isValidUrl(serverUrl)) {
            Log.e(TAG, "Invalid Server URL: " + serverUrl);
            throw new IllegalArgumentException("Invalid server URL: '" + serverUrl + "'");
        }

        return serverUrl;
    }

    private static ApiDao createApiDao() {
        return new Retrofit.Builder()
                .baseUrl(getServerUrl())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiDao.class);
    }
}
