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

package com.damienwesterman.defensedrill.data;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Drill class contains all the information about a single drill.
 */
public class Drill {
    public static final int HIGH_CONFIDENCE = 0;
    public static final int MEDIUM_CONFIDENCE = 2;
    public static final int LOW_CONFIDENCE = 4;
    public static final int INVALID_SERVER_DRILL_ID = -1;
    @Embedded
    private DrillEntity drillEntity;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(value = DrillCategoryJoinEntity.class,
                    parentColumn = "drill_id", entityColumn = "category_id")
    )
    private List<CategoryEntity> categories;

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
     * @param drillEntity   DrillEntity
     * @param categories        CategoryEntity list
     * @param subCategories     SubCategoryEntity list
     */
    protected Drill(DrillEntity drillEntity, List<CategoryEntity> categories, List<SubCategoryEntity> subCategories) {
        this.drillEntity = drillEntity;
        this.categories = categories;
        this.subCategories = subCategories;
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Drill name.
     * @param lastDrilled   Date (in milliseconds since epoch) the drill was last drilled.
     * @param newDrill      True = new drill.
     * @param confidence    Confidence level (HIGH/MEDIUM/LOW_CONFIDENCE).
     * @param notes         User notes on the drill.
     * @param serverDrillId ID of this drill on the server, for retrieving drill information
     * @param categories        List of categories the Drill belongs to
     * @param subCategories     List of subCategories the Drill belongs to
     */
    @Ignore
    public Drill(@NotNull String name, long lastDrilled, boolean newDrill, int confidence,
                 String notes, long serverDrillId, List<CategoryEntity> categories,
                 List<SubCategoryEntity> subCategories) {
        this.drillEntity = new DrillEntity(name, lastDrilled, newDrill, confidence, notes,
                                            serverDrillId);
        this.categories = categories;
        this.subCategories = subCategories;
    }

    /**
     * Internal use only
     */
    protected DrillEntity getDrillEntity() {
        return drillEntity;
    }

    /**
     * Room DB only
     */
    protected void setDrillEntity(DrillEntity drillEntity) {
        this.drillEntity = drillEntity;
    }

    public long getId() {
        return this.drillEntity.getId();
    }

    /**
     * Room DB only
     */
    protected void setId(long id) {
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

    public boolean isNewDrill() {
        return this.drillEntity.isNewDrill();
    }

    public void setNewDrill(boolean newDrill) {
        this.drillEntity.setNewDrill(newDrill);
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

    public long getServerDrillId() {
        return this.drillEntity.getServerDrillId();
    }

    public void setServerDrillId(long serverDrillId) {
        this.drillEntity.setServerDrillId(serverDrillId);
    }

    public List<CategoryEntity> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryEntity> categories) {
        this.categories = categories;
    }

    public void addCategory(CategoryEntity category) {
        this.categories.add(category);
    }

    public void removeCategory(CategoryEntity category) {
        this.categories.remove(category);
    }

    public List<SubCategoryEntity> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(List<SubCategoryEntity> subCategories) {
        this.subCategories = subCategories;
    }

    public void addSubCategory(SubCategoryEntity subCategory) {
        this.subCategories.add(subCategory);
    }

    public void removeSubCategory(SubCategoryEntity subCategory) {
        this.subCategories.remove(subCategory);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drill drill = (Drill) o;
        return drillEntity.equals(drill.drillEntity) && categories.equals(drill.categories) && subCategories.equals(drill.subCategories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(drillEntity, categories, subCategories);
    }
}
