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

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Entity(indices = {@Index(value = {"name"}, unique = true)}, tableName = DrillEntity.TABLE_NAME)
public class DrillEntity {
    @Ignore
    public static final String TABLE_NAME = "drill";

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    @ColumnInfo(name = "last_drilled")
    private long lastDrilled;
    @ColumnInfo(name = "new_drill")
    private boolean newDrill;
    private int confidence;
    private String notes;
    /** Url for the description on how to perform this drill - To Be Implemented */
    @ColumnInfo(name = "how_to_desc_url")
    private String howToDescUrl;
    /** Url for the video on how to perform this drill - To Be Implemented */
    @ColumnInfo(name = "how_to_video_url")
    private String howToVideoUrl;

    /**
     * Default Constructor.
     */
    @Ignore
    public DrillEntity() {
        this.id = -1;
        this.name = "";
        this.lastDrilled = System.currentTimeMillis();
        this.newDrill = true;
        this.confidence = Drill.LOW_CONFIDENCE;
        this.notes = "";
        this.howToDescUrl = "";
        this.howToVideoUrl = "";
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
     * @param howToDescUrl  URL for description on how to perform the drill.
     * @param howToVideoUrl URL for video on how to perform the drill.
     */
    public DrillEntity(long id, String name, long lastDrilled, boolean newDrill, int confidence,
                       String notes, String howToDescUrl, String howToVideoUrl) {
        this.id = id;
        this.name = name;
        this.lastDrilled = lastDrilled;
        this.newDrill = newDrill;
        this.confidence = confidence;
        this.notes = notes;
        this.howToDescUrl = howToDescUrl;
        this.howToVideoUrl = howToVideoUrl;
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Drill name.
     * @param lastDrilled   Date (in milliseconds since epoch) the drill was last drilled.
     * @param newDrill      True = new drill.
     * @param confidence    Confidence level (HIGH/MEDIUM/LOW_CONFIDENCE).
     * @param notes         User notes on the drill.
     * @param howToDescUrl  URL for description on how to perform the drill.
     * @param howToVideoUrl URL for video on how to perform the drill.
     */
    @Ignore
    public DrillEntity(String name, long lastDrilled, boolean newDrill, int confidence,
                       String notes, String howToDescUrl, String howToVideoUrl) {
        this.id = -1;
        this.name = name;
        this.lastDrilled = lastDrilled;
        this.newDrill = newDrill;
        this.confidence = confidence;
        this.notes = notes;
        this.howToDescUrl = howToDescUrl;
        this.howToVideoUrl = howToVideoUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
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

    public String getHowToDescUrl() {
        return howToDescUrl;
    }

    public void setHowToDescUrl(String howToDescUrl) {
        this.howToDescUrl = howToDescUrl;
    }

    public String getHowToVideoUrl() {
        return howToVideoUrl;
    }

    public void setHowToVideoUrl(String howToVideoUrl) {
        this.howToVideoUrl = howToVideoUrl;
    }
}