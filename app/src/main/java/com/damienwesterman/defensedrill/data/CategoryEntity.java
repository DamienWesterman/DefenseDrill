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

package com.damienwesterman.defensedrill.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import org.jetbrains.annotations.NotNull;

@Entity(indices = {@Index(value = {"name"}, unique = true)}, tableName = CategoryEntity.TABLE_NAME)
public class CategoryEntity extends AbstractCategoryEntity {
    @Ignore
    public static final String TABLE_NAME = "category";

    /**
     * Fully parameterized constructor - for Room DB only.
     *
     * @param id            RoomDB generated id.
     * @param name          Name of the category.
     * @param description   Description of the category.
     */
    protected CategoryEntity(long id, @NotNull String name, String description) {
        super(id, name, description);
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Name of the category.
     * @param description   Description of the category.
     */
    @Ignore
    public CategoryEntity(String name, @NotNull String description) {
        super(name, description);
    }

    /**
     * Default Constructor.
     */
    @Ignore
    public CategoryEntity() {
        super();
    }
}
