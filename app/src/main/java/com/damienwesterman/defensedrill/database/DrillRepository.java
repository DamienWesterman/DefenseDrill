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

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class is used to interact with the SQLite database.
 * <br>
 * Call {@link DrillRepository#getInstance(Context)} to use the repo.
 * <br><br>
 * All methods are synchronized, and thus all calls are thread safe.
 */
public class DrillRepository {
    private static DrillRepository instance;

    private final DrillDatabase db;
    private final DrillDao drillDao;
    private final GroupDao groupDao;
    private final SubGroupDao subGroupDao;

    /**
     * Private constructor, access class with {@link #getInstance(Context context)}.
     *
     * @param context   Application context.
     */
    private DrillRepository(Context context) {
        this.db = DrillDatabase.getInstance(context);
        this.drillDao = this.db.getDrillDao();
        this.groupDao = this.db.getGroupDao();
        this.subGroupDao = this.db.getSubGroupDao();
    }

    /**
     * Get running DrillRepository instance.
     *
     * @param context Application context.
     * @return DrillRepository instance.
     */
    public synchronized static DrillRepository getInstance(Context context) {
        if ( null == instance) {
            instance = new DrillRepository(context.getApplicationContext());
        }

        return instance;
    }

    /**
     * Return a list of all Drills in the database.
     *
     * @return  List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills() {
        return this.drillDao.getAllDrills();
    }

    /**
     * Return a list of all Drills that belong to the specified group.
     *
     * @param group Specific group of drills.
     * @return      List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills(GroupEntity group) {
        if (null == group) {
            return new ArrayList<>();
        }
        return this.drillDao.findAllDrillsByGroup(group.getId());
    }

    /**
     * Return a list of all Drills that belong to the specified sub group.
     *
     * @param subGroup  Specific sub group of drills.
     * @return          List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills(SubGroupEntity subGroup) {
        if (null == subGroup) {
            return new ArrayList<>();
        }
        return this.drillDao.findAllDrillsBySubGroup(subGroup.getId());
    }

    /**
     * Return a list of all Drills that belong to both the specified group and sub group.
     *
     * @param group     Specific group of drills.
     * @param subGroup  Specific sub group of drills.
     * @return          List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills(GroupEntity group, SubGroupEntity subGroup) {
        if (null == group || null == subGroup) {
            return new ArrayList<>();
        }
        return this.drillDao.findAllDrillsByGroupAndSubGroup(group.getId(), subGroup.getId());
    }

    /**
     * Return the drill that matches the given ID.
     *
     * @param id    ID of the drill to find.
     * @return      Drill object or null if the id does not exist in the database.
     */
    public synchronized Drill getDrill(long id) {
        return this.drillDao.findDrillById(id);
    }

    /**
     * Return the drill that matches the given name;
     *
     * @param name  Name of the drill to find.
     * @return      Drill object or null if the name does not exist in the database.
     */
    public synchronized Drill getDrill(String name) {
        if (null == name) {
            return null;
        }
        return this.drillDao.findDrillByName(name);
    }

    /**
     * Insert the given drill(s).
     *
     * @param drills    Drill(s) to insert.
     * @throws SQLiteConstraintException If name is not unique, name is null, or a group/subgroup does not exist.
     */
    public synchronized void insertDrills(Drill... drills) {
        if (null == drills) {
            return;
        }

        db.runInTransaction(() -> {
            for (Drill drill : drills) {
                if (null == drill) {
                    continue;
                }

                if (null == drill.getGroups()) {
                    drill.setGroups(new ArrayList<>());
                }
                if (null == drill.getSubGroups()) {
                    drill.setSubGroups(new ArrayList<>());
                }

                drillDao.insert(drill.getDrillEntity());

                // Need to extract the auto generated ID in order to update the join tables
                Drill insertedDrill = drillDao.findDrillByName(drill.getName());
                if (null != insertedDrill) {
                    long drillId = insertedDrill.getId();

                    for (GroupEntity group : drill.getGroups()) {
                        drillDao.insert(new DrillGroupJoinEntity(drillId, group.getId()));
                    }
                    for (SubGroupEntity subGroup : drill.getSubGroups()) {
                        drillDao.insert(new DrillSubGroupJoinEntity(drillId, subGroup.getId()));
                    }
                }
            }
        });
    }

    /**
     * Update the given drill(s).
     *
     * @param drills    Drill(s) to update.
     * @throws SQLiteConstraintException If name is not unique, name is null, or a group/subgroup does not exist.
     */
    public synchronized void updateDrills(Drill... drills) {
        if (null == drills) {
            return;
        }

        db.runInTransaction(() -> {
            for (Drill drill : drills) {
                if (null == drill) {
                    continue;
                }
                if (null == drill.getGroups()) {
                    drill.setGroups(new ArrayList<>());
                }
                if (null == drill.getSubGroups()) {
                    drill.setSubGroups(new ArrayList<>());
                }

                drillDao.update(drill.getDrillEntity());

                long drillId = drill.getId();

                // Add/remove new/removed groups
                Set<Long> existingGroupIds = drillDao.findAllGroupJoinByDrillId(drillId).stream()
                        .map(DrillGroupJoinEntity::getGroupId)
                        .collect(Collectors.toSet());

                Set<Long> newGroupIds = drill.getGroups().stream()
                        .map(GroupEntity::getId)
                        .collect(Collectors.toSet());

                Set<Long> groupsToRemove = new HashSet<>(existingGroupIds);
                groupsToRemove.removeAll(newGroupIds);

                Set<Long> groupsToAdd = new HashSet<>(newGroupIds);
                groupsToAdd.removeAll(existingGroupIds);

                for (Long groupId : groupsToRemove) {
                    drillDao.delete(new DrillGroupJoinEntity(drillId, groupId));
                }

                for (Long groupId : groupsToAdd) {
                    drillDao.insert(new DrillGroupJoinEntity(drillId, groupId));
                }


                // Add/remove new/removed groups
                Set<Long> existingSubGroupIds = drillDao.findAllSubGroupJoinByDrillId(drillId).stream()
                        .map(DrillSubGroupJoinEntity::getSubGroupId)
                        .collect(Collectors.toSet());

                Set<Long> newSubGroupIds = drill.getSubGroups().stream()
                        .map(SubGroupEntity::getId)
                        .collect(Collectors.toSet());

                Set<Long> subGroupsToRemove = new HashSet<>(existingSubGroupIds);
                groupsToRemove.removeAll(newSubGroupIds);

                Set<Long> subGroupsToAdd = new HashSet<>(newSubGroupIds);
                groupsToAdd.removeAll(existingSubGroupIds);

                for (Long subGroupId : subGroupsToRemove) {
                    drillDao.delete(new DrillSubGroupJoinEntity(drillId, subGroupId));
                }

                for (Long subGroupId : subGroupsToAdd) {
                    drillDao.insert(new DrillSubGroupJoinEntity(drillId, subGroupId));
                }
            }
        });
    }

    /**
     * Delete the given drill(s).
     *
     * @param drills    Drill(s) to delete.
     */
    public synchronized void deleteDrills(Drill... drills) {
        if (null == drills) {
            return;
        }
        this.db.runInTransaction(() -> {
            for (Drill drill : drills) {
                if (null == drill) {
                    continue;
                }
                this.drillDao.delete(drill.getDrillEntity());
            }
        });
    }

    /**
     * Get all groups in the database.
     *
     * @return  List of GroupEntity objects.
     */
    public synchronized List<GroupEntity> getAllGroups() {
        return this.groupDao.getAll();
    }

    /**
     * Find a group based on the given id.
     *
     * @param id    Group ID.
     * @return      GroupEntity object.
     */
    public synchronized GroupEntity getGroup(long id) {
        return this.groupDao.findById(id);
    }

    /**
     * Find a group based on the given name.
     *
     * @param name  Group name.
     * @return      GroupEntity object.
     */
    public synchronized GroupEntity getGroup(String name) {
        if (null == name) {
            return null;
        }
        return this.groupDao.findByName(name);
    }

    /**
     * Insert the given group(s).
     *
     * @param groups    Group(s) to insert.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     */
    public synchronized void insertGroups(GroupEntity... groups) {
        if (null == groups) {
            return;
        }
        db.runInTransaction(() -> {
            for (GroupEntity group : groups) {
                if (null == group) {
                    continue;
                }
                this.groupDao.insert(group);
            }

        });
    }

    /**
     * Update the given group(s).
     *
     * @param groups    Group(s) to update.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     */
    public synchronized void updateGroups(GroupEntity... groups) {
        if (null == groups) {
            return;
        }
        db.runInTransaction(() -> {
            for (GroupEntity group : groups) {
                if (null == group) {
                    continue;
                }
                this.groupDao.update(group);
            }

        });
    }

    /**
     * Delete the given group(s).
     *
     * @param groups    Group(s) to delete.
     */
    public synchronized void deleteGroups(GroupEntity... groups) {
        if (null == groups) {
            return;
        }
        db.runInTransaction(() -> {
            for (GroupEntity group : groups) {
                if (null == group) {
                    continue;
                }
                this.groupDao.delete(group);
            }

        });
    }

    /**
     * Get all subGroups in the database.
     *
     * @return  List of SubGroupEntity objects.
     */
    public synchronized List<SubGroupEntity> getAllSubGroups() {
        return this.subGroupDao.getAll();
    }

    /**
     * Get all subGroups of a given group.
     *
     * @param group Group to search for its subGroups.
     * @return      List of SubGroupEntity objects.
     */
    public synchronized List<SubGroupEntity> getAllSubGroups(GroupEntity group) {
        if (null == group) {
            return null;
        }
        return this.subGroupDao.findAllByGroup(group.getId());
    }

    /**
     * Find a subGroup based on the given id.
     *
     * @param id    SubGroup ID.
     * @return      SubGroupEntity object.
     */
    public synchronized SubGroupEntity getSubGroup(long id) {
        return this.subGroupDao.findById(id);
    }

    /**
     * Find a subGroup based on the given name.
     *
     * @param name  SubGroup name.
     * @return      SubGroupEntity object.
     */
    public synchronized SubGroupEntity getSubGroup(String name) {
        if (null == name) {
            return null;
        }
        return this.subGroupDao.findByName(name);
    }

    /**
     * Insert the given subGroup(s).
     *
     * @param subGroups SubGroup(s) to insert.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     */
    public synchronized void insertSubGroups(SubGroupEntity... subGroups) {
        if (null == subGroups) {
            return;
        }
        db.runInTransaction(() -> {
            for (SubGroupEntity subGroup : subGroups) {
                if (null == subGroup) {
                    continue;
                }
                this.subGroupDao.insert(subGroup);
            }

        });
    }

    /**
     * Update the given subGroup(s).
     *
     * @param subGroups SubGroup(s) to update.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     */
    public synchronized void updateSubGroups(SubGroupEntity... subGroups) {
        if (null == subGroups) {
            return;
        }
        db.runInTransaction(() -> {
            for (SubGroupEntity subGroup : subGroups) {
                if (null == subGroup) {
                    continue;
                }
                this.subGroupDao.update(subGroup);
            }

        });
    }

    /**
     * Delete the given subGroup(s).
     *
     * @param subGroups SubGroup(s) to delete.
     */
    public synchronized void deleteSubGroups(SubGroupEntity... subGroups) {
        if (null == subGroups) {
            return;
        }
        db.runInTransaction(() -> {
            for (SubGroupEntity subGroup : subGroups) {
                if (null == subGroup) {
                    continue;
                }
                this.subGroupDao.delete(subGroup);
            }

        });
    }
}
