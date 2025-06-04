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

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.data.local.SharedPrefs;
import com.damienwesterman.defensedrill.data.remote.dto.CategoryDTO;
import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
import com.damienwesterman.defensedrill.data.remote.dto.SubCategoryDTO;
import com.damienwesterman.defensedrill.util.Constants;

import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import retrofit2.Response;

/**
 * Repository class for interacting with the API server backend.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class ApiRepo {
    private final SharedPrefs sharedPrefs;
    private final ApiDao apiDao;

    /**
     * Get an observable for the API call to retrieve all Drills from the server.
     *
     * @return                          Observable List of DrillDTO objects.
     * @throws IllegalArgumentException Thrown if {@link SharedPrefs#getJwt()} is empty.
     */
    public Observable<Response<List<DrillDTO>>> getAllDrills()
            throws IllegalArgumentException {
        String jwt = sharedPrefs.getJwt();
        if (jwt.isEmpty()) {
            throw new IllegalArgumentException("No login credentials");
        }
        String jwtHeader = "jwt=" + jwt;

        return apiDao.getAllDrills(jwtHeader);
    }

    /**
     * Get an observable for the API call to retrieve all Drills from the server updated after the
     * given timestamp.
     *
     * @param timestamp Timestamp of millis since epoch in UTC.
     * @return          Observable List of DrillDTO objects.
     */
    public Observable<Response<List<DrillDTO>>> getAllDrillsUpdatedAfterTimestamp(long timestamp) {
        String jwt = sharedPrefs.getJwt();
        if (jwt.isEmpty()) {
            throw new IllegalArgumentException("No login credentials");
        }
        String jwtHeader = "jwt=" + jwt;

        return apiDao.getDrillsUpdatedAfterTimestamp(jwtHeader, timestamp);
    }

    /**
     * Get an observable for the API call to retrieve all Categories from the server.
     *
     * @return                          Observable List of CategoryDTO objects.
     * @throws IllegalArgumentException Thrown if {@link SharedPrefs#getJwt()} is empty.
     */
    public Observable<Response<List<CategoryDTO>>> getAllCategories()
            throws IllegalArgumentException {
        String jwt = sharedPrefs.getJwt();
        if (jwt.isEmpty()) {
            throw new IllegalArgumentException("No login credentials");
        }
        String jwtHeader = "jwt=" + jwt;

        return apiDao.getAllCategories(jwtHeader);
    }

    /**
     * Get an observable for the API call to retrieve all Categories from the server updated after
     * the given timestamp.
     *
     * @param timestamp Timestamp of millis since epoch in UTC.
     * @return          Observable List of CategoryDTO objects.
     */
    public Observable<Response<List<CategoryDTO>>> getAllCategoriesUpdatedAfterTimestamp(long timestamp) {
        String jwt = sharedPrefs.getJwt();
        if (jwt.isEmpty()) {
            throw new IllegalArgumentException("No login credentials");
        }
        String jwtHeader = "jwt=" + jwt;

        return apiDao.getCategoriesUpdatedAfterTimestamp(jwtHeader, timestamp);
    }

    /**
     * Get an observable for the API call to retrieve all SubCategories from the server.
     *
     * @return                          Observable List of SubCategoryDTO objects.
     * @throws IllegalArgumentException Thrown if {@link SharedPrefs#getJwt()} is empty.
     */
    public Observable<Response<List<SubCategoryDTO>>> getAllSubCategories()
            throws IllegalArgumentException {
        String jwt = sharedPrefs.getJwt();
        if (jwt.isEmpty()) {
            throw new IllegalArgumentException("No login credentials");
        }
        String jwtHeader = "jwt=" + jwt;

        return apiDao.getAllSubCategories(jwtHeader);
    }

    /**
     * Get an observable for the API call to retrieve all SubCategories from the server updated
     * after the given timestamp.
     *
     * @param timestamp Timestamp of millis since epoch in UTC.
     * @return          Observable List of SubCategoryDTO objects.
     */
    public Observable<Response<List<SubCategoryDTO>>> getAllSubCategoriesUpdatedAfterTimestamp(long timestamp) {
        String jwt = sharedPrefs.getJwt();
        if (jwt.isEmpty()) {
            throw new IllegalArgumentException("No login credentials");
        }
        String jwtHeader = "jwt=" + jwt;

        return apiDao.getSubCategoriesUpdatedAfterTimestamp(jwtHeader, timestamp);
    }

    /**
     * Get an observable for the API call to retrieve a Drill by its server ID.
     *
     * @param serverDrillId             Drill's server ID.
     * @return                          Observable DrillDTO.
     * @throws IllegalArgumentException Thrown if {@link SharedPrefs#getJwt()} is empty.
     */
    public Observable<DrillDTO> getDrill(@NonNull Long serverDrillId)
            throws IllegalArgumentException {
        String jwt = sharedPrefs.getJwt();
        if (jwt.isEmpty()) {
            throw new IllegalArgumentException("No login credentials");
        }
        String jwtHeader = "jwt=" + jwt;

        return apiDao.getDrillById(jwtHeader, serverDrillId);
    }

    /**
     * Create a String URI for accessing an instruction's video.
     *
     * @param videoId   Video ID.
     * @return          String URI.
     */
    @NonNull
    public static String createVideoUri(@NonNull String videoId) {
        return Constants.SERVER_URL +
                "/videos/" +
                videoId +
                "/stream";
    }

    /**
     * Create a Map containing the necessary headers for API access.
     *
     * @param jwt   String JWT.
     * @return      Map containing the headers.
     */
    @NonNull
    public static Map<String, String> getHeaders(@NonNull String jwt) {
        return Map.of(
                "Content-Type", "application/json",
                "Cookie", "jwt=" + jwt
        );
    }
}
