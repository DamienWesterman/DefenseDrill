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

import com.damienwesterman.defensedrill.data.remote.dto.CategoryDTO;
import com.damienwesterman.defensedrill.data.remote.dto.DrillDTO;
import com.damienwesterman.defensedrill.data.remote.dto.SubCategoryDTO;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Retrofit interface for retrieving info from the DefenseDrill API.
 */
/* package-private */ interface ApiDao {
    @GET("api/drill")
    @Headers("Content-Type: application/json") // Need this so it knows it is an API request
    Observable<Response<List<DrillDTO>>> getAllDrills(@Header("Cookie") String jwtHeader);

    @GET("api/drill/update")
    @Headers("Content-Type: application/json") // Need this so it knows it is an API request
    Observable<Response<List<DrillDTO>>> getDrillsUpdatedAfterTimestamp(
            @Header("Cookie") String jwtHeader,
            @Query("updateTimestamp") long timestamp);

    @GET("api/category")
    @Headers("Content-Type: application/json") // Need this so it knows it is an API request
    Observable<Response<List<CategoryDTO>>> getAllCategories(@Header("Cookie") String jwtHeader);

    @GET("api/category/update")
    @Headers("Content-Type: application/json") // Need this so it knows it is an API request
    Observable<Response<List<CategoryDTO>>> getCategoriesUpdatedAfterTimestamp(
            @Header("Cookie") String jwtHeader,
            @Query("updateTimestamp") long timestamp);

    @GET("api/sub_category")
    @Headers("Content-Type: application/json") // Need this so it knows it is an API request
    Observable<Response<List<SubCategoryDTO>>> getAllSubCategories(@Header("Cookie") String jwtHeader);

    @GET("api/sub_category/update")
    @Headers("Content-Type: application/json") // Need this so it knows it is an API request
    Observable<Response<List<SubCategoryDTO>>> getSubCategoriesUpdatedAfterTimestamp(
            @Header("Cookie") String jwtHeader,
            @Query("updateTimestamp") long timestamp);

    @GET("api/drill/id/{id}")
    @Headers("Content-Type: application/json") // Need this so it knows it is an API request
    Observable<DrillDTO> getDrillById(@Header("Cookie") String jwtHeader,
                                      @Path("id") Long drillServerId);
}
