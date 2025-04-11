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

import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Drill class contains all the information about a single drill.
 */
@ToString
@EqualsAndHashCode
public class Drill {
    public static final int HIGH_CONFIDENCE = 0;
    public static final int MEDIUM_CONFIDENCE = 2;
    public static final int LOW_CONFIDENCE = 4;
    public static final int INVALID_SERVER_DRILL_ID = -1;
    @Embedded
    private DrillEntity drillEntity;

    @Setter
    @Getter
    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(value = DrillCategoryJoinEntity.class,
                    parentColumn = "drill_id", entityColumn = "category_id")
    )
    private List<CategoryEntity> categories;

    @Setter
    @Getter
    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(value = DrillSubCategoryJoinEntity.class,
                    parentColumn = "drill_id", entityColumn = "sub_category_id")
    )
    private List<SubCategoryEntity> subCategories;

    /**
     * Default Constructor
     */
    @Ignore
    public Drill() {
        this.drillEntity = new DrillEntity();
        this.categories = new ArrayList<>();
        this.subCategories = new ArrayList<>();
    }

    /**
     * Parameterized constructor - ROOM DB ONLY
     *
     * @param drillEntity       DrillEntity
     * @param categories        CategoryEntity list
     * @param subCategories     SubCategoryEntity list
     */
    Drill(DrillEntity drillEntity, List<CategoryEntity> categories, List<SubCategoryEntity> subCategories) {
        this.drillEntity = drillEntity;
        this.categories = categories;
        this.subCategories = subCategories;
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Drill name.
     * @param lastDrilled   Date (in milliseconds since epoch) the drill was last drilled.
     * @param confidence    Confidence level (HIGH/MEDIUM/LOW_CONFIDENCE).
     * @param notes         User notes on the drill.
     * @param serverDrillId ID of this drill on the server, for retrieving drill information
     * @param isKnownDrill  Represents whether the user knows the drill and if it should be used
     *                      during drill generation.
     * @param categories    List of categories the Drill belongs to
     * @param subCategories List of subCategories the Drill belongs to
     */
    @Ignore
    public Drill(@NotNull String name, long lastDrilled, int confidence,
                 String notes, @Nullable Long serverDrillId, boolean isKnownDrill,
                 List<CategoryEntity> categories, List<SubCategoryEntity> subCategories) {
        this.drillEntity = new DrillEntity(name, lastDrilled, confidence, notes,
                                            serverDrillId, isKnownDrill);
        this.categories = categories;
        this.subCategories = subCategories;
    }

    /**
     * Internal use only
     */
    DrillEntity getDrillEntity() {
        return drillEntity;
    }

    /**
     * Room DB only
     */
    void setDrillEntity(DrillEntity drillEntity) {
        this.drillEntity = drillEntity;
    }

    public long getId() {
        return this.drillEntity.getId();
    }

    /**
     * Room DB only
     */
    void setId(long id) {
        this.drillEntity.setId(id);
    }

    public String getName() {
        return this.drillEntity.getName();
    }

    public void setName(@NotNull String name) {
        this.drillEntity.setName(name);
    }

    public long getLastDrilled() {
        return this.drillEntity.getLastDrilled();
    }

    public void setLastDrilled(long lastDrilled) {
        this.drillEntity.setLastDrilled(lastDrilled );
    }

    public int getConfidence() {
        return this.drillEntity.getConfidence();
    }

    public void setConfidence(int confidence) {
        this.drillEntity.setConfidence(confidence);
    }

    public String getNotes() {
        return this.drillEntity.getNotes();
    }

    public void setNotes(String notes) {
        this.drillEntity.setNotes(notes);
    }

    public Long getServerDrillId() {
        return this.drillEntity.getServerDrillId();
    }

    public void setServerDrillId(Long serverDrillId) {
        this.drillEntity.setServerDrillId(serverDrillId);
    }

    public boolean isKnownDrill() {
        return this.drillEntity.isKnownDrill();
    }

    public void setIsKnownDrill(boolean isKnownDrill) {
        this.drillEntity.setKnownDrill(isKnownDrill);
    }

    public void addCategory(CategoryEntity category) {
        this.categories.add(category);
    }

    public void removeCategory(CategoryEntity category) {
        this.categories.remove(category);
    }

    public void addSubCategory(SubCategoryEntity subCategory) {
        this.subCategories.add(subCategory);
    }

    public void removeSubCategory(SubCategoryEntity subCategory) {
        this.subCategories.remove(subCategory);
    }

    public boolean isNewDrill() {
        return this.drillEntity.isNewDrill();
    }
}
