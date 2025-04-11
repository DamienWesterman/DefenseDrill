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
 * Copyright 2024 Damien Westerman
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

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Join table entity for the {@link DrillEntity} and {@link SubCategoryEntity} entities. This should
 * only be used by RoomDB internally.
 */
@Entity(
    tableName = DrillSubCategoryJoinEntity.TABLE_NAME,
    primaryKeys = {"drill_id", "sub_category_id"},
    foreignKeys = {
            @ForeignKey(entity = DrillEntity.class, parentColumns = "id",
                    childColumns = "drill_id", onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE),
            @ForeignKey(entity = SubCategoryEntity.class, parentColumns = "id",
                    childColumns = "sub_category_id", onDelete = ForeignKey.CASCADE,
                    onUpdate = ForeignKey.CASCADE)
    },
    indices = {
            @Index(value = {"drill_id"}),
            @Index(value = {"sub_category_id"})
    }
)
@AllArgsConstructor
@Getter
@Setter
/* package-private */ class DrillSubCategoryJoinEntity {
    @Ignore
    public static final String TABLE_NAME = "drill_sub_category_join";

    @ColumnInfo(name = "drill_id")
    private long drillId;
    @ColumnInfo(name = "sub_category_id")
    private long subCategoryId;
}
