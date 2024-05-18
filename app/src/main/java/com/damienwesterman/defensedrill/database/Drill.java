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

package com.damienwesterman.defensedrill.database;

import androidx.room.Embedded;
import androidx.room.Ignore;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

/**
 * Drill class contains all the information about a single drill.
 */
public class Drill {
    public static final int HIGH_CONFIDENCE = 0;
    public static final int MEDIUM_CONFIDENCE = 2;
    public static final int LOW_CONFIDENCE = 4;
    @Embedded
    private DrillEntity drillEntity;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(value = DrillGroupJoinEntity.class,
                    parentColumn = "drill_id", entityColumn = "group_id")
    )
    private List<GroupEntity> groups;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(value = DrillSubGroupJoinEntity.class,
                    parentColumn = "drill_id", entityColumn = "sub_group_id")
    )
    private List<SubGroupEntity> subGroups;

    /**
     * Default Constructor
     */
    @Ignore
    public Drill() {
        this.drillEntity = new DrillEntity();
        this.groups = new ArrayList<>();
        this.subGroups = new ArrayList<>();
    }

    /**
     * Parameterized constructor - ROOM DB ONLY
     *
     * @param drillEntity   DrillEntity
     * @param groups        GroupEntity list
     * @param subGroups     SubGroupEntity list
     */
    protected Drill(DrillEntity drillEntity, List<GroupEntity> groups, List<SubGroupEntity> subGroups) {
        this.drillEntity = drillEntity;
        this.groups = groups;
        this.subGroups = subGroups;
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
     * @param groups        List of groups the Drill belongs to
     * @param subGroups     List of subGroups the Drill belongs to
     */
    @Ignore
    public Drill(String name, long lastDrilled, boolean newDrill, int confidence,
                       String notes, long serverDrillId, List<GroupEntity> groups,
                       List<SubGroupEntity> subGroups) {
        this.drillEntity = new DrillEntity(name, lastDrilled, newDrill, confidence, notes,
                                            serverDrillId);
        this.groups = groups;
        this.subGroups = subGroups;
    }

    /**
     * Room DB only
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

    public void setName(String name) {
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

    public List<GroupEntity> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupEntity> groups) {
        this.groups = groups;
    }

    public List<SubGroupEntity> getSubGroups() {
        return subGroups;
    }

    public void setSubGroups(List<SubGroupEntity> subGroups) {
        this.subGroups = subGroups;
    }
}
