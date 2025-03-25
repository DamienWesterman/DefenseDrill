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

import android.webkit.URLUtil;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;

import java.util.List;

import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import io.reactivex.rxjava3.core.Observable;
import lombok.RequiredArgsConstructor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * TODO: Doc comments
 */
@RequiredArgsConstructor
public class ApiRepo {
    private final SharedPrefs sharedPrefs;

    // TODO: Doc comments
    // TODO: make sure caller checks JWT first or something and displays popup if empty
    public Observable<List<DrillDTO>> getAllDrills()
            throws IllegalArgumentException {
        String serverUrl = sharedPrefs.getServerUrl();
        if (!URLUtil.isValidUrl(serverUrl)) {
            throw new IllegalArgumentException("Invalid server URL: '" + serverUrl + "'");
        }

        String jwt = sharedPrefs.getJwt();
        if (jwt.isEmpty()) {
            throw new IllegalArgumentException("No login credentials");
        }
        String jwtHeader = "jwt=" + jwt;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(serverUrl)
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ApiDao dao = retrofit.create(ApiDao.class);

        return dao.getAllDrills(jwtHeader);
    }
}
