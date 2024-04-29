package com.damienwesterman.defensedrill.database;

import androidx.room.Ignore;
import androidx.room.PrimaryKey;

public abstract class AbstractGroupEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private String description;

    /**
     * Fully parameterized constructor - for Room DB only.
     *
     * @param id            RoomDB generated id.
     * @param name          Name of the group.
     * @param description   Description of the group.
     */
    public AbstractGroupEntity(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Name of the group.
     * @param description   Description of the group.
     */
    @Ignore
    public AbstractGroupEntity(String name, String description) {
        this.id = -1;
        this.name = name;
        this.description = description;
    }

    /**
     * Default Constructor.
     */
    @Ignore
    public AbstractGroupEntity() {
        this.id = -1;
        this.name = "";
        this.description = "";
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
