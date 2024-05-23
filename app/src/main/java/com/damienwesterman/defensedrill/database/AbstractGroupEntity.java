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

import androidx.annotation.NonNull;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

public abstract class AbstractGroupEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @NonNull
    private String name;
    private String description;

    /**
     * Fully parameterized constructor - for Room DB only.
     *
     * @param id            RoomDB generated id.
     * @param name          Name of the group.
     * @param description   Description of the group.
     */
    protected AbstractGroupEntity(long id, @NonNull String name, String description) {
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
    public AbstractGroupEntity(@NonNull String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Default Constructor.
     */
    @Ignore
    public AbstractGroupEntity() {
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
        AbstractGroupEntity a = (AbstractGroupEntity) o;
        return this.id == a.id
                && 0 == this.name.compareTo(a.name)
                && 0 == this.description.compareTo(a.description);
    }
}
