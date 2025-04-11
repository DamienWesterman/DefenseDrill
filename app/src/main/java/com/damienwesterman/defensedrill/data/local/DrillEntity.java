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
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import lombok.EqualsAndHashCode;

@Entity(indices = {@Index(value = {"name"}, unique = true)}, tableName = DrillEntity.TABLE_NAME)
@EqualsAndHashCode
/* package-private */ class DrillEntity {
    @Ignore
    public static final String TABLE_NAME = "drill";

    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    private String name;
    @ColumnInfo(name = "last_drilled")
    private long lastDrilled;
    private int confidence;
    private String notes;
    /** ID of this drill on the server, for retrieving instructions and videos */
    @Nullable
    @ColumnInfo(name = "server_drill_id")
    private Long serverDrillId;
    // TODO: Future PR - Implement the features that make use of this, and use it during drill generation
    /** Represents whether the user knows the drill and if it should be used during drill generation */
    private boolean isKnownDrill;

    /**
     * Default Constructor.
     */
    @Ignore
    public DrillEntity() {
        this.name = "";
        this.lastDrilled = System.currentTimeMillis();
        this.confidence = Drill.LOW_CONFIDENCE;
        this.notes = "";
        this.serverDrillId = null;
        this.isKnownDrill = false;
    }

    /**
     * Fully parameterized constructor - for Room DB only.
     *
     * @param id            RoomDB generated id.
     * @param name          Drill name.
     * @param lastDrilled   Date (in milliseconds since epoch) the drill was last drilled.
     * @param confidence    Confidence level (HIGH/MEDIUM/LOW_CONFIDENCE).
     * @param notes         User notes on the drill.
     * @param serverDrillId ID of this drill on the server, for retrieving drill information.
     * @param isKnownDrill    Represents whether the user knows the drill and if it should be used
     *                      during drill generation.
     */
    protected DrillEntity(long id, @NonNull String name, long lastDrilled, int confidence,
                          String notes, @Nullable Long serverDrillId, boolean isKnownDrill) {
        this.id = id;
        this.name = name;
        this.lastDrilled = lastDrilled;
        this.confidence = confidence;
        this.notes = notes;
        this.serverDrillId = serverDrillId;
        this.isKnownDrill = isKnownDrill;
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Drill name.
     * @param lastDrilled   Date (in milliseconds since epoch) the drill was last drilled.
     * @param confidence    Confidence level (HIGH/MEDIUM/LOW_CONFIDENCE).
     * @param notes         User notes on the drill.
     * @param serverDrillId ID of this drill on the server, for retrieving drill information
     * @param isKnownDrill    Represents whether the user knows the drill and if it should be used
     *                      during drill generation.
     */
    @Ignore
    public DrillEntity(@NonNull String name, long lastDrilled, int confidence,
                       String notes, @Nullable Long serverDrillId, boolean isKnownDrill) {
        this.name = name;
        this.lastDrilled = lastDrilled;
        this.confidence = confidence;
        this.notes = notes;
        this.serverDrillId = serverDrillId;
        this.isKnownDrill = isKnownDrill;
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

    public long getLastDrilled() {
        return lastDrilled;
    }

    public void setLastDrilled(long lastDrilled) {
        this.lastDrilled = lastDrilled;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getServerDrillId() {
        return this.serverDrillId;
    }

    public void setServerDrillId(Long serverDrillId) {
        this.serverDrillId = serverDrillId;
    }

    public boolean isKnownDrill() {
        return isKnownDrill;
    }

    public void setIsKnownDrill(boolean knownDrill) {
        this.isKnownDrill = knownDrill;
    }

    public boolean isNewDrill() {
        return 0 >= lastDrilled;
    }
}