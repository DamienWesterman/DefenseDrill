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
/*
 * Copyright 2025 Damien Westerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.damienwesterman.defensedrill.ui.viewmodel;

import android.app.Application;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.local.Drill;
import com.damienwesterman.defensedrill.data.local.DrillRepository;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

/**
 * View model for {@link Drill} objects geared towards displaying a list of all drills, and allowing
 * changing the value of {@link Drill#isKnownDrill()}.
 */
@HiltViewModel
public class UnlockDrillsViewModel extends AndroidViewModel {
    private final static String TAG = UnlockDrillsViewModel.class.getSimpleName();

    private final DrillRepository repo;

    @Getter
    private final MutableLiveData<List<Drill>> displayedDrills;
    @Nullable
    private List<Drill> allDrills;
    @Getter
    private boolean showKnownDrills;
    @Getter
    private boolean showUnknownDrills;
    private final Object lock = new Object();

    @Inject
    public UnlockDrillsViewModel(@NonNull Application application, @NonNull DrillRepository repo) {
        super(application);

        this.repo = repo;

        displayedDrills = new MutableLiveData<>();
        showKnownDrills = true;
        showUnknownDrills = true;
    }

    public void populateDrills() {
        if (null == allDrills) {
            new Thread(() -> {
                allDrills = repo.getAllDrills();
                displayedDrills.postValue(allDrills);
            }).start();
        }
    }

    /**
     * Set whether we should show known drills in the list. Re-loads the list.
     *
     * @param show  true if we should show known drills.
     */
    public void setShowKnownDrills(boolean show) {
        this.showKnownDrills = show;
        displayFilteredList();
    }

    /**
     * Set whether we should show unknown drills in the list. Re-loads the list.
     *
     * @param show  true if we should show unknown drills.
     */
    public void setShowUnknownDrills(boolean show) {
        this.showUnknownDrills = show;
        displayFilteredList();
    }

    /**
     * Filter the displayed drills based on the current filter settings.
     */
    public void displayFilteredList() {
        if (allDrills != null) {
            displayedDrills.postValue(allDrills.stream()
                .filter(drill -> {
                    if (!showKnownDrills && drill.isKnownDrill()) {
                        return false;
                    } else if (!showUnknownDrills && !drill.isKnownDrill()) {
                        return false;
                    } else {
                        return true;
                    }
                })
                .collect(Collectors.toList()));
        }
    }

    /**
     * Update and save if the drill is known.
     *
     * @param drill     Drill to update.
     * @param isKnown   true if the user set to known.
     */
    public void setDrillKnown(@NonNull Drill drill, boolean isKnown) {
        new Thread(() -> {
            synchronized (lock) {
                drill.setIsKnownDrill(isKnown);
                try {
                    if (repo.updateDrills(drill)) {
                        if (allDrills != null) {
                            /*
                             Update the list so if there is a destructive action (like screen
                             rotation) we can have the correct information. However, the checkbox is
                             already changed in the UI, so we don't need to call
                             displayedDrills.postValue(). This should also be okay to do slowly in
                             the background as it should not be time sensitive.
                             */
                            for (Drill oneDrill : allDrills) {
                                if (oneDrill.getId() == drill.getId()) {
                                    oneDrill.setIsKnownDrill(isKnown);
                                    break;
                                }
                            }
                        } else {
                            // We somehow did this before the drills were loaded, big issue
                            throw new RuntimeException("called setDrillKnown() before populateDrills()");
                        }
                    } else {
                        // Should not happen
                        Log.e(TAG, "setDrillKnown() failed call to updateDrills()");
                    }
                } catch (SQLiteConstraintException e) {
                    // Also should not happen
                    Log.e(TAG, "setDrillKnown() threw exception:", e);
                }
            }
        }).start();
    }
}
