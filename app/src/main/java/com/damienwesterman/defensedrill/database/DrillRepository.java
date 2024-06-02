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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
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
     * @param groupId   ID of the specific group of drills.
     * @return          List of Drill objects.
     */
    public synchronized List<Drill> getAllDrillsByGroupId(long groupId) {
        return this.drillDao.findAllDrillsByGroup(groupId);
    }

    /**
     * Return a list of all Drills that belong to the specified sub group.
     *
     * @param subGroupId    ID of the specific sub group of drills.
     * @return              List of Drill objects.
     */
    public synchronized List<Drill> getAllDrillsBySubGroupId(long subGroupId) {
        return this.drillDao.findAllDrillsBySubGroup(subGroupId);
    }

    /**
     * Return a list of all Drills that belong to both the specified group and sub group.
     *
     * @param groupId       ID of the specific group of drills.
     * @param subGroupId    ID of the specific sub group of drills.
     * @return              List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills(long groupId, long subGroupId) {
        return this.drillDao.findAllDrillsByGroupAndSubGroup(groupId, subGroupId);
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
     * @return boolean. True if <i>all</i> inserts succeeded. False if <i>ANY SINGLE</i> insert fails.
     */
    public synchronized boolean insertDrills(Drill... drills) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == drills) {
            return success.get();
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

                if (1 != drillDao.insert(drill.getDrillEntity()).length) {
                    success.set(false);
                }

                // Need to extract the auto generated ID in order to update the join tables
                Drill insertedDrill = drillDao.findDrillByName(drill.getName());
                if (null != insertedDrill) {
                    long drillId = insertedDrill.getId();

                    for (GroupEntity group : drill.getGroups()) {
                        if (null == group) {
                            success.set(false);
                            continue;
                        }
                        if (1 != drillDao.insert(new DrillGroupJoinEntity(drillId, group.getId())).length) {
                            success.set(false);
                        }
                    }
                    for (SubGroupEntity subGroup : drill.getSubGroups()) {
                        if (null == subGroup) {
                            success.set(false);
                            continue;
                        }
                        if (1 != drillDao.insert(new DrillSubGroupJoinEntity(drillId, subGroup.getId())).length) {
                            success.set(false);
                        }
                    }
                }
            }
        });

        return success.get();
    }

    /**
     * Update the given drill(s).
     *
     * @param drills    Drill(s) to update.
     * @throws SQLiteConstraintException If name is not unique, name is null, or a group/subgroup does not exist.
     * @return boolean. True if <i>all</i> updates succeeded. False if <i>ANY SINGLE</i> updates fails.
     */
    public synchronized boolean updateDrills(Drill... drills) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == drills) {
            return success.get();
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

                if (1 != drillDao.update(drill.getDrillEntity())) {
                    success.set(false);
                }

                long drillId = drill.getId();

                // Add/remove new/removed groups
                Set<Long> existingGroupIds = drillDao.findAllGroupJoinByDrillId(drillId).stream()
                        .filter(Objects::nonNull)
                        .map(DrillGroupJoinEntity::getGroupId)
                        .collect(Collectors.toSet());

                Set<Long> newGroupIds = drill.getGroups().stream()
                        .filter(Objects::nonNull)
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
                    if (1 != drillDao.insert(new DrillGroupJoinEntity(drillId, groupId)).length) {
                        success.set(false);
                    }
                }


                // Add/remove new/removed groups
                Set<Long> existingSubGroupIds = drillDao.findAllSubGroupJoinByDrillId(drillId).stream()
                        .filter(Objects::nonNull)
                        .map(DrillSubGroupJoinEntity::getSubGroupId)
                        .collect(Collectors.toSet());

                Set<Long> newSubGroupIds = drill.getSubGroups().stream()
                        .filter(Objects::nonNull)
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
                    if (1 != drillDao.insert(new DrillSubGroupJoinEntity(drillId, subGroupId)).length) {
                        success.set(false);
                    }
                }
            }
        });

        return success.get();
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
     * @return      GroupEntity object or null if the id does not exist in the database.
     */
    public synchronized GroupEntity getGroup(long id) {
        return this.groupDao.findById(id);
    }

    /**
     * Find a group based on the given name.
     *
     * @param name  Group name.
     * @return      GroupEntity object or null if the name does not exist in the database.
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
     * @return boolean. True if <i>all</i> inserts succeeded. False if <i>ANY SINGLE</i> insert fails.
     */
    public synchronized boolean insertGroups(GroupEntity... groups) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == groups) {
            return success.get();
        }
        db.runInTransaction(() -> {
            for (GroupEntity group : groups) {
                if (null == group) {
                    continue;
                }
                if (1 != this.groupDao.insert(group).length) {
                    success.set(false);
                }
            }

        });

        return success.get();
    }

    /**
     * Update the given group(s).
     *
     * @param groups    Group(s) to update.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     * @return boolean. True if <i>all</i> updates succeeded. False if <i>ANY SINGLE</i> updates fails.
     */
    public synchronized boolean updateGroups(GroupEntity... groups) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == groups) {
            return success.get();
        }
        db.runInTransaction(() -> {
            for (GroupEntity group : groups) {
                if (null == group) {
                    continue;
                }
                if (1 != this.groupDao.update(group)) {
                    success.set(false);
                }
            }

        });

        return success.get();
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
     * @param groupId   ID of the group to search for its subGroups.
     * @return          List of SubGroupEntity objects.
     */
    public synchronized List<SubGroupEntity> getAllSubGroups(long groupId) {
        return this.subGroupDao.findAllByGroup(groupId);
    }

    /**
     * Find a subGroup based on the given id.
     *
     * @param id    SubGroup ID.
     * @return      SubGroupEntity object or null if the id does not exist in the database.
     */
    public synchronized SubGroupEntity getSubGroup(long id) {
        return this.subGroupDao.findById(id);
    }

    /**
     * Find a subGroup based on the given name.
     *
     * @param name  SubGroup name.
     * @return      SubGroupEntity object or null if the name does not exist in the database.
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
     * @return boolean. True if <i>all</i> inserts succeeded. False if <i>ANY SINGLE</i> insert fails.
     */
    public synchronized boolean insertSubGroups(SubGroupEntity... subGroups) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == subGroups) {
            return success.get();
        }

        db.runInTransaction(() -> {
            for (SubGroupEntity subGroup : subGroups) {
                if (null == subGroup) {
                    continue;
                }
                if (1 != this.subGroupDao.insert(subGroup).length) {
                    success.set(false);
                }
            }

        });

        return success.get();
    }

    /**
     * Update the given subGroup(s).
     *
     * @param subGroups SubGroup(s) to update.
     * @throws SQLiteConstraintException If name is not unique or name is null.
     * @return boolean. True if <i>all</i> updates succeeded. False if <i>ANY SINGLE</i> updates fails.
     */
    public synchronized boolean updateSubGroups(SubGroupEntity... subGroups) {
        AtomicBoolean success = new AtomicBoolean(true);

        if (null == subGroups) {
            return success.get();
        }
        db.runInTransaction(() -> {
            for (SubGroupEntity subGroup : subGroups) {
                if (null == subGroup) {
                    continue;
                }
                if (1 != this.subGroupDao.update(subGroup)) {
                    success.set(false);
                }
            }

        });

        return success.get();
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
