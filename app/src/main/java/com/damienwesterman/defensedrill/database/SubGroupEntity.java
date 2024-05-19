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

@Entity(indices = {@Index(value = {"name"}, unique = true)}, tableName = SubGroupEntity.TABLE_NAME)
public class SubGroupEntity extends AbstractGroupEntity {
    @Ignore
    public static final String TABLE_NAME = "sub_group";

    /**
     * Fully parameterized constructor - for Room DB only.
     *
     * @param id            RoomDB generated id.
     * @param name          Name of the group.
     * @param description   Description of the group.
     */
    protected SubGroupEntity(long id, String name, String description) {
        super(id, name, description);
    }

    /**
     * Usable fully parameterized constructor.
     *
     * @param name          Name of the group.
     * @param description   Description of the group.
     */
    @Ignore
    public SubGroupEntity(String name, String description) {
        super(name, description);
    }

    /**
     * Default Constructor.
     */
    @Ignore
    public SubGroupEntity() {
        super();
    }
}
