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

package com.damienwesterman.defensedrill.ui.view_models;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * TODO Doc comments
 */
public class DrillListViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Drill>> drills;
    private List<CategoryEntity> allCategories;
    private List<SubCategoryEntity> allSubCategories;

    private Set<Long> categoryFilterIds;
    private Set<Long> subCategoryFilterIds;
    private final DrillRepository repo;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public DrillListViewModel(@NonNull Application application) {
        super(application);

        repo = DrillRepository.getInstance(application);
        drills = new MutableLiveData<>();
    }

    public LiveData<List<Drill>> getDrills() {
        return drills;
    }

    public void populateDrills() {
        if (null == drills.getValue()) {
            executor.execute(() -> drills.postValue(repo.getAllDrills()));
        }
    }

    public void rePopulateDrills() {
        executor.execute(() -> drills.postValue(repo.getAllDrills()));
    }

    public void filterDrills(List<Long> categoryIds, List<Long> subCategoryIds) {
        executor.execute(() -> drills.postValue(repo.getAllDrills(categoryIds, subCategoryIds)));
    }

    public List<CategoryEntity> getAllCategories() {
        return allCategories;
    }

    public List<SubCategoryEntity> getAllSubCategories() {
        return allSubCategories;
    }

    public void loadAllCategories() {
        if (null == allCategories) {
            executor.execute(() -> allCategories = repo.getAllCategories());
        }
    }

    public void loadAllSubCategories() {
        if (null == allSubCategories) {
            executor.execute(() -> allSubCategories = repo.getAllSubCategories());
        }
    }

    public Set<Long> getCategoryFilterIds() {
        if (null == categoryFilterIds) {
            categoryFilterIds = allCategories.stream().map(CategoryEntity::getId)
                    .collect(Collectors.toSet());
        }

        return categoryFilterIds;
    }

    public void setCategoryFilterIds(Set<Long> categoryFilterIds) {
        this.categoryFilterIds = categoryFilterIds;
    }

    public Set<Long> getSubCategoryFilterIds() {
        if (null == subCategoryFilterIds) {
            subCategoryFilterIds = allSubCategories.stream().map(SubCategoryEntity::getId)
                    .collect(Collectors.toSet());
        }

        return subCategoryFilterIds;
    }

    public void setSubCategoryFilterIds(Set<Long> subCategoryFilterIds) {
        this.subCategoryFilterIds = subCategoryFilterIds;
    }


    public @Nullable Drill findDrillById(long id) {
        Drill ret = null;
        List<Drill> allDrills = drills.getValue();

        if (null != allDrills) {
            for (Drill drill : allDrills) {
                if (drill.getId() == id) {
                    ret = drill;
                    break;
                }
            }
        }

        return ret;
    }

    public void deleteDrill(Drill drill) {
        executor.execute(() -> {
            if (null != drill) {
                List<Drill> newDrills = drills.getValue();
                if (newDrills != null) {
                    newDrills.remove(drill);
                    drills.postValue(newDrills);
                    repo.deleteDrills(drill);
                }
            }
        });
    }
}
