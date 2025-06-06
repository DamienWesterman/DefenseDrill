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

import com.damienwesterman.defensedrill.data.local.CategoryEntity;
import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.SubCategoryEntity;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * DTO for Drills.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class DrillDTO implements Serializable {
    @NonNull
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private List<CategoryDTO> categories;
    @SerializedName(value = "sub_categories")
    @NonNull
    private List<SubCategoryDTO> subCategories;
    @NonNull
    private List<InstructionsDTO> instructions;
    @SerializedName("related_drills")
    @NonNull
    private List<RelatedDrillDTO> relatedDrills;

    /**
     * Convert the DTO into the locally used Drill object.
     *
     * @param categoryMap       Map of CategoryEntity objects mapped by their Server Id.
     * @param subCategoryMap    Map of SubCategoryEntity objects mapped by their Server Id.
     * @return                  Drill object.
     */
    @NonNull
    public Drill toDrill(@NonNull final Map<Long, CategoryEntity> categoryMap,
                         @NonNull final Map<Long, SubCategoryEntity> subCategoryMap) {
        Drill ret = new Drill(
                name,
                0,
                Drill.LOW_CONFIDENCE,
                "",
                id, // serverId
                false,
                new ArrayList<>(),
                new ArrayList<>()
        );

        for (CategoryDTO category : this.categories) {
            if (categoryMap.containsKey(category.getId())) {
                Optional.ofNullable(categoryMap.get(category.getId()))
                    .ifPresent(ret::addCategory);
            }
        }

        for (SubCategoryDTO subCategory : this.subCategories) {
            if (subCategoryMap.containsKey(subCategory.getId())) {
                Optional.ofNullable(subCategoryMap.get(subCategory.getId()))
                    .ifPresent(ret::addSubCategory);
            }
        }

        return ret;
    }
}
