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

package com.damienwesterman.defensedrill.data.remote.dto;

import androidx.annotation.NonNull;

import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO for SubCategories.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class SubCategoryDTO implements Serializable {
    @NonNull
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private String description;

    /**
     * Convert the DTO into the locally used SubCategoryEntity object.
     *
     * @return SubCategoryEntity object.
     */
    @NonNull
    public SubCategoryEntity toSubCategoryEntity() {
        return SubCategoryEntity.builder()
                .name(this.name)
                .description(this.description)
                .serverId(this.id)
                .build();
    }
}
