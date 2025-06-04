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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity(indices = {@Index(value = {"name"}, unique = true)}, tableName = DrillEntity.TABLE_NAME)
@AllArgsConstructor
@Getter
@Setter
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
    /** Should correspond to values such as {@link Drill#LOW_CONFIDENCE} */
    private int confidence;
    @Nullable
    private String notes;
    /** ID of this drill on the server, for retrieving instructions and videos */
    @Nullable
    @ColumnInfo(name = "server_drill_id")
    private Long serverDrillId;
    /** Represents whether the user knows the drill and if it should be used during drill generation */
    private boolean isKnownDrill;

    /**
     * Default Constructor.
     */
    @Ignore
    public DrillEntity() {
        this.name = "";
        this.lastDrilled = 0;
        this.confidence = Drill.LOW_CONFIDENCE;
        this.notes = "";
        this.serverDrillId = null;
        this.isKnownDrill = false;
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Drill name.
     * @param lastDrilled   Date (in milliseconds since epoch) the drill was last drilled.
     * @param confidence    Confidence level (HIGH/MEDIUM/LOW_CONFIDENCE).
     * @param notes         User notes on the drill.
     * @param serverDrillId ID of this drill on the server, for retrieving drill information.
     * @param isKnownDrill  Represents whether the user knows the drill and if it should be used
     *                      during drill generation.
     */
    @Ignore
    public DrillEntity(@NonNull String name, long lastDrilled, int confidence,
                       @Nullable String notes, @Nullable Long serverDrillId, boolean isKnownDrill) {
        this.name = name;
        this.lastDrilled = lastDrilled;
        this.confidence = confidence;
        this.notes = notes;
        this.serverDrillId = serverDrillId;
        this.isKnownDrill = isKnownDrill;
    }

    public boolean isNewDrill() {
        return 0 >= lastDrilled;
    }
}
