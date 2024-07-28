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
import androidx.annotation.Nullable;
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

/**
 * View model for {@link Drill} objects geared towards creation of a new Drill.
 */
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

    /**
     * Attempt to add a new drill to the database.
     *
     * @param drill     Drill to attempt to add to the database.
     * @param callback  Callback to call when the insert is finished.
     */
    public void saveDrill(Drill drill, @NonNull CreateNewEntityCallback callback) {
        executor.execute(() -> {
            try {
                repo.insertDrills(drill);
                callback.onSuccess();
            } catch (SQLiteConstraintException e) {
                callback.onFailure(e.getMessage());
            }
        });
    }

    /**
     * Get the list of all categories in the database.
     * <br><br>
     * {@link #loadAllCategories()} should have been called prior otherwise will return null.
     *
     * @return List of CategoryEntity objects.
     */
    public @Nullable List<CategoryEntity> getAllCategories() {
        return allCategories;
    }

    /**
     * Get the list of all sub-categories in the database.
     * <br><br>
     * {@link #loadAllSubCategories()} should have been called prior otherwise will return null.
     *
     * @return List of SubCategoryEntity objects.
     */
    public @Nullable List<SubCategoryEntity> getAllSubCategories() {
        return allSubCategories;
    }

    /**
     * Get the list of categories the user has checked.
     * <br><br>
     * Saved here to persist across destructive events (i.e. screen rotations). Can modify the list
     * that is returned and it will be persisted, for example:
     * <br><br>
     * {@code createDrillViewModel.getCheckedCategoryEntities().add(category);}
     * <br><br>
     * The newly added category will exist in the list maintained by this view model.
     *
     * @return Persisted list of CategoryEntity objects.
     */
    public List<CategoryEntity> getCheckedCategoryEntities() {
        return this.checkedCategoryEntities;
    }

    /**
     * Get the list of sub-categories the user has checked.
     * <br><br>
     * Saved here to persist across destructive events (i.e. screen rotations). Can modify the list
     * that is returned and it will be persisted, for example:
     * <br><br>
     * {@code createDrillViewModel.getCheckedSubCategoryEntities().add(subCategory);}
     * <br><br>
     * The newly added sub-category will exist in the list maintained by this view model.
     *
     * @return Persisted list of SubCategoryEntity objects.
     */
    public List<SubCategoryEntity> getCheckedSubCategoryEntities() {
        return this.checkedSubCategoryEntities;
    }

    /**
     * Load all categories from the database. Should be called before {@link #getAllCategories()}.
     */
    public void loadAllCategories() {
        if (null == allCategories) {
            executor.execute(() -> allCategories = repo.getAllCategories());
        }
    }

    /**
     * Load all sub-categories from the database. Should be called before
     * {@link #getAllSubCategories()}.
     */
    public void loadAllSubCategories() {
        if (null == allSubCategories) {
            executor.execute(() -> allSubCategories = repo.getAllSubCategories());
        }
    }
}
