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

package com.damienwesterman.defensedrill.data.local;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

/**
 * Dagger module for Dependency Injection for the local data layer.
 */
@Module
@InstallIn(SingletonComponent.class)
public class LocalDependenciesModule {
    private static DrillDatabase instance = null;

    @Provides
    @Singleton
    public static SharedPrefs getSharedPrefs(@ApplicationContext Context applicationContext) {
        return new SharedPrefs(applicationContext);
    }

    @Provides
    @Singleton
    public static DrillRepository getDrillRepository(@ApplicationContext Context applicationContext) {
        return new DrillRepository(getDatabase(applicationContext));
    }

    @Provides
    @Singleton
    public static SimulatedAttackRepo getSimulatedAttackRepo(@ApplicationContext Context applicationContext) {
        return new SimulatedAttackRepo(getDatabase(applicationContext).getWeeklyHourPolicyDao());
    }

    private static DrillDatabase getDatabase(Context context) {
        if (null == instance) {
            instance = DrillDatabase.instantiate(context);
        }
        return instance;
    }
}
