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

@Entity(indices = {@Index(value = {"name"}, unique = true)}, tableName = SubCategoryEntity.TABLE_NAME)
public class SubCategoryEntity extends AbstractCategoryEntity {
    @Ignore
    public static final String TABLE_NAME = "sub_category";

    /**
     * Fully parameterized constructor - for Room DB only.
     *
     * @param id            RoomDB generated id.
     * @param name          Name of the subCategory.
     * @param description   Description of the subCategory.
     */
    protected SubCategoryEntity(long id, @NotNull String name, String description) {
        super(id, name, description);
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Name of the subCategory.
     * @param description   Description of the subCategory.
     */
    @Ignore
    public SubCategoryEntity(@NotNull String name, String description) {
        super(name, description);
    }

    /**
     * Default Constructor.
     */
    @Ignore
    public SubCategoryEntity() {
        super();
    }
}
