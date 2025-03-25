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

package com.damienwesterman.defensedrill.di;

import android.content.Context;

import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.ApiRepo;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * TODO Doc comments
 */
@Module
@InstallIn(SingletonComponent.class)
public class AppDependenciesModule {
    @Provides
    @Singleton
    public static SharedPrefs getSharedPrefs(@ApplicationContext Context applicationContext) {
        return new SharedPrefs(applicationContext);
    }

    @Provides
    @Singleton
    public static ApiRepo getApiRepo(SharedPrefs sharedPrefs) {
        return new ApiRepo(sharedPrefs);
    }

    /*
        AuthRepo and ServerHealthRepo do not need to be singletons, as they should be seldom used
        under normal use cases.
     */
}
