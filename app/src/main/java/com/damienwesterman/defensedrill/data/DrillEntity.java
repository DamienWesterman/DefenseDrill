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

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(indices = {@Index(value = {"name"}, unique = true)}, tableName = DrillEntity.TABLE_NAME)
/* package-private */ class DrillEntity {
    @Ignore
    public static final String TABLE_NAME = "drill";

    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    private String name;
    @ColumnInfo(name = "last_drilled")
    private long lastDrilled;
    @ColumnInfo(name = "new_drill")
    private boolean newDrill;
    private int confidence;
    private String notes;
    /** ID of this drill on the server, for retrieving instructions and videos */
    @ColumnInfo(name = "server_drill_id")
    private long serverDrillId;

    /**
     * Default Constructor.
     */
    @Ignore
    public DrillEntity() {
        this.name = "";
        this.lastDrilled = System.currentTimeMillis();
        this.newDrill = true;
        this.confidence = Drill.LOW_CONFIDENCE;
        this.notes = "";
        this.serverDrillId = -1;
    }

    /**
     * Fully parameterized constructor - for Room DB only.
     *
     * @param id            RoomDB generated id.
     * @param name          Drill name.
     * @param lastDrilled   Date (in milliseconds since epoch) the drill was last drilled.
     * @param newDrill      True = new drill.
     * @param confidence    Confidence level (HIGH/MEDIUM/LOW_CONFIDENCE).
     * @param notes         User notes on the drill.
     * @param serverDrillId ID of this drill on the server, for retrieving drill information
     */
    protected DrillEntity(long id, @NonNull String name, long lastDrilled, boolean newDrill, int confidence,
                       String notes, long serverDrillId) {
        this.id = id;
        this.name = name;
        this.lastDrilled = lastDrilled;
        this.newDrill = newDrill;
        this.confidence = confidence;
        this.notes = notes;
        this.serverDrillId = serverDrillId;
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
     */
    @Ignore
    public DrillEntity(@NonNull String name, long lastDrilled, boolean newDrill, int confidence,
                       String notes, long serverDrillId) {
        this.name = name;
        this.lastDrilled = lastDrilled;
        this.newDrill = newDrill;
        this.confidence = confidence;
        this.notes = notes;
        this.serverDrillId = serverDrillId;
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

    public boolean isNewDrill() {
        return newDrill;
    }

    public void setNewDrill(boolean newDrill) {
        this.newDrill = newDrill;
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

    public long getServerDrillId() {
        return this.serverDrillId;
    }

    public void setServerDrillId(long serverDrillId) {
        this.serverDrillId = serverDrillId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DrillEntity that = (DrillEntity) o;
        return id == that.id && lastDrilled == that.lastDrilled && newDrill == that.newDrill && confidence == that.confidence && serverDrillId == that.serverDrillId && name.equals(that.name) && Objects.equals(notes, that.notes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, lastDrilled, newDrill, confidence, notes, serverDrillId);
    }
}