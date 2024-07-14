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
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.damienwesterman.defensedrill.data.CategoryEntity;
import com.damienwesterman.defensedrill.data.Drill;
import com.damienwesterman.defensedrill.data.DrillRepository;
import com.damienwesterman.defensedrill.data.SubCategoryEntity;
import com.damienwesterman.defensedrill.ui.utils.CreateNewEntityCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

// TODO doc comments
public class CreateDrillViewModel extends AndroidViewModel {
    private final DrillRepository repo;
    private List<CategoryEntity> allCategories;
    private List<SubCategoryEntity> allSubCategories;
    private final List<CategoryEntity> checkedCategoryEntities = new ArrayList<>();
    private final List<SubCategoryEntity> checkedSubCategoryEntities = new ArrayList<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    public CreateDrillViewModel(@NonNull Application application) {
        super(application);

        repo = DrillRepository.getInstance(application);
    }

    // TODO doc comments, onFailure() called when SQLiteConstraintException basically when name already exists
    public void saveDrill(Drill drill, CreateNewEntityCallback callback) {
        executor.execute(() -> {
            try {
                repo.insertDrills(drill);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onFailure(e.getMessage());
            }
        });
    }

    public List<CategoryEntity> getAllCategories() {
        return allCategories;
    }

    public List<SubCategoryEntity> getAllSubCategories() {
        return allSubCategories;
    }

    public List<CategoryEntity> getCheckedCategoryEntities() {
        return this.checkedCategoryEntities;
    }

    public List<SubCategoryEntity> getCheckedSubCategoryEntities() {
        return this.checkedSubCategoryEntities;
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
}
