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

import java.util.List;

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
        return this.drillDao.getAll();
    }

    /**
     * Return a list of all Drills that belong to the specified group.
     *
     * @param group Specific group of drills.
     * @return      List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills(GroupEntity group) {
        return this.drillDao.findAllByGroup(group.getId());
    }

    /**
     * Return a list of all Drills that belong to the specified sub group.
     *
     * @param subGroup  Specific sub group of drills.
     * @return          List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills(SubGroupEntity subGroup) {
        return this.drillDao.findAllBySubGroup(subGroup.getId());
    }

    /**
     * Return a list of all Drills that belong to both the specified group and sub group.
     *
     * @param group     Specific group of drills.
     * @param subGroup  Specific sub group of drills.
     * @return          List of Drill objects.
     */
    public synchronized List<Drill> getAllDrills(GroupEntity group, SubGroupEntity subGroup) {
        return this.drillDao.findAllByGroupAndSubGroup(group.getId(), subGroup.getId());
    }

    /**
     * Return the drill that matches the given ID.
     *
     * @param id    ID of the drill to find.
     * @return      Drill object or null if the id does not exist in the database.
     */
    public synchronized Drill getDrill(long id) {
        return this.drillDao.findById(id);
    }

    /**
     * Return the drill that matches the given name;
     *
     * @param name  Name of the drill to find.
     * @return      Drill object or null if the name does not exist in the database.
     */
    public synchronized Drill getDrill(String name) {
        return this.drillDao.findByName(name);
    }

    /**
     * Insert or update the given drill(s).
     * <br><br>
     * If inserting a new Drill, caller is responsible for ensuring the name is unique,
     * otherwise the existing Drill will be updated. Caller is also responsible for ensuring
     * the groups and subGroups already exist.
     *
     * @param drills    Drill(s) to insert or update.
     */
    public synchronized void upsertDrills(Drill... drills) {
        db.runInTransaction(() -> {
            for (Drill drill : drills) {
                drillDao.upsert(drill.getDrillEntity());

                // Need to extract the auto generated ID in order to update the join tables
                Drill insertedDrill = drillDao.findByName(drill.getName());
                if (null != insertedDrill) {
                    long drillId = insertedDrill.getId();

                    for (GroupEntity group : drill.getGroups()) {
                        drillDao.upsert(new DrillGroupJoinEntity(drillId, group.getId()));
                    }
                    for (SubGroupEntity subGroup : drill.getSubGroups()) {
                        drillDao.upsert(new DrillSubGroupJoinEntity(drillId, subGroup.getId()));
                    }
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
        this.db.runInTransaction(() -> {
            for (Drill drill : drills) {
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
    public synchronized GroupEntity getOneGroup(long id) {
        return this.groupDao.findById(id);
    }

    /**
     * Find a group based on the given name.
     *
     * @param name  Group name.
     * @return      GroupEntity object.
     */
    public synchronized GroupEntity getOneGroup(String name) {
        return this.groupDao.findByName(name);
    }

    /**
     * Insert or update the given group(s).
     *
     * @param groups    Group(s) to insert or update.
     */
    public synchronized void upsertGroups(GroupEntity... groups) {
        this.groupDao.upsert(groups);
    }

    /**
     * Delete the given group(s).
     *
     * @param groups    Group(s) to delete.
     */
    public synchronized void deleteGroups(GroupEntity... groups) {
        this.groupDao.delete(groups);
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
        return this.subGroupDao.findAllByGroup(group.getId());
    }

    /**
     * Find a subGroup based on the given id.
     *
     * @param id    SubGroup ID.
     * @return      SubGroupEntity object.
     */
    public synchronized SubGroupEntity getOneSubGroup(long id) {
        return this.subGroupDao.findById(id);
    }

    /**
     * Find a subGroup based on the given name.
     *
     * @param name  SubGroup name.
     * @return      SubGroupEntity object.
     */
    public synchronized SubGroupEntity getOneSubGroup(String name) {
        return this.subGroupDao.findByName(name);
    }

    /**
     * Insert or update the given subGroup(s).
     *
     * @param subGroups SubGroup(s) to insert or update.
     */
    public synchronized void upsertSubGroups(SubGroupEntity... subGroups) {
        this.subGroupDao.upsert(subGroups);
    }

    /**
     * Delete the given subGroup(s).
     *
     * @param subGroups SubGroup(s) to delete.
     */
    public synchronized void deleteSubGroups(SubGroupEntity... subGroups) {
        this.subGroupDao.delete(subGroups);
    }
}
