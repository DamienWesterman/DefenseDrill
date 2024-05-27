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
 * First call should be to {@link DrillGenerator#generateDrill()}, <i>after</i> which
 * {@link DrillGenerator#regenerateDrill()} can be called to skip the first drill and select a
 * different drill from the original list given to the constructor. regenerateDrill() can be called
 * multiple times to continuously select a new drill. The drill skipped by regenerateDrill() can be
 * added back into the list of possibilities by calling {@link DrillGenerator#resetSkippedDrills()}.
 * <br><br>
 * Each drill in the list given to the constructor has a weighted possibility to be chosen, with all
 * drills having a possibility. These weights are determined by the last date the drill was drilled
 * and the user defined confidence level. If there are any drills marked as new in the list, the
 * returned drill (by generateDrill() or regenerateDrill()) is <i>guaranteed</i> to be a new drill.
 */
public class DrillGenerator {
    private static final int NO_DRILL_GENERATED = -1;

    private final Random random;
    private final List<Drill> originalDrills;
    private Map<Long, Drill> drillPossibilities;
    private long lastGeneratedDrillId;

    /**
     * Constructor.
     *
     * @param drills    List of Drills to randomly select from.
     * @param random    Random object to use for random Drill selection. Can be seeded.
     */
    public DrillGenerator(List<Drill> drills, Random random) {
        this.originalDrills = drills;
        this.random = random;
        this.drillPossibilities = idDrillMapFromDrillList(drills);
        this.lastGeneratedDrillId = -1;
    }

    /**
     * Randomly select a Drill from the drills list provided to the constructor.
     *
     * @return  Random Drill.
     */
    public synchronized @Nullable Drill generateDrill() {
        generateDrillIndexFromPossibilities();

        return (0 <= lastGeneratedDrillId) ? drillPossibilities.get(lastGeneratedDrillId) : null;
    }

    /**
     * Randomly selects a new Drill, skipping any drill previously returned by
     * {@link  DrillGenerator#generateDrill()} <i>or</i> this method. Can return null if
     * all drills given to the constructor have been skipped. Calling
     * {@link DrillGenerator#resetSkippedDrills()} will add back all previously skipped drills into
     * the list of possibilities.
     *
     * @return  Newly regenerated Drill.
     */
    public synchronized @Nullable Drill regenerateDrill() {
        if (0 <= lastGeneratedDrillId) {
            // Only remove the last generated drill if we have a valid lastGeneratedDrillId
            drillPossibilities.remove(lastGeneratedDrillId);
        } else {
            // Do nothing. Could have an invalid lastGeneratedDrillId if resetSkippedDrills() was
            // called
        }

        return generateDrill();
    }

    /**
     * Adds all drills skipped by {@link DrillGenerator#regenerateDrill()} back into the list of
     * possibilities.
     */
    public synchronized void resetSkippedDrills() {
        lastGeneratedDrillId = NO_DRILL_GENERATED;
        this.drillPossibilities = idDrillMapFromDrillList(this.originalDrills);
    }

    /**
     * Convert {@literal List<Drill>} into {@literal Map<Long, Drill>}, using the Drill's ID as its
     * key.
     *
     * @param drills    List of drills
     * @return          Map of Drill IDs to their respective Drill.
     */
    private Map<Long, Drill> idDrillMapFromDrillList(List<Drill> drills) {
        return drills.stream().collect(Collectors.toMap(
            Drill::getId,
            drill -> drill,
            (existing, replacement) -> existing,
            HashMap::new
        ));
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
}
