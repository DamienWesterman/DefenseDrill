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

package com.damienwesterman.defensedrill.services;

import androidx.annotation.Nullable;

import com.damienwesterman.defensedrill.database.Drill;
import com.damienwesterman.defensedrill.database.DrillRepository;
import com.damienwesterman.defensedrill.database.GroupEntity;
import com.damienwesterman.defensedrill.database.SubGroupEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * This service class is responsible for generating a pseudo random drill.
 * <br><br>
 * First call should be to any {@link DrillGenerator#generateDrill()} type method, <i>after</i>
 * which {@link DrillGenerator#regenerateDrill()} can be called to skip the first drill and select a
 * different drill that matches the criteria given to the first generateDrill() call. The drills
 * skipped by regenerateDrill() can be added back into the list of possibilities by calling
 * {@link DrillGenerator#resetSkippedDrills()}.
 * <br><br>
 * Each drill that matches the criteria given to the generateDrill() has a weighted possibility to
 * be chosen. These weights are determined by the last date the drill was drilled and the user
 * defined confidence level. If there are any drills marked as new in the list, the returned drill
 * (by generateDrill() or regenerateDrill()) is <i>guaranteed</i> to be a new drill.
 */
public class DrillGenerator {
    private static final int NO_DRILL_GENERATED = -1;

    private final DrillRepository repository;
    private final Random random;
    private Map<Long, Drill> drillPossibilities;
    private final Map<Long, Drill> skippedDrillsIndexes;
    private long lastGeneratedDrillId;

    /**
     * Constructor.
     *
     * @param repository    DrillRepository instance.
     */
    public DrillGenerator(DrillRepository repository) {
        this.repository = repository;
        this.random = new Random();
        this.drillPossibilities = new HashMap<>();
        this.skippedDrillsIndexes = new HashMap<>();
        this.lastGeneratedDrillId = -1;
    }

    /**
     * Randomly select a Drill from the entire database. Can return null the database is empty.
     *
     * @return  Random Drill.
     */
    public synchronized @Nullable Drill generateDrill() {
        drillPossibilities = repository.getAllDrills()
                .stream().collect(Collectors.toMap(
                        Drill::getId,
                        drill -> drill,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));

        generateDrillIndexFromPossibilities();

        return (0 <= lastGeneratedDrillId) ? drillPossibilities.get(lastGeneratedDrillId) : null;
    }

    /**
     * Randomly select a Drill of the given Group. Can return null if no Drills of the given group
     * exist.
     *
     * @param group Group the Drill must belong to.
     * @return      Random Drill of the given Group.
     */
    public synchronized @Nullable Drill generateDrill(GroupEntity group) {
        if (null == group) {
            return null;
        }

        drillPossibilities = repository.getAllDrills(group)
                .stream().collect(Collectors.toMap(
                        Drill::getId,
                        drill -> drill,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));

        generateDrillIndexFromPossibilities();

        return (0 <= lastGeneratedDrillId) ? drillPossibilities.get(lastGeneratedDrillId) : null;
    }

    /**
     * Randomly select a Drill of the given SubGroup. Can return null if no Drills of the given
     * SubGroup exist.
     *
     * @param subGroup  SubGroup the Drill must belong to.
     * @return          Random Drill of the give SubGroup.
     */
    public synchronized @Nullable Drill generateDrill(SubGroupEntity subGroup) {
        if (null == subGroup) {
            return null;
        }

        drillPossibilities = repository.getAllDrills(subGroup)
                .stream().collect(Collectors.toMap(
                        Drill::getId,
                        drill -> drill,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));

        generateDrillIndexFromPossibilities();

        return (0 <= lastGeneratedDrillId) ? drillPossibilities.get(lastGeneratedDrillId) : null;
    }

    /**
     * Randomly select a Drill of the given Group <i>and</i> SubGroup. Can return null if no Drills
     * of the given Group and SubGroup exist.
     *
     * @param group     Group the Drill must belong to.
     * @param subGroup  SubGroup the Drill must belong to.
     * @return          Random Drill of the given Group and SubGroup.
     */
    public synchronized @Nullable Drill generateDrill(GroupEntity group, SubGroupEntity subGroup) {
        if (null == group || null == subGroup) {
            return null;
        }

        drillPossibilities = repository.getAllDrills(group, subGroup)
                .stream().collect(Collectors.toMap(
                        Drill::getId,
                        drill -> drill,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));

        generateDrillIndexFromPossibilities();

        return (0 <= lastGeneratedDrillId) ? drillPossibilities.get(lastGeneratedDrillId) : null;
    }

    /**
     * Randomly selects a new Drill, skipping any drill previously returned by a
     * {@link  DrillGenerator#generateDrill()} type method <i>or</i> this method. Can return null if
     * all drills matching the previously called generateDrill() criteria have been skipped. Calling
     * {@link DrillGenerator#resetSkippedDrills()} will add back all previously skipped drills into
     * the list of possibilities.
     * <br><br>
     * <u>Warning</u>: <i><b>MUST</b></i> call a generateDrill() type method before calling this
     * one, otherwise will definitely return null.
     *
     * @return  Newly regenerated Drill.
     */
    public synchronized @Nullable Drill regenerateDrill() {
        if (0 <= lastGeneratedDrillId) {
            // Only remove the last generated drill if we have a valid lastGeneratedDrillId
            skippedDrillsIndexes.put(lastGeneratedDrillId, drillPossibilities.get(lastGeneratedDrillId));
            drillPossibilities.remove(lastGeneratedDrillId);
        } else {
            // Do nothing. Could have an invalid lastGeneratedDrillId if resetSkippedDrills() was
            // called
        }

        generateDrillIndexFromPossibilities();

        return (0 <= lastGeneratedDrillId) ? drillPossibilities.get(lastGeneratedDrillId) : null;
    }

    /**
     * Adds all drills skipped by {@link DrillGenerator#regenerateDrill()} back into the list of
     * possibilities.
     */
    public synchronized void resetSkippedDrills() {
        lastGeneratedDrillId = NO_DRILL_GENERATED;
        for (long drillId : skippedDrillsIndexes.keySet()) {
            if (!drillPossibilities.containsKey(drillId)) {
                Drill drill = skippedDrillsIndexes.get(drillId);
                drillPossibilities.put(drillId, drill);
            }
        }
        skippedDrillsIndexes.clear();
    }

    /**
     * Private helper function to get the weighted value of a Drill's last drilled date. Essentially
     * returns the number of (30 day) months since the Drill has last been drilled, emphasizing
     * Drills that haven't been practiced in a while.
     *
     * @param date  Date in milliseconds.
     * @return      Weight factor of the last time a Drill was drilled.
     */
    private int getDateWeightFactor(long date) {
        final long THIRTY_DAYS_IN_MILLIS = 30L * 24 * 60 * 60 * 1000;
        long currTime = System.currentTimeMillis();
        long timeDiff = currTime - date;

        if (0 > timeDiff) {
            // Invalid date
            return 0;
        } else {
            // Return the number of months since last drilled, capping off at 5
            return (int) (timeDiff % THIRTY_DAYS_IN_MILLIS);
        }
    }

    /**
     * Private helper function to use the weights of each Drill to randomly select a Drill to
     * return. Saves the selected Drill's ID into {@link DrillGenerator#lastGeneratedDrillId}.
     * Random weighted selection algorithm adopted from a Stack Overflow answer.
     *
     * @see <a href="https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java">Stack Overflow Algorithm</a>
     */
    private void generateDrillIndexFromPossibilities() {
        if (drillPossibilities.isEmpty()) {
            lastGeneratedDrillId = NO_DRILL_GENERATED;
            return;
        }

        NavigableMap<Long, Long> weightedDrillIds = new TreeMap<>(); // Essentially a weighted list of IDs
        List<Long> newDrillIds = new ArrayList<>();
        long totalWeight = 0;

        for (long drillId : drillPossibilities.keySet()) {
            Drill drill = drillPossibilities.get(drillId);

            if (null == drill) {
                continue;
            }

            if (drill.isNewDrill()) {
                newDrillIds.add(drillId);
                continue;
            }

            // All drills get at least one entry into the list
            long weight = 1;
            weight += drill.getConfidence();
            weight += getDateWeightFactor(drill.getLastDrilled());

            totalWeight += weight;
            weightedDrillIds.put(totalWeight, drillId);
        }

        if (!newDrillIds.isEmpty()) {
            lastGeneratedDrillId = newDrillIds.get(random.nextInt(newDrillIds.size()));
        } else if (!weightedDrillIds.isEmpty()){
            long randomLong = (long) (random.nextDouble() * totalWeight);
            Map.Entry<Long, Long> temp = weightedDrillIds.ceilingEntry(randomLong);
            lastGeneratedDrillId = (null == temp) ? NO_DRILL_GENERATED : temp.getValue();

        } else {
            // Empty possibilities list or some other issue
            lastGeneratedDrillId = NO_DRILL_GENERATED;
        }
    }
}
