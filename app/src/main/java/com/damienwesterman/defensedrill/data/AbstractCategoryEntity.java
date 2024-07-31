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
