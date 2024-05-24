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

import com.damienwesterman.defensedrill.database.Drill;
import com.damienwesterman.defensedrill.database.DrillRepository;

// TODO: ALL DOC COMMENTS
public class DrillGenerator {
    private final DrillRepository repository;

    public DrillGenerator(DrillRepository repository) {
        this.repository = repository;
    }

    public Drill generateDrill() {
        return null;
    }
}
