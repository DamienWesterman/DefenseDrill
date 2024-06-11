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

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;

import org.jetbrains.annotations.NotNull;

@Entity(indices = {@Index(value = {"name"}, unique = true)}, tableName = CategoryEntity.TABLE_NAME)
public class CategoryEntity extends AbstractCategoryEntity {
    @Ignore
    public static final String TABLE_NAME = "category";

    /**
     * Fully parameterized constructor - for Room DB only.
     *
     * @param id            RoomDB generated id.
     * @param name          Name of the category.
     * @param description   Description of the category.
     */
    protected CategoryEntity(long id, @NotNull String name, String description) {
        super(id, name, description);
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Name of the category.
     * @param description   Description of the category.
     */
    @Ignore
    public CategoryEntity(String name, @NotNull String description) {
        super(name, description);
    }

    /**
     * Default Constructor.
     */
    @Ignore
    public CategoryEntity() {
        super();
    }
}
