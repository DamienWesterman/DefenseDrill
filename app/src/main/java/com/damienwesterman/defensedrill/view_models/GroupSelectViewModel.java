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

package com.damienwesterman.defensedrill.view_models;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.damienwesterman.defensedrill.database.DrillRepository;
import com.damienwesterman.defensedrill.database.GroupEntity;

import java.util.List;

/**
 * TODO Doc comments
 */
public class GroupSelectViewModel extends AndroidViewModel {
    private List<GroupEntity> groups;
    private final DrillRepository repo;

    public GroupSelectViewModel(Application application) {
        super(application);

        repo = DrillRepository.getInstance(application);
        groups = null;
    }

    public List<GroupEntity> getGroups() {
        if (null == groups) {
            groups = repo.getAllGroups();
        }
        return this.groups;
    }
}
