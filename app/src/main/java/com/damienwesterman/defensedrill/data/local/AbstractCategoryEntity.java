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

import androidx.annotation.NonNull;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

public abstract class AbstractCategoryEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    private String name;
    private String description;

    /**
     * Fully parameterized constructor - for Room DB only.
     *
     * @param id            RoomDB generated id.
     * @param name          Name of the category.
     * @param description   Description of the category.
     */
    protected AbstractCategoryEntity(long id, @NonNull String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Name of the category.
     * @param description   Description of the category.
     */
    @Ignore
    public AbstractCategoryEntity(@NonNull String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Default Constructor.
     */
    @Ignore
    public AbstractCategoryEntity() {
        this.name = "";
        this.description = "";
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o || getClass() != o.getClass()) {
            return false;
        }
        AbstractCategoryEntity a = (AbstractCategoryEntity) o;
        return this.id == a.id
                && 0 == this.name.compareTo(a.name)
                && 0 == this.description.compareTo(a.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description);
    }
}
