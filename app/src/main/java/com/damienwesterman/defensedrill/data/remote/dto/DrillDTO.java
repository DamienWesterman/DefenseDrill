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

import com.damienwesterman.defensedrill.data.local.Drill;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

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
public class DrillDTO {
    private Long id;
    private String name;
    private List<CategoryDTO> categories;
    @SerializedName(value = "sub_categories")
    private List<SubCategoryDTO> subCategories;
    private List<InstructionsDTO> instructions;
    @SerializedName("related_drills")
    private List<RelatedDrillDTO> relatedDRills;

    /**
     * Convert the DTO into the locally used Drill object.
     *
     * @return Drill object.
     */
    @NonNull
    public Drill toDrill() {
        // TODO: Properly implement
        Drill ret = new Drill(
                name,
                0,
                true,
                Drill.LOW_CONFIDENCE,
                "",
                id, // serverId
                new ArrayList<>(),
                new ArrayList<>()
        );

        return ret;
    }
}
